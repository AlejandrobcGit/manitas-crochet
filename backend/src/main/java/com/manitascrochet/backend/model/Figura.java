package com.manitascrochet.backend.model;

import java.util.List;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import lombok.Data;

@Data
@Document(collection = "figuras")
public class Figura {

    @Id
    private String id;

    private String nombre;

    private String descripcion;

    private String categoria;

    private String dificultad;

    private String autor;

    private String imagenPrincipal;

    private List<String> materiales;
}