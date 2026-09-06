package com.manitascrochet.backend.dto;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class PaginaFigurasDto {

    private List<FiguraListadoDto> contenido;

    private int paginaActual;

    private int totalPaginas;

    private long totalElementos;

    private int tamanoPagina;
}
