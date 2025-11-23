package com.vaultweb.passwordmanager.backend.model.dtos;

import lombok.Data;
import lombok.Getter;

/**
 * @author rashmi.soni
 **/
@Data
@Getter
public class PasswordGenerationRequestDto {
  Integer length = 12;
  boolean includeUppercase = true;
  boolean includeNumbers = true;
  boolean includeSpecial = true;
}
