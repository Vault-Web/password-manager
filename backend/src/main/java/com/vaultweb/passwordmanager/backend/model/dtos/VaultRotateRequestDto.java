package com.vaultweb.passwordmanager.backend.model.dtos;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class VaultRotateRequestDto {

  @NotBlank(message = "Current master password is required")
  @Size(min = 8, max = 128)
  private String currentMasterPassword;

  @NotBlank(message = "New master password is required")
  @Size(min = 8, max = 128)
  private String newMasterPassword;
}
