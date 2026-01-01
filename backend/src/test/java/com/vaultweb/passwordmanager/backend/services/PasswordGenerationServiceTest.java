package com.vaultweb.passwordmanager.backend.services;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

import com.vaultweb.passwordmanager.backend.exceptions.InvalidRequestException;

/**
 * @author rashmi.soni
 */
class PasswordGenerationServiceTest {

  private final PasswordGenerationService svc = new PasswordGenerationService();

  @Test
  void generatesDefaultLength() {
    String p = svc.generate(null, null, null, null);
    assertNotNull(p);
    assertEquals(16, p.length());
  }

  @Test
  void respectsLengthAndClasses() {
    String p = svc.generate(12, true, true, true);
    assertEquals(12, p.length());
    assertTrue(p.matches(".*[a-z].*"));
    assertTrue(p.matches(".*[A-Z].*"));
    assertTrue(p.matches(".*[0-9].*"));
    assertTrue(p.matches(".*[!@#$%&*()\\-_=+\\[\\]{};:,.<>?].*"));
  }

  @Test
  void errorsWhenTooShort() {
    assertThrows(InvalidRequestException.class, () -> {
    svc.generate(5, true, true, true);
});
  }
}
