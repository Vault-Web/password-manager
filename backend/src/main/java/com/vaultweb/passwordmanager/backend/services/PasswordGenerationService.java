package com.vaultweb.passwordmanager.backend.services;

import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Service;

/**
 * @author rashmi.soni
 */
@Service
public class PasswordGenerationService {
  private static final SecureRandom RANDOM = new SecureRandom();

  private static final String LOWER = "abcdefghijklmnopqrstuvwxyz";
  private static final String UPPER = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
  private static final String DIGITS = "0123456789";
  private static final String SPECIAL = "!@#$%&*()-_=+[]{};:,.<>?";

  private static final int DEFAULT_LENGTH = 16;
  private static final int MIN_LENGTH = 8;
  private static final int MAX_LENGTH = 128;

  public String generate(
      Integer length, Boolean includeUppercase, Boolean includeNumbers, Boolean includeSpecial) {

    int len = (length == null) ? DEFAULT_LENGTH : length;
    if (len < MIN_LENGTH) {
      throw new IllegalArgumentException("length must be >= " + MIN_LENGTH);
    }
    if (len > MAX_LENGTH) {
      throw new IllegalArgumentException("length must be <= " + MAX_LENGTH);
    }

    boolean useUpper = includeUppercase == null || includeUppercase;
    boolean useNumbers = includeNumbers == null || includeNumbers;
    boolean useSpecial = includeSpecial == null || includeSpecial;

    // Always include lowercase
    List<String> pools = new ArrayList<>();
    pools.add(LOWER);
    if (useUpper) pools.add(UPPER);
    if (useNumbers) pools.add(DIGITS);
    if (useSpecial) pools.add(SPECIAL);

    StringBuilder allChars = new StringBuilder();
    pools.forEach(allChars::append);
    if (allChars.isEmpty()) {
      throw new IllegalArgumentException("At least one character class must be enabled.");
    }

    char[] password = new char[len];
    int pos = 0;
    for (String pool : pools) {
      if (pos >= len) break;
      password[pos++] = randomCharFrom(pool);
    }
    for (; pos < len; pos++) {
      password[pos] = randomCharFrom(allChars.toString());
    }
    for (int i = password.length - 1; i > 0; i--) {
      int j = RANDOM.nextInt(i + 1);
      char tmp = password[i];
      password[i] = password[j];
      password[j] = tmp;
    }
    return new String(password);
  }

  private char randomCharFrom(String s) {
    int idx = RANDOM.nextInt(s.length());
    return s.charAt(idx);
  }
}
