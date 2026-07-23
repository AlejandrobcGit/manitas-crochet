package com.manitascrochet.backend.dto;

import java.util.List;

import com.manitascrochet.backend.model.Dificultad;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class FiguraDetalleDto {
      private String id;

    private String nombre;

    private String descripcion;

    private String categoria;

    private Dificultad dificultad;

    private String autor;

    private String imagenPrincipal;

    private List<String> imagenesSecundarias;

    private List<ColorDto> colores;

    private Integer altura;

    private Integer ancho;
    
    private Integer peso;
}
