package com.vaultweb.passwordmanager.backend.exceptions;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;

@Slf4j
@ControllerAdvice
public class GlobalExceptionHandler {

  private ResponseEntity<Map<String, Object>> buildErrorResponse(
      String message, HttpStatus status, String errorCode) {

    Map<String, Object> response = new LinkedHashMap<>();
    response.put("timestamp", LocalDateTime.now());
    response.put("status", status.value());
    response.put("error", message);
    response.put("errorCode", errorCode);

    return ResponseEntity.status(status).body(response);
  }

  @ExceptionHandler(InvalidCredentialsException.class)
  public ResponseEntity<Map<String, Object>> handleInvalidCredentials(
      InvalidCredentialsException ex, WebRequest request) {
    log.warn("Invalid credentials - Path: {}", request.getDescription(false));
    return buildErrorResponse(ex.getMessage(), HttpStatus.UNAUTHORIZED, "INVALID_CREDENTIALS");
  }

  @ExceptionHandler(ConflictException.class)
  public ResponseEntity<Map<String, Object>> handleConflict(
      ConflictException ex, WebRequest request) {
    log.warn("Conflict - Path: {}", request.getDescription(false));
    return buildErrorResponse(ex.getMessage(), HttpStatus.CONFLICT, "CONFLICT");
  }

  @ExceptionHandler(VaultNotInitializedException.class)
  public ResponseEntity<Map<String, Object>> handleVaultNotInitialized(
      VaultNotInitializedException ex, WebRequest request) {
    log.warn("Vault not initialized - Path: {}", request.getDescription(false));
    return buildErrorResponse(ex.getMessage(), HttpStatus.CONFLICT, "VAULT_NOT_INITIALIZED");
  }

  @ExceptionHandler(VaultLockedException.class)
  public ResponseEntity<Map<String, Object>> handleVaultLocked(
      VaultLockedException ex, WebRequest request) {
    log.warn("Vault locked - Path: {}", request.getDescription(false));
    return buildErrorResponse(ex.getMessage(), HttpStatus.PRECONDITION_REQUIRED, "VAULT_LOCKED");
  }

  @ExceptionHandler(NotFoundException.class)
  public ResponseEntity<Map<String, Object>> handleNotFound(
      NotFoundException ex, WebRequest request) {
    log.info("Resource not found: {} - Path: {}", ex.getMessage(), request.getDescription(false));
    return buildErrorResponse(ex.getMessage(), HttpStatus.NOT_FOUND, "NOT_FOUND");
  }

  @ExceptionHandler(PasswordBreachCheckException.class)
  public ResponseEntity<Map<String, Object>> handlePasswordBreachCheckError(
      PasswordBreachCheckException ex, WebRequest request) {
    log.error(
        "Password Breach check failed: {} - Path: {}",
        ex.getMessage(),
        request.getDescription(false));
    return buildErrorResponse(
        "Unable to verify password security at this time. Please try again later",
        HttpStatus.SERVICE_UNAVAILABLE,
        "BREACH_CHECK_UNAVAILABLE");
  }

  @ExceptionHandler(UnauthorizedException.class)
  public ResponseEntity<Map<String, Object>> handleUnauthorized(
      UnauthorizedException ex, WebRequest request) {
    log.warn("Unauthorized access - Path: {}", request.getDescription(false));
    return buildErrorResponse(ex.getMessage(), HttpStatus.UNAUTHORIZED, "UNAUTHORIZED");
  }

  @ExceptionHandler(IllegalArgumentException.class)
  public ResponseEntity<Map<String, Object>> handleIllegalArgument(
      IllegalArgumentException ex, WebRequest request) {
    log.warn("Invalid argument - Path: {}", request.getDescription(false));
    return buildErrorResponse(
        "Invalid request parameters", HttpStatus.BAD_REQUEST, "INVALID_ARGUMENT");
  }

  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ResponseEntity<Map<String, Object>> handleValidationErrors(
      MethodArgumentNotValidException ex, WebRequest request) {

    String errorMessage =
        ex.getBindingResult().getFieldErrors().stream()
            .findFirst()
            .map(DefaultMessageSourceResolvable::getDefaultMessage)
            .orElse("Invalid request");
    log.warn("Validation error: {} - Path: {}", errorMessage, request.getDescription(false));
    return buildErrorResponse(errorMessage, HttpStatus.BAD_REQUEST, "VALIDATION_ERROR");
  }

  // Catch-all for unexpected errors
  @ExceptionHandler(Exception.class)
  public ResponseEntity<Map<String, Object>> handleGeneralErrors(Exception ex, WebRequest request) {
    log.error(
        "Unexpected error - Path: {} - Exception: {}",
        request.getDescription(false),
        ex.getClass().getSimpleName(),
        ex);
    return buildErrorResponse(
        "Internal server error", HttpStatus.INTERNAL_SERVER_ERROR, "INTERNAL_ERROR");
  }
}
