package com.vaultweb.passwordmanager.backend;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test to verify that Spring property placeholders with defaults work correctly.
 * This mimics the pattern used in application.properties:
 * jwt.secret=${JWT_SECRET:default_value}
 */
@SpringBootTest(classes = {})
@TestPropertySource(properties = {
    "test.property.with.env=${TEST_ENV_VAR:fallback_value}",
    "test.property.direct=direct_value"
})
public class PropertyInjectionTest {

    @Value("${test.property.with.env}")
    private String propertyWithEnv;

    @Value("${test.property.direct}")
    private String propertyDirect;

    @Test
    void testPropertyPlaceholderWithDefault() {
        // This tests the ${VAR:default} pattern
        assertNotNull(propertyWithEnv);
        // Should be 'fallback_value' since TEST_ENV_VAR is not set
        assertEquals("fallback_value", propertyWithEnv);
        
        assertNotNull(propertyDirect);
        assertEquals("direct_value", propertyDirect);
        
        System.out.println("✓ Property placeholder with default works correctly!");
        System.out.println("  propertyWithEnv: " + propertyWithEnv);
        System.out.println("  propertyDirect: " + propertyDirect);
    }
}
