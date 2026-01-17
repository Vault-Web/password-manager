package com.vaultweb.passwordmanager.backend.model.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class VaultVerifyResponseDto {
  private boolean valid;
}
