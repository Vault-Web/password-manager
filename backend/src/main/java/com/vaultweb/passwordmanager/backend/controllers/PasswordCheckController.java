package com.vaultweb.passwordmanager.backend.controllers;

import com.vaultweb.passwordmanager.backend.model.dtos.PasswordCheckRequestDto;
import com.vaultweb.passwordmanager.backend.model.dtos.PasswordCheckResponseDto;
import com.vaultweb.passwordmanager.backend.services.BreachedPasswordService;
import com.vaultweb.passwordmanager.backend.services.PasswordStrengthService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/password")
public class PasswordCheckController {

  private final PasswordStrengthService strengthService;
  private final BreachedPasswordService breachedService;

  public PasswordCheckController(
      PasswordStrengthService strengthService, BreachedPasswordService breachedService) {
    this.strengthService = strengthService;
    this.breachedService = breachedService;
  }

  @PostMapping("/check")
  public PasswordCheckResponseDto check(@RequestBody PasswordCheckRequestDto request) {
    String password = request.getPassword();

    int breachCount = breachedService.checkIfBreached(password);
    boolean breached = breachCount > 0;

    int score = strengthService.calculateStrength(password);
    String rating = strengthService.rating(score);

    return new PasswordCheckResponseDto(breached, breachCount, score, rating);
  }
}
