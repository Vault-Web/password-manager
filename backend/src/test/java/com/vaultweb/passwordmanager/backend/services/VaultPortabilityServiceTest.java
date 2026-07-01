package com.vaultweb.passwordmanager.backend.services;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.vaultweb.passwordmanager.backend.exceptions.InvalidCredentialsException;
import com.vaultweb.passwordmanager.backend.model.Category;
import com.vaultweb.passwordmanager.backend.model.PasswordEntry;
import com.vaultweb.passwordmanager.backend.model.dtos.VaultImportResponseDto;
import com.vaultweb.passwordmanager.backend.repositories.CategoryRepository;
import com.vaultweb.passwordmanager.backend.repositories.PasswordEntryRepository;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

class VaultPortabilityServiceTest {

  private static final Long OWNER = 7L;

  private PasswordEntryRepository passwordEntryRepository;
  private CategoryRepository categoryRepository;
  private VaultService vaultService;
  private VaultSessionService vaultSessionService;
  private VaultCryptoService crypto;
  private VaultPortabilityService service;

  @BeforeEach
  void setUp() {
    passwordEntryRepository = mock(PasswordEntryRepository.class);
    categoryRepository = mock(CategoryRepository.class);
    vaultService = mock(VaultService.class);
    vaultSessionService = mock(VaultSessionService.class);
    crypto = new VaultCryptoService(10_000); // real crypto for the export envelope

    service =
        new VaultPortabilityService(
            passwordEntryRepository, categoryRepository, vaultService, vaultSessionService, crypto);

    // Vault not initialized: entry passwords are plaintext, so storage encryption is passthrough.
    when(vaultService.isInitialized(OWNER)).thenReturn(false);
    when(vaultService.encryptPasswordForStorage(eq(OWNER), isNull(), anyString()))
        .thenAnswer(inv -> inv.getArgument(2));
    when(categoryRepository.findAllByOwnerId(OWNER)).thenReturn(new ArrayList<>());
  }

  private PasswordEntry entry(
      String name, String username, String password, String url, String notes, Category category) {
    PasswordEntry e = new PasswordEntry();
    e.setOwnerId(OWNER);
    e.setName(name);
    e.setUsername(username);
    e.setPassword(password);
    e.setUrl(url);
    e.setNotes(notes);
    e.setCategory(category);
    return e;
  }

  @Test
  void encryptedExportThenImportRoundTrips() {
    Category social = new Category();
    social.setId(1L);
    social.setName("Social");
    social.setOwnerId(OWNER);
    List<PasswordEntry> entries =
        List.of(
            entry("GitHub", "gabriel", "s3cr3t", "https://github.com", "main, account", social),
            entry("Email", "gab@x.com", "p@ss\"word", null, "with\nnewline", null));
    when(passwordEntryRepository.findAllByOwnerId(OWNER)).thenReturn(entries);

    VaultPortabilityService.VaultExportResult result =
        service.export(OWNER, null, null, true, "export-pw", false);
    assertTrue(result.encrypted());
    assertEquals(2, result.entryCount());
    assertTrue(crypto.isEncryptedExport(result.content()));

    when(categoryRepository.save(any(Category.class)))
        .thenAnswer(
            inv -> {
              Category c = inv.getArgument(0);
              c.setId(99L);
              return c;
            });
    ArgumentCaptor<PasswordEntry> captor = ArgumentCaptor.forClass(PasswordEntry.class);
    when(passwordEntryRepository.save(captor.capture())).thenAnswer(inv -> inv.getArgument(0));

    VaultImportResponseDto summary =
        service.importVault(OWNER, result.content(), "export-pw", null, null);

    assertEquals(2, summary.getImported());
    List<PasswordEntry> created = captor.getAllValues();
    assertEquals(2, created.size());

    PasswordEntry github =
        created.stream().filter(e -> "GitHub".equals(e.getName())).findFirst().orElseThrow();
    assertEquals("gabriel", github.getUsername());
    assertEquals("s3cr3t", github.getPassword());
    assertEquals("https://github.com", github.getUrl());
    assertEquals("main, account", github.getNotes()); // comma survived the CSV round-trip
    assertEquals("Social", github.getCategory().getName());

    PasswordEntry email =
        created.stream().filter(e -> "Email".equals(e.getName())).findFirst().orElseThrow();
    assertEquals("p@ss\"word", email.getPassword()); // quote survived
    assertEquals("with\nnewline", email.getNotes()); // embedded newline survived
    assertNull(email.getUrl());
  }

  @Test
  void plaintextExportRequiresConfirmation() {
    when(passwordEntryRepository.findAllByOwnerId(OWNER)).thenReturn(List.of());

    assertThrows(
        IllegalArgumentException.class,
        () -> service.export(OWNER, null, null, false, null, false));

    VaultPortabilityService.VaultExportResult confirmed =
        service.export(OWNER, null, null, false, null, true);
    assertFalse(confirmed.encrypted());
    assertFalse(crypto.isEncryptedExport(confirmed.content()));
  }

  @Test
  void encryptedExportRequiresExportPassword() {
    assertThrows(
        IllegalArgumentException.class, () -> service.export(OWNER, null, null, true, "  ", false));
  }

  @Test
  void importWithWrongPasswordFails() {
    when(passwordEntryRepository.findAllByOwnerId(OWNER))
        .thenReturn(List.of(entry("GitHub", "gabriel", "s3cr3t", null, null, null)));
    String blob = service.export(OWNER, null, null, true, "right-pw", false).content();

    assertThrows(
        InvalidCredentialsException.class,
        () -> service.importVault(OWNER, blob, "wrong-pw", null, null));
  }

  @Test
  void importsPlaintextCsvFromAnotherManagerAndCreatesCategory() {
    // KeePass-style header (Title/Group) exercises the column-alias mapping.
    String csv =
        "Title,Username,Password,URL,Notes,Group\r\n"
            + "Bank,john,hunter2,https://bank.example,,Finance\r\n";
    when(categoryRepository.save(any(Category.class)))
        .thenAnswer(
            inv -> {
              Category c = inv.getArgument(0);
              c.setId(5L);
              return c;
            });
    ArgumentCaptor<PasswordEntry> captor = ArgumentCaptor.forClass(PasswordEntry.class);
    when(passwordEntryRepository.save(captor.capture())).thenAnswer(inv -> inv.getArgument(0));

    VaultImportResponseDto summary = service.importVault(OWNER, csv, null, null, null);

    assertEquals(1, summary.getImported());
    assertEquals(1, summary.getCategoriesCreated());
    PasswordEntry imported = captor.getValue();
    assertEquals("Bank", imported.getName());
    assertEquals("hunter2", imported.getPassword());
    assertEquals("Finance", imported.getCategory().getName());
  }

  @Test
  void importSkipsIncompleteRows() {
    ArgumentCaptor<PasswordEntry> captor = ArgumentCaptor.forClass(PasswordEntry.class);
    when(passwordEntryRepository.save(captor.capture())).thenAnswer(inv -> inv.getArgument(0));

    String csv = "name,username,password\r\nGitHub,gabriel,\r\nReal,user,pw\r\n";
    VaultImportResponseDto summary = service.importVault(OWNER, csv, null, null, null);

    assertEquals(1, summary.getImported());
    assertEquals(1, summary.getSkipped());
    assertEquals("Real", captor.getValue().getName());
  }

  @Test
  void importRejectsUnrecognizedHeader() {
    assertThrows(
        IllegalArgumentException.class,
        () -> service.importVault(OWNER, "foo,bar\r\n1,2\r\n", null, null, null));
  }
}
