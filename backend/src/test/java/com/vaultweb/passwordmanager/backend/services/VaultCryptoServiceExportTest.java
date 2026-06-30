package com.vaultweb.passwordmanager.backend.services;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.vaultweb.passwordmanager.backend.exceptions.InvalidCredentialsException;
import java.nio.charset.StandardCharsets;
import org.junit.jupiter.api.Test;

class VaultCryptoServiceExportTest {

  // Reduced iterations keep the test fast; the algorithm is identical to production.
  private final VaultCryptoService crypto = new VaultCryptoService(10_000);

  @Test
  void encryptThenDecryptRoundTrips() {
    byte[] plaintext =
        "name,username,password\r\nGitHub,gabriel,s3cr3t\r\n".getBytes(StandardCharsets.UTF_8);
    String envelope = crypto.encryptWithPassword(plaintext, "export-pw");

    assertTrue(crypto.isEncryptedExport(envelope));
    assertArrayEquals(plaintext, crypto.decryptWithPassword(envelope, "export-pw"));
  }

  @Test
  void wrongPasswordIsRejected() {
    String envelope = crypto.encryptWithPassword("data".getBytes(StandardCharsets.UTF_8), "right");
    assertThrows(
        InvalidCredentialsException.class, () -> crypto.decryptWithPassword(envelope, "wrong"));
  }

  @Test
  void plaintextCsvIsNotDetectedAsEncrypted() {
    assertFalse(crypto.isEncryptedExport("name,username,password\r\nGitHub,gabriel,pw"));
  }

  @Test
  void decryptingNonEnvelopeThrows() {
    assertThrows(
        IllegalArgumentException.class, () -> crypto.decryptWithPassword("not-an-envelope", "pw"));
  }

  @Test
  void decryptingTruncatedEnvelopeThrows() {
    assertThrows(
        IllegalArgumentException.class,
        () -> crypto.decryptWithPassword(VaultCryptoService.EXPORT_PREFIX + "AAAA", "pw"));
  }
}
