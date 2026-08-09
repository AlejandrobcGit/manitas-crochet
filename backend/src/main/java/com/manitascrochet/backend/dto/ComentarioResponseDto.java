package com.manitascrochet.backend.dto;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ComentarioResponseDto {

    String usuario;
    String figuraId;
    Integer valoracion;
    String comentario;
    LocalDateTime fechaCreacion;
    LocalDateTime fechaModificacion;

}
