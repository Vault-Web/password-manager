package com.vaultweb.passwordmanager.backend.model.dtos;

import static org.junit.jupiter.api.Assertions.assertFalse;

import java.time.Instant;
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

  @Test
  void vaultMigrateRequestDto_toStringDoesNotLeakMasterPassword() {
    VaultMigrateRequestDto dto = new VaultMigrateRequestDto();
    dto.setMasterPassword(SECRET);
    assertFalse(dto.toString().contains(SECRET));
  }

  @Test
  void passwordGenerationResponseDto_toStringDoesNotLeakPassword() {
    PasswordGenerationResponseDto dto = new PasswordGenerationResponseDto(SECRET);
    assertFalse(dto.toString().contains(SECRET));
  }

  @Test
  void passwordRevealResponseDto_toStringDoesNotLeakPassword() {
    PasswordRevealResponseDto dto = new PasswordRevealResponseDto(1L, "GitHub", SECRET);
    assertFalse(dto.toString().contains(SECRET));
  }

  @Test
  void vaultUnlockResponseDto_toStringDoesNotLeakToken() {
    VaultUnlockResponseDto dto = new VaultUnlockResponseDto(SECRET, Instant.ofEpochMilli(0));
    assertFalse(dto.toString().contains(SECRET));
  }

  @Test
  void vaultExportRequestDto_toStringDoesNotLeakSecrets() {
    VaultExportRequestDto dto = new VaultExportRequestDto();
    dto.setMasterPassword(SECRET);
    dto.setExportPassword(OTHER_SECRET);
    String result = dto.toString();
    assertFalse(result.contains(SECRET));
    assertFalse(result.contains(OTHER_SECRET));
  }

  @Test
  void vaultImportRequestDto_toStringDoesNotLeakSecretsOrData() {
    VaultImportRequestDto dto = new VaultImportRequestDto();
    dto.setData(SECRET);
    dto.setImportPassword(OTHER_SECRET);
    dto.setMasterPassword("yet-another-top-secret");
    String result = dto.toString();
    assertFalse(result.contains(SECRET));
    assertFalse(result.contains(OTHER_SECRET));
    assertFalse(result.contains("yet-another-top-secret"));
  }
}
