package com.manitascrochet.backend.security;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

class JwtUtilsTest {

    private static final String SECRET =
            "clave-de-prueba-para-jwt-con-mas-de-32-bytes-1234567890";

    private JwtUtils jwtUtils;

    @BeforeEach
    void setUp() {
        jwtUtils = new JwtUtils();
        ReflectionTestUtils.setField(jwtUtils, "jwtSecret", SECRET);
        ReflectionTestUtils.setField(jwtUtils, "jwtExpirationMs", 60_000L);
        ReflectionTestUtils.setField(jwtUtils, "jwtRefreshExpirationMs", 600_000L);
    }

    @Test
    @DisplayName("Genera y valida un access token")
    void generaYValidaAccessToken() {
        String token = jwtUtils.generateTokenFromEmail("ana@test.es");

        assertTrue(jwtUtils.validateJwtToken(token));
        assertEquals("ana@test.es", jwtUtils.getEmailFromJwtToken(token));
    }

    @Test
    @DisplayName("Genera y valida un refresh token")
    void generaYValidaRefreshToken() {
        String token = jwtUtils.generateRefreshTokenFromEmail("ana@test.es");

        assertTrue(jwtUtils.validateJwtToken(token));
        assertEquals("ana@test.es", jwtUtils.getEmailFromJwtToken(token));
    }

    @Test
    @DisplayName("El token se firma con HS256")
    void usaAlgoritmoHs256() {
        String token = jwtUtils.generateTokenFromEmail("ana@test.es");
        String headerJson = new String(
                Base64.getUrlDecoder().decode(token.split("\\.")[0]),
                StandardCharsets.UTF_8);

        assertTrue(headerJson.contains("\"alg\":\"HS256\""));
    }

    @Test
    @DisplayName("Rechaza un token con la firma manipulada")
    void rechazaTokenManipulado() {
        String token = jwtUtils.generateTokenFromEmail("ana@test.es");
        String tampered = token.substring(0, token.length() - 2) + "xx";

        assertFalse(jwtUtils.validateJwtToken(tampered));
    }

    @Test
    @DisplayName("Rechaza un token expirado")
    void rechazaTokenExpirado() {
        ReflectionTestUtils.setField(jwtUtils, "jwtExpirationMs", -1_000L);
        String token = jwtUtils.generateTokenFromEmail("ana@test.es");

        assertFalse(jwtUtils.validateJwtToken(token));
    }

    @Test
    @DisplayName("Rechaza un token mal formado")
    void rechazaTokenMalFormado() {
        assertFalse(jwtUtils.validateJwtToken("esto-no-es-un-jwt"));
    }

    @Test
    @DisplayName("Rechaza un token vacío")
    void rechazaTokenVacio() {
        assertFalse(jwtUtils.validateJwtToken(""));
    }
}
