package com.manitascrochet.backend.service;

import static org.assertj.core.api.Assertions.*;
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

import com.manitascrochet.backend.exception.GlobalExceptionHandler.CodigoColorDuplicadoException;
import com.manitascrochet.backend.exception.GlobalExceptionHandler.ColorDuplicadoException;
import com.manitascrochet.backend.exception.GlobalExceptionHandler.ColorNoEncontradoException;
import com.manitascrochet.backend.model.Color;
import com.manitascrochet.backend.repository.ColorRepository;

@ExtendWith(MockitoExtension.class)
class ColorServiceTest {

    @Mock
    ColorRepository repository;

    @Mock
    MongoTemplate mongoTemplate;

    @InjectMocks
    ColorService service;

    private Color color(String id, String nombre, String codigo) {
        Color c = new Color();
        c.setId(id);
        c.setNombre(nombre);
        c.setCodigo(codigo);
        return c;
    }

    @Test
    void filtraYGuardaColor() {
        Color c = color("1", "Rojo", "#f00");

        when(mongoTemplate.find(any(Query.class), eq(Color.class)))
                .thenReturn(List.of(c));

        assertThat(service.obtenerTodos("ro", "f"))
                .containsExactly(c);

        when(repository.findByNombreIgnoreCase("Rojo"))
                .thenReturn(Optional.empty());

        when(repository.findByCodigoIgnoreCase("#f00"))
                .thenReturn(Optional.empty());

        when(repository.save(c))
                .thenReturn(c);

        assertThat(service.guardar(c))
                .isSameAs(c);
    }

    @Test
    void obtenerTodosSinFiltros() {

        List<Color> colores = List.of(
                color("1", "Rojo", "#f00"),
                color("2", "Azul", "#00f"));

        when(mongoTemplate.find(any(Query.class), eq(Color.class)))
                .thenReturn(colores);

        List<Color> resultado = service.obtenerTodos(null, null);

        assertThat(resultado)
                .hasSize(2)
                .containsExactlyElementsOf(colores);
    }

    @Test
    void rechazaNombreOCodigoDuplicado() {

        Color c = color(null, "Rojo", "#f00");

        when(repository.findByNombreIgnoreCase("Rojo"))
                .thenReturn(Optional.of(
                        color("x", "Rojo", "#000")));

        assertThatThrownBy(() -> service.guardar(c))
                .isInstanceOf(ColorDuplicadoException.class);

        reset(repository);

        when(repository.findByNombreIgnoreCase("Rojo"))
                .thenReturn(Optional.empty());

        when(repository.findByCodigoIgnoreCase("#f00"))
                .thenReturn(Optional.of(
                        color("x", "Otro", "#f00")));

        assertThatThrownBy(() -> service.guardar(c))
                .isInstanceOf(CodigoColorDuplicadoException.class);
    }

    @Test
    void actualizaYElimina() {

        Color old = color("1", "Rojo", "#f00");
        Color change = color(null, "Azul", "#00f");

        when(repository.findById("1"))
                .thenReturn(Optional.of(old));

        when(repository.findByNombreIgnoreCase("Azul"))
                .thenReturn(Optional.empty());

        when(repository.findByCodigoIgnoreCase("#00f"))
                .thenReturn(Optional.empty());

        when(repository.save(old))
                .thenReturn(old);

        assertThat(service.actualizar("1", change).getNombre())
                .isEqualTo("Azul");

        service.eliminar("1");

        verify(repository).deleteById("1");

        when(repository.findById("x"))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.obtenerPorId("x"))
                .isInstanceOf(ColorNoEncontradoException.class);
    }

    @Test
    void actualizarLanzaExcepcionSiNoExiste() {

        when(repository.findById("99"))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() ->
                service.actualizar(
                        "99",
                        color(null, "Rojo", "#f00")))
                .isInstanceOf(ColorNoEncontradoException.class);
    }

    @Test
    void actualizarLanzaExcepcionSiNombreDuplicado() {

        Color existente = color("1", "Rojo", "#f00");
        Color otro = color("2", "Azul", "#00f");

        when(repository.findById("1"))
                .thenReturn(Optional.of(existente));

        when(repository.findByNombreIgnoreCase("Azul"))
                .thenReturn(Optional.of(otro));

        assertThatThrownBy(() ->
                service.actualizar(
                        "1",
                        color(null, "Azul", "#111")))
                .isInstanceOf(ColorDuplicadoException.class);
    }

    @Test
    void actualizarPermiteMismoNombreMismoId() {

        Color existente = color("1", "Rojo", "#f00");
        Color actualizado = color(null, "Rojo", "#123");

        when(repository.findById("1"))
                .thenReturn(Optional.of(existente));

        when(repository.findByNombreIgnoreCase("Rojo"))
                .thenReturn(Optional.of(existente));

        when(repository.findByCodigoIgnoreCase("#123"))
                .thenReturn(Optional.empty());

        when(repository.save(any(Color.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Color resultado = service.actualizar("1", actualizado);

        assertThat(resultado.getCodigo()).isEqualTo("#123");
    }

    @Test
    void actualizarLanzaExcepcionSiCodigoDuplicado() {

        Color existente = color("1", "Rojo", "#f00");
        Color otro = color("2", "Azul", "#00f");

        when(repository.findById("1"))
                .thenReturn(Optional.of(existente));

        when(repository.findByNombreIgnoreCase(anyString()))
                .thenReturn(Optional.empty());

        when(repository.findByCodigoIgnoreCase("#00f"))
                .thenReturn(Optional.of(otro));

        assertThatThrownBy(() ->
                service.actualizar(
                        "1",
                        color(null, "Verde", "#00f")))
                .isInstanceOf(CodigoColorDuplicadoException.class);
    }

    @Test
    void actualizarPermiteMismoCodigoMismoId() {

        Color existente = color("1", "Rojo", "#f00");
        Color actualizado = color(null, "Verde", "#f00");

        when(repository.findById("1"))
                .thenReturn(Optional.of(existente));

        when(repository.findByNombreIgnoreCase("Verde"))
                .thenReturn(Optional.empty());

        when(repository.findByCodigoIgnoreCase("#f00"))
                .thenReturn(Optional.of(existente));

        when(repository.save(any(Color.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Color resultado = service.actualizar("1", actualizado);

        assertThat(resultado.getNombre()).isEqualTo("Verde");
    }

    @Test
    void eliminarLanzaExcepcionCuandoNoExiste() {

        when(repository.findById("99"))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.eliminar("99"))
                .isInstanceOf(ColorNoEncontradoException.class);

        verify(repository, never())
                .deleteById(anyString());
    }
}