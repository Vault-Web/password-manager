package com.vaultweb.passwordmanager.backend.security;

/** Lightweight principal representation for authenticated Vault Web users. */
public record AuthenticatedUser(Long userId, String username) {}
