package com.manitascrochet.backend.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import jakarta.mail.MessagingException;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.manitascrochet.backend.dto.security.JwtResponseDto;
import com.manitascrochet.backend.dto.security.LoginDto;
import com.manitascrochet.backend.dto.security.ResetPasswordRequest;
import com.manitascrochet.backend.dto.security.SignupDto;
import com.manitascrochet.backend.exception.GlobalExceptionHandler.TokenInvalidoException;
import com.manitascrochet.backend.exception.security.InvalidRefreshTokenException;
import com.manitascrochet.backend.model.Rol;
import com.manitascrochet.backend.repository.UsuarioRepository;
import com.manitascrochet.backend.security.JwtUtils;
import com.manitascrochet.backend.security.UserDetailsImpl;
import com.manitascrochet.backend.security.UserDetailsServiceImpl;
import com.manitascrochet.backend.service.VerificacionEmailService;

@ExtendWith(MockitoExtension.class)
class AuthControllerTest {
    @Mock AuthenticationManager authenticationManager;
    @Mock UsuarioRepository users;
    @Mock PasswordEncoder encoder;
    @Mock JwtUtils jwt;
    @Mock UserDetailsServiceImpl detailsService;
    @Mock VerificacionEmailService verification;
    @Mock Authentication authentication;
    @InjectMocks AuthController controller;

    @AfterEach void clearContext() { org.springframework.security.core.context.SecurityContextHolder.clearContext(); }

    private SignupDto signup(String username, String email) {
        SignupDto dto = new SignupDto(); dto.setUsername(username); dto.setEmail(email); dto.setPassword("secreto"); return dto;
    }
    private UserDetailsImpl user() {
        return new UserDetailsImpl("u1", "ana", "ana@test.es", "hash", true,
                java.util.List.of(new SimpleGrantedAuthority("ROLE_USER")));
    }

    @Test void registraUsuarioYAdministrador() {
        when(users.existsByUsername(anyString())).thenReturn(false);
        when(users.existsByEmail(anyString())).thenReturn(false);
        when(encoder.encode("secreto")).thenReturn("hash");
        controller.registerUser(signup("ana", "ana@test.es"));
        controller.crearAdmin(signup("admin", "admin@test.es"));
        verify(users).save(argThat(u -> u.getRol() == Rol.USER));
        verify(users).save(argThat(u -> u.getRol() == Rol.ADMIN));
    }

    @Test void autenticaYRefrescaTokens() {
        when(authenticationManager.authenticate(any())).thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn(user());
        when(jwt.generateJwtToken(authentication)).thenReturn("access");
        when(jwt.generateRefreshToken(authentication)).thenReturn("refresh");
        assertThat(controller.authenticateUser(new LoginDto(), new MockHttpServletResponse()).getBody())
                .isInstanceOf(JwtResponseDto.class);
        when(jwt.getEmailFromJwtToken("old")).thenReturn("ana@manitas-crochet.com");
        when(jwt.validateJwtToken("old")).thenReturn(true);
        when(detailsService.loadUserByUsername("ana@manitas-crochet.com")).thenReturn(user());
        when(jwt.generateTokenFromEmail("ana@manitas-crochet.com")).thenReturn("new-access");
        when(jwt.generateRefreshTokenFromEmail("ana@manitas-crochet.com")).thenReturn("new-refresh");
        assertThat(controller.refreshToken("old", new MockHttpServletResponse()).getBody())
                .isInstanceOf(JwtResponseDto.class);
        assertThatThrownBy(() -> controller.refreshToken(null, new MockHttpServletResponse()))
                .isInstanceOf(InvalidRefreshTokenException.class);
    }

    @Test void logoutYRecuperacionSonSeguros() throws Exception {
        MockHttpServletResponse response = new MockHttpServletResponse();
        controller.logout(response);
        Cookie cookie = response.getCookie("refreshToken");
        assertThat(cookie.getMaxAge()).isZero();
        controller.enviarCorreoRecuperarContrasena("ana@test.es");
        verify(verification).enviarCorreoRecuperacion("ana@test.es");
        ResetPasswordRequest reset = new ResetPasswordRequest();
        org.springframework.test.util.ReflectionTestUtils.setField(reset, "token", "token");
        org.springframework.test.util.ReflectionTestUtils.setField(reset, "nuevaContrasena", "nueva1234");
        controller.restablecerContrasena(reset);
        doThrow(new TokenInvalidoException()).when(verification).restablecerContrasena(anyString(), anyString());
        assertThat(controller.restablecerContrasena(reset).getStatusCode().value()).isEqualTo(400);
        UserDetailsImpl user = user();
        assertThat(controller.EnviarCorreoverificar(user).getBody()).containsEntry("status", "success");
        assertThat(controller.verificar("token").getBody()).containsEntry("status", "success");
    }

    @Test void recuperacionOcultaFalloMail() throws MessagingException {
        doThrow(new MessagingException("smtp")).when(verification).enviarCorreoRecuperacion("x@test.es");
        assertThat(controller.enviarCorreoRecuperarContrasena("x@test.es").getStatusCode().is2xxSuccessful()).isTrue();
    }
}
