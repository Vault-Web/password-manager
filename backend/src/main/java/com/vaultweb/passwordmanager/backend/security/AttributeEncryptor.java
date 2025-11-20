package com.vaultweb.passwordmanager.backend.security;

import com.vaultweb.passwordmanager.backend.services.EncryptionService;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@Converter
@RequiredArgsConstructor
public class AttributeEncryptor implements AttributeConverter<String, String> {

  private final EncryptionService encryptionService;

  /**
   * Converts the provided attribute to its corresponding encrypted value suitable for database
   * storage.
   *
   * @param attribute the plaintext attribute value to be converted. If the input is null, the
   *     method may return null.
   * @return the encrypted string representation of the input attribute, or null if the input is
   *     null.
   */
  @Override
  public String convertToDatabaseColumn(String attribute) {
    return encryptionService.encrypt(attribute);
  }

  /**
   * Converts the provided database value into its corresponding entity attribute value. This method
   * is typically used to decrypt data fetched from the database to its original form before serving
   * it to the application.
   *
   * @param dbData the encrypted string value retrieved from the database. If the input is null, the
   *     method may return null without processing.
   * @return the decrypted string representation of the database value, or null if the input is
   *     null.
   */
  @Override
  public String convertToEntityAttribute(String dbData) {
    return encryptionService.decrypt(dbData);
  }
}
