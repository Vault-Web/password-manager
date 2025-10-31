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
        PasswordEntry created = service.create(new PasswordEntry(dto));
        return ResponseEntity
                .created(URI.create("/api/passwords/" + created.getId()))
                .body(new PasswordEntryDto(created));
    }

    @GetMapping
    public ResponseEntity<List<PasswordEntryDto>> getAll() {
        List<PasswordEntryDto> dtos = service.getAll().stream()
                .map(PasswordEntryDto::new)
                .toList();
        return ResponseEntity.ok(dtos);
    }

    @GetMapping("/{id}")
    public ResponseEntity<PasswordEntryDto> getById(@PathVariable Long id) {
        return service.getById(id)
                .map(entry -> ResponseEntity.ok(new PasswordEntryDto(entry)))
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}")
    public ResponseEntity<PasswordEntryDto> update(@PathVariable Long id, @Valid @RequestBody PasswordEntryDto dto) {
        PasswordEntry updated = service.update(id, new PasswordEntry(dto));
        return ResponseEntity.ok(new PasswordEntryDto(updated));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}
