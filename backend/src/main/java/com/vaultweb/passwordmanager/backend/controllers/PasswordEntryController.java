package com.vaultweb.passwordmanager.backend.controllers;

import com.vaultweb.passwordmanager.backend.model.PasswordEntry;
import com.vaultweb.passwordmanager.backend.services.PasswordEntryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/api/passwords")
@RequiredArgsConstructor
public class PasswordEntryController {

    private final PasswordEntryService service;

    @PostMapping
    public ResponseEntity<PasswordEntry> create(@Valid @RequestBody PasswordEntry entry) {
        PasswordEntry created = service.create(entry);
        return ResponseEntity.created(URI.create("/api/passwords/" + created.getId())).body(created);

    @GetMapping
    public ResponseEntity<List<PasswordEntry>> getAll() {
        return ResponseEntity.ok(service.getAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<PasswordEntry> getById(@PathVariable Long id) {
        return service.getById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}")
    public ResponseEntity<PasswordEntry> update(@PathVariable Long id, @Valid @RequestBody PasswordEntry updated) {
        return ResponseEntity.ok(service.update(id, updated));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}
