package com.manitascrochet.backend.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class CategoriaRequestDto {
    @NotBlank(message = "El nombre de la categoría es obligatorio")
    private String nombre;
}