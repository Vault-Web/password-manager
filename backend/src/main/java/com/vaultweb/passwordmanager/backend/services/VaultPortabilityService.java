package com.vaultweb.passwordmanager.backend.services;

import com.vaultweb.passwordmanager.backend.exceptions.VaultLockedException;
import com.vaultweb.passwordmanager.backend.model.Category;
import com.vaultweb.passwordmanager.backend.model.PasswordEntry;
import com.vaultweb.passwordmanager.backend.model.dtos.VaultImportResponseDto;
import com.vaultweb.passwordmanager.backend.repositories.CategoryRepository;
import com.vaultweb.passwordmanager.backend.repositories.PasswordEntryRepository;
import com.vaultweb.passwordmanager.backend.support.CsvSupport;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Exports and imports the vault as CSV, addressing the lock-in concern in issue #81.
 *
 * <p>Export is encrypted by default (password-protected envelope); a plaintext CSV requires
 * explicit confirmation. Import auto-detects an encrypted envelope vs. a plaintext CSV, maps common
 * column headers from other managers (Bitwarden, KeePass) so entries can be brought in without
 * manual editing, and recreates categories by name. Passwords are always stored using the same
 * vault encryption path as normal entry creation.
 */
@Service
@RequiredArgsConstructor
public class VaultPortabilityService {

  private static final String[] CSV_HEADER = {
    "name", "username", "password", "url", "notes", "category"
  };

  private static final Map<String, String> COLUMN_ALIASES = buildColumnAliases();

  private static final char BYTE_ORDER_MARK = (char) 0xFEFF;

  private final PasswordEntryRepository passwordEntryRepository;
  private final CategoryRepository categoryRepository;
  private final VaultService vaultService;
  private final VaultSessionService vaultSessionService;
  private final VaultCryptoService crypto;

  /** Result of an export: the file content, whether it is encrypted, and the entry count. */
  public record VaultExportResult(String content, boolean encrypted, int entryCount) {}

  /**
   * Exports all of the owner's entries as CSV, optionally encrypted under an export password.
   *
   * @param ownerId the vault owner
   * @param masterPassword master password (used when no vault token is supplied)
   * @param vaultToken active vault session token, or null
   * @param encrypted whether to encrypt the export (the default)
   * @param exportPassword password protecting an encrypted export
   * @param confirmPlaintext explicit acknowledgement required for an unencrypted export
   * @return the export result
   */
  @Transactional(readOnly = true)
  public VaultExportResult export(
      Long ownerId,
      String masterPassword,
      String vaultToken,
      boolean encrypted,
      String exportPassword,
      boolean confirmPlaintext) {
    if (encrypted) {
      if (isBlank(exportPassword)) {
        throw new IllegalArgumentException("exportPassword is required for an encrypted export");
      }
    } else if (!confirmPlaintext) {
      throw new IllegalArgumentException(
          "Plaintext export must be explicitly confirmed (confirmPlaintext=true)");
    }

    byte[] dek = resolveDek(ownerId, masterPassword, vaultToken);
    List<PasswordEntry> entries = passwordEntryRepository.findAllByOwnerId(ownerId);

    List<String[]> rows = new ArrayList<>();
    rows.add(CSV_HEADER.clone());
    for (PasswordEntry entry : entries) {
      String category = entry.getCategory() != null ? entry.getCategory().getName() : "";
      rows.add(
          new String[] {
            nullToEmpty(entry.getName()),
            nullToEmpty(entry.getUsername()),
            decryptForExport(ownerId, dek, entry),
            nullToEmpty(entry.getUrl()),
            nullToEmpty(entry.getNotes()),
            nullToEmpty(category)
          });
    }
    String csv = CsvSupport.write(rows);

    if (encrypted) {
      String envelope =
          crypto.encryptWithPassword(csv.getBytes(StandardCharsets.UTF_8), exportPassword);
      return new VaultExportResult(envelope, true, entries.size());
    }
    return new VaultExportResult(csv, false, entries.size());
  }

  /**
   * Imports entries from an encrypted export envelope or a plaintext CSV (format auto-detected).
   *
   * @param ownerId the vault owner
   * @param data the encrypted envelope or plaintext CSV
   * @param importPassword export password (required only for an encrypted envelope)
   * @param masterPassword master password (used when no vault token is supplied)
   * @param vaultToken active vault session token, or null
   * @return a summary of what was imported
   */
  @Transactional
  public VaultImportResponseDto importVault(
      Long ownerId, String data, String importPassword, String masterPassword, String vaultToken) {
    if (isBlank(data)) {
      throw new IllegalArgumentException("No data to import");
    }

    String csv;
    String candidate = data.stripLeading();
    if (crypto.isEncryptedExport(candidate)) {
      if (isBlank(importPassword)) {
        throw new IllegalArgumentException("importPassword is required for an encrypted export");
      }
      csv =
          new String(crypto.decryptWithPassword(candidate, importPassword), StandardCharsets.UTF_8);
    } else {
      csv = data;
    }
    csv = stripByteOrderMark(csv);

    List<String[]> rows = CsvSupport.parse(csv);
    if (rows.isEmpty()) {
      return new VaultImportResponseDto(0, 0, 0);
    }

    Map<String, Integer> columns = mapHeader(rows.get(0));
    if (!columns.containsKey("name") || !columns.containsKey("password")) {
      throw new IllegalArgumentException(
          "Unrecognized CSV header; expected columns such as name, username, password, url, notes, category");
    }

    byte[] dek = resolveDek(ownerId, masterPassword, vaultToken);
    Map<String, Category> categoriesByName = new HashMap<>();
    for (Category category : categoryRepository.findAllByOwnerId(ownerId)) {
      categoriesByName.put(category.getName().toLowerCase(Locale.ROOT), category);
    }

    int imported = 0;
    int categoriesCreated = 0;
    int skipped = 0;
    for (int r = 1; r < rows.size(); r++) {
      String[] row = rows.get(r);
      String name = field(row, columns, "name");
      String username = field(row, columns, "username");
      String password = field(row, columns, "password");

      // The entry model requires name, username, and password; skip rows that cannot form one.
      if (isBlank(name) || isBlank(username) || isBlank(password)) {
        skipped++;
        continue;
      }

      PasswordEntry entry = new PasswordEntry();
      entry.setOwnerId(ownerId);
      entry.setName(name);
      entry.setUsername(username);
      entry.setUrl(emptyToNull(field(row, columns, "url")));
      entry.setNotes(emptyToNull(field(row, columns, "notes")));

      String categoryName = field(row, columns, "category");
      if (!isBlank(categoryName)) {
        String key = categoryName.toLowerCase(Locale.ROOT);
        Category category = categoriesByName.get(key);
        if (category == null) {
          category = new Category();
          category.setName(categoryName.trim());
          category.setOwnerId(ownerId);
          category = categoryRepository.save(category);
          categoriesByName.put(key, category);
          categoriesCreated++;
        }
        entry.setCategory(category);
      }

      entry.setPassword(encryptForStorage(ownerId, dek, password));
      passwordEntryRepository.save(entry);
      imported++;
    }
    return new VaultImportResponseDto(imported, categoriesCreated, skipped);
  }

  /** Resolves the DEK once: null when the vault is not initialized (passwords are plaintext). */
  private byte[] resolveDek(Long ownerId, String masterPassword, String vaultToken) {
    if (!vaultService.isInitialized(ownerId)) {
      return null;
    }
    if (!isBlank(vaultToken)) {
      return vaultSessionService.requireDek(ownerId, vaultToken);
    }
    if (isBlank(masterPassword)) {
      throw new VaultLockedException("Master password or vault token required (vault initialized)");
    }
    return vaultService.unwrapDekForSession(ownerId, masterPassword);
  }

  /** Decrypts a stored password for export without triggering the reveal-path migration write. */
  private String decryptForExport(Long ownerId, byte[] dek, PasswordEntry entry) {
    String stored = entry.getPassword();
    if (dek == null || !crypto.isVaultEncryptedPassword(stored)) {
      return nullToEmpty(stored);
    }
    return crypto.decryptPasswordWithDek(dek, stored, ownerId);
  }

  private String encryptForStorage(Long ownerId, byte[] dek, String password) {
    if (dek == null) {
      return vaultService.encryptPasswordForStorage(ownerId, null, password);
    }
    return vaultService.encryptPasswordForStorageWithDek(ownerId, dek, password);
  }

  private static Map<String, Integer> mapHeader(String[] header) {
    Map<String, Integer> columns = new HashMap<>();
    for (int i = 0; i < header.length; i++) {
      String key = header[i] == null ? "" : header[i].trim().toLowerCase(Locale.ROOT);
      String canonical = COLUMN_ALIASES.get(key);
      if (canonical != null) {
        columns.putIfAbsent(canonical, i);
      }
    }
    return columns;
  }

  private static String field(String[] row, Map<String, Integer> columns, String canonical) {
    Integer index = columns.get(canonical);
    if (index == null || index >= row.length || row[index] == null) {
      return "";
    }
    return row[index];
  }

  private static Map<String, String> buildColumnAliases() {
    Map<String, String> aliases = new HashMap<>();
    putAll(aliases, "name", "name", "title", "service");
    putAll(aliases, "username", "username", "user", "login", "login_username", "login name");
    putAll(aliases, "password", "password", "login_password");
    putAll(aliases, "url", "url", "uri", "website", "login_uri");
    putAll(aliases, "notes", "notes", "note");
    putAll(aliases, "category", "category", "folder", "group", "grouping");
    return aliases;
  }

  private static void putAll(Map<String, String> map, String canonical, String... names) {
    for (String name : names) {
      map.put(name, canonical);
    }
  }

  private static boolean isBlank(String value) {
    return value == null || value.isBlank();
  }

  private static String nullToEmpty(String value) {
    return value == null ? "" : value;
  }

  private static String emptyToNull(String value) {
    return isBlank(value) ? null : value;
  }

  private static String stripByteOrderMark(String value) {
    return !value.isEmpty() && value.charAt(0) == BYTE_ORDER_MARK ? value.substring(1) : value;
  }
}
