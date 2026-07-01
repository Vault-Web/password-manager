package com.vaultweb.passwordmanager.backend.support;

import java.util.ArrayList;
import java.util.List;

/**
 * Minimal RFC 4180 CSV writer and parser.
 *
 * <p>Kept dependency-free on purpose: the vault export/import only needs a small, predictable CSV
 * implementation, and pulling in a CSV library for that would be overkill. Fields are quoted only
 * when they contain a comma, double quote, or line break; embedded double quotes are escaped by
 * doubling them. Rows are separated by CRLF on write, and parsing accepts both CRLF and LF.
 */
public final class CsvSupport {

  private static final char DELIMITER = ',';
  private static final char QUOTE = '"';
  private static final String ROW_SEPARATOR = "\r\n";

  private CsvSupport() {}

  /**
   * Serializes the given rows to a CSV string.
   *
   * @param rows the rows to write; each element is one record's fields
   * @return the CSV representation, with CRLF row separators
   */
  public static String write(List<String[]> rows) {
    StringBuilder sb = new StringBuilder();
    for (String[] row : rows) {
      for (int i = 0; i < row.length; i++) {
        if (i > 0) {
          sb.append(DELIMITER);
        }
        sb.append(escape(row[i]));
      }
      sb.append(ROW_SEPARATOR);
    }
    return sb.toString();
  }

  private static String escape(String field) {
    String value = field == null ? "" : field;
    boolean mustQuote =
        value.indexOf(DELIMITER) >= 0
            || value.indexOf(QUOTE) >= 0
            || value.indexOf('\n') >= 0
            || value.indexOf('\r') >= 0;
    if (!mustQuote) {
      return value;
    }
    return QUOTE + value.replace("\"", "\"\"") + QUOTE;
  }

  /**
   * Parses a CSV string into rows. Handles quoted fields containing commas, quotes, and line
   * breaks. Both CRLF and LF line endings are accepted.
   *
   * @param csv the CSV text to parse
   * @return the parsed rows; each element is one record's fields
   */
  public static List<String[]> parse(String csv) {
    List<String[]> rows = new ArrayList<>();
    List<String> current = new ArrayList<>();
    StringBuilder field = new StringBuilder();
    boolean inQuotes = false;
    boolean rowHasContent = false;
    int i = 0;
    int n = csv.length();

    while (i < n) {
      char c = csv.charAt(i);
      if (inQuotes) {
        if (c == QUOTE) {
          if (i + 1 < n && csv.charAt(i + 1) == QUOTE) {
            field.append(QUOTE);
            i += 2;
          } else {
            inQuotes = false;
            i++;
          }
        } else {
          field.append(c);
          i++;
        }
        continue;
      }

      if (c == QUOTE) {
        inQuotes = true;
        rowHasContent = true;
        i++;
      } else if (c == DELIMITER) {
        current.add(field.toString());
        field.setLength(0);
        rowHasContent = true;
        i++;
      } else if (c == '\r' || c == '\n') {
        current.add(field.toString());
        field.setLength(0);
        rows.add(current.toArray(new String[0]));
        current = new ArrayList<>();
        rowHasContent = false;
        i += (c == '\r' && i + 1 < n && csv.charAt(i + 1) == '\n') ? 2 : 1;
      } else {
        field.append(c);
        rowHasContent = true;
        i++;
      }
    }

    if (rowHasContent || field.length() > 0 || !current.isEmpty()) {
      current.add(field.toString());
      rows.add(current.toArray(new String[0]));
    }
    return rows;
  }
}
