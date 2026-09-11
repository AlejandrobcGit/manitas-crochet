package com.manitascrochet.backend.repository;

import java.util.List;

import org.springframework.data.mongodb.repository.Aggregation;
import org.springframework.data.mongodb.repository.MongoRepository;

import com.manitascrochet.backend.dto.VisualizacionesPorFiguraDto;
import com.manitascrochet.backend.model.Visualizacion;

public interface VisualizacionRepository extends MongoRepository<Visualizacion, String> {
    long countByFiguraId(String figuraId);

    @Aggregation(pipeline = {
            "{ $match: { 'figuraId': { $in: ?0 } } }",
            "{ $group: { _id: '$figuraId', total: { $sum: 1 } } }" })
    List<VisualizacionesPorFiguraDto> contarAgrupadasPorFiguraId(List<String> figuraIds);
}
