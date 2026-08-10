package com.manitascrochet.backend.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.manitascrochet.backend.dto.CategoriaRequestDto;
import com.manitascrochet.backend.model.Categoria;
import com.manitascrochet.backend.service.CategoriaService;

@ExtendWith(MockitoExtension.class)
class CategoriaControllerTest {
    @Mock CategoriaService service;
    @InjectMocks CategoriaController controller;

    @Test void delegaConsultas() {
        Categoria categoria = new Categoria();
        when(service.obtenerTodas("am")).thenReturn(List.of(categoria));
        when(service.obtenerPorId("1")).thenReturn(categoria);
        assertThat(controller.obtenerTodas("am")).containsExactly(categoria);
        assertThat(controller.obtenerPorId("1")).isSameAs(categoria);
        verify(service).obtenerTodas("am");
        verify(service).obtenerPorId("1");
    }

    @Test void creaActualizaYEliminaMapeandoNombre() {
        Categoria guardada = new Categoria();
        when(service.guardar(any(Categoria.class))).thenReturn(guardada);
        when(service.actualizar(eq("1"), any(Categoria.class))).thenReturn(guardada);
        CategoriaRequestDto dto = new CategoriaRequestDto("Animales");
        assertThat(controller.crear(dto)).isSameAs(guardada);
        assertThat(controller.actualizar("1", new CategoriaRequestDto("Flores"))).isSameAs(guardada);
        controller.eliminar("1");
        verify(service).guardar(argThat(c -> "Animales".equals(c.getNombre())));
        verify(service).actualizar(eq("1"), argThat(c -> "Flores".equals(c.getNombre())));
        verify(service).eliminar("1");
    }
}
