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

  /**
   * Creates a new category for the authenticated user based on the provided details.
   *
   * @param user the authenticated user performing the operation
   * @param dto the data transfer object containing the details of the category to create
   * @return a {@code ResponseEntity} containing the created {@code CategoryDto} along with the
   *     location URI of the newly created category resource
   */
  @PostMapping
  public ResponseEntity<CategoryDto> create(
      @AuthenticationPrincipal AuthenticatedUser user, @Valid @RequestBody CategoryDto dto) {
    CategoryDto created = service.create(dto, user.userId());
    return ResponseEntity.created(URI.create("/api/categories/" + created.getId())).body(created);
  }

  /**
   * Retrieves all categories associated with the authenticated user.
   *
   * @param user the authenticated user whose categories are being retrieved
   * @return a {@code ResponseEntity} containing a list of {@code CategoryDto} objects representing
   *     the user's categories
   */
  @GetMapping
  public ResponseEntity<List<CategoryDto>> getAll(@AuthenticationPrincipal AuthenticatedUser user) {
    return ResponseEntity.ok(service.getAll(user.userId()));
  }

  /**
   * Retrieves a specific category based on its ID for the authenticated user.
   *
   * @param user the authenticated user requesting the category
   * @param id the unique identifier of the category to retrieve
   * @return a {@code ResponseEntity} containing a {@code CategoryDto} that represents the specified
   *     category
   */
  @GetMapping("/{id}")
  public ResponseEntity<CategoryDto> getById(
      @AuthenticationPrincipal AuthenticatedUser user, @PathVariable Long id) {
    return ResponseEntity.ok(service.get(id, user.userId()));
  }

  /**
   * Updates an existing category for the authenticated user.
   *
   * @param user the authenticated user performing the update operation
   * @param id the unique identifier of the category to update
   * @param dto the data transfer object containing the updated category information
   * @return a {@code ResponseEntity} containing the updated {@code CategoryDto}
   */
  @PutMapping("/{id}")
  public ResponseEntity<CategoryDto> update(
      @AuthenticationPrincipal AuthenticatedUser user,
      @PathVariable Long id,
      @Valid @RequestBody CategoryDto dto) {
    return ResponseEntity.ok(service.update(id, user.userId(), dto));
  }

  /**
   * Deletes a category identified by its unique ID for the authenticated user.
   *
   * @param user the authenticated user performing the delete operation
   * @param id the unique identifier of the category to delete
   * @return a {@code ResponseEntity} with no content upon successful deletion
   */
  @DeleteMapping("/{id}")
  public ResponseEntity<Void> delete(
      @AuthenticationPrincipal AuthenticatedUser user, @PathVariable Long id) {
    service.delete(id, user.userId());
    return ResponseEntity.noContent().build();
  }
}
