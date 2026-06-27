package com.vaultweb.passwordmanager.backend.model.dtos;

import com.vaultweb.passwordmanager.backend.model.PasswordEntry;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.ToString;

@Data
@AllArgsConstructor
public class PasswordRevealResponseDto {

  @Schema(description = "Database identifier of the password entry")
  private Long id;

  @Schema(description = "Display name of the entry (service/site)")
  private String name;

  @Schema(description = "Revealed plaintext password")
  @ToString.Exclude
  private String password;

  public static PasswordRevealResponseDto fromEntry(PasswordEntry entry) {
    return new PasswordRevealResponseDto(entry.getId(), entry.getName(), entry.getPassword());
  }
}
