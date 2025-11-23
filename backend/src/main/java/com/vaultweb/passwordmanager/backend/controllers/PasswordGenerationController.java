package com.vaultweb.passwordmanager.backend.controllers;

import com.vaultweb.passwordmanager.backend.model.dtos.PasswordGenerationRequestDto;
import com.vaultweb.passwordmanager.backend.model.dtos.PasswordGenerationResponseDto;
import com.vaultweb.passwordmanager.backend.services.PasswordGenerationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
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

  @Operation(
      summary = "Generate a secure password",
      description =
          "Creates a random password based on selected character rules such as length, uppercase, numbers, and special characters.")
  @ApiResponses(
      value = {
        @ApiResponse(responseCode = "200", description = "Password generated successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid input request")
      })
  @PostMapping(
      value = "/generate",
      consumes = MediaType.APPLICATION_JSON_VALUE,
      produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<PasswordGenerationResponseDto> generate(
      @io.swagger.v3.oas.annotations.parameters.RequestBody(
              description = "Password generation rules",
              required = true,
              content =
                  @Content(schema = @Schema(implementation = PasswordGenerationRequestDto.class)))
          @Valid
          @RequestBody
          PasswordGenerationRequestDto req) {
    String pwd =
        generator.generate(
            req.getLength(),
            req.isIncludeUppercase(),
            req.isIncludeNumbers(),
            req.isIncludeSpecial());
    return ResponseEntity.ok(new PasswordGenerationResponseDto(pwd));
  }
}
