package com.manitascrochet.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class FiguraListadoDto {

    private String id;

    private String nombre;

    private String categoria;

    private String imagenPrincipal;

    private int altura;
    
    private int ancho;
}