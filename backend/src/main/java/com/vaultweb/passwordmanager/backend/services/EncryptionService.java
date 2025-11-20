package com.vaultweb.passwordmanager.backend.services;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.Base64;
import javax.crypto.Cipher;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class EncryptionService {

  private static final int GCM_IV_LENGTH = 12;
  private static final int GCM_TAG_LENGTH = 128;

  private final SecretKeySpec secretKey;
  private final SecureRandom secureRandom = new SecureRandom();

  /**
   * Initializes an instance of the EncryptionService with the provided encryption key. This
   * constructor validates the key, ensuring it is Base64-encoded and of valid length (16, 24, or 32
   * bytes).
   *
   * @param base64Key the Base64-encoded encryption key. It must decode to a byte array with a
   *     length of 16, 24, or 32. If the key is null, blank, not Base64-encoded, or of an invalid
   *     length, an IllegalArgumentException is thrown.
   */
  public EncryptionService(@Value("${encryption.secret") String base64Key) {
    if (base64Key == null || base64Key.isBlank()) {
      throw new IllegalArgumentException("Encryption key cannot be null or blank");
    }

    byte[] keyBytes;
    try {
      keyBytes = Base64.getDecoder().decode(base64Key);
    } catch (IllegalArgumentException e) {
      throw new IllegalArgumentException("Encryption key must be Base64-encoded", e);
    }

    if (!(keyBytes.length == 16 || keyBytes.length == 24 || keyBytes.length == 32)) {
      throw new IllegalArgumentException("Encryption key must be 16, 24, or 32 bytes long");
    }

    this.secretKey = new SecretKeySpec(keyBytes, "AES");
  }

  /**
   * Encrypts the provided plaintext string using AES encryption in GCM mode. The method generates a
   * random initialization vector (IV) for each encryption operation and combines it with the
   * encrypted data. The result is Base64-encoded for safe transport or storage.
   *
   * @param plainText the plaintext string to be encrypted. It must not be null. If null, the method
   *     will return null without performing encryption.
   * @return the encrypted string in Base64-encoded format, including the IV and ciphertext. Throws
   *     a RuntimeException if encryption fails due to an error in the process.
   */
  public String encrypt(String plainText) {
    if (plainText == null) return null;

    try {
      byte[] iv = new byte[GCM_IV_LENGTH];
      secureRandom.nextBytes(iv);

      Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
      GCMParameterSpec spec = new GCMParameterSpec(GCM_TAG_LENGTH, iv);
      cipher.init(Cipher.ENCRYPT_MODE, secretKey, spec);

      byte[] encrypted = cipher.doFinal(plainText.getBytes(StandardCharsets.UTF_8));
      ByteBuffer buffer = ByteBuffer.allocate(iv.length + encrypted.length);
      buffer.put(iv);
      buffer.put(encrypted);

      return Base64.getEncoder().encodeToString(buffer.array());
    } catch (Exception e) {
      throw new RuntimeException("Encryption failed", e);
    }
  }

  /**
   * Decrypts the provided cipher text, which is expected to be in Base64-encoded format. The method
   * extracts the initialization vector (IV) and decrypts the encrypted data using AES encryption in
   * GCM mode. If the cipher text is invalid or decryption fails, an exception is thrown.
   *
   * @param cipherText the Base64-encoded cipher text to decrypt. It must include both the IV and
   *     the encrypted data. If null, the method will return null without processing. If the cipher
   *     text is too short to include the IV and GCM tag, an IllegalArgumentException will be
   *     thrown.
   * @return the decrypted string encoded in UTF-8 format. If decryption fails for any reason, a
   *     RuntimeException is thrown.
   */
  public String decrypt(String cipherText) {
    if (cipherText == null) return null;

    try {
      byte[] decoded = Base64.getDecoder().decode(cipherText);

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
      cipher.init(Cipher.DECRYPT_MODE, secretKey, spec);

      byte[] decrypted = cipher.doFinal(encrypted);
      return new String(decrypted, StandardCharsets.UTF_8);
    } catch (IllegalArgumentException e) {
      throw e;
    } catch (Exception e) {
      throw new RuntimeException("Decryption failed", e);
    }
  }
}
