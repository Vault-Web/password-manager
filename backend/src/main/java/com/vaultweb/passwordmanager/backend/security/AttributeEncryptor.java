package com.vaultweb.passwordmanager.backend.security;

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

    private static final int GCM_IV_LENGTH = 12; // 96 bits
    private static final int GCM_TAG_LENGTH = 128; // 128 bits

    @Value("${encryption.secret}")
    private String secretKey;

    private byte[] keyBytes;
    private static final SecureRandom secureRandom = new SecureRandom();

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
        try {
            if (attribute == null) return null;
            
            // Generate a random IV for each encryption operation
            byte[] iv = new byte[GCM_IV_LENGTH];
            secureRandom.nextBytes(iv);
            
            // Initialize cipher with AES/GCM mode
            Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
            GCMParameterSpec parameterSpec = new GCMParameterSpec(GCM_TAG_LENGTH, iv);
            cipher.init(Cipher.ENCRYPT_MODE, new SecretKeySpec(keyBytes, "AES"), parameterSpec);
            
            // Encrypt the data
            byte[] encryptedData = cipher.doFinal(attribute.getBytes(StandardCharsets.UTF_8));
            
            // Prepend IV to encrypted data: [IV][encrypted data]
            byte[] combined = new byte[iv.length + encryptedData.length];
            System.arraycopy(iv, 0, combined, 0, iv.length);
            System.arraycopy(encryptedData, 0, combined, iv.length, encryptedData.length);
            
            return Base64.getEncoder().encodeToString(combined);
        } catch (Exception e) {
            throw new RuntimeException("Error encrypting", e);
        }
    }

    @Override
    public String convertToEntityAttribute(String dbData) {
        if (dbData == null) return null;
        try {
            // Decode the Base64 data
            byte[] combined = Base64.getDecoder().decode(dbData);
            
            // Validate minimum data length
            if (combined.length < GCM_IV_LENGTH) {
                throw new RuntimeException("Invalid encrypted data: too short");
            }
            
            // Extract IV from the beginning of the data
            byte[] iv = new byte[GCM_IV_LENGTH];
            System.arraycopy(combined, 0, iv, 0, GCM_IV_LENGTH);
            
            // Extract encrypted data
            byte[] encryptedData = new byte[combined.length - GCM_IV_LENGTH];
            System.arraycopy(combined, GCM_IV_LENGTH, encryptedData, 0, encryptedData.length);
            
            // Initialize cipher with AES/GCM mode
            Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
            GCMParameterSpec parameterSpec = new GCMParameterSpec(GCM_TAG_LENGTH, iv);
            cipher.init(Cipher.DECRYPT_MODE, new SecretKeySpec(keyBytes, "AES"), parameterSpec);
            
            // Decrypt the data
            byte[] decryptedBytes = cipher.doFinal(encryptedData);
            return new String(decryptedBytes, StandardCharsets.UTF_8);
        } catch (Exception e) {
            throw new RuntimeException("Could not decrypt field. Possibly wrong encryption key or corrupted data.", e);
        }
    }

}

    
