package com.manitascrochet.backend.dto;

import java.util.List;

import com.manitascrochet.backend.model.Dificultad;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class FiguraRequestDto {

    @NotBlank(message = "El nombre es obligatorio")
    @Size(max = 150, message = "El nombre no puede superar los 150 caracteres")
    private String nombre;

    @Size(max = 2000, message = "La descripción no puede superar los 2000 caracteres")
    private String descripcion;

    @NotBlank(message = "La categoría es obligatoria")
    private String categoriaId;

    @NotNull(message = "La dificultad es obligatoria")
    private Dificultad dificultad;

    @Size(min = 2, max = 150, message = "El autor debe tener entre 2 y 150 caracteres")
    private String autor;

    @NotEmpty(message = "Debe indicar al menos un color")
    private List<String> coloresIds;

    @NotNull(message = "La altura es obligatoria")
    @Positive(message = "La altura debe ser mayor que 0")
    private Integer altura;

    @NotNull(message = "El ancho es obligatorio")
    @Positive(message = "El ancho debe ser mayor que 0")
    private Integer ancho;

    @NotNull(message = "El peso es obligatorio")
    @Positive(message = "El peso debe ser mayor que 0")
    private Integer peso;
}