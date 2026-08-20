package com.manitascrochet.backend.service;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import java.time.LocalDateTime;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;
import com.manitascrochet.backend.exception.GlobalExceptionHandler.*;
import com.manitascrochet.backend.model.*;
import com.manitascrochet.backend.repository.*;

@ExtendWith(MockitoExtension.class)
class VerificacionEmailServiceTest {
    @Mock
    TokenVerificacionRepository tokens;
    @Mock
    EmailService email;
    @Mock
    UsuarioRepository users;
    @Mock
    MongoTemplate mongo;
    @Mock
    PasswordEncoder encoder;
    @InjectMocks
    VerificacionEmailService service;

    @BeforeEach
    void config() {
        ReflectionTestUtils.setField(service, "app_frontend_url", "frontend.test");
        ReflectionTestUtils.setField(service, "protocolo", "https");
    }

    private Usuario user() {
        return user(false);
    }

    private Usuario user(boolean emailVerificado) {
        return new Usuario("u1", "ana", "ana@test", "old", Rol.USER, emailVerificado);
    }

    // ---------------------------------------------------------
    // enviarCorreoVerificacion
    // ---------------------------------------------------------

    @Test
    void enviaVerificacionYGuardaToken() throws Exception {
        when(users.findByEmail("ana@manitascochet.com")).thenReturn(Optional.of(user()));
        service.enviarCorreoVerificacion("ana@manitascochet.com");
        ArgumentCaptor<TokenVerificacion> cap = ArgumentCaptor.forClass(TokenVerificacion.class);
        verify(tokens).save(cap.capture());
        verify(email).enviar(eq("ana@test"), contains("Verificación"), contains("verificar-email?token="));
        assertThat(cap.getValue().getUsuarioId()).isEqualTo("u1");
        assertThat(cap.getValue().isUsado()).isFalse();
    }

    @Test
    void enviarVerificacion_usuarioNoEncontrado_lanzaExcepcion() {
        when(users.findByEmail("ana@maniatascrochet.com")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.enviarCorreoVerificacion("ana@maniatascrochet.com"))
                .isInstanceOf(UsuarioNotFoundException.class);

        verifyNoInteractions(tokens, email);
    }

    @Test
    void enviarVerificacion_emailYaVerificado_lanzaExcepcion() {
        when(users.findByEmail("ana@maniatascrochet.com")).thenReturn(Optional.of(user(true)));

        assertThatThrownBy(() -> service.enviarCorreoVerificacion("ana@maniatascrochet.com"))
                .isInstanceOf(EmailYaVerificadoException.class);

        verifyNoInteractions(tokens, email);
    }

    // ---------------------------------------------------------
    // verificarCuenta
    // ---------------------------------------------------------

    @Test
    void verificaTokenAtomicoYMarcaUsuario() {
        TokenVerificacion token = TokenVerificacion.builder().usuarioId("u1").token("t").usado(false)
                .fechaExpiracion(LocalDateTime.now().plusMinutes(2)).build();
        when(mongo.findAndModify(any(), any(), any(), eq(TokenVerificacion.class))).thenReturn(token);
        when(users.findById("u1")).thenReturn(Optional.of(user()));
        service.verificarCuenta("t");
        ArgumentCaptor<Usuario> cap = ArgumentCaptor.forClass(Usuario.class);
        verify(users).save(cap.capture());
        assertThat(cap.getValue().isEmailVerificado()).isTrue();
    }

    @Test
    void verificarCuenta_tokenNoExiste_lanzaTokenInvalido() {
        when(mongo.findAndModify(any(), any(), any(), eq(TokenVerificacion.class))).thenReturn(null);
        when(tokens.findByToken("t")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.verificarCuenta("t"))
                .isInstanceOf(TokenInvalidoException.class);

        verifyNoInteractions(users);
    }

    @Test
    void verificarCuenta_tokenNoUsadoPeroExpirado_lanzaTokenExpirado() {
        TokenVerificacion existente = TokenVerificacion.builder().usuarioId("u1").token("t").usado(false)
                .fechaExpiracion(LocalDateTime.now().minusMinutes(1)).build();
        when(mongo.findAndModify(any(), any(), any(), eq(TokenVerificacion.class))).thenReturn(null);
        when(tokens.findByToken("t")).thenReturn(Optional.of(existente));

        assertThatThrownBy(() -> service.verificarCuenta("t"))
                .isInstanceOf(TokenExpiradoException.class);

        verifyNoInteractions(users);
    }

    @Test
    void verificarCuenta_estadoInesperado_lanzaTokenYaUsado() {
        // No usado, no expirado, pero el findAndModify atómico no pudo marcarlo:
        // condición de carrera resuelta por otra request. Se trata como ya usado.
        TokenVerificacion existente = TokenVerificacion.builder().usuarioId("u1").token("t").usado(false)
                .fechaExpiracion(LocalDateTime.now().plusMinutes(5)).build();
        when(mongo.findAndModify(any(), any(), any(), eq(TokenVerificacion.class))).thenReturn(null);
        when(tokens.findByToken("t")).thenReturn(Optional.of(existente));

        assertThatThrownBy(() -> service.verificarCuenta("t"))
                .isInstanceOf(TokenYaUsadoException.class);

        verifyNoInteractions(users);
    }

    @Test
    void verificarCuenta_usuarioNoEncontrado_lanzaExcepcion() {
        TokenVerificacion token = TokenVerificacion.builder().usuarioId("u1").token("t").usado(false)
                .fechaExpiracion(LocalDateTime.now().plusMinutes(2)).build();
        when(mongo.findAndModify(any(), any(), any(), eq(TokenVerificacion.class))).thenReturn(token);
        when(users.findById("u1")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.verificarCuenta("t"))
                .isInstanceOf(UsuarioNotFoundException.class);

        verify(users, never()).save(any());
    }

    @Test
    void verificarCuenta_usuarioYaVerificado_lanzaExcepcion() {
        TokenVerificacion token = TokenVerificacion.builder().usuarioId("u1").token("t").usado(false)
                .fechaExpiracion(LocalDateTime.now().plusMinutes(2)).build();
        when(mongo.findAndModify(any(), any(), any(), eq(TokenVerificacion.class))).thenReturn(token);
        when(users.findById("u1")).thenReturn(Optional.of(user(true)));

        assertThatThrownBy(() -> service.verificarCuenta("t"))
                .isInstanceOf(EmailYaVerificadoException.class);

        verify(users, never()).save(any());
    }

    // ---------------------------------------------------------
    // enviarCorreoRecuperacion
    // ---------------------------------------------------------

    @Test
    void enviaCorreoRecuperacionYGuardaToken() throws Exception {
        when(users.findByEmail("ana@test")).thenReturn(Optional.of(user()));

        service.enviarCorreoRecuperacion("ana@test");

        ArgumentCaptor<TokenVerificacion> cap = ArgumentCaptor.forClass(TokenVerificacion.class);
        verify(tokens).save(cap.capture());
        verify(email).enviar(eq("ana@test"), contains("Recuperación"), contains("recuperar-contrasena?token="));
        assertThat(cap.getValue().getUsuarioId()).isEqualTo("u1");
        assertThat(cap.getValue().isUsado()).isFalse();
    }

    @Test
    void enviarCorreoRecuperacion_emailNoEncontrado_lanzaExcepcion() {
        when(users.findByEmail("noexiste@test")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.enviarCorreoRecuperacion("noexiste@test"))
                .isInstanceOf(EmailNotFoundException.class);

        verifyNoInteractions(tokens, email);
    }

    // ---------------------------------------------------------
    // restablecerContrasena
    // ---------------------------------------------------------

    @Test
    void rechazaTokenUsadoYRestablecePassword() {
        TokenVerificacion used = TokenVerificacion.builder().token("t").usado(true)
                .fechaExpiracion(LocalDateTime.now().plusMinutes(1)).build();
        when(mongo.findAndModify(any(), any(), any(), eq(TokenVerificacion.class))).thenReturn(null);
        when(tokens.findByToken("t")).thenReturn(Optional.of(used));
        assertThatThrownBy(() -> service.verificarCuenta("t")).isInstanceOf(TokenYaUsadoException.class);
        TokenVerificacion recovery = TokenVerificacion.builder().usuarioId("u1").token("r").usado(false)
                .fechaExpiracion(LocalDateTime.now().plusMinutes(1)).build();
        when(tokens.findByToken("r")).thenReturn(Optional.of(recovery));
        when(users.findById("u1")).thenReturn(Optional.of(user()));
        when(encoder.encode("new")).thenReturn("encoded");
        service.restablecerContrasena("r", "new");
        verify(encoder).encode("new");
        verify(users).save(any(Usuario.class));
        verify(tokens).save(recovery);
        assertThat(recovery.isUsado()).isTrue();
    }

    @Test
    void restablecerContrasena_tokenNoExiste_lanzaTokenInvalido() {
        when(tokens.findByToken("r")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.restablecerContrasena("r", "new"))
                .isInstanceOf(TokenInvalidoException.class);

        verifyNoInteractions(encoder, users);
    }

    @Test
    void restablecerContrasena_tokenYaUsado_lanzaExcepcion() {
        TokenVerificacion recovery = TokenVerificacion.builder().usuarioId("u1").token("r").usado(true)
                .fechaExpiracion(LocalDateTime.now().plusMinutes(5)).build();
        when(tokens.findByToken("r")).thenReturn(Optional.of(recovery));

        assertThatThrownBy(() -> service.restablecerContrasena("r", "new"))
                .isInstanceOf(TokenInvalidoRecuperacionException.class);

        verifyNoInteractions(encoder, users);
    }

    @Test
    void restablecerContrasena_tokenExpirado_lanzaExcepcion() {
        TokenVerificacion recovery = TokenVerificacion.builder().usuarioId("u1").token("r").usado(false)
                .fechaExpiracion(LocalDateTime.now().minusMinutes(1)).build();
        when(tokens.findByToken("r")).thenReturn(Optional.of(recovery));

        assertThatThrownBy(() -> service.restablecerContrasena("r", "new"))
                .isInstanceOf(TokenInvalidoRecuperacionException.class);

        verifyNoInteractions(encoder, users);
    }

    @Test
    void restablecerContrasena_usuarioNoEncontrado_lanzaExcepcion() {
        TokenVerificacion recovery = TokenVerificacion.builder().usuarioId("u1").token("r").usado(false)
                .fechaExpiracion(LocalDateTime.now().plusMinutes(5)).build();
        when(tokens.findByToken("r")).thenReturn(Optional.of(recovery));
        when(users.findById("u1")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.restablecerContrasena("r", "new"))
                .isInstanceOf(TokenInvalidoRecuperacionException.class);

        verify(tokens, never()).save(any());
    }
}