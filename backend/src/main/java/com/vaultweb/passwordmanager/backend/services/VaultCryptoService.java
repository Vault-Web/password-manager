package com.vaultweb.passwordmanager.backend.services;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Base64;
import javax.crypto.Cipher;
import javax.crypto.Mac;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;
import org.springframework.stereotype.Service;

@Service
public class VaultCryptoService {

  public static final String PASSWORD_PREFIX = "v1:";

  private static final int PBKDF2_SALT_LEN = 16;
  private static final int PBKDF2_KEY_LEN_BYTES = 32;

  // Tunable; chosen to be slow-ish but not insane for a web request.
  private static final int DEFAULT_PBKDF2_ITERATIONS = 210_000;

  private static final int GCM_IV_LENGTH = 12;
  private static final int GCM_TAG_LENGTH = 128;

  private static final String KDF_ALGORITHM = "PBKDF2WithHmacSHA256";

  private final SecureRandom secureRandom = new SecureRandom();

  public int defaultIterations() {
    return DEFAULT_PBKDF2_ITERATIONS;
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
            "Invalid cipher text: too short to contain IV and GCM tag");
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
