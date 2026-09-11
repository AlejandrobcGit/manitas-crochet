package com.manitascrochet.backend.repository;

import java.util.List;

import java.util.List;

import org.springframework.data.mongodb.repository.MongoRepository;

import com.manitascrochet.backend.model.Figura;

public interface FiguraRepository extends MongoRepository<Figura, String> {
    Boolean existsByCategoriaId ( String categoriaId);
    Boolean existsByColoresIdsContaining(String colorId);
    // MEJORA 3: figuras pendientes de backfill de métricas denormalizadas
    // (solo las que aún no tienen puntuacionMedia) para el arranque inicial.
    List<Figura> findByPuntuacionMediaIsNull();
}