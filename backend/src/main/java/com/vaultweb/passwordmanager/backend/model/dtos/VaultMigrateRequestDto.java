package com.vaultweb.passwordmanager.backend.model.dtos;

import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.ToString;

@Data
public class VaultMigrateRequestDto {

  @Size(min = 8, max = 1024)
  @ToString.Exclude
  private String masterPassword;
}
