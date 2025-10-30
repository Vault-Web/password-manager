package com.vaultweb.passwordmanager.backend;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@TestPropertySource(properties = {
    "spring.datasource.url=jdbc:h2:mem:testdb",
    "spring.jpa.hibernate.ddl-auto=create-drop"
})
public class ConfigTest {

    @Value("${jwt.secret}")
    private String jwtSecret;

    @Value("${encryption.secret}")
    private String encryptionSecret;

    @Test
    void testDefaultValues() {
        // Should use default values when env vars not set
        assertNotNull(jwtSecret);
        assertNotNull(encryptionSecret);
        assertEquals("ab9bb63c49d6d8b4029a1e6e3b1947d34be053f8ce5a0ee391e46f393014694e", jwtSecret);
        assertEquals("zXhiwEukXkHn0g7mZ2HVwA==", encryptionSecret);
    }
}
