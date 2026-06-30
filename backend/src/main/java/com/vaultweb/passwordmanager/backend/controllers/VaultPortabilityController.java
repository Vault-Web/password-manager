package com.vaultweb.passwordmanager.backend.controllers;

import com.vaultweb.passwordmanager.backend.model.dtos.VaultExportRequestDto;
import com.vaultweb.passwordmanager.backend.model.dtos.VaultImportRequestDto;
import com.vaultweb.passwordmanager.backend.model.dtos.VaultImportResponseDto;
import com.vaultweb.passwordmanager.backend.security.AuthenticatedUser;
import com.vaultweb.passwordmanager.backend.services.VaultPortabilityService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** Export and import the vault (issue #81). */
@RestController
@RequestMapping("/api/vault")
@RequiredArgsConstructor
public class VaultPortabilityController {

  private static final MediaType TEXT_CSV = new MediaType("text", "csv");

  private final VaultPortabilityService portabilityService;

  /**
   * Exports the authenticated user's vault. The response is a downloadable file: an encrypted
   * envelope by default, or a plaintext CSV when explicitly confirmed.
   */
  @PostMapping("/export")
  public ResponseEntity<String> export(
      @AuthenticationPrincipal AuthenticatedUser user,
      @RequestHeader(value = "X-Vault-Token", required = false) String vaultToken,
      @Valid @RequestBody VaultExportRequestDto dto) {
    boolean encrypted = dto.getEncrypted() == null || dto.getEncrypted();
    VaultPortabilityService.VaultExportResult result =
        portabilityService.export(
            user.userId(),
            dto.getMasterPassword(),
            vaultToken,
            encrypted,
            dto.getExportPassword(),
            Boolean.TRUE.equals(dto.getConfirmPlaintext()));

    String filename = result.encrypted() ? "vault-export.vault" : "vault-export.csv";
    MediaType contentType = result.encrypted() ? MediaType.APPLICATION_OCTET_STREAM : TEXT_CSV;
    return ResponseEntity.ok()
        .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
        .contentType(contentType)
        .body(result.content());
  }

  /** Imports entries into the authenticated user's vault from an export file or plaintext CSV. */
  @PostMapping("/import")
  public ResponseEntity<VaultImportResponseDto> importVault(
      @AuthenticationPrincipal AuthenticatedUser user,
      @RequestHeader(value = "X-Vault-Token", required = false) String vaultToken,
      @Valid @RequestBody VaultImportRequestDto dto) {
    return ResponseEntity.ok(
        portabilityService.importVault(
            user.userId(),
            dto.getData(),
            dto.getImportPassword(),
            dto.getMasterPassword(),
            vaultToken));
  }
}
