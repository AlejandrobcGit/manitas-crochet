package com.manitascrochet.backend.repository;

import org.springframework.data.mongodb.repository.MongoRepository;

import com.manitascrochet.backend.model.Figura;

public interface FiguraRepository extends MongoRepository<Figura, String> {

}