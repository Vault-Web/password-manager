package com.vaultweb.passwordmanager.backend.repositories;

import com.vaultweb.passwordmanager.backend.model.PasswordEntry;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PasswordEntryRepository extends JpaRepository<PasswordEntry, Long> {}
