package com.manitascrochet.backend.service;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Query;

import com.manitascrochet.backend.exception.GlobalExceptionHandler.CategoriaDuplicadaException;
import com.manitascrochet.backend.exception.GlobalExceptionHandler.CategoriaEnUsoException;
import com.manitascrochet.backend.exception.GlobalExceptionHandler.CategoriaNoEncontradaException;
import com.manitascrochet.backend.model.Categoria;
import com.manitascrochet.backend.repository.CategoriaRepository;
import com.manitascrochet.backend.repository.FiguraRepository;

@ExtendWith(MockitoExtension.class)
class CategoriaServiceTest {
    @Mock
    CategoriaRepository repository;
    @Mock
    FiguraRepository figuraRepository;
    @Mock
    MongoTemplate mongoTemplate;
    @InjectMocks
    CategoriaService service;

    @Test
    void obtieneConFiltroUsandoMongoTemplate() {
        Categoria c = new Categoria();
        c.setId("1");
        c.setNombre("Amigurumis");
        when(mongoTemplate.find(any(Query.class), eq(Categoria.class))).thenReturn(List.of(c));
        assertThat(service.obtenerTodas("amigur")).containsExactly(c);
        verify(mongoTemplate).find(any(Query.class), eq(Categoria.class));
    }

    @Test
    void guardaYRechazaDuplicada() {
        Categoria c = new Categoria();
        c.setNombre("Flores");
        when(repository.findByNombreIgnoreCase("Flores")).thenReturn(Optional.empty());
        when(repository.save(c)).thenReturn(c);
        assertThat(service.guardar(c)).isSameAs(c);
        when(repository.findByNombreIgnoreCase("Flores")).thenReturn(Optional.of(c));
        assertThatThrownBy(() -> service.guardar(c)).isInstanceOf(CategoriaDuplicadaException.class);
    }

    @Test
    void actualizaYEliminaSoloSiExiste() {
        Categoria actual = new Categoria();
        actual.setId("1");
        actual.setNombre("Vieja");
        Categoria cambio = new Categoria();
        cambio.setNombre("Nueva");
        
        when(repository.findById("1")).thenReturn(Optional.of(actual));
        when(repository.findByNombreIgnoreCase("Nueva")).thenReturn(Optional.empty());
        when(repository.save(actual)).thenReturn(actual);
        assertThat(service.actualizar("1", cambio).getNombre())
                .isEqualTo("Nueva");
        when(repository.existsById("1")).thenReturn(true);
        when(figuraRepository.existsByCategoriaId("1")).thenReturn(false);
        service.eliminar("1");
        verify(repository).deleteById("1");
        when(repository.findById("x")).thenReturn(Optional.empty());
        assertThatThrownBy(() -> service.obtenerPorId("x"))
                .isInstanceOf(CategoriaNoEncontradaException.class);
    }

    @Test
    void obtenerTodasSinFiltroNoAplicaCriteria() {
        when(mongoTemplate.find(any(Query.class), eq(Categoria.class))).thenReturn(List.of());
        assertThat(service.obtenerTodas(null)).isEmpty();
        assertThat(service.obtenerTodas("   ")).isEmpty();
        verify(mongoTemplate, times(2)).find(any(Query.class), eq(Categoria.class));
    }

    @Test
    void obtenerPorIdRetornaCategoriaSiExiste() {
        Categoria c = new Categoria();
        c.setId("1");
        c.setNombre("Amigurumis");
        when(repository.findById("1")).thenReturn(Optional.of(c));
        assertThat(service.obtenerPorId("1")).isSameAs(c);
    }

    @Test
    void actualizarLanzaExcepcionSiNoExiste() {
        Categoria cambio = new Categoria();
        cambio.setNombre("Nueva");
        when(repository.findById("x")).thenReturn(Optional.empty());
        assertThatThrownBy(() -> service.actualizar("x", cambio))
                .isInstanceOf(CategoriaNoEncontradaException.class);
    }

    @Test
    void actualizarLanzaDuplicadaSiNombreYaExisteEnOtraCategoria() {
        Categoria actual = new Categoria();
        actual.setId("1");
        actual.setNombre("Vieja");

        Categoria otra = new Categoria();
        otra.setId("2");
        otra.setNombre("Nueva");

        Categoria cambio = new Categoria();
        cambio.setNombre("Nueva");

        when(repository.findById("1")).thenReturn(Optional.of(actual));
        when(repository.findByNombreIgnoreCase("Nueva")).thenReturn(Optional.of(otra));

        assertThatThrownBy(() -> service.actualizar("1", cambio))
                .isInstanceOf(CategoriaDuplicadaException.class);
    }

    @Test
    void actualizarPermiteConservarElMismoNombreEnLaMismaCategoria() {
        Categoria actual = new Categoria();
        actual.setId("1");
        actual.setNombre("Vieja");

        Categoria cambio = new Categoria();
        cambio.setNombre("Vieja");

        when(repository.findById("1")).thenReturn(Optional.of(actual));
        when(repository.findByNombreIgnoreCase("Vieja")).thenReturn(Optional.of(actual));
        when(repository.save(actual)).thenReturn(actual);

        assertThat(service.actualizar("1", cambio).getNombre()).isEqualTo("Vieja");
    }

    @Test
    void eliminarLanzaExcepcionSiNoExiste() {
        when(repository.existsById("x")).thenReturn(false);
        assertThatThrownBy(() -> service.eliminar("x"))
                .isInstanceOf(CategoriaNoEncontradaException.class);
        verify(repository, never()).deleteById(any());
    }

    @Test
    void noDebeEliminarCategoriaEnUso() {
        when(repository.existsById("1")).thenReturn(true);
        when(figuraRepository.existsByCategoriaId("1")).thenReturn(true);

        assertThrows(
                CategoriaEnUsoException.class,
                () -> service.eliminar("1"));

        verify(repository, never()).deleteById(any());
    }

    @Test
    void debeEliminarCategoriaNoUtilizada() {
        when(repository.existsById("1")).thenReturn(true);
        when(figuraRepository.existsByCategoriaId("1")).thenReturn(false);

        service.eliminar("1");

        verify(repository).deleteById("1");
    }
}
