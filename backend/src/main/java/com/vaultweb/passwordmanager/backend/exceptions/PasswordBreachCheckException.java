package com.vaultweb.passwordmanager.backend.exceptions;

/**
 * @author rashmi.soni
 */
public class PasswordBreachCheckException extends RuntimeException {
  public PasswordBreachCheckException(String message) {
    super(message);
  }
}
