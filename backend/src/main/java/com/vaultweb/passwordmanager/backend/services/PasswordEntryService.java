package com.vaultweb.passwordmanager.backend.services;

import com.vaultweb.passwordmanager.backend.exceptions.NotFoundException;
import com.vaultweb.passwordmanager.backend.model.PasswordEntry;
import com.vaultweb.passwordmanager.backend.repositories.PasswordEntryRepository;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PasswordEntryService {

  private final PasswordEntryRepository repository;

  /**
   * Creates a new PasswordEntry by saving it to the database.
   *
   * @param entry the PasswordEntry object to be created and saved
   * @return the saved PasswordEntry object
   */
  public PasswordEntry create(PasswordEntry entry) {
    return repository.save(entry);
  }

  /**
   * Retrieves all PasswordEntry entities from the repository.
   *
   * @return a list of all PasswordEntry entities
   */
  public List<PasswordEntry> getAll() {
    return repository.findAll();
  }

  /**
   * Retrieves a PasswordEntry by its unique identifier.
   *
   * @param id the unique identifier of the PasswordEntry to retrieve
   * @return an Optional containing the PasswordEntry if found, or an empty Optional if not found
   */
  public Optional<PasswordEntry> getById(Long id) {
    return repository.findById(id);
  }

  /**
   * Updates an existing PasswordEntry with new values provided in the updated object. If the
   * PasswordEntry with the specified ID does not exist, a NotFoundException is thrown.
   *
   * @param id the ID of the PasswordEntry to be updated
   * @param updated the PasswordEntry object containing the updated values
   * @return the updated PasswordEntry after saving the changes
   */
  public PasswordEntry update(Long id, PasswordEntry updated) {
    PasswordEntry existing =
        repository
            .findById(id)
            .orElseThrow(() -> new NotFoundException("Password entry not found with id " + id));

    existing.setName(updated.getName());
    existing.setUsername(updated.getUsername());
    existing.setPassword(updated.getPassword());
    existing.setUrl(updated.getUrl());
    existing.setNotes(updated.getNotes());

    return repository.save(existing);
  }

  /**
   * Deletes a PasswordEntry entity with the specified ID. If the PasswordEntry does not exist, a
   * NotFoundException is thrown.
   *
   * @param id the ID of the PasswordEntry to be deleted
   */
  public void delete(Long id) {
    PasswordEntry entry =
        repository
            .findById(id)
            .orElseThrow(() -> new NotFoundException("Password entry not found with id " + id));
    repository.delete(entry);
  }
}
