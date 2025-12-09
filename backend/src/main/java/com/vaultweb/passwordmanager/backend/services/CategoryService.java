package com.vaultweb.passwordmanager.backend.services;

import com.vaultweb.passwordmanager.backend.exceptions.NotFoundException;
import com.vaultweb.passwordmanager.backend.model.Category;
import com.vaultweb.passwordmanager.backend.model.dtos.CategoryDto;
import com.vaultweb.passwordmanager.backend.repositories.CategoryRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CategoryService {

  private final CategoryRepository repository;

  /**
   * Creates and persists a new category for a specified owner using the provided category details.
   *
   * @param dto the data transfer object containing the details of the category to create
   * @param ownerId the ID of the owner associated with the category
   * @return a {@code CategoryDto} representing the newly created category
   */
  public CategoryDto create(CategoryDto dto, Long ownerId) {
    Category category = new Category();
    category.setName(dto.getName());
    category.setColor(dto.getColor());
    category.setDescription(dto.getDescription());
    category.setOwnerId(ownerId);
    return new CategoryDto(repository.save(category));
  }

  /**
   * Retrieves all categories associated with the specified owner.
   *
   * @param ownerId the ID of the owner whose categories are to be retrieved
   * @return a list of {@code CategoryDto} objects representing the categories of the specified
   *     owner
   */
  public List<CategoryDto> getAll(Long ownerId) {
    return repository.findAllByOwnerId(ownerId).stream().map(CategoryDto::new).toList();
  }

  /**
   * Retrieves a category based on its ID and the owner's ID.
   *
   * @param id the unique identifier of the category
   * @param ownerId the ID of the owner associated with the category
   * @return a {@code CategoryDto} representing the data of the retrieved category
   * @throws NotFoundException if no category is found with the specified ID and owner ID
   */
  public CategoryDto get(Long id, Long ownerId) {
    return new CategoryDto(findOwned(id, ownerId));
  }

  /**
   * Updates an existing category with the specified ID and owner ID using the provided category
   * data.
   *
   * @param id the unique identifier of the category to update
   * @param ownerId the ID of the owner associated with the category
   * @param dto the data transfer object containing the updated category details
   * @return a {@code CategoryDto} representing the updated category
   */
  public CategoryDto update(Long id, Long ownerId, CategoryDto dto) {
    Category category = findOwned(id, ownerId);
    category.setName(dto.getName());
    category.setColor(dto.getColor());
    category.setDescription(dto.getDescription());
    return new CategoryDto(repository.save(category));
  }

  /**
   * Deletes a category identified by its unique ID and associated with a specific owner. The
   * category must exist and belong to the specified owner for the deletion to succeed.
   *
   * @param id the unique identifier of the category to delete
   * @param ownerId the ID of the owner associated with the category
   * @throws NotFoundException if no category is found with the specified ID and ownerId
   */
  public void delete(Long id, Long ownerId) {
    Category category = findOwned(id, ownerId);
    repository.delete(category);
  }

  /**
   * Retrieves a {@code Category} entity based on its unique ID and the ID of the associated owner.
   * If no matching category is found, a {@code NotFoundException} is thrown.
   *
   * @param id the unique identifier of the category to look up
   * @param ownerId the ID of the owner associated with the category
   * @return the {@code Category} entity that matches the provided ID and owner ID
   * @throws NotFoundException if no category is found with the specified ID and owner ID
   */
  public Category findOwned(Long id, Long ownerId) {
    return repository
        .findByIdAndOwnerId(id, ownerId)
        .orElseThrow(
            () -> new NotFoundException("Category not found with id " + id + " for current user"));
  }
}
