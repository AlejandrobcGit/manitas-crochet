package com.manitascrochet.backend.model;

import java.time.LocalDateTime;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.CompoundIndexes;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import lombok.Data;

@Data
@Document(collection = "favoritos")
@CompoundIndexes({
                // favoritoRepository.findByUsuarioIdAndActivoTrue (página favoritos)
                @CompoundIndex(name = "idx_fav_usuario_activo", def = "{ 'usuarioId': 1, 'activo': 1 }"),
                // findByUsuarioIdAndFiguraIdInAndActivoTrue (batch $in del catálogo/detalle)
                @CompoundIndex(name = "idx_fav_usuario_figura_activo", def = "{ 'usuarioId': 1, 'figuraId': 1, 'activo': 1 }")
})
public class Favorito {

    @Id
    private String id;

    private String usuarioId;

    @Indexed(name = "idx_fav_figura")
    private String figuraId;

    private LocalDateTime fechaAlta;

    private LocalDateTime fechaBaja;

    private boolean activo;
}