package com.vaultweb.passwordmanager.backend.controllers;

import com.vaultweb.passwordmanager.backend.model.dtos.PasswordGenerationRequestDto;
import com.vaultweb.passwordmanager.backend.model.dtos.PasswordGenerationResponseDto;
import com.vaultweb.passwordmanager.backend.services.PasswordGenerationService;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * @author rashmi.soni
 */
@RestController
@RequestMapping("/api/passwords")
public class PasswordGenerationController {
  private final PasswordGenerationService generator;

  public PasswordGenerationController(PasswordGenerationService generator) {
    this.generator = generator;
  }

  @PostMapping(
      value = "/generate",
      consumes = MediaType.APPLICATION_JSON_VALUE,
      produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<PasswordGenerationResponseDto> generate(
      @RequestBody PasswordGenerationRequestDto req) {
    String pwd =
        generator.generate(
            req.getLength(),
            req.isIncludeUppercase(),
            req.isIncludeNumbers(),
            req.isIncludeSpecial());
    return ResponseEntity.ok(new PasswordGenerationResponseDto(pwd));
  }
}
