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

  public CategoryDto create(CategoryDto dto, Long ownerId) {
    Category category = new Category();
    category.setName(dto.getName());
    category.setColor(dto.getColor());
    category.setDescription(dto.getDescription());
    category.setOwnerId(ownerId);
    return new CategoryDto(repository.save(category));
  }

  public List<CategoryDto> getAll(Long ownerId) {
    return repository.findAllByOwnerId(ownerId).stream().map(CategoryDto::new).toList();
  }

  public CategoryDto get(Long id, Long ownerId) {
    return new CategoryDto(findOwned(id, ownerId));
  }

  public CategoryDto update(Long id, Long ownerId, CategoryDto dto) {
    Category category = findOwned(id, ownerId);
    category.setName(dto.getName());
    category.setColor(dto.getColor());
    category.setDescription(dto.getDescription());
    return new CategoryDto(repository.save(category));
  }

  public void delete(Long id, Long ownerId) {
    Category category = findOwned(id, ownerId);
    repository.delete(category);
  }

  public Category findOwned(Long id, Long ownerId) {
    return repository
        .findByIdAndOwnerId(id, ownerId)
        .orElseThrow(
            () -> new NotFoundException("Category not found with id " + id + " for current user"));
  }
}
