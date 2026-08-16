package com.manitascrochet.backend.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.mongodb.repository.MongoRepository;

import com.manitascrochet.backend.model.Comentario;

public interface ComentarioRepository extends MongoRepository<Comentario, String> {
    List<Comentario> findByFiguraIdOrderByFechaCreacionDesc(String figuraId);
    Optional<Comentario> findByIdAndUsuarioId(String id, String usuarioId);
    Optional<Comentario> findByUsuarioIdAndFiguraId(String usuarioId, String figuraId);
    void deleteByFiguraId(String figuraId);
    long countByFiguraId(String figuraId);
}