package com.vaultweb.passwordmanager.backend.security;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.Base64;

import javax.crypto.Cipher;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Component
@Converter
public class AttributeEncryptor implements AttributeConverter<String, String> {

    private static final int GCM_IV_LENGTH = 12;
    private static final int GCM_TAG_LENGTH = 128;
    private static final int GCM_TAG_LENGTH_BYTES = GCM_TAG_LENGTH / 8;

    @Value("${encryption.secret}")
    private String secretKey;

    private byte[] keyBytes;
    private SecureRandom secureRandom;

    private SecureRandom getSecureRandom() {
        if (secureRandom == null) {
            secureRandom = new SecureRandom();
        }
        return secureRandom;
    }

    /**
     * Validates the encryption key for use in cryptographic operations.
     *
     * This method ensures that the key used for encryption and decryption is not null,
     * is properly Base64-encoded, and corresponds to a valid length (16, 24, or 32 bytes)
     * for AES encryption standards. The method is annotated with {@code @PostConstruct}
     * to ensure it is invoked after dependency injection but before the object is used.
     *
     * @throws IllegalArgumentException if the secret key is invalid
     */
    @PostConstruct
    private void validateKey() {
        if (secretKey == null) {
            throw new IllegalArgumentException("Encryption key cannot be null");
        }

        try {
            keyBytes = Base64.getDecoder().decode(secretKey);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Encryption key must be Base64-encoded", e);
        }

        if (!(keyBytes.length == 16 || keyBytes.length == 24 || keyBytes.length == 32)) {
            throw new IllegalArgumentException("Encryption key must be 16, 24, or 32 bytes long");
        }
    }

    /**
     * Converts the given attribute into its encrypted database column representation.
     * The method uses AES encryption in GCM mode with a randomly generated initialization vector (IV).
     * The resulting encrypted data is encoded into a Base64 string for storage.
     *
     * @param attribute the plain text attribute to be encrypted. If null, the method will return null.
     * @return the encrypted and Base64-encoded string representation of the attribute for database storage.
     * @throws RuntimeException if an error occurs during the encryption process.
     */
    @Override
    public String convertToDatabaseColumn(String attribute) {
        if (attribute == null) return null;

        try {
            byte[] iv = new byte[GCM_IV_LENGTH];
            getSecureRandom().nextBytes(iv);

            Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
            GCMParameterSpec spec = new GCMParameterSpec(GCM_TAG_LENGTH, iv);
            cipher.init(Cipher.ENCRYPT_MODE, new SecretKeySpec(keyBytes, "AES"), spec);

            byte[] encrypted = cipher.doFinal(attribute.getBytes(StandardCharsets.UTF_8));

            ByteBuffer buffer = ByteBuffer.allocate(iv.length + encrypted.length);
            buffer.put(iv);
            buffer.put(encrypted);

            return Base64.getEncoder().encodeToString(buffer.array());
        } catch (Exception e) {
            throw new RuntimeException("Error encrypting field", e);
        }
    }

    /**
     * Converts the encrypted database column value back into its original attribute representation.
     * This method decrypts the Base64-encoded database data using AES encryption in GCM mode.
     * If the provided database value is null, it will return null.
     *
     * @param dbData the Base64-encoded and encrypted database column value to be decrypted. If null, the method will return null.
     * @return the decrypted original string representation of the attribute, or null if the input is null.
     * @throws RuntimeException if an error occurs during the decryption process, such as an invalid key or corrupted data.
     */
    @Override
    public String convertToEntityAttribute(String dbData) {
        if (dbData == null) return null;

        try {
            byte[] decoded = Base64.getDecoder().decode(dbData);

            if (decoded.length < GCM_IV_LENGTH + GCM_TAG_LENGTH_BYTES) {
                throw new IllegalArgumentException("Invalid encrypted data length: expected at least "
                        + (GCM_IV_LENGTH + GCM_TAG_LENGTH_BYTES) + " bytes, got " + decoded.length + " bytes");
            }

            ByteBuffer buffer = ByteBuffer.wrap(decoded);
            byte[] iv = new byte[GCM_IV_LENGTH];
            buffer.get(iv);
            byte[] encrypted = new byte[buffer.remaining()];
            buffer.get(encrypted);

            Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
            GCMParameterSpec spec = new GCMParameterSpec(GCM_TAG_LENGTH, iv);
            cipher.init(Cipher.DECRYPT_MODE, new SecretKeySpec(keyBytes, "AES"), spec);

            byte[] decrypted = cipher.doFinal(encrypted);
            return new String(decrypted, StandardCharsets.UTF_8);

        } catch (Exception e) {
            throw new RuntimeException("Could not decrypt field — possibly wrong key or corrupted data.", e);
        }
    }
}

    
