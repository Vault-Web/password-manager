package com.vaultweb.passwordmanager.backend.services;

import com.vaultweb.passwordmanager.backend.exceptions.InvalidCredentialsException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Base64;
import javax.crypto.AEADBadTagException;
import javax.crypto.Cipher;
import javax.crypto.Mac;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class VaultCryptoService {

  public static final String PASSWORD_PREFIX = "v1:";

  /** Magic prefix identifying a password-encrypted vault export envelope. */
  public static final String EXPORT_PREFIX = "VWENC1:";

  private static final int PBKDF2_SALT_LEN = 16;
  private static final int PBKDF2_KEY_LEN_BYTES = 32;

  private static final int DEFAULT_PBKDF2_ITERATIONS = 210_000;

  /** Defensive upper bound on the PBKDF2 iterations read from an untrusted export envelope. */
  private static final int MAX_EXPORT_ITERATIONS = 10_000_000;

  private static final int GCM_IV_LENGTH = 12;
  private static final int GCM_TAG_LENGTH = 128;

  private static final String KDF_ALGORITHM = "PBKDF2WithHmacSHA256";

  private final SecureRandom secureRandom = new SecureRandom();
  private final int defaultPbkdf2Iterations;

  public VaultCryptoService(
      @Value("${vault.crypto.pbkdf2.iterations:" + DEFAULT_PBKDF2_ITERATIONS + "}")
          int defaultPbkdf2Iterations) {
    if (defaultPbkdf2Iterations <= 0) {
      throw new IllegalStateException("vault.crypto.pbkdf2.iterations must be > 0");
    }
    this.defaultPbkdf2Iterations = defaultPbkdf2Iterations;
  }

  public int defaultIterations() {
    return defaultPbkdf2Iterations;
  }

  /** Generates a random salt for PBKDF2 key derivation. */
  public byte[] randomSalt() {
    byte[] salt = new byte[PBKDF2_SALT_LEN];
    secureRandom.nextBytes(salt);
    return salt;
  }

  /**
   * Derives a Key Encryption Key (KEK) from the given master password, salt, and iteration count
   *
   * @param masterPassword
   * @param salt
   * @param iterations
   * @return the derived KEK bytes
   */
  public byte[] deriveKek(String masterPassword, byte[] salt, int iterations) {
    PBEKeySpec spec =
        new PBEKeySpec(masterPassword.toCharArray(), salt, iterations, 8 * PBKDF2_KEY_LEN_BYTES);
    try {
      SecretKeyFactory skf = SecretKeyFactory.getInstance(KDF_ALGORITHM);
      return skf.generateSecret(spec).getEncoded();
    } catch (GeneralSecurityException e) {
      throw new IllegalStateException("Unable to derive key", e);
    } finally {
      spec.clearPassword();
    }
  }

  /**
   * Computes the master password verifier using HMAC-SHA256.
   *
   * @param kekBytes the Key Encryption Key bytes
   * @return the computed verifier bytes
   */
  public byte[] computeVerifier(byte[] kekBytes) {
    try {
      Mac mac = Mac.getInstance("HmacSHA256");
      mac.init(new SecretKeySpec(kekBytes, "HmacSHA256"));
      mac.update("vault-web-master-verifier".getBytes(StandardCharsets.UTF_8));
      return mac.doFinal();
    } catch (GeneralSecurityException e) {
      throw new IllegalStateException("Unable to compute verifier", e);
    }
  }

  /**
   * Compares two byte arrays in constant time to prevent timing attacks.
   *
   * @param a
   * @param b
   * @return true if arrays are equal, false otherwise
   */
  public boolean constantTimeEquals(byte[] a, byte[] b) {
    return MessageDigest.isEqual(a, b);
  }

  /**
   * Generates a random Data Encryption Key (DEK) for encrypting vault data.
   *
   * @return the generated DEK bytes
   */
  public byte[] generateDek() {
    byte[] dek = new byte[32];
    secureRandom.nextBytes(dek);
    return dek;
  }

  /**
   * Wraps (encrypts) the DEK using the KEK and owner-specific AAD.
   *
   * @param kekBytes
   * @param dekBytes
   * @param ownerId
   * @return the wrapped DEK as a Base64-encoded string
   */
  public String wrapDek(byte[] kekBytes, byte[] dekBytes, Long ownerId) {
    return encryptAesGcmToBase64(kekBytes, dekBytes, aadForOwner(ownerId));
  }

  /**
   * Unwraps (decrypts) the DEK using the KEK and owner-specific AAD.
   *
   * @param kekBytes
   * @param wrappedDek
   * @param ownerId
   * @return the unwrapped DEK bytes
   */
  public byte[] unwrapDek(byte[] kekBytes, String wrappedDek, Long ownerId) {
    return decryptAesGcmFromBase64(kekBytes, wrappedDek, aadForOwner(ownerId));
  }

  /**
   * Checks if the given password string is vault-encrypted.
   *
   * @param value
   * @return true if the password is vault-encrypted, false otherwise
   */
  public boolean isVaultEncryptedPassword(String value) {
    return value != null && value.startsWith(PASSWORD_PREFIX);
  }

  /**
   * Encrypts a plaintext password using the DEK and owner-specific AAD.
   *
   * @param dekBytes
   * @param plaintext
   * @param ownerId
   * @return the encrypted password string with prefix
   */
  public String encryptPasswordWithDek(byte[] dekBytes, String plaintext, Long ownerId) {
    String blob =
        encryptAesGcmToBase64(
            dekBytes, plaintext.getBytes(StandardCharsets.UTF_8), aadForOwner(ownerId));
    return PASSWORD_PREFIX + blob;
  }

  /**
   * Decrypts an encrypted password using the DEK and owner-specific AAD.
   *
   * @param dekBytes
   * @param encrypted
   * @param ownerId
   * @return the decrypted plaintext password
   */
  public String decryptPasswordWithDek(byte[] dekBytes, String encrypted, Long ownerId) {
    if (!isVaultEncryptedPassword(encrypted)) {
      throw new IllegalArgumentException("Password is not vault-encrypted");
    }
    String blob = encrypted.substring(PASSWORD_PREFIX.length());
    byte[] plainBytes = decryptAesGcmFromBase64(dekBytes, blob, aadForOwner(ownerId));
    return new String(plainBytes, StandardCharsets.UTF_8);
  }

  /**
   * Checks whether the given value is a password-encrypted vault export envelope.
   *
   * @param value the value to inspect
   * @return true if the value carries the export envelope prefix
   */
  public boolean isEncryptedExport(String value) {
    return value != null && value.startsWith(EXPORT_PREFIX);
  }

  /**
   * Encrypts arbitrary bytes under a password into a self-contained, portable envelope.
   *
   * <p>The envelope is {@code EXPORT_PREFIX + base64(iterations | salt | iv | ciphertext+tag)} and
   * is deliberately not bound to an owner (no AAD) so an export can be restored on any vault
   * instance using only the export password.
   *
   * @param plaintext the bytes to encrypt
   * @param password the export password
   * @return the encrypted envelope string
   */
  public String encryptWithPassword(byte[] plaintext, String password) {
    byte[] salt = randomSalt();
    int iterations = defaultPbkdf2Iterations;
    byte[] key = deriveKek(password, salt, iterations);
    try {
      byte[] iv = new byte[GCM_IV_LENGTH];
      secureRandom.nextBytes(iv);

      Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
      cipher.init(
          Cipher.ENCRYPT_MODE,
          new SecretKeySpec(key, "AES"),
          new GCMParameterSpec(GCM_TAG_LENGTH, iv));
      byte[] ciphertext = cipher.doFinal(plaintext);

      ByteBuffer buffer =
          ByteBuffer.allocate(Integer.BYTES + salt.length + iv.length + ciphertext.length);
      buffer.putInt(iterations);
      buffer.put(salt);
      buffer.put(iv);
      buffer.put(ciphertext);
      return EXPORT_PREFIX + Base64.getEncoder().encodeToString(buffer.array());
    } catch (GeneralSecurityException e) {
      throw new IllegalStateException("Export encryption failed", e);
    }
  }

  /**
   * Decrypts an envelope produced by {@link #encryptWithPassword(byte[], String)}.
   *
   * @param encrypted the encrypted envelope string
   * @param password the export password
   * @return the decrypted plaintext bytes
   * @throws InvalidCredentialsException if the password is wrong or the data was tampered with
   * @throws IllegalArgumentException if the envelope is not well-formed
   */
  public byte[] decryptWithPassword(String encrypted, String password) {
    if (!isEncryptedExport(encrypted)) {
      throw new IllegalArgumentException("Not an encrypted vault export");
    }
    byte[] decoded;
    try {
      decoded = Base64.getDecoder().decode(encrypted.substring(EXPORT_PREFIX.length()));
    } catch (IllegalArgumentException e) {
      throw new IllegalArgumentException("Malformed vault export", e);
    }
    if (decoded.length < Integer.BYTES + PBKDF2_SALT_LEN + GCM_IV_LENGTH + (GCM_TAG_LENGTH / 8)) {
      throw new IllegalArgumentException("Malformed vault export: too short");
    }

    ByteBuffer buffer = ByteBuffer.wrap(decoded);
    int iterations = buffer.getInt();
    if (iterations <= 0 || iterations > MAX_EXPORT_ITERATIONS) {
      throw new IllegalArgumentException("Malformed vault export: invalid iteration count");
    }
    byte[] salt = new byte[PBKDF2_SALT_LEN];
    buffer.get(salt);
    byte[] iv = new byte[GCM_IV_LENGTH];
    buffer.get(iv);
    byte[] ciphertext = new byte[buffer.remaining()];
    buffer.get(ciphertext);

    byte[] key = deriveKek(password, salt, iterations);
    try {
      Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
      cipher.init(
          Cipher.DECRYPT_MODE,
          new SecretKeySpec(key, "AES"),
          new GCMParameterSpec(GCM_TAG_LENGTH, iv));
      return cipher.doFinal(ciphertext);
    } catch (AEADBadTagException e) {
      throw new InvalidCredentialsException("Invalid export password or corrupted export file");
    } catch (GeneralSecurityException e) {
      throw new IllegalStateException("Export decryption failed", e);
    }
  }

  /**
   * Generates Additional Authenticated Data (AAD) for the given owner ID.
   *
   * @param ownerId
   * @return the AAD bytes
   */
  private byte[] aadForOwner(Long ownerId) {
    return ("owner:" + ownerId).getBytes(StandardCharsets.UTF_8);
  }

  /**
   * Encrypts plaintext bytes using AES-GCM and encodes the result in Base64.
   *
   * @param keyBytes
   * @param plaintext
   * @param aad
   * @return the Base64-encoded ciphertext
   */
  private String encryptAesGcmToBase64(byte[] keyBytes, byte[] plaintext, byte[] aad) {
    try {
      byte[] iv = new byte[GCM_IV_LENGTH];
      secureRandom.nextBytes(iv);

      Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
      GCMParameterSpec spec = new GCMParameterSpec(GCM_TAG_LENGTH, iv);
      cipher.init(Cipher.ENCRYPT_MODE, new SecretKeySpec(keyBytes, "AES"), spec);
      if (aad != null) {
        cipher.updateAAD(aad);
      }

      byte[] encrypted = cipher.doFinal(plaintext);
      ByteBuffer buffer = ByteBuffer.allocate(iv.length + encrypted.length);
      buffer.put(iv);
      buffer.put(encrypted);
      return Base64.getEncoder().encodeToString(buffer.array());
    } catch (GeneralSecurityException e) {
      throw new IllegalStateException("Encryption failed", e);
    }
  }

  /**
   * Decrypts Base64-encoded ciphertext using AES-GCM.
   *
   * @param keyBytes
   * @param base64
   * @param aad
   * @return the decrypted plaintext bytes
   */
  private byte[] decryptAesGcmFromBase64(byte[] keyBytes, String base64, byte[] aad) {
    try {
      byte[] decoded = Base64.getDecoder().decode(base64);
      if (decoded.length < GCM_IV_LENGTH + 16) {
        throw new IllegalArgumentException(
            "Invalid ciphertext: too short to contain IV and GCM tag");
      }

      ByteBuffer buffer = ByteBuffer.wrap(decoded);
      byte[] iv = new byte[GCM_IV_LENGTH];
      buffer.get(iv);

      byte[] encrypted = new byte[buffer.remaining()];
      buffer.get(encrypted);

      Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
      GCMParameterSpec spec = new GCMParameterSpec(GCM_TAG_LENGTH, iv);
      cipher.init(Cipher.DECRYPT_MODE, new SecretKeySpec(keyBytes, "AES"), spec);
      if (aad != null) {
        cipher.updateAAD(aad);
      }

      return cipher.doFinal(encrypted);
    } catch (IllegalArgumentException e) {
      throw e;
    } catch (GeneralSecurityException e) {
      throw new IllegalStateException("Decryption failed", e);
    }
  }
}
