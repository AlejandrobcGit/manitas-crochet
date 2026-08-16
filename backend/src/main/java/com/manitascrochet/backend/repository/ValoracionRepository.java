package com.manitascrochet.backend.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.mongodb.repository.MongoRepository;

import com.manitascrochet.backend.model.Valoracion;

public interface ValoracionRepository extends MongoRepository<Valoracion, String> {

    Optional<Valoracion> findByUsuarioIdAndFiguraId(
            String usuarioId,
            String figuraId);

    List<Valoracion> findByFiguraId(String figuraId);
    void deleteByFiguraId(String figuraId);
    long countByFiguraId(String figuraId);
}