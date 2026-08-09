package com.manitascrochet.backend.model;

import java.time.LocalDateTime;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import lombok.Data;

@Data
@Document(collection = "visualizaciones")
public class Visualizacion {
    @Id
    String id;
    String usuarioId;
    String figuraId;
    LocalDateTime fecha;
}
