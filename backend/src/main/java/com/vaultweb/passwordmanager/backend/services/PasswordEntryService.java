package com.vaultweb.passwordmanager.backend.services;

import com.vaultweb.passwordmanager.backend.model.PasswordEntry;
import com.vaultweb.passwordmanager.backend.repositories.PasswordEntryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class PasswordEntryService {

    private final PasswordEntryRepository repository;

    public PasswordEntry create(PasswordEntry entry) {
        return repository.save(entry);
    }

    public List<PasswordEntry> getAll() {
        return repository.findAll();
    }

    public Optional<PasswordEntry> getById(Long id) {
        return repository.findById(id);
    }

    public PasswordEntry update(Long id, PasswordEntry updated) {
        PasswordEntry existing = repository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Password entry not found with id " + id));

        existing.setName(updated.getName());
        existing.setUsername(updated.getUsername());
        existing.setPassword(updated.getPassword());
        existing.setUrl(updated.getUrl());
        existing.setNotes(updated.getNotes());

        return repository.save(existing);
    }

    public void delete(Long id) {
        if (!repository.existsById(id)) {
            throw new IllegalArgumentException("Password entry not found with id " + id);
        }
        repository.deleteById(id);
    }
}

