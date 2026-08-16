package com.manitascrochet.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Top10 {

    String figura;
    long visualizaciones;
    long favoritos;
    long comentarios;
    double ValoracionMedia;    
}
