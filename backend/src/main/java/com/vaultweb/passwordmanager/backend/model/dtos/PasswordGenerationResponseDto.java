package com.vaultweb.passwordmanager.backend.model.dtos;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * @author rashmi.soni
 */
@Data
@AllArgsConstructor
public class PasswordGenerationResponseDto {
  @Schema(description = "Generated strong password", example = "Aa1@bC3$dE")
  private String password;
}
