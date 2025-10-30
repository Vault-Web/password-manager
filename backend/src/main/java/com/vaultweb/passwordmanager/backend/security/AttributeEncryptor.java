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

    private static final int GCM_IV_LENGTH = 12;  // 96 bits
    private static final int GCM_TAG_LENGTH = 128; // 128 bits (in bits)
    private static final int GCM_TAG_LENGTH_BYTES = GCM_TAG_LENGTH / 8;

    @Value("${encryption.secret}")
    private String secretKey;

    private byte[] keyBytes;
    private final SecureRandom secureRandom = new SecureRandom();

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

    @Override
    public String convertToDatabaseColumn(String attribute) {
        if (attribute == null) return null;

        try {
            byte[] iv = new byte[GCM_IV_LENGTH];
            secureRandom.nextBytes(iv);

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

    @Override
    public String convertToEntityAttribute(String dbData) {
        if (dbData == null) return null;

        try {
            byte[] decoded = Base64.getDecoder().decode(dbData);

            if (decoded.length < GCM_IV_LENGTH + GCM_TAG_LENGTH_BYTES) {
                throw new IllegalArgumentException("Invalid encrypted data length");
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

    
