package com.vaultweb.passwordmanager.backend.model.dtos;

import java.time.Instant;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class VaultUnlockResponseDto {
  private String token;
  private Instant expiresAt;
}
