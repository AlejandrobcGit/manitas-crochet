package com.manitascrochet.backend.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.manitascrochet.backend.dto.ColorResquestDto;
import com.manitascrochet.backend.model.Color;
import com.manitascrochet.backend.service.ColorService;

@ExtendWith(MockitoExtension.class)
class ColorControllerTest {
    @Mock ColorService service;
    @InjectMocks ColorController controller;

    @Test void obtieneListaYPorId() {
        Color color = new Color();
        when(service.obtenerTodos("ro", "#f00")).thenReturn(List.of(color));
        when(service.obtenerPorId("1")).thenReturn(color);
        assertThat(controller.obtenerTodas("ro", "#f00")).containsExactly(color);
        assertThat(controller.obtenerPorId("1")).isSameAs(color);
    }

    @Test void creaActualizaYEliminaMapeandoCampos() {
        Color resultado = new Color();
        when(service.guardar(any(Color.class))).thenReturn(resultado);
        when(service.actualizar(eq("1"), any(Color.class))).thenReturn(resultado);
        assertThat(controller.crear(new ColorResquestDto("Rojo", "#f00"))).isSameAs(resultado);
        assertThat(controller.actualizar("1", new ColorResquestDto("Azul", "#00f"))).isSameAs(resultado);
        controller.eliminar("1");
        verify(service).guardar(argThat(c -> "Rojo".equals(c.getNombre()) && "#f00".equals(c.getCodigo())));
        verify(service).actualizar(eq("1"), argThat(c -> "Azul".equals(c.getNombre()) && "#00f".equals(c.getCodigo())));
        verify(service).eliminar("1");
    }
}
