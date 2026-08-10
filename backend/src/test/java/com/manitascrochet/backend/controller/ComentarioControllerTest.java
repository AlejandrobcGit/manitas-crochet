package com.manitascrochet.backend.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

import com.manitascrochet.backend.dto.ComentarioDto;
import com.manitascrochet.backend.dto.ComentarioResponseDto;
import com.manitascrochet.backend.model.Comentario;
import com.manitascrochet.backend.security.UserDetailsImpl;
import com.manitascrochet.backend.service.ComentarioService;

@ExtendWith(MockitoExtension.class)
class ComentarioControllerTest {
    @Mock ComentarioService service;
    @InjectMocks ComentarioController controller;

    private UserDetailsImpl user() { UserDetailsImpl u = new UserDetailsImpl(); u.setId("u1"); return u; }

    @Test void guardaObtieneYEliminaComentario() {
        Comentario comentario = new Comentario();
        ComentarioResponseDto response = new ComentarioResponseDto("c1", "ana", 5, "hola", null, null);
        when(service.guardarComentario(new ComentarioDto("f1", "hola"), "u1")).thenReturn(comentario);
        when(service.obtenerComentariosFigura("f1")).thenReturn(List.of(response));
        assertThat(controller.guardarComentario(new ComentarioDto("f1", "hola"), user()).getStatusCode())
                .isEqualTo(HttpStatus.CREATED);
        assertThat(controller.obtenerComentariosFigura("f1").getBody()).containsExactly(response);
        assertThat(controller.eliminarComentario("c1", user()).getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
        verify(service).eliminarComentario("c1", "u1");
    }

    @Test void devuelve204CuandoUsuarioNoTieneComentario() {
        when(service.obtenerComentarioUsuarioFigura("f1", "u1")).thenReturn(null);
        assertThat(controller.obtenerComentarioUsuarioFigura("f1", user()).getStatusCode())
                .isEqualTo(HttpStatus.NO_CONTENT);
    }
}
