package com.vaultweb.passwordmanager.backend.model.dtos;

import lombok.Getter;

@Getter
public class PasswordCheckResponseDto {
  private final boolean breached;
  private final int breachCount;
  private final int strengthScore;
  private final String strengthRating;

  public PasswordCheckResponseDto(
      boolean breached, int breachCount, int strengthScore, String strengthRating) {
    this.breached = breached;
    this.breachCount = breachCount;
    this.strengthScore = strengthScore;
    this.strengthRating = strengthRating;
  }
}
