package com.vaultweb.passwordmanager.backend.model.dtos;

import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class VaultMigrateRequestDto {

  @Size(min = 8, max = 128)
  private String masterPassword;
}
