package com.vaultweb.passwordmanager.backend.support;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import org.junit.jupiter.api.Test;

class CsvSupportTest {

  @Test
  void roundTripsSimpleRows() {
    List<String[]> rows =
        List.of(
            new String[] {"name", "username", "password"},
            new String[] {"GitHub", "gabriel", "s3cr3t"});
    List<String[]> parsed = CsvSupport.parse(CsvSupport.write(rows));
    assertEquals(2, parsed.size());
    assertArrayEquals(new String[] {"name", "username", "password"}, parsed.get(0));
    assertArrayEquals(new String[] {"GitHub", "gabriel", "s3cr3t"}, parsed.get(1));
  }

  @Test
  void roundTripsFieldsWithCommasQuotesAndNewlines() {
    String[] row = {"a,b", "say \"hi\"", "line1\nline2", ""};
    List<String[]> parsed = CsvSupport.parse(CsvSupport.write(List.<String[]>of(row)));
    assertEquals(1, parsed.size());
    assertArrayEquals(row, parsed.get(0));
  }

  @Test
  void quotesFieldsOnlyWhenNeeded() {
    String csv = CsvSupport.write(List.<String[]>of(new String[] {"plain", "with,comma"}));
    assertTrue(csv.startsWith("plain,\"with,comma\""));
  }

  @Test
  void parsesEscapedQuotes() {
    List<String[]> parsed = CsvSupport.parse("\"a\"\"b\",c\r\n");
    assertArrayEquals(new String[] {"a\"b", "c"}, parsed.get(0));
  }

  @Test
  void acceptsLfOnlyLineEndings() {
    List<String[]> parsed = CsvSupport.parse("h1,h2\nv1,v2\n");
    assertEquals(2, parsed.size());
    assertArrayEquals(new String[] {"v1", "v2"}, parsed.get(1));
  }

  @Test
  void capturesTrailingRowWithoutNewline() {
    assertArrayEquals(new String[] {"a", "b", "c"}, CsvSupport.parse("a,b,c").get(0));
  }

  @Test
  void emptyInputYieldsNoRows() {
    assertTrue(CsvSupport.parse("").isEmpty());
  }
}
