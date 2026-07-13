package com.manitascrochet.backend.repository;

import java.util.Optional;

import org.springframework.data.mongodb.repository.MongoRepository;

import com.manitascrochet.backend.model.Color;

public interface ColorRepository extends MongoRepository<Color, String> {
    Optional<Color> findByCodigoIgnoreCase(String codigo);
    Optional<Color> findByNombreIgnoreCase(String nombre);
}