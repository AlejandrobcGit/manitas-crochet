package com.manitascrochet.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ComentarioDto {

    private String figuraId;

    private String comentario;
}