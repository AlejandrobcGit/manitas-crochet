package com.manitascrochet.backend.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import com.manitascrochet.backend.dto.ComentarioDto;
import com.manitascrochet.backend.dto.ValoracionDto;
import com.manitascrochet.backend.exception.GlobalExceptionHandler.ComentarioNoEncontradoException;
import com.manitascrochet.backend.model.Comentario;
import com.manitascrochet.backend.model.Usuario;
import com.manitascrochet.backend.repository.ComentarioRepository;
import com.manitascrochet.backend.repository.UsuarioRepository;

@ExtendWith(MockitoExtension.class)
class ComentarioServiceTest {
    @Mock
    ComentarioRepository repository;
    @Mock
    UsuarioRepository users;
    @Mock
    ValoracionService ratings;
    @InjectMocks
    ComentarioService service;

    @Test
    void creaYActualizaComentario() {
        ComentarioDto dto = new ComentarioDto("f", "texto");
        when(repository.findByUsuarioIdAndFiguraId("u", "f")).thenReturn(Optional.empty());
        when(repository.save(any(Comentario.class))).thenAnswer(invocation -> invocation.getArgument(0));
        Comentario created = service.guardarComentario(dto, "u");
        assertThat(created.getComentario()).isEqualTo("texto");
        assertThat(created.getUsuarioId()).isEqualTo("u");
        verify(repository).save(created);
        when(repository.findByUsuarioIdAndFiguraId("u", "f")).thenReturn(Optional.of(created));
        service.guardarComentario(new ComentarioDto("f", "nuevo"), "u");
        assertThat(created.getComentario()).isEqualTo("nuevo");
    }

    @Test
    void convierteConUsuarioYFallback() {
        Comentario c = new Comentario();
        c.setUsuarioId("u");
        c.setFiguraId("f");
        c.setComentario("ok");
        Usuario u = new Usuario();
        u.setUsername("ana");
        when(users.findById("u")).thenReturn(Optional.of(u));
        when(ratings.obtenerValoracionUsuario("u", "f")).thenReturn(new ValoracionDto(4));
        assertThat(service.convertirAResponseDto(c).getUsuario()).isEqualTo("ana");
        when(users.findById("u")).thenReturn(Optional.empty());
        assertThat(service.convertirAResponseDto(c).getUsuario()).isEqualTo("u");
    }

    @Test
    void listaYElimina() {
        Comentario c = new Comentario();
        c.setUsuarioId("u");
        c.setFiguraId("f");
        when(repository.findByFiguraIdOrderByFechaCreacionDesc("f")).thenReturn(List.of(c));
        when(users.findById("u")).thenReturn(Optional.empty());
        when(ratings.obtenerValoracionUsuario("u", "f")).thenReturn(new ValoracionDto(0));
        assertThat(service.obtenerComentariosFigura("f")).hasSize(1);
        when(repository.findByIdAndUsuarioId("c", "u")).thenReturn(Optional.of(c));
        service.eliminarComentario("c", "u");
        verify(repository).delete(c);
        when(repository.findByIdAndUsuarioId("x", "u")).thenReturn(Optional.empty());
        assertThatThrownBy(() -> service.eliminarComentario("x", "u"))
                .isInstanceOf(ComentarioNoEncontradoException.class);
        service.eliminarComentariosPorFigura("f");
        verify(repository).deleteByFiguraId("f");
    }
}
