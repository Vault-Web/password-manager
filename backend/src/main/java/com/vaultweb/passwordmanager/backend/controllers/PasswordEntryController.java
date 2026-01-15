package com.vaultweb.passwordmanager.backend.controllers;

import com.vaultweb.passwordmanager.backend.exceptions.VaultLockedException;
import com.vaultweb.passwordmanager.backend.model.PasswordEntry;
import com.vaultweb.passwordmanager.backend.model.dtos.PasswordEntryDto;
import com.vaultweb.passwordmanager.backend.model.dtos.PasswordRevealRequestDto;
import com.vaultweb.passwordmanager.backend.model.dtos.PasswordRevealResponseDto;
import com.vaultweb.passwordmanager.backend.security.AuthenticatedUser;
import com.vaultweb.passwordmanager.backend.services.PasswordEntryService;
import com.vaultweb.passwordmanager.backend.services.VaultService;
import jakarta.validation.Valid;
import java.net.URI;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/passwords")
@RequiredArgsConstructor
public class PasswordEntryController {

  private final PasswordEntryService service;
  private final VaultService vaultService;

  /**
   * Creates a new password entry based on the provided data and returns the created entry.
   *
   * @param dto the data transfer object containing the details of the password entry to be created.
   *     It must be validated and conform to the specified constraints.
   * @return a ResponseEntity containing the created PasswordEntryDto and the location of the new
   *     resource.
   */
  @PostMapping
  public ResponseEntity<PasswordEntryDto> create(
      @AuthenticationPrincipal AuthenticatedUser user,
      @RequestHeader(value = "X-Vault-Token", required = false) String vaultToken,
      @Valid @RequestBody PasswordEntryDto dto) {
    PasswordEntry created =
        service.create(
            new PasswordEntry(dto),
            user.userId(),
            dto.getCategoryId(),
            dto.getMasterPassword(),
            vaultToken);
    return ResponseEntity.created(URI.create("/api/passwords/" + created.getId()))
        .body(new PasswordEntryDto(created));
  }

  /**
   * Retrieves all password entries.
   *
   * @return a ResponseEntity containing a list of PasswordEntryDto objects representing all stored
   *     password entries.
   */
  @GetMapping
  public ResponseEntity<List<PasswordEntryDto>> getAll(
      @AuthenticationPrincipal AuthenticatedUser user) {
    List<PasswordEntryDto> dtos =
        service.getAll(user.userId()).stream().map(PasswordEntryDto::new).toList();
    return ResponseEntity.ok(dtos);
  }

  /**
   * Retrieves a password entry by its unique identifier.
   *
   * @param id the unique identifier of the password entry to be retrieved
   * @return a ResponseEntity containing the PasswordEntryDto if found, or a ResponseEntity with a
   *     404 status if not found
   */
  @GetMapping("/{id}")
  public ResponseEntity<PasswordEntryDto> getById(
      @AuthenticationPrincipal AuthenticatedUser user, @PathVariable Long id) {
    return service
        .getById(id, user.userId())
        .map(entry -> ResponseEntity.ok(new PasswordEntryDto(entry)))
        .orElse(ResponseEntity.notFound().build());
  }

  /**
   * Legacy reveal endpoint for setups without a master password vault.
   *
   * <p>If a vault is initialized, revealing requires the master password and callers must use the
   * POST variant.
   */
  @GetMapping("/{id}/reveal")
  public ResponseEntity<PasswordRevealResponseDto> reveal(
      @AuthenticationPrincipal AuthenticatedUser user, @PathVariable Long id) {
    if (vaultService.isInitialized(user.userId())) {
      throw new VaultLockedException(
          "Master password required. Use POST /api/passwords/{id}/reveal with masterPassword.");
    }

    return service
        .getById(id, user.userId())
        .map(PasswordRevealResponseDto::fromEntry)
        .map(ResponseEntity::ok)
        .orElse(ResponseEntity.notFound().build());
  }

  /** Reveals a password for vault-enabled users. Requires masterPassword in the request body. */
  @PostMapping("/{id}/reveal")
  public ResponseEntity<PasswordRevealResponseDto> revealWithMasterPassword(
      @AuthenticationPrincipal AuthenticatedUser user,
      @PathVariable Long id,
      @RequestHeader(value = "X-Vault-Token", required = false) String vaultToken,
      @RequestBody(required = false) PasswordRevealRequestDto dto) {
    String masterPassword = dto != null ? dto.getMasterPassword() : null;
    return ResponseEntity.ok(service.reveal(id, user.userId(), masterPassword, vaultToken));
  }

  /**
   * Updates an existing password entry identified by its unique ID with the provided data.
   *
   * @param id the unique identifier of the password entry to be updated
   * @param dto the data transfer object containing the updated details for the password entry. It
   *     must be validated and conform to the specified constraints.
   * @return a ResponseEntity containing the updated PasswordEntryDto object
   */
  @PutMapping("/{id}")
  public ResponseEntity<PasswordEntryDto> update(
      @AuthenticationPrincipal AuthenticatedUser user,
      @PathVariable Long id,
      @RequestHeader(value = "X-Vault-Token", required = false) String vaultToken,
      @Valid @RequestBody PasswordEntryDto dto) {
    PasswordEntry updated =
        service.update(
            id,
            new PasswordEntry(dto),
            user.userId(),
            dto.getCategoryId(),
            dto.getMasterPassword(),
            vaultToken);
    return ResponseEntity.ok(new PasswordEntryDto(updated));
  }

  /**
   * Deletes the password entry identified by its unique ID.
   *
   * @param id the unique identifier of the password entry to be deleted
   * @return a ResponseEntity with no content if the deletion is successful
   */
  @DeleteMapping("/{id}")
  public ResponseEntity<Void> delete(
      @AuthenticationPrincipal AuthenticatedUser user, @PathVariable Long id) {
    service.delete(id, user.userId());
    return ResponseEntity.noContent().build();
  }
}
