package com.vaultweb.passwordmanager.backend.model.dtos;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.ToString;

/**
 * Request body for exporting the vault.
 *
 * <p>Either {@code masterPassword} or the {@code X-Vault-Token} header is used to unlock the vault.
 * When {@code encrypted} is {@code true} (the default), {@code exportPassword} protects the export
 * file. A plaintext export must be explicitly confirmed via {@code confirmPlaintext}.
 */
@Data
public class VaultExportRequestDto {

  @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
  @ToString.Exclude
  private String masterPassword;

  @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
  @ToString.Exclude
  private String exportPassword;

  /** Whether the export is encrypted. Defaults to {@code true} when omitted. */
  private Boolean encrypted;

  /** Explicit acknowledgement required to produce an unencrypted (plaintext) export. */
  private Boolean confirmPlaintext;
}
