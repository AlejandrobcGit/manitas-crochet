package com.manitascrochet.backend.model;

import java.time.LocalDateTime;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.CompoundIndexes;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import lombok.Data;

@Document(collection = "valoraciones")
@Data
@CompoundIndexes({
                // Valoración única por (usuario, figura) en upsert y evita colisiones al consultar.
                @CompoundIndex(name = "idx_val_usuario_figura", def = "{ 'usuarioId': 1, 'figuraId': 1 }")
})
public class Valoracion {

    @Id
    private String id;

    private String usuarioId;

    // findById + findByIdIn + count + delete por figura (detalle, catálogo y dashboard)
    @Indexed(name = "idx_val_figura")
    private String figuraId;

    private Integer puntuacion;

    private LocalDateTime fecha;
}