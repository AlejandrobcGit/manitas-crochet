package com.manitascrochet.backend.model;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import lombok.Data;

@Document(collection = "figuras")
@Data
public class Figura {

    @Id
    private String id;

    private String nombre;

    private String descripcion;

    private String categoriaId;

    private Dificultad dificultad;

    private String autor;

    private String imagenPrincipal;

    private List<String> imagenesSecundarias;

    private List<String> coloresIds;

    private Integer altura;

    private Integer ancho;

    private Integer peso;

    private LocalDateTime fechaCreacion;

    private LocalDateTime fechaModificacion;
}