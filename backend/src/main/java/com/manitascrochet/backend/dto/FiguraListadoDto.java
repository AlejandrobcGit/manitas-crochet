package com.manitascrochet.backend.dto;

import java.util.List;

import com.manitascrochet.backend.model.Dificultad;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class FiguraListadoDto {

    private String id;

    private String nombre;

    private String descripcion;

    private String categoria;

    private Dificultad dificultad;

    private String autor;

    private String imagenPrincipal;

    private List<ColorDto> colores;
}