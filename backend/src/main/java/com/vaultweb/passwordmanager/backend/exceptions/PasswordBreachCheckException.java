package com.vaultweb.passwordmanager.backend.exceptions;

public class PasswordBreachCheckException extends RuntimeException {
  public PasswordBreachCheckException(String message) {
    super(message);
  }
}
