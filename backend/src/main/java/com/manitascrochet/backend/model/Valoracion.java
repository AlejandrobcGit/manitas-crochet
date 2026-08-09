package com.manitascrochet.backend.model;

import java.time.LocalDateTime;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import lombok.Data;

@Data
@Document(collection = "valoraciones")
public class Valoracion {

    @Id
    private String id;

    private String usuarioId;

    private String figuraId;

    private Integer puntuacion;

    private LocalDateTime fecha;
}