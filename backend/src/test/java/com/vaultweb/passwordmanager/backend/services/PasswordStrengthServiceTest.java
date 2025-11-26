package com.vaultweb.passwordmanager.backend.services;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

/**
 * @author rashmi.soni
 */
class PasswordStrengthServiceTest {

  private final PasswordStrengthService service = new PasswordStrengthService();

  @Test
  void testStrongPassword() {
    int score = service.calculateStrength("Aa1@StrongPassword123!");
    assertTrue(score >= 80);
    assertEquals("Strong", service.rating(score));
  }

  @Test
  void testModeratePassword() {
    int score = service.calculateStrength("Test1234");
    assertTrue(score >= 50 && score < 80);
    assertEquals("Moderate", service.rating(score));
  }

  @Test
  void testWeakPassword() {
    int score = service.calculateStrength("abc");
    assertTrue(score < 50);
    assertEquals("Weak", service.rating(score));
  }
}
