package com.vaultweb.passwordmanager.backend.controllers;

import com.vaultweb.passwordmanager.backend.model.PasswordEntry;
import com.vaultweb.passwordmanager.backend.services.PasswordEntryService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/passwords")
public class PasswordEntryController {

    private final PasswordEntryService service;

    public PasswordEntryController(PasswordEntryService service) {
        this.service = service;
    }

    @PostMapping
    public ResponseEntity<PasswordEntry> create(@RequestBody PasswordEntry entry) {
        return ResponseEntity.ok(service.create(entry));
    }

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
    public ResponseEntity<PasswordEntry> update(@PathVariable Long id, @RequestBody PasswordEntry updated) {
        return ResponseEntity.ok(service.update(id, updated));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}
