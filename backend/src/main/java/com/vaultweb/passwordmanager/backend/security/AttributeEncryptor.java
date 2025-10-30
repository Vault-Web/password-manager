package com.vaultweb.passwordmanager.backend.security;

import java.util.Base64;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;

import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Component
@Converter
public class AttributeEncryptor implements AttributeConverter<String, String> {

    @Value("${encryption.secret}")
    private String secretKey;

    @PostConstruct
    private void validateKey() {
        if (secretKey == null || !(secretKey.length() == 16 || secretKey.length() == 24 || secretKey.length() == 32)) {
            throw new IllegalArgumentException("Encryption key must be 16, 24, or 32 characters long");
        }
    }

    @Override
    public String convertToDatabaseColumn(String attribute) {
        try {
            if (attribute == null) return null;
            Cipher cipher = Cipher.getInstance("AES");
            cipher.init(Cipher.ENCRYPT_MODE,
                new SecretKeySpec(secretKey.getBytes(), "AES"));
            return Base64.getEncoder().encodeToString(cipher.doFinal(attribute.getBytes()));
        } catch (Exception e) {
            throw new RuntimeException("Error encrypting", e);
        }
    }

    @Override
    public String convertToEntityAttribute(String dbData) {
        try {
            if (dbData == null) return null;
            Cipher cipher = Cipher.getInstance("AES");
            cipher.init(Cipher.DECRYPT_MODE,
                new SecretKeySpec(secretKey.getBytes(), "AES"));
            return new String(cipher.doFinal(Base64.getDecoder().decode(dbData)));
        } catch (Exception e) {
            throw new RuntimeException("Error decrypting", e);
        }
    }
}

    
