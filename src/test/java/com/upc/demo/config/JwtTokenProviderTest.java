package com.upc.demo.config;

import com.upc.demo.entidad.enums.Role;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class JwtTokenProviderTest {

    private JwtTokenProvider jwtTokenProvider;
    private final String validSecret = "9a6747f3b64c61d4a607645061ce7212513a94f55393b2cef440355125a4e921";
    private final long expirationMs = 3600000; // 1 hour

    @BeforeEach
    void setUp() {
        jwtTokenProvider = new JwtTokenProvider();
        ReflectionTestUtils.setField(jwtTokenProvider, "jwtSecret", validSecret);
        ReflectionTestUtils.setField(jwtTokenProvider, "jwtExpirationInMs", expirationMs);
    }

    @Test
    @DisplayName("validateJwtSecret lanza IllegalStateException cuando el secreto es nulo o vacio")
    void validateJwtSecret_whenNullOrEmpty_throwsException() {
        ReflectionTestUtils.setField(jwtTokenProvider, "jwtSecret", null);
        assertThrows(IllegalStateException.class, () -> jwtTokenProvider.validateJwtSecret());

        ReflectionTestUtils.setField(jwtTokenProvider, "jwtSecret", "   ");
        assertThrows(IllegalStateException.class, () -> jwtTokenProvider.validateJwtSecret());
    }

    @Test
    @DisplayName("validateJwtSecret lanza IllegalStateException cuando el secreto tiene menos de 32 caracteres")
    void validateJwtSecret_whenShortSecret_throwsException() {
        ReflectionTestUtils.setField(jwtTokenProvider, "jwtSecret", "clave_corta_insegura_123");
        assertThrows(IllegalStateException.class, () -> jwtTokenProvider.validateJwtSecret());
    }

    @Test
    @DisplayName("validateJwtSecret pasa sin errores cuando el secreto cumple con la entropia requerida")
    void validateJwtSecret_whenValidSecret_succeeds() {
        assertDoesNotThrow(() -> jwtTokenProvider.validateJwtSecret());
    }

    @Test
    @DisplayName("generateToken y extraccion de claims funcionan correctamente")
    void generateTokenAndExtractClaims_success() {
        UUID userId = UUID.randomUUID();
        String email = "turista@capilladelmonte.gov.ar";
        Role role = Role.TOURIST;

        String token = jwtTokenProvider.generateToken(userId, email, role);

        assertNotNull(token);
        assertTrue(jwtTokenProvider.validateToken(token));
        assertEquals(email, jwtTokenProvider.getEmailFromToken(token));
        assertEquals(userId, jwtTokenProvider.getUserIdFromToken(token));
        assertEquals(role, jwtTokenProvider.getRoleFromToken(token));
    }

    @Test
    @DisplayName("validateToken retorna false ante un token malformado")
    void validateToken_whenMalformed_returnsFalse() {
        assertFalse(jwtTokenProvider.validateToken("token.invalido.malformado"));
    }
}
