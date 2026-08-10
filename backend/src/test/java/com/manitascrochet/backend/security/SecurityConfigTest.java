package com.manitascrochet.backend.security;

import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.options;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;

import jakarta.servlet.FilterChain;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import com.manitascrochet.backend.repository.UsuarioRepository;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Suite de tests para {@link SecurityConfig}.
 *
 * Se divide en dos bloques:
 *  1) {@link WebLayerTests}: prueba el comportamiento real de la cadena de
 *     filtros (autenticación, autorización por rol, handlers de error, CORS)
 *     usando MockMvc contra un controller de prueba (TestController) que
 *     replica las rutas relevantes de tu API. Así no depende de tus
 *     controllers reales ni de la base de datos.
 *  2) {@link BeanUnitTests}: prueba en aislamiento los @Bean simples
 *     (PasswordEncoder, AuthenticationManager) sin levantar contexto Spring.
 *
 * Requiere en el classpath: spring-boot-starter-test y
 * spring-security-test.
 */
class SecurityConfigTest {

    // ============================================================
    // 1) TESTS DE LA CADENA DE FILTROS (integración de capa web)
    // ============================================================
    @Nested
    @WebMvcTest(controllers = SecurityConfigTest.TestController.class)
    @AutoConfigureMockMvc(addFilters = true)
    @Import(SecurityConfig.class)
    @TestPropertySource(properties = {
            "app.cors.allowed-origins=https://tudominio.com"
    })
    @DisplayName("SecurityConfig - reglas de autorización, errores y CORS")
    class WebLayerTests {

        @Autowired
        private MockMvc mockMvc;

        // El filtro real de JWT se mockea: en estos tests la autenticación
        // se simula con @WithMockUser, no necesitamos tokens reales.
        @MockitoBean
        private AuthTokenFilter authTokenFilter;

        // WebMvcTest carga BackendApplication, cuyo initAdmin necesita este bean.
        @MockitoBean
        private UsuarioRepository usuarioRepository;

        @BeforeEach
        void continuarCadenaDespuesDelFiltroJwt() throws Exception {
            doAnswer(invocation -> {
                FilterChain chain = invocation.getArgument(2);
                chain.doFilter(invocation.getArgument(0), invocation.getArgument(1));
                return null;
            }).when(authTokenFilter).doFilter(any(), any(), any());
        }

        // -------------------- Endpoints públicos --------------------

        @Test
        @DisplayName("GET /auth/** es público")
        void authEsPublico() throws Exception {
            mockMvc.perform(get("/auth/login"))
                    .andExpect(status().isOk());
        }

        @Test
        @DisplayName("GET /api/** es público por defecto")
        void getApiEsPublicoPorDefecto() throws Exception {
            mockMvc.perform(get("/api/patrones"))
                    .andExpect(status().isOk());
        }

        @Test
        @DisplayName("GET /api/comentarios/** es público explícitamente")
        void getComentariosEsPublico() throws Exception {
            mockMvc.perform(get("/api/comentarios/1"))
                    .andExpect(status().isOk());
        }

        // -------------------- Endpoints autenticados --------------------

        @Test
        @DisplayName("GET /api/favorito/** sin token -> 401 con cuerpo esperado")
        void getFavoritoSinAuth401() throws Exception {
            mockMvc.perform(get("/api/favorito/1"))
                    .andExpect(status().isUnauthorized())
                    .andExpect(jsonPath("$.status").value(401))
                    .andExpect(jsonPath("$.error").value("UNAUTHORIZED"))
                    .andExpect(jsonPath("$.mensaje").value("No autenticado. Por favor inicia sesión."))
                    .andExpect(jsonPath("$.timestamp").exists());
        }

        @Test
        @WithMockUser(roles = "USER")
        @DisplayName("GET /api/favorito/** autenticado -> 200")
        void getFavoritoConAuth200() throws Exception {
            mockMvc.perform(get("/api/favorito/1"))
                    .andExpect(status().isOk());
        }

        @Test
        @WithMockUser(roles = "USER")
        @DisplayName("POST /api/favorito/** autenticado -> 200")
        void postFavoritoConAuth200() throws Exception {
            mockMvc.perform(post("/api/favorito/1").with(csrf()))
                    .andExpect(status().isOk());
        }

        @Test
        @DisplayName("POST /api/comentarios/** sin token -> 401")
        void postComentariosSinAuth401() throws Exception {
            mockMvc.perform(post("/api/comentarios/1").with(csrf()))
                    .andExpect(status().isUnauthorized());
        }

        @Test
        @WithMockUser(roles = "USER")
        @DisplayName("DELETE /api/comentarios/** autenticado -> 200")
        void deleteComentariosConAuth200() throws Exception {
            mockMvc.perform(delete("/api/comentarios/1").with(csrf()))
                    .andExpect(status().isOk());
        }

        @Test
        @WithMockUser(roles = "USER")
        @DisplayName("POST /api/valoraciones/** autenticado -> 200")
        void postValoracionesConAuth200() throws Exception {
            mockMvc.perform(post("/api/valoraciones/1").with(csrf()))
                    .andExpect(status().isOk());
        }

        @Test
        @DisplayName("GET /auth/enviarcorreoverificar sin token -> 401")
        void enviarCorreoVerificarSinAuth401() throws Exception {
            mockMvc.perform(get("/auth/enviarcorreoverificar"))
                    .andExpect(status().isUnauthorized());
        }

        @Test
        @WithMockUser(roles = "USER")
        @DisplayName("GET /auth/enviarcorreoverificar autenticado -> 200")
        void enviarCorreoVerificarConAuth200() throws Exception {
            mockMvc.perform(get("/auth/enviarcorreoverificar"))
                    .andExpect(status().isOk());
        }

        // -------------------- Endpoints solo ADMIN --------------------

        @Test
        @DisplayName("POST /api/** sin token -> 401 (no llega a evaluar el rol)")
        void postApiSinAuth401() throws Exception {
            mockMvc.perform(post("/api/patrones")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{}"))
                    .andExpect(status().isUnauthorized());
        }

        @Test
        @WithMockUser(roles = "USER")
        @DisplayName("POST /api/** autenticado sin rol ADMIN -> 403 con cuerpo esperado")
        void postApiConUserNoAdmin403() throws Exception {
            mockMvc.perform(post("/api/patrones")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{}"))
                    .andExpect(status().isForbidden())
                    .andExpect(jsonPath("$.status").value(403))
                    .andExpect(jsonPath("$.error").value("ACCESS_DENIED"))
                    .andExpect(jsonPath("$.mensaje").value("No tienes permisos para realizar esta acción."));
        }

        @Test
        @WithMockUser(roles = "ADMIN")
        @DisplayName("POST /api/** con rol ADMIN -> 200")
        void postApiConAdmin200() throws Exception {
            mockMvc.perform(post("/api/patrones")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{}"))
                    .andExpect(status().isOk());
        }

        @Test
        @WithMockUser(roles = "ADMIN")
        @DisplayName("PUT /api/** con rol ADMIN -> 200")
        void putApiConAdmin200() throws Exception {
            mockMvc.perform(put("/api/patrones/1")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{}"))
                    .andExpect(status().isOk());
        }

        @Test
        @WithMockUser(roles = "USER")
        @DisplayName("DELETE /api/** sin rol ADMIN -> 403")
        void deleteApiConUserNoAdmin403() throws Exception {
            mockMvc.perform(delete("/api/patrones/1").with(csrf()))
                    .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("/auth/admin/** sin token -> 401")
        void authAdminSinAuth401() throws Exception {
            mockMvc.perform(get("/auth/admin/panel"))
                    .andExpect(status().isUnauthorized());
        }

        @Test
        @WithMockUser(roles = "USER")
        @DisplayName("/auth/admin/** con rol USER -> 403")
        void authAdminConUserNoAdmin403() throws Exception {
            mockMvc.perform(get("/auth/admin/panel"))
                    .andExpect(status().isForbidden());
        }

        @Test
        @WithMockUser(roles = "ADMIN")
        @DisplayName("/auth/admin/** con rol ADMIN -> 200")
        void authAdminConAdmin200() throws Exception {
            mockMvc.perform(get("/auth/admin/panel"))
                    .andExpect(status().isOk());
        }

        // -------------------- CORS --------------------

        @Test
        @DisplayName("Preflight CORS para origen permitido devuelve cabeceras correctas")
        void corsPreflightOrigenPermitido() throws Exception {
            mockMvc.perform(options("/api/patrones")
                            .header("Origin", "https://tudominio.com")
                            .header("Access-Control-Request-Method", "GET"))
                    .andExpect(status().isOk())
                    .andExpect(header().string("Access-Control-Allow-Origin", "https://tudominio.com"))
                    .andExpect(header().string("Access-Control-Allow-Credentials", "true"));
        }
    }

    // ============================================================
    // 2) TESTS DE BEANS EN AISLAMIENTO (sin contexto Spring)
    // ============================================================
    @Nested
    @DisplayName("SecurityConfig - beans en aislamiento")
    class BeanUnitTests {

        @Test
        @DisplayName("passwordEncoder() codifica y verifica correctamente con BCrypt")
        void passwordEncoderCodificaYVerifica() {
            SecurityConfig config = new SecurityConfig(mock(AuthTokenFilter.class), List.of("*"));

            PasswordEncoder encoder = config.passwordEncoder();
            String raw = "miPasswordSegura123";
            String hash = encoder.encode(raw);

            assertNotEquals(raw, hash);
            assertTrue(encoder.matches(raw, hash));
        }

        @Test
        @DisplayName("corsConfigurationSource() no lanza excepción y registra la ruta /**")
        void corsConfigurationSourceSeConstruyeCorrectamente() {
            SecurityConfig config = new SecurityConfig(
                    mock(AuthTokenFilter.class),
                    List.of("https://tudominio.com"));

            assertNotEquals(null, config.corsConfigurationSource());
        }
    }

    // ============================================================
    // Controller de apoyo solo para estos tests: expone las mismas
    // rutas que protege SecurityConfig, devolviendo 200 "OK" cuando
    // el request logra pasar el filtro de seguridad. Así se testea
    // el efecto real de las reglas, no una simulación.
    // ============================================================
    @RestController
    static class TestController {

        @GetMapping("/auth/login")
        String authLogin() { return "ok"; }

        @GetMapping("/auth/enviarcorreoverificar")
        String enviarCorreoVerificar() { return "ok"; }

        @RequestMapping("/auth/admin/panel")
        String authAdmin() { return "ok"; }

        @GetMapping("/api/patrones")
        String getPatrones() { return "ok"; }

        @PostMapping("/api/patrones")
        String postPatrones() { return "ok"; }

        @PutMapping("/api/patrones/{id}")
        String putPatrones() { return "ok"; }

        @DeleteMapping("/api/patrones/{id}")
        String deletePatrones() { return "ok"; }

        @GetMapping("/api/comentarios/{id}")
        String getComentarios() { return "ok"; }

        @PostMapping("/api/comentarios/{id}")
        String postComentarios() { return "ok"; }

        @DeleteMapping("/api/comentarios/{id}")
        String deleteComentarios() { return "ok"; }

        @GetMapping("/api/favorito/{id}")
        String getFavorito() { return "ok"; }

        @PostMapping("/api/favorito/{id}")
        String postFavorito() { return "ok"; }

        @PostMapping("/api/valoraciones/{id}")
        String postValoraciones() { return "ok"; }
    }
}
