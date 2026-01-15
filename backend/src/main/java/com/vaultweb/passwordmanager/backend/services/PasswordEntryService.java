package com.vaultweb.passwordmanager.backend.services;

import com.vaultweb.passwordmanager.backend.exceptions.NotFoundException;
import com.vaultweb.passwordmanager.backend.model.Category;
import com.vaultweb.passwordmanager.backend.model.PasswordEntry;
import com.vaultweb.passwordmanager.backend.model.dtos.PasswordRevealResponseDto;
import com.vaultweb.passwordmanager.backend.repositories.CategoryRepository;
import com.vaultweb.passwordmanager.backend.repositories.PasswordEntryRepository;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PasswordEntryService {

  private final PasswordEntryRepository repository;
  private final CategoryRepository categoryRepository;
  private final VaultService vaultService;
  private final VaultSessionService vaultSessionService;

  /**
   * Create entry in "legacy" mode (no master password / no vault token).
   *
   * @return the created PasswordEntry
   */
  public PasswordEntry create(PasswordEntry entry, Long ownerId, Long categoryId) {
    entry.setOwnerId(ownerId);
    entry.setCategory(resolveCategory(categoryId, ownerId));
    entry.setPassword(
        vaultService.encryptPasswordForStorage(ownerId, (String) null, entry.getPassword()));
    return repository.save(entry);
  }

    /**
     * Create entry by providing the user's master password.
     *
     * @return the created PasswordEntry
     */
    public PasswordEntry create(
      PasswordEntry entry, Long ownerId, Long categoryId, String masterPassword) {
    entry.setOwnerId(ownerId);
    entry.setCategory(resolveCategory(categoryId, ownerId));
    entry.setPassword(
        vaultService.encryptPasswordForStorage(ownerId, masterPassword, entry.getPassword()));
    return repository.save(entry);
  }

    /**
     * Create entry using either an active vault session token or a master password.
     *
     * @return the created PasswordEntry
     */
    public PasswordEntry create(
      PasswordEntry entry,
      Long ownerId,
      Long categoryId,
      String masterPassword,
      String vaultToken) {
    entry.setOwnerId(ownerId);
    entry.setCategory(resolveCategory(categoryId, ownerId));

    if (vaultToken != null && !vaultToken.isBlank()) {
      byte[] dek = vaultSessionService.requireDek(ownerId, vaultToken);
      entry.setPassword(vaultService.encryptPasswordForStorage(ownerId, dek, entry.getPassword()));
      return repository.save(entry);
    }

    entry.setPassword(
        vaultService.encryptPasswordForStorage(ownerId, masterPassword, entry.getPassword()));
    return repository.save(entry);
  }

  /**
   * Retrieves all PasswordEntry entities from the repository.
   *
   * @return a list of all PasswordEntry entities
   */
  public List<PasswordEntry> getAll(Long ownerId) {
    return repository.findAllByOwnerId(ownerId);
  }

  /**
   * Retrieves a PasswordEntry by its unique identifier.
   *
   * @param id the unique identifier of the PasswordEntry to retrieve
   * @return an Optional containing the PasswordEntry if found, or an empty Optional if not found
   */
  public Optional<PasswordEntry> getById(Long id, Long ownerId) {
    return repository.findByIdAndOwnerId(id, ownerId);
  }

  /**
   * Update entry in "legacy" mode (no master password / no vault token).
   *
   * @return the updated PasswordEntry
   */
  public PasswordEntry update(Long id, PasswordEntry updated, Long ownerId, Long categoryId) {
    PasswordEntry existing =
        repository
            .findByIdAndOwnerId(id, ownerId)
            .orElseThrow(() -> new NotFoundException("Password entry not found with id " + id));

    existing.setName(updated.getName());
    existing.setUsername(updated.getUsername());
    existing.setPassword(
      vaultService.encryptPasswordForStorage(ownerId, (String) null, updated.getPassword()));
    existing.setUrl(updated.getUrl());
    existing.setNotes(updated.getNotes());
    existing.setCategory(resolveCategory(categoryId, ownerId));

    return repository.save(existing);
  }

    /**
     * Update entry by providing the user's master password.
     *
     * @return the updated PasswordEntry
     */
    public PasswordEntry update(
      Long id, PasswordEntry updated, Long ownerId, Long categoryId, String masterPassword) {
    PasswordEntry existing =
        repository
            .findByIdAndOwnerId(id, ownerId)
            .orElseThrow(() -> new NotFoundException("Password entry not found with id " + id));

    existing.setName(updated.getName());
    existing.setUsername(updated.getUsername());
    existing.setPassword(
        vaultService.encryptPasswordForStorage(ownerId, masterPassword, updated.getPassword()));
    existing.setUrl(updated.getUrl());
    existing.setNotes(updated.getNotes());
    existing.setCategory(resolveCategory(categoryId, ownerId));

    return repository.save(existing);
  }

    /**
     * Update entry using either an active vault session token or a master password.
     *
     * @return the updated PasswordEntry
     */
    public PasswordEntry update(
      Long id,
      PasswordEntry updated,
      Long ownerId,
      Long categoryId,
      String masterPassword,
      String vaultToken) {
    PasswordEntry existing =
        repository
            .findByIdAndOwnerId(id, ownerId)
            .orElseThrow(() -> new NotFoundException("Password entry not found with id " + id));

    existing.setName(updated.getName());
    existing.setUsername(updated.getUsername());

    if (vaultToken != null && !vaultToken.isBlank()) {
      byte[] dek = vaultSessionService.requireDek(ownerId, vaultToken);
      existing.setPassword(
          vaultService.encryptPasswordForStorage(ownerId, dek, updated.getPassword()));
    } else {
      existing.setPassword(
          vaultService.encryptPasswordForStorage(ownerId, masterPassword, updated.getPassword()));
    }

    existing.setUrl(updated.getUrl());
    existing.setNotes(updated.getNotes());
    existing.setCategory(resolveCategory(categoryId, ownerId));

    return repository.save(existing);
  }

  /**
   * Reveal (decrypt) a password by providing the user's master password.
   *
   * @return the PasswordRevealResponseDto containing the revealed password
   */
  public PasswordRevealResponseDto reveal(Long id, Long ownerId, String masterPassword) {
    PasswordEntry entry =
        repository
            .findByIdAndOwnerId(id, ownerId)
            .orElseThrow(() -> new NotFoundException("Password entry not found with id " + id));

    String plainPassword = vaultService.decryptPasswordForReveal(ownerId, masterPassword, entry);
    return new PasswordRevealResponseDto(entry.getId(), entry.getName(), plainPassword);
  }

    /**
     * Reveal (decrypt) a password using either an active vault session token or a master password.
     *
     * @return the PasswordRevealResponseDto containing the revealed password
     */
    public PasswordRevealResponseDto reveal(
      Long id, Long ownerId, String masterPassword, String vaultToken) {
    PasswordEntry entry =
        repository
            .findByIdAndOwnerId(id, ownerId)
            .orElseThrow(() -> new NotFoundException("Password entry not found with id " + id));

    String plainPassword;
    if (vaultToken != null && !vaultToken.isBlank()) {
      byte[] dek = vaultSessionService.requireDek(ownerId, vaultToken);
      plainPassword = vaultService.decryptPasswordForReveal(ownerId, dek, entry);
    } else {
      plainPassword = vaultService.decryptPasswordForReveal(ownerId, masterPassword, entry);
    }

    return new PasswordRevealResponseDto(entry.getId(), entry.getName(), plainPassword);
  }

  /**
   * Deletes a PasswordEntry entity with the specified ID. If the PasswordEntry does not exist, a
   * NotFoundException is thrown.
   *
   * @param id the ID of the PasswordEntry to be deleted
   */
  public void delete(Long id, Long ownerId) {
    PasswordEntry entry =
        repository
            .findByIdAndOwnerId(id, ownerId)
            .orElseThrow(() -> new NotFoundException("Password entry not found with id " + id));
    repository.delete(entry);
  }

  /**
   * Resolves the Category entity for the given categoryId and ownerId.
   * @param categoryId
   * @param ownerId
   * @return the Category entity, or null if categoryId is null
   */
  private Category resolveCategory(Long categoryId, Long ownerId) {
    if (categoryId == null) {
      return null;
    }
    return categoryRepository
        .findByIdAndOwnerId(categoryId, ownerId)
        .orElseThrow(
            () ->
                new NotFoundException(
                    "Category not found with id " + categoryId + " for current user"));
  }
}
