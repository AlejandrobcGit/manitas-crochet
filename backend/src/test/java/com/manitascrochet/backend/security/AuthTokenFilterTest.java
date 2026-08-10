package com.manitascrochet.backend.security;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;

import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@ExtendWith(MockitoExtension.class)
class AuthTokenFilterTest {

    @Mock
    JwtUtils jwtUtils;

    @Mock
    UserDetailsServiceImpl userDetailsService;

    @Mock
    HttpServletRequest request;

    @Mock
    HttpServletResponse response;

    @Mock
    FilterChain filterChain;

    @Mock
    UserDetails userDetails;

    private AuthTokenFilter filter() {
        return new AuthTokenFilter(jwtUtils, userDetailsService);
    }

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void dejaPasarLasRutasPublicasSinIntentarAutenticar() throws Exception {
        when(request.getServletPath()).thenReturn("/swagger-ui/index.html");

        filter().doFilterInternal(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
        verifyNoInteractions(jwtUtils, userDetailsService);
    }

    @Test
    void dejaPasarUnaRutaPublicaCuandoTieneUnSufijo() throws Exception {
        when(request.getServletPath()).thenReturn("/auth/signup/extra");

        filter().doFilterInternal(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
        verifyNoInteractions(jwtUtils, userDetailsService);
    }

    @Test
    void dejaPasarSiNoHayCabeceraAuthorization() throws Exception {
        when(request.getServletPath()).thenReturn("/api/figuras");
        when(request.getHeader("Authorization")).thenReturn(null);

        filter().doFilterInternal(request, response, filterChain);

        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
        verify(filterChain).doFilter(request, response);
        verifyNoInteractions(jwtUtils, userDetailsService);
    }

    @Test
    void ignoraUnaCabeceraQueNoEsBearer() throws Exception {
        when(request.getServletPath()).thenReturn("/api/figuras");
        when(request.getHeader("Authorization")).thenReturn("Basic credentials");

        filter().doFilterInternal(request, response, filterChain);

        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
        verify(filterChain).doFilter(request, response);
        verifyNoInteractions(jwtUtils, userDetailsService);
    }

    @Test
    void ignoraUnaCabeceraBearerVacia() throws Exception {
        when(request.getServletPath()).thenReturn("/api/figuras");
        when(request.getHeader("Authorization")).thenReturn("Bearer ");
        when(jwtUtils.validateJwtToken("")).thenReturn(false);

        filter().doFilterInternal(request, response, filterChain);

        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
        verify(jwtUtils).validateJwtToken("");
        verify(filterChain).doFilter(request, response);
        verifyNoInteractions(userDetailsService);
    }

    @Test
    void noAutenticaCuandoElTokenEsInvalido() throws Exception {
        when(request.getServletPath()).thenReturn("/api/figuras");
        when(request.getHeader("Authorization")).thenReturn("Bearer invalid-token");
        when(jwtUtils.validateJwtToken("invalid-token")).thenReturn(false);

        filter().doFilterInternal(request, response, filterChain);

        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
        verify(jwtUtils).validateJwtToken("invalid-token");
        verify(jwtUtils, never()).getUserNameFromJwtToken(any());
        verifyNoInteractions(userDetailsService);
        verify(filterChain).doFilter(request, response);
    }

    @Test
    void autenticaCuandoElTokenEsValido() throws Exception {
        when(request.getServletPath()).thenReturn("/api/figuras");
        when(request.getHeader("Authorization")).thenReturn("Bearer valid-token");
        when(jwtUtils.validateJwtToken("valid-token")).thenReturn(true);
        when(jwtUtils.getUserNameFromJwtToken("valid-token")).thenReturn("ana");
        when(userDetailsService.loadUserByUsername("ana")).thenReturn(userDetails);
        when(userDetails.getAuthorities()).thenReturn(List.of());

        filter().doFilterInternal(request, response, filterChain);

        assertThat(SecurityContextHolder.getContext().getAuthentication())
            .isInstanceOf(UsernamePasswordAuthenticationToken.class);
        assertThat(SecurityContextHolder.getContext().getAuthentication().getPrincipal())
            .isSameAs(userDetails);
        verify(filterChain).doFilter(request, response);
    }

    @Test
    void continuaLaCadenaSiLaAutenticacionLanzaUnaExcepcion() throws Exception {
        when(request.getServletPath()).thenReturn("/api/figuras");
        when(request.getHeader("Authorization")).thenReturn("Bearer valid-token");
        when(jwtUtils.validateJwtToken("valid-token")).thenThrow(new RuntimeException("token error"));

        filter().doFilterInternal(request, response, filterChain);

        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
        verify(filterChain).doFilter(request, response);
    }

    @Test
    void continuaLaCadenaSiNoPuedeCargarElUsuario() throws Exception {
        when(request.getServletPath()).thenReturn("/api/figuras");
        when(request.getHeader("Authorization")).thenReturn("Bearer valid-token");
        when(jwtUtils.validateJwtToken("valid-token")).thenReturn(true);
        when(jwtUtils.getUserNameFromJwtToken("valid-token")).thenReturn("ana");
        when(userDetailsService.loadUserByUsername("ana"))
                .thenThrow(new RuntimeException("user error"));

        filter().doFilterInternal(request, response, filterChain);

        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
        verify(filterChain).doFilter(request, response);
    }

    @Test
    void propagaLaExcepcionDeLaCadena() throws Exception {
        when(request.getServletPath()).thenReturn("/api/figuras");
        when(request.getHeader("Authorization")).thenReturn(null);
        doThrow(new IOException("chain error")).when(filterChain).doFilter(request, response);

        assertThat(org.assertj.core.api.Assertions.catchThrowable(
                () -> filter().doFilterInternal(request, response, filterChain)))
                .isInstanceOf(IOException.class)
                .hasMessage("chain error");
    }
}