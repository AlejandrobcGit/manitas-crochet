package com.manitascrochet.backend.repository;

import org.springframework.data.mongodb.repository.MongoRepository;

import com.manitascrochet.backend.model.Visualizacion;

public interface VisualizacionRepository extends MongoRepository<Visualizacion, String> {

}
