package com.vaultweb.passwordmanager.backend.model.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Summary of a vault import: how many entries were created, categories created, and rows skipped.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class VaultImportResponseDto {

  private int imported;
  private int categoriesCreated;
  private int skipped;
}
