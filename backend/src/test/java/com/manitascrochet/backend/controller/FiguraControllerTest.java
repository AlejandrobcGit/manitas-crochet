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
import org.springframework.mock.web.MockMultipartFile;

import com.manitascrochet.backend.dto.FiguraDetalleDto;
import com.manitascrochet.backend.dto.FiguraRequestDto;
import com.manitascrochet.backend.model.Dificultad;
import com.manitascrochet.backend.service.FiguraService;
import com.manitascrochet.backend.service.VisualizacionService;

@ExtendWith(MockitoExtension.class)
class FiguraControllerTest {
    @Mock FiguraService service;
    @Mock VisualizacionService visualizaciones;
    @InjectMocks FiguraController controller;

    private FiguraRequestDto dto() {
        return new FiguraRequestDto("Oso", "Descripción", "cat1", Dificultad.PRINCIPIANTE,
                "Ana", List.of("red"), 10, 8, 100);
    }

    @Test void obtieneListadoYDetalleYMarcaVisualizacion() {
        FiguraDetalleDto detalle = new FiguraDetalleDto(null, "Oso", null, null, null, null, null,
            null, null, null, null, null, null, null, null);
        when(service.obtenerPorIdDto("f1", null)).thenReturn(detalle);
        assertThat(controller.obtenerPorId("f1", null)).isSameAs(detalle);
        verify(visualizaciones).marcarVisualizacion("f1", null);
    }

    @Test void creaMapeandoDtoYArchivos() {
        FiguraDetalleDto detalle = mock(FiguraDetalleDto.class);
        MockMultipartFile principal = new MockMultipartFile("imagenPrincipal", "oso.png", "image/png", new byte[] { 1 });
        MockMultipartFile secundaria = new MockMultipartFile("imagenesSecundarias", "detalle.png", "image/png", new byte[] { 2 });
        when(service.crear(any(), same(principal), anyList())).thenReturn(detalle);
        assertThat(controller.crearFigura(dto(), principal, List.of(secundaria))).isSameAs(detalle);
        verify(service).crear(argThat(f -> "Oso".equals(f.getNombre()) && "cat1".equals(f.getCategoriaId())
                && f.getColoresIds().contains("red") && f.getAltura() == 10), same(principal), anyList());
    }

    @Test void actualizaYElimina() {
        FiguraDetalleDto detalle = mock(FiguraDetalleDto.class);
        when(service.actualizar(eq("f1"), any(), isNull(), isNull())).thenReturn(detalle);
        assertThat(controller.actualizar("f1", dto(), null, null)).isSameAs(detalle);
        controller.eliminar("f1");
        verify(service).eliminar("f1");
        verify(service).actualizar(eq("f1"), argThat(f -> "Descripción".equals(f.getDescripcion())), isNull(), isNull());
    }
}
