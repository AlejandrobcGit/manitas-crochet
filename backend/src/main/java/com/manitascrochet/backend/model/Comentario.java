package com.manitascrochet.backend.model;

import java.time.LocalDateTime;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.CompoundIndexes;
import org.springframework.data.mongodb.core.mapping.Document;

import lombok.Data;

@Data
@Document(collection = "comentarios")
@CompoundIndexes({
                // findByFiguraIdOrderByFechaCreacionDesc (hilo de comentarios del detalle).
                // El prefijo { figuraId: 1 } también cubre countByFiguraId, deleteByFiguraId
                // y el count agrupado del dashboard.
                @CompoundIndex(name = "idx_co_figura_fecha", def = "{ 'figuraId': 1, 'fechaCreacion': -1 }"),
                // findByUsuarioIdAndFiguraId / existsByUsuarioIdAndFiguraId (comprobación
                // antes de crear/editar comentario) y findByIdAndUsuarioId (propietario).
                @CompoundIndex(name = "idx_co_usuario_figura", def = "{ 'usuarioId': 1, 'figuraId': 1 }")
})
public class Comentario {

    @Id
    String id;

    String usuarioId;

    String figuraId;

    String comentario;

    LocalDateTime fechaCreacion;

    LocalDateTime fechaModificacion;
}
