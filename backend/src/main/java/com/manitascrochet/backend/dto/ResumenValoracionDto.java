package com.manitascrochet.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ResumenValoracionDto {

    private Double valoracionMedia;

    private Long totalValoraciones;
}