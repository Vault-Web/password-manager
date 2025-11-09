package com.vaultweb.passwordmanager.backend.security;

import com.vaultweb.passwordmanager.backend.services.EncryptionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Component
@Converter
@RequiredArgsConstructor
public class AttributeEncryptor implements AttributeConverter<String, String> {

    private final EncryptionService encryptionService;

    @Override
    public String convertToDatabaseColumn(String attribute) {
        return encryptionService.encrypt(attribute);
    }

    @Override
    public String convertToEntityAttribute(String dbData) {
        return encryptionService.decrypt(dbData);
    }
}

    
