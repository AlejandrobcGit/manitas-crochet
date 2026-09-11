package com.manitascrochet.backend.model;

import java.time.LocalDateTime;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import lombok.Data;

@Data
@Document(collection = "visualizaciones")
@CompoundIndex(name = "idx_visual_evolucion", def = "{ 'fecha': -1, 'figuraId': 1 }")
public class Visualizacion {
    @Id
    String id;
    String usuarioId;

    // countByFiguraId, $match figuraId ($in) en el group del detalle/dashboard, y el
    // multikey del catálogo cuando hay pagina de visualizaciones.
    @Indexed(name = "idx_visual_figura")
    String figuraId;
    // fechaCreacion? El campo se llama fecha (tendring 30d / evolución mensual):
    @Indexed(name = "idx_visual_fecha")
    LocalDateTime fecha;
}
