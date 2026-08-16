package com.manitascrochet.backend.dto;

import java.util.List;

import org.bson.Document;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class DashboardResponseDto {

    // KPIs generales

    long totalFigures;

    long totalViews;

    long totalFavorites;

    long totalComments;

    double averageRating;

    List<Top10> top10View;

    List<Top10> top10Favoritos;

    List<Top10> top10Valoraciones;

    List<Top10> top10Comentarios;

    List<Document> trendingFigures;

    List<Document> evolucionVisualizaciones;

    List<Document> evolucionFavoritos;

    List<Document> evolucionComentarios;

    List<Document> evolucionValoraciones;


}
