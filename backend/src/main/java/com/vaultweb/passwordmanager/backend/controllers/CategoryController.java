package com.vaultweb.passwordmanager.backend.controllers;

import com.vaultweb.passwordmanager.backend.model.dtos.CategoryDto;
import com.vaultweb.passwordmanager.backend.security.AuthenticatedUser;
import com.vaultweb.passwordmanager.backend.services.CategoryService;
import jakarta.validation.Valid;
import java.net.URI;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/categories")
@RequiredArgsConstructor
public class CategoryController {

  private final CategoryService service;

  @PostMapping
  public ResponseEntity<CategoryDto> create(
      @AuthenticationPrincipal AuthenticatedUser user,
      @Valid @RequestBody CategoryDto dto) {
    CategoryDto created = service.create(dto, user.userId());
    return ResponseEntity.created(URI.create("/api/categories/" + created.getId())).body(created);
  }

  @GetMapping
  public ResponseEntity<List<CategoryDto>> getAll(
      @AuthenticationPrincipal AuthenticatedUser user) {
    return ResponseEntity.ok(service.getAll(user.userId()));
  }

  @GetMapping("/{id}")
  public ResponseEntity<CategoryDto> getById(
      @AuthenticationPrincipal AuthenticatedUser user, @PathVariable Long id) {
    return ResponseEntity.ok(service.get(id, user.userId()));
  }

  @PutMapping("/{id}")
  public ResponseEntity<CategoryDto> update(
      @AuthenticationPrincipal AuthenticatedUser user,
      @PathVariable Long id,
      @Valid @RequestBody CategoryDto dto) {
    return ResponseEntity.ok(service.update(id, user.userId(), dto));
  }

  @DeleteMapping("/{id}")
  public ResponseEntity<Void> delete(
      @AuthenticationPrincipal AuthenticatedUser user, @PathVariable Long id) {
    service.delete(id, user.userId());
    return ResponseEntity.noContent().build();
  }
}
