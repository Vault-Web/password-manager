package com.vaultweb.passwordmanager.backend.controllers;

import com.vaultweb.passwordmanager.backend.model.PasswordEntry;
import com.vaultweb.passwordmanager.backend.model.dtos.PasswordEntryDto;
import com.vaultweb.passwordmanager.backend.services.PasswordEntryService;
import jakarta.validation.Valid;
import java.net.URI;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/passwords")
@RequiredArgsConstructor
public class PasswordEntryController {

  private final PasswordEntryService service;

  /**
   * Creates a new password entry based on the provided data and returns the created entry.
   *
   * @param dto the data transfer object containing the details of the password entry to be created.
   *     It must be validated and conform to the specified constraints.
   * @return a ResponseEntity containing the created PasswordEntryDto and the location of the new
   *     resource.
   */
  @PostMapping
  public ResponseEntity<PasswordEntryDto> create(@Valid @RequestBody PasswordEntryDto dto) {
    PasswordEntry created = service.create(new PasswordEntry(dto));
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
  public ResponseEntity<List<PasswordEntryDto>> getAll() {
    List<PasswordEntryDto> dtos = service.getAll().stream().map(PasswordEntryDto::new).toList();
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
  public ResponseEntity<PasswordEntryDto> getById(@PathVariable Long id) {
    return service
        .getById(id)
        .map(entry -> ResponseEntity.ok(new PasswordEntryDto(entry)))
        .orElse(ResponseEntity.notFound().build());
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
      @PathVariable Long id, @Valid @RequestBody PasswordEntryDto dto) {
    PasswordEntry updated = service.update(id, new PasswordEntry(dto));
    return ResponseEntity.ok(new PasswordEntryDto(updated));
  }

  /**
   * Deletes the password entry identified by its unique ID.
   *
   * @param id the unique identifier of the password entry to be deleted
   * @return a ResponseEntity with no content if the deletion is successful
   */
  @DeleteMapping("/{id}")
  public ResponseEntity<Void> delete(@PathVariable Long id) {
    service.delete(id);
    return ResponseEntity.noContent().build();
  }
}
