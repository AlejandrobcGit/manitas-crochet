package com.manitascrochet.backend.repository;

import java.util.Optional;

import org.springframework.data.mongodb.repository.MongoRepository;

import com.manitascrochet.backend.model.TokenVerificacion;

public interface TokenVerificacionRepository extends MongoRepository<TokenVerificacion, String> {
    Optional<TokenVerificacion> findByToken(String token);
}
