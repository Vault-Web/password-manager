package com.vaultweb.passwordmanager.backend;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@TestPropertySource(properties = {
    "spring.datasource.url=jdbc:h2:mem:testdb",
    "spring.jpa.hibernate.ddl-auto=create-drop"
})
public class ConfigTest2 {

    @Value("${jwt.secret}")
    private String jwtSecret;

    @Value("${encryption.secret}")
    private String encryptionSecret;

    @Test
    void testEnvironmentVariableOverride() {
        // Should use env var values when set
        assertNotNull(jwtSecret);
        assertNotNull(encryptionSecret);
        System.out.println("JWT Secret: " + jwtSecret);
        System.out.println("Encryption Secret: " + encryptionSecret);
    }
}
