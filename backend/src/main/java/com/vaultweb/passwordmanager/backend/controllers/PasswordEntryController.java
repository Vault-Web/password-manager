package com.vaultweb.passwordmanager.backend.controllers;

import com.vaultweb.passwordmanager.backend.model.PasswordEntry;
import com.vaultweb.passwordmanager.backend.model.dtos.PasswordEntryDto;
import com.vaultweb.passwordmanager.backend.services.PasswordEntryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/passwords")
@RequiredArgsConstructor
public class PasswordEntryController {

    private final PasswordEntryService service;

    @PostMapping
    public ResponseEntity<PasswordEntryDto> create(@Valid @RequestBody PasswordEntryDto dto) {
        PasswordEntry created = service.create(fromDto(dto));
        return ResponseEntity
                .created(URI.create("/api/passwords/" + created.getId()))
                .body(toDto(created));
    }

    @GetMapping
    public ResponseEntity<List<PasswordEntryDto>> getAll() {
        List<PasswordEntryDto> dtos = service.getAll().stream()
                .map(this::toDto)
                .toList();
        return ResponseEntity.ok(dtos);
    }

    @GetMapping("/{id}")
    public ResponseEntity<PasswordEntryDto> getById(@PathVariable Long id) {
        return service.getById(id)
                .map(entry -> ResponseEntity.ok(toDto(entry)))
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}")
    public ResponseEntity<PasswordEntryDto> update(@PathVariable Long id, @Valid @RequestBody PasswordEntryDto dto) {
        PasswordEntry updated = service.update(id, fromDto(dto));
        return ResponseEntity.ok(toDto(updated));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }

    private PasswordEntryDto toDto(PasswordEntry entry) {
        return new PasswordEntryDto(
                entry.getId(),
                entry.getName(),
                entry.getUsername(),
                entry.getPassword(),
                entry.getUrl(),
                entry.getNotes()
        );
    }

    private PasswordEntry fromDto(PasswordEntryDto dto) {
        PasswordEntry entry = new PasswordEntry();
        entry.setName(dto.getName());
        entry.setUsername(dto.getUsername());
        entry.setPassword(dto.getPassword());
        entry.setUrl(dto.getUrl());
        entry.setNotes(dto.getNotes());
        return entry;
    }
}
