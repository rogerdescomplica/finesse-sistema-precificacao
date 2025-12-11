package com.finesse.security;

import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.*;

class JwtTokenProviderTest {
    @Test
    void shortSecretIsUpgradedTo256Bits() {
        JwtTokenProvider provider = new JwtTokenProvider();
        ReflectionTestUtils.setField(provider, "jwtSecret", "short");
        assertDoesNotThrow(() -> ReflectionTestUtils.invokeMethod(provider, "getSigningKey"));
    }
}