package com.vaultweb.passwordmanager.backend.model.dtos;

import java.time.Instant;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.ToString;

@Data
@AllArgsConstructor
public class VaultUnlockResponseDto {
  @ToString.Exclude private String token;
  private Instant expiresAt;
}
