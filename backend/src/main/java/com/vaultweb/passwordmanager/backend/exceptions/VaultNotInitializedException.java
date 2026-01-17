package com.vaultweb.passwordmanager.backend.exceptions;

public class VaultNotInitializedException extends RuntimeException {
  public VaultNotInitializedException(String message) {
    super(message);
  }
}
