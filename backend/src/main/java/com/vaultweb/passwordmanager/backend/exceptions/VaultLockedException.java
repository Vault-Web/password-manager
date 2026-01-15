package com.vaultweb.passwordmanager.backend.exceptions;

public class VaultLockedException extends RuntimeException {
  public VaultLockedException(String message) {
    super(message);
  }
}
