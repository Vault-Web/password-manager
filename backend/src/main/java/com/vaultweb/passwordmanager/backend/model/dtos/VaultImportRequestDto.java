package com.vaultweb.passwordmanager.backend.model.dtos;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.ToString;

/**
 * Request body for importing into the vault.
 *
 * <p>{@code data} is either an encrypted vault export or a plaintext CSV; the format is
 * auto-detected. {@code importPassword} is required only when {@code data} is an encrypted export.
 * Either {@code masterPassword} or the {@code X-Vault-Token} header unlocks the vault so imported
 * passwords can be encrypted at rest.
 */
@Data
public class VaultImportRequestDto {

  @NotBlank(message = "Import data is required")
  @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
  @ToString.Exclude
  private String data;

  @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
  @ToString.Exclude
  private String importPassword;

  @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
  @ToString.Exclude
  private String masterPassword;
}
