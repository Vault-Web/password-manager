package com.vaultweb.passwordmanager.backend.model.dtos;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;

/**
 * @author rashmi.soni
 */
@Getter
@Schema(
    name = "PasswordCheckResponse",
    description = "Response containing breach details and strength evaluation for a password.")
public class PasswordCheckResponseDto {
  @Schema(
      description = "Indicates whether the password is found in breach databases.",
      example = "false")
  private final boolean breached;

  @Schema(description = "Number of times the password appeared in breach records.", example = "0")
  private final int breachCount;

  @Schema(
      description = "Password strength score (0–4 or 0–100 depending on your logic).",
      example = "4")
  private final int strengthScore;

  @Schema(description = "Human-friendly strength rating based on score.", example = "STRONG")
  private final String strengthRating;

  public PasswordCheckResponseDto(
      boolean breached, int breachCount, int strengthScore, String strengthRating) {
    this.breached = breached;
    this.breachCount = breachCount;
    this.strengthScore = strengthScore;
    this.strengthRating = strengthRating;
  }
}
