package com.manitascrochet.backend.model;

import java.time.LocalDateTime;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import lombok.Data;

@Document(collection = "favoritos")
@Data
public class Favorito {

    @Id
    private String id;

    private String usuarioId;

    private String figuraId;

    private LocalDateTime fechaAlta;

    private LocalDateTime fechaBaja;

    private boolean activo;
}