package com.vaultweb.passwordmanager.backend.services;

import org.springframework.stereotype.Service;

/**
 * @author rashmi.soni
 */
@Service
public class PasswordStrengthService {

  public int calculateStrength(String password) {
    int score = 0;

    if (password.length() >= 12) score += 40;
    if (password.matches(".*[A-Z].*")) score += 20;
    if (password.matches(".*[a-z].*")) score += 20;
    if (password.matches(".*\\d.*")) score += 10;
    if (password.matches(".*[@#$%^&+=!?.()].*")) score += 10;

    return Math.min(score, 100);
  }

  public String rating(int score) {
    if (score >= 80) return "Strong";
    if (score >= 50) return "Moderate";
    return "Weak";
  }
}
