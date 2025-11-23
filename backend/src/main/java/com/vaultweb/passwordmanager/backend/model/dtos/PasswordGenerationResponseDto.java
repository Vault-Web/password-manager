package com.vaultweb.passwordmanager.backend.model.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * @author rashmi.soni
 */
@Data
@AllArgsConstructor
public class PasswordGenerationResponseDto {
  private String password;
}
