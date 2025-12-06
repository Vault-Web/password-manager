package com.vaultweb.passwordmanager.backend.repositories;

import com.vaultweb.passwordmanager.backend.model.PasswordEntry;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PasswordEntryRepository extends JpaRepository<PasswordEntry, Long> {

	List<PasswordEntry> findAllByOwnerId(Long ownerId);

	Optional<PasswordEntry> findByIdAndOwnerId(Long id, Long ownerId);
}
