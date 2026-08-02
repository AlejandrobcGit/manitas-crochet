package com.manitascrochet.backend.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ColorResquestDto {

    @NotBlank(message = "El nombre del color es obligatorio")
    private String nombre;

    @NotBlank(message = "El código del color es obligatorio")
    private String codigo;
}