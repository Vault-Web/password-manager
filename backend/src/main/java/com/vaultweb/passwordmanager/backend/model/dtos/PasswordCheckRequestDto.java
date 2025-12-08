package com.vaultweb.passwordmanager.backend.model.dtos;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;

/**
 * @author rashmi.soni
 */
@Getter
@Schema(
    name = "PasswordCheckRequest",
    description = "Request object containing the password to be validated.")
public class PasswordCheckRequestDto {
  @Schema(
      description = "Password that needs to be checked for strength and breach status.",
      example = "MyStr0ng@Pass123",
      required = true)
  @NotBlank(message = "Password cannot be blank")
  @Size(min = 6, max = 128, message = "Password must be between 6 and 128 characters")
  private String password;
}
