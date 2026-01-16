package com.vaultweb.passwordmanager.backend.model.dtos;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class VaultSetupRequestDto {

  @NotBlank(message = "Master password is required")
  @Size(min = 8, max = 1024)
  private String masterPassword;
}
