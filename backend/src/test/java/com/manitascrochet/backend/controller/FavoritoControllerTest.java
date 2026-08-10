package com.manitascrochet.backend.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;

import com.manitascrochet.backend.service.FavoritoService;

@ExtendWith(MockitoExtension.class)
class FavoritoControllerTest {
    @Mock FavoritoService service;
    @Mock Authentication authentication;
    @InjectMocks FavoritoController controller;

    @Test void cambiaFavoritoConUsuarioAutenticado() {
        when(authentication.getName()).thenReturn("ana");
        when(service.cambiarFavorito("ana", "f1")).thenReturn(true);
        assertThat(controller.cambiarFavorito("f1", authentication).getBody()).isTrue();
        verify(service).cambiarFavorito("ana", "f1");
    }

    @Test void obtieneFavoritosActivos() {
        when(authentication.getName()).thenReturn("ana");
        when(service.obtenerFavoritosActivos("ana")).thenReturn(List.of("f1", "f2"));
        assertThat(controller.obtenerFavoritos(authentication).getBody()).containsExactly("f1", "f2");
    }
}
