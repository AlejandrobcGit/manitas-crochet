package com.manitascrochet.backend.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ComentarioDto {

    @NotBlank(message = "El identificador de la figura es obligatorio")
    private String figuraId;

    @NotBlank(message = "El comentario no puede estar vacío")
    private String comentario;
}