package com.manitascrochet.backend.dto;

import org.springframework.data.annotation.Id;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class VisualizacionesPorFiguraDto {

    @Id
    private String figuraId;

    private long total;
}