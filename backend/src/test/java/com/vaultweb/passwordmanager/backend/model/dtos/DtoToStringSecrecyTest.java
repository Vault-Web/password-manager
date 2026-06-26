package com.vaultweb.passwordmanager.backend.model.dtos;

import static org.junit.jupiter.api.Assertions.assertFalse;

import org.junit.jupiter.api.Test;

class DtoToStringSecrecyTest {

  private static final String SECRET = "TOP_SECRET_VALUE";
  private static final String OTHER_SECRET = "ANOTHER_TOP_SECRET";

  @Test
  void vaultSetupRequestDto_toStringDoesNotLeakMasterPassword() {
    VaultSetupRequestDto dto = new VaultSetupRequestDto();
    dto.setMasterPassword(SECRET);
    assertFalse(dto.toString().contains(SECRET));
  }

  @Test
  void vaultVerifyRequestDto_toStringDoesNotLeakMasterPassword() {
    VaultVerifyRequestDto dto = new VaultVerifyRequestDto();
    dto.setMasterPassword(SECRET);
    assertFalse(dto.toString().contains(SECRET));
  }

  @Test
  void passwordRevealRequestDto_toStringDoesNotLeakMasterPassword() {
    PasswordRevealRequestDto dto = new PasswordRevealRequestDto();
    dto.setMasterPassword(SECRET);
    assertFalse(dto.toString().contains(SECRET));
  }

  @Test
  void vaultRotateRequestDto_toStringDoesNotLeakMasterPasswords() {
    VaultRotateRequestDto dto = new VaultRotateRequestDto();
    dto.setCurrentMasterPassword(SECRET);
    dto.setNewMasterPassword(OTHER_SECRET);
    String result = dto.toString();
    assertFalse(result.contains(SECRET));
    assertFalse(result.contains(OTHER_SECRET));
  }

  @Test
  void passwordEntryDto_toStringDoesNotLeakPasswordOrMasterPassword() {
    PasswordEntryDto dto = new PasswordEntryDto();
    dto.setName("GitHub");
    dto.setUsername("gabriel");
    dto.setPassword(SECRET);
    dto.setMasterPassword(OTHER_SECRET);
    String result = dto.toString();
    assertFalse(result.contains(SECRET));
    assertFalse(result.contains(OTHER_SECRET));
  }
}
