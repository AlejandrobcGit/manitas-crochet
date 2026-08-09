package com.manitascrochet.backend.repository;

import java.util.List;

import org.springframework.data.mongodb.repository.MongoRepository;

import com.manitascrochet.backend.model.Favorito;

public interface FavoritoRepository extends MongoRepository<Favorito, String> {
    Favorito findByUsuarioIdAndFiguraIdAndActivoTrue (String usuario,String figuraId);
    List<Favorito> findByUsuarioIdAndActivoTrue(String usuarioId);
}