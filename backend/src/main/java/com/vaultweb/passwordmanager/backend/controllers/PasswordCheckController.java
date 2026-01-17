package com.vaultweb.passwordmanager.backend.controllers;

import com.vaultweb.passwordmanager.backend.model.dtos.PasswordCheckRequestDto;
import com.vaultweb.passwordmanager.backend.model.dtos.PasswordCheckResponseDto;
import com.vaultweb.passwordmanager.backend.services.BreachedPasswordService;
import com.vaultweb.passwordmanager.backend.services.PasswordStrengthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

/**
 * @author rashmi.soni
 */
@RestController
@RequestMapping("/api/passwords")
@Tag(
    name = "Password Checker",
    description = "APIs related to password strength and breach checking.")
public class PasswordCheckController {

  private final PasswordStrengthService strengthService;
  private final BreachedPasswordService breachedService;

  public PasswordCheckController(
      PasswordStrengthService strengthService, BreachedPasswordService breachedService) {
    this.strengthService = strengthService;
    this.breachedService = breachedService;
  }

  @Operation(
      summary = "Check password strength and breach status",
      description =
          "Evaluates whether a password has been breached and calculates its strength score/rating.",
      responses = {
        @ApiResponse(
            responseCode = "200",
            description = "Password evaluation result",
            content =
                @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = PasswordCheckResponseDto.class)))
      })
  @PostMapping("/check")
  public PasswordCheckResponseDto check(@Valid @RequestBody PasswordCheckRequestDto request) {
    String password = request.getPassword();

    int breachCount = breachedService.checkIfBreached(password);
    boolean breached = breachCount > 0;

    int score = strengthService.calculateStrength(password);
    String rating = strengthService.rating(score);

    return new PasswordCheckResponseDto(breached, breachCount, score, rating);
  }
}
