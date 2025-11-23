package com.vaultweb.passwordmanager.backend.model.dtos;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * @author rashmi.soni
 */
@Data
public class PasswordGenerationRequestDto {
  @Schema(
      description = "Desired length of the generated password",
      example = "12",
      minimum = "6",
      maximum = "64")
  @NotNull(message = "Password length cannot be null")
  @Min(value = 6, message = "Password length must be at least 6")
  @Max(value = 128, message = "Password length must not exceed 64")
  Integer length = 12;

  @Schema(description = "Include uppercase letters (A-Z)", example = "true")
  boolean includeUppercase = true;

  @Schema(description = "Include numeric digits (0-9)", example = "true")
  boolean includeNumbers = true;

  @Schema(description = "Include special characters (!@#$%)", example = "true")
  boolean includeSpecial = true;
}
