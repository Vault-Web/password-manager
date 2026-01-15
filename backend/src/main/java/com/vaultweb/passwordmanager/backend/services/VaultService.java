package com.vaultweb.passwordmanager.backend.services;

import com.vaultweb.passwordmanager.backend.exceptions.ConflictException;
import com.vaultweb.passwordmanager.backend.exceptions.InvalidCredentialsException;
import com.vaultweb.passwordmanager.backend.exceptions.VaultLockedException;
import com.vaultweb.passwordmanager.backend.exceptions.VaultNotInitializedException;
import com.vaultweb.passwordmanager.backend.model.PasswordEntry;
import com.vaultweb.passwordmanager.backend.model.Vault;
import com.vaultweb.passwordmanager.backend.repositories.PasswordEntryRepository;
import com.vaultweb.passwordmanager.backend.repositories.VaultRepository;
import java.util.Base64;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class VaultService {

  private final VaultRepository vaultRepository;
  private final PasswordEntryRepository passwordEntryRepository;
  private final VaultCryptoService crypto;

  public boolean isInitialized(Long ownerId) {
    return vaultRepository.existsByOwnerId(ownerId);
  }

  /**
   * Sets up a new vault for the given owner with the specified master password.
   *
   * @param ownerId
   * @param masterPassword
   */
  @Transactional
  public void setup(Long ownerId, String masterPassword) {
    if (vaultRepository.existsByOwnerId(ownerId)) {
      throw new ConflictException("Vault is already initialized");
    }

    byte[] salt = crypto.randomSalt();
    int iterations = crypto.defaultIterations();

    byte[] kek = crypto.deriveKek(masterPassword, salt, iterations);
    byte[] verifier = crypto.computeVerifier(kek);

    byte[] dek = crypto.generateDek();
    String wrappedDek = crypto.wrapDek(kek, dek, ownerId);

    Vault vault = new Vault();
    vault.setOwnerId(ownerId);
    vault.setKdfSalt(Base64.getEncoder().encodeToString(salt));
    vault.setKdfIterations(iterations);
    vault.setWrappedDek(wrappedDek);
    vault.setVerifier(Base64.getEncoder().encodeToString(verifier));

    vaultRepository.save(vault);
  }

  /**
   * Verifies the given master password for the owner's vault.
   *
   * @param ownerId
   * @param masterPassword
   */
  public void verify(Long ownerId, String masterPassword) {
    Vault vault =
        vaultRepository
            .findByOwnerId(ownerId)
            .orElseThrow(() -> new VaultNotInitializedException("Vault is not initialized"));

    byte[] salt = Base64.getDecoder().decode(vault.getKdfSalt());
    byte[] kek = crypto.deriveKek(masterPassword, salt, vault.getKdfIterations());

    byte[] expectedVerifier = Base64.getDecoder().decode(vault.getVerifier());
    byte[] actualVerifier = crypto.computeVerifier(kek);

    if (!crypto.constantTimeEquals(expectedVerifier, actualVerifier)) {
      throw new InvalidCredentialsException("Invalid master password");
    }

    // Also validate by trying to unwrap DEK (detect corrupted vault data)
    crypto.unwrapDek(kek, vault.getWrappedDek(), ownerId);
  }

  /**
   * Unwraps and returns the vault DEK for the given owner after validating the provided master
   * password.
   */
  public byte[] unwrapDekForSession(Long ownerId, String masterPassword) {
    return unwrapDek(ownerId, masterPassword);
  }

  /**
   * Rotates the master password for the owner's vault.
   *
   * @param ownerId
   * @param oldMasterPassword
   * @param newMasterPassword
   */
  @Transactional
  public void rotate(Long ownerId, String oldMasterPassword, String newMasterPassword) {
    Vault vault =
        vaultRepository
            .findByOwnerId(ownerId)
            .orElseThrow(() -> new VaultNotInitializedException("Vault is not initialized"));

    // Unwrap DEK using old password
    byte[] oldSalt = Base64.getDecoder().decode(vault.getKdfSalt());
    byte[] oldKek = crypto.deriveKek(oldMasterPassword, oldSalt, vault.getKdfIterations());

    byte[] expectedVerifier = Base64.getDecoder().decode(vault.getVerifier());
    if (!crypto.constantTimeEquals(expectedVerifier, crypto.computeVerifier(oldKek))) {
      throw new InvalidCredentialsException("Invalid master password");
    }

    byte[] dek = crypto.unwrapDek(oldKek, vault.getWrappedDek(), ownerId);

    // Re-wrap with new password & new salt
    byte[] newSalt = crypto.randomSalt();
    int iterations = crypto.defaultIterations();
    byte[] newKek = crypto.deriveKek(newMasterPassword, newSalt, iterations);

    vault.setKdfSalt(Base64.getEncoder().encodeToString(newSalt));
    vault.setKdfIterations(iterations);
    vault.setVerifier(Base64.getEncoder().encodeToString(crypto.computeVerifier(newKek)));
    vault.setWrappedDek(crypto.wrapDek(newKek, dek, ownerId));

    vaultRepository.save(vault);
  }

  /**
   * Encrypts a plaintext password for storage. If vault is not initialized, returns plaintext.
   *
   * @param ownerId
   * @param masterPassword
   * @param plainPassword
   * @return the encrypted password string for storage
   */
  public String encryptPasswordForStorage(
      Long ownerId, String masterPassword, String plainPassword) {
    if (!isInitialized(ownerId)) {
      return plainPassword;
    }
    if (masterPassword == null || masterPassword.isBlank()) {
      throw new VaultLockedException("Master password required (vault initialized)");
    }

    byte[] dek = unwrapDek(ownerId, masterPassword);
    return crypto.encryptPasswordWithDek(dek, plainPassword, ownerId);
  }

  public String encryptPasswordForStorage(Long ownerId, byte[] dek, String plainPassword) {
    if (!isInitialized(ownerId)) {
      return plainPassword;
    }
    if (dek == null) {
      throw new VaultLockedException("Vault is locked");
    }
    return crypto.encryptPasswordWithDek(dek, plainPassword, ownerId);
  }

  /**
   * Decrypts a stored password for reveal. If vault is not initialized, returns stored value.
   *
   * @param ownerId
   * @param masterPassword
   * @param entry
   * @return the plaintext password
   */
  @Transactional
  public String decryptPasswordForReveal(Long ownerId, String masterPassword, PasswordEntry entry) {
    if (!isInitialized(ownerId)) {
      return entry.getPassword();
    }
    if (masterPassword == null || masterPassword.isBlank()) {
      throw new VaultLockedException("Master password required to reveal passwords");
    }

    byte[] dek = unwrapDek(ownerId, masterPassword);

    String stored = entry.getPassword();
    if (crypto.isVaultEncryptedPassword(stored)) {
      return crypto.decryptPasswordWithDek(dek, stored, ownerId);
    }

    // Legacy: stored value is plaintext (after server-side decrypt). Require master password
    // anyway.
    String migrated = crypto.encryptPasswordWithDek(dek, stored, ownerId);
    entry.setPassword(migrated);
    passwordEntryRepository.save(entry);
    return stored;
  }

  /**
   * Decrypts a stored password for reveal. If vault is not initialized, returns stored value.
   *
   * @param ownerId
   * @param dek
   * @param entry
   * @return the plaintext password
   */
  @Transactional
  public String decryptPasswordForReveal(Long ownerId, byte[] dek, PasswordEntry entry) {
    if (!isInitialized(ownerId)) {
      return entry.getPassword();
    }
    if (dek == null) {
      throw new VaultLockedException("Vault is locked");
    }

    String stored = entry.getPassword();
    if (crypto.isVaultEncryptedPassword(stored)) {
      return crypto.decryptPasswordWithDek(dek, stored, ownerId);
    }

    String migrated = crypto.encryptPasswordWithDek(dek, stored, ownerId);
    entry.setPassword(migrated);
    passwordEntryRepository.save(entry);
    return stored;
  }

  /**
   * Migrates all plaintext passwords to vault-encrypted format.
   *
   * @param ownerId
   * @param masterPassword
   * @return the number of passwords migrated
   */
  @Transactional
  public int migrateAllPasswords(Long ownerId, String masterPassword) {
    if (!isInitialized(ownerId)) {
      throw new VaultNotInitializedException("Vault is not initialized");
    }
    if (masterPassword == null || masterPassword.isBlank()) {
      throw new VaultLockedException("Master password required to migrate passwords");
    }

    byte[] dek = unwrapDek(ownerId, masterPassword);

    List<PasswordEntry> entries = passwordEntryRepository.findAllByOwnerId(ownerId);
    int migrated = 0;
    for (PasswordEntry entry : entries) {
      String stored = entry.getPassword();
      if (crypto.isVaultEncryptedPassword(stored)) {
        continue;
      }
      entry.setPassword(crypto.encryptPasswordWithDek(dek, stored, ownerId));
      passwordEntryRepository.save(entry);
      migrated++;
    }

    return migrated;
  }

  /**
   * migrates all plaintext passwords to vault-encrypted format.
   *
   * @param ownerId
   * @param dek
   * @return the number of passwords migrated
   */
  @Transactional
  public int migrateAllPasswords(Long ownerId, byte[] dek) {
    if (!isInitialized(ownerId)) {
      throw new VaultNotInitializedException("Vault is not initialized");
    }
    if (dek == null) {
      throw new VaultLockedException("Vault is locked");
    }

    List<PasswordEntry> entries = passwordEntryRepository.findAllByOwnerId(ownerId);
    int migrated = 0;
    for (PasswordEntry entry : entries) {
      String stored = entry.getPassword();
      if (crypto.isVaultEncryptedPassword(stored)) {
        continue;
      }
      entry.setPassword(crypto.encryptPasswordWithDek(dek, stored, ownerId));
      passwordEntryRepository.save(entry);
      migrated++;
    }

    return migrated;
  }

  /**
   * Unwraps the DEK for the owner's vault using the provided master password.
   *
   * @param ownerId
   * @param masterPassword
   * @return
   */
  private byte[] unwrapDek(Long ownerId, String masterPassword) {
    Vault vault =
        vaultRepository
            .findByOwnerId(ownerId)
            .orElseThrow(() -> new VaultNotInitializedException("Vault is not initialized"));

    byte[] salt = Base64.getDecoder().decode(vault.getKdfSalt());
    byte[] kek = crypto.deriveKek(masterPassword, salt, vault.getKdfIterations());

    byte[] expectedVerifier = Base64.getDecoder().decode(vault.getVerifier());
    byte[] actualVerifier = crypto.computeVerifier(kek);

    if (!crypto.constantTimeEquals(expectedVerifier, actualVerifier)) {
      throw new InvalidCredentialsException("Invalid master password");
    }

    return crypto.unwrapDek(kek, vault.getWrappedDek(), ownerId);
  }
}
