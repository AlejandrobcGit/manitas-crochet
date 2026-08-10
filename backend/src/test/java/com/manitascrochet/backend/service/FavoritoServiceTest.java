package com.manitascrochet.backend.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import com.manitascrochet.backend.model.Favorito;
import com.manitascrochet.backend.repository.FavoritoRepository;

@ExtendWith(MockitoExtension.class)
class FavoritoServiceTest {
    @Mock
    FavoritoRepository repository;
    @InjectMocks
    FavoritoService service;

    @Test
    void activaCuandoNoExiste() {
        when(repository.findByUsuarioIdAndFiguraIdAndActivoTrue("u", "f")).thenReturn(null);
        assertThat(service.cambiarFavorito("u", "f")).isTrue();
        ArgumentCaptor<Favorito> cap = ArgumentCaptor.forClass(Favorito.class);
        verify(repository).save(cap.capture());
        assertThat(cap.getValue().isActivo()).isTrue();
        assertThat(cap.getValue().getUsuarioId()).isEqualTo("u");
    }

    @Test
    void desactivaCuandoExiste() {
        Favorito f = new Favorito();
        f.setActivo(true);
        when(repository.findByUsuarioIdAndFiguraIdAndActivoTrue("u", "f")).thenReturn(f);
        assertThat(service.cambiarFavorito("u", "f")).isFalse();
        assertThat(f.isActivo()).isFalse();
        verify(repository).save(f);
    }

    @Test
    void listaIdsActivos() {
        Favorito a = new Favorito();
        a.setFiguraId("a");
        Favorito b = new Favorito();
        b.setFiguraId("b");
        when(repository.findByUsuarioIdAndActivoTrue("u")).thenReturn(List.of(a, b));
        assertThat(service.obtenerFavoritosActivos("u")).containsExactly("a", "b");
    }
}
