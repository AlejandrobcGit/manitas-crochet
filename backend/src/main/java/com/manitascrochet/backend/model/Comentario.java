package com.manitascrochet.backend.model;

import java.time.LocalDateTime;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import lombok.Data;

@Data
@Document(collection = "comentarios")
public class Comentario {
    @Id
    String id;
    String usuarioId;
    String figuraId;
    String comentario;
    LocalDateTime fechaCreacion;
    LocalDateTime fechaModificacion;
}
