package com.vaultweb.passwordmanager.backend.model.dtos;

import com.vaultweb.passwordmanager.backend.model.Category;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CategoryDto {

  private Long id;

  @NotBlank(message = "Category name is required")
  @Size(max = 60)
  private String name;

  @Pattern(regexp = "^#[0-9a-fA-F]{6}$", message = "Color must be a hex value like #AABBCC")
  private String color;

  @Size(max = 200)
  private String description;

  public CategoryDto(Category category) {
    this.id = category.getId();
    this.name = category.getName();
    this.color = category.getColor();
    this.description = category.getDescription();
  }
}
