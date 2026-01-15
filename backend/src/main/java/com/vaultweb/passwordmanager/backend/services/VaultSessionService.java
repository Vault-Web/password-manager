package com.vaultweb.passwordmanager.backend.services;

import com.vaultweb.passwordmanager.backend.exceptions.VaultLockedException;
import java.security.SecureRandom;
import java.time.Clock;
import java.time.Instant;
import java.util.Base64;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class VaultSessionService {

  private static final int TOKEN_BYTES = 32;

  private final SecureRandom secureRandom = new SecureRandom();
  private final Map<String, VaultSession> sessions = new ConcurrentHashMap<>();
  private final Clock clock;

  private final VaultService vaultService;
  private final long ttlSeconds;

  @Autowired
  public VaultSessionService(
      VaultService vaultService, @Value("${vault.session.ttlSeconds:900}") long ttlSeconds) {
    this(vaultService, ttlSeconds, Clock.systemUTC());
  }

  // Visible for testing
  VaultSessionService(VaultService vaultService, long ttlSeconds, Clock clock) {
    this.vaultService = vaultService;
    this.ttlSeconds = ttlSeconds;
    this.clock = clock;
  }

  /**
   * Unlocks the vault for the given user by verifying the master password and creating a new
   * session.
   *
   * @param ownerId
   * @param masterPassword
   * @return the vault unlock information containing the session token and expiration time
   */
  public VaultUnlock unlock(Long ownerId, String masterPassword) {
    if (!vaultService.isInitialized(ownerId)) {
      throw new VaultLockedException("Vault is not initialized");
    }

    byte[] dek = vaultService.unwrapDekForSession(ownerId, masterPassword);

    String token = generateToken();
    Instant expiresAt = clock.instant().plusSeconds(ttlSeconds);

    sessions.put(token, new VaultSession(ownerId, dek, expiresAt));
    return new VaultUnlock(token, expiresAt);
  }

  /**
   * Requires a valid DEK from an active vault session.
   *
   * @param ownerId
   * @param token
   * @return the DEK bytes
   */
  public byte[] requireDek(Long ownerId, String token) {
    if (token == null || token.isBlank()) {
      throw new VaultLockedException("Vault token required");
    }

    VaultSession session = sessions.get(token);
    if (session == null) {
      throw new VaultLockedException("Invalid or expired vault token");
    }

    if (!ownerId.equals(session.ownerId())) {
      throw new VaultLockedException("Invalid vault token for current user");
    }

    if (session.expiresAt().isBefore(clock.instant())) {
      sessions.remove(token);
      throw new VaultLockedException("Invalid or expired vault token");
    }

    return session.dek();
  }

  /**
   * Locks the vault session associated with the given token.
   *
   * @param ownerId
   * @param token
   */
  public void lock(Long ownerId, String token) {
    if (token == null || token.isBlank()) {
      return;
    }

    VaultSession session = sessions.get(token);
    if (session != null && ownerId.equals(session.ownerId())) {
      sessions.remove(token);
    }
  }

  /**
   * Generates a secure random token.
   *
   * @return the generated token string
   */
  private String generateToken() {
    byte[] bytes = new byte[TOKEN_BYTES];
    secureRandom.nextBytes(bytes);
    return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
  }

  public record VaultUnlock(String token, Instant expiresAt) {}

  private record VaultSession(Long ownerId, byte[] dek, Instant expiresAt) {}
}
