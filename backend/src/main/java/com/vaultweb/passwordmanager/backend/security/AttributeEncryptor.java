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

    private static final int GCM_IV_LENGTH = 12; // 96 bits
    private static final int GCM_TAG_LENGTH = 128; // 128 bits
    
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
        try {
            if (attribute == null) return null;
            
            // Generate random IV
            byte[] iv = new byte[GCM_IV_LENGTH];
            secureRandom.nextBytes(iv);
            
            // Initialize cipher with GCM mode
            Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
            GCMParameterSpec parameterSpec = new GCMParameterSpec(GCM_TAG_LENGTH, iv);
            cipher.init(Cipher.ENCRYPT_MODE, new SecretKeySpec(keyBytes, "AES"), parameterSpec);
            
            // Encrypt the data
            byte[] encryptedData = cipher.doFinal(attribute.getBytes(StandardCharsets.UTF_8));
            
            // Combine IV and encrypted data
            ByteBuffer byteBuffer = ByteBuffer.allocate(iv.length + encryptedData.length);
            byteBuffer.put(iv);
            byteBuffer.put(encryptedData);
            
            return Base64.getEncoder().encodeToString(byteBuffer.array());
        } catch (Exception e) {
            throw new RuntimeException("Error encrypting", e);
        }
    }

    @Override
    public String convertToEntityAttribute(String dbData) {
        if (dbData == null) return null;
        try {
            // Decode the Base64 data
            byte[] decodedData = Base64.getDecoder().decode(dbData);
            
            // Validate minimum data length
            if (decodedData.length < GCM_IV_LENGTH) {
                throw new IllegalArgumentException("Invalid encrypted data length");
            }
            
            // Extract IV and encrypted data
            ByteBuffer byteBuffer = ByteBuffer.wrap(decodedData);
            byte[] iv = new byte[GCM_IV_LENGTH];
            byteBuffer.get(iv);
            byte[] encryptedData = new byte[byteBuffer.remaining()];
            byteBuffer.get(encryptedData);
            
            // Initialize cipher with GCM mode
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

    
