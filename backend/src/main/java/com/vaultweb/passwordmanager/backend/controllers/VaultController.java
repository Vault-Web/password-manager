package com.vaultweb.passwordmanager.backend.controllers;

import com.vaultweb.passwordmanager.backend.model.dtos.VaultMigrateRequestDto;
import com.vaultweb.passwordmanager.backend.model.dtos.VaultMigrateResponseDto;
import com.vaultweb.passwordmanager.backend.model.dtos.VaultRotateRequestDto;
import com.vaultweb.passwordmanager.backend.model.dtos.VaultSetupRequestDto;
import com.vaultweb.passwordmanager.backend.model.dtos.VaultStatusResponseDto;
import com.vaultweb.passwordmanager.backend.model.dtos.VaultUnlockResponseDto;
import com.vaultweb.passwordmanager.backend.model.dtos.VaultVerifyRequestDto;
import com.vaultweb.passwordmanager.backend.model.dtos.VaultVerifyResponseDto;
import com.vaultweb.passwordmanager.backend.security.AuthenticatedUser;
import com.vaultweb.passwordmanager.backend.services.VaultService;
import com.vaultweb.passwordmanager.backend.services.VaultSessionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/vault")
@RequiredArgsConstructor
public class VaultController {

  private final VaultService vaultService;
  private final VaultSessionService vaultSessionService;

  /**
   * Checks if the vault is initialized for the authenticated user.
   *
   * @param user
   * @return the vault status response
   */
  @GetMapping("/status")
  public ResponseEntity<VaultStatusResponseDto> status(
      @AuthenticationPrincipal AuthenticatedUser user) {
    return ResponseEntity.ok(new VaultStatusResponseDto(vaultService.isInitialized(user.userId())));
  }

  /**
   * Sets up the vault with the provided master password.
   *
   * @param user
   * @param dto
   * @return no content response
   */
  @PostMapping("/setup")
  public ResponseEntity<Void> setup(
      @AuthenticationPrincipal AuthenticatedUser user,
      @Valid @RequestBody VaultSetupRequestDto dto) {
    vaultService.setup(user.userId(), dto.getMasterPassword());
    return ResponseEntity.noContent().build();
  }

  /**
   * Verifies the provided master password for the authenticated user.
   *
   * @param user
   * @param dto
   * @return the vault verify response
   */
  @PostMapping("/verify")
  public ResponseEntity<VaultVerifyResponseDto> verify(
      @AuthenticationPrincipal AuthenticatedUser user,
      @Valid @RequestBody VaultVerifyRequestDto dto) {
    vaultService.verify(user.userId(), dto.getMasterPassword());
    return ResponseEntity.ok(new VaultVerifyResponseDto(true));
  }

  /**
   * Session-like unlock: validates master password once and returns a short-lived vault token.
   *
   * @return the vault unlock response containing the token and its expiration time
   */
  @PostMapping("/unlock")
  public ResponseEntity<VaultUnlockResponseDto> unlock(
      @AuthenticationPrincipal AuthenticatedUser user,
      @Valid @RequestBody VaultVerifyRequestDto dto) {
    VaultSessionService.VaultUnlock unlock =
        vaultSessionService.unlock(user.userId(), dto.getMasterPassword());
    return ResponseEntity.ok(new VaultUnlockResponseDto(unlock.token(), unlock.expiresAt()));
  }

  /**
   * Locks the vault session associated with the provided vault token.
   *
   * @param user
   * @param vaultToken
   * @return no content response
   */
  @PostMapping("/lock")
  public ResponseEntity<Void> lock(
      @AuthenticationPrincipal AuthenticatedUser user,
      @RequestHeader(value = "X-Vault-Token", required = false) String vaultToken) {
    vaultSessionService.lock(user.userId(), vaultToken);
    return ResponseEntity.noContent().build();
  }

  /**
   * Rotates the master password, re-encrypting all vault data with the new password.
   *
   * @param user
   * @param dto
   * @return no content response
   */
  @PostMapping("/rotate")
  public ResponseEntity<Void> rotate(
      @AuthenticationPrincipal AuthenticatedUser user,
      @Valid @RequestBody VaultRotateRequestDto dto) {
    vaultService.rotate(user.userId(), dto.getCurrentMasterPassword(), dto.getNewMasterPassword());
    return ResponseEntity.noContent().build();
  }

  /**
   * Migrates all plaintext passwords to vault-encrypted format.
   *
   * @param user
   * @param vaultToken
   * @param dto
   * @return the vault migrate response with number of passwords migrated
   */
  @PostMapping("/migrate")
  public ResponseEntity<VaultMigrateResponseDto> migrate(
      @AuthenticationPrincipal AuthenticatedUser user,
      @RequestHeader(value = "X-Vault-Token", required = false) String vaultToken,
      @Valid @RequestBody(required = false) VaultMigrateRequestDto dto) {
    int migrated;
    if (vaultToken != null && !vaultToken.isBlank()) {
      byte[] dek = vaultSessionService.requireDek(user.userId(), vaultToken);
      migrated = vaultService.migrateAllPasswords(user.userId(), dek);
    } else {
      if (dto == null || dto.getMasterPassword() == null || dto.getMasterPassword().isBlank()) {
        throw new IllegalArgumentException(
            "masterPassword is required when no X-Vault-Token is provided");
      }
      migrated = vaultService.migrateAllPasswords(user.userId(), dto.getMasterPassword());
    }
    return ResponseEntity.ok(new VaultMigrateResponseDto(migrated));
  }
}
