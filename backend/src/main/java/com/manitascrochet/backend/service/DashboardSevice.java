package com.manitascrochet.backend.service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.bson.Document;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationResults;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.stereotype.Service;

import com.manitascrochet.backend.dto.DashboardResponseDto;
import com.manitascrochet.backend.dto.Top10;
import com.manitascrochet.backend.model.Comentario;
import com.manitascrochet.backend.model.Favorito;
import com.manitascrochet.backend.model.Valoracion;
import com.manitascrochet.backend.model.Visualizacion;
import com.manitascrochet.backend.repository.ComentarioRepository;
import com.manitascrochet.backend.repository.FavoritoRepository;
import com.manitascrochet.backend.repository.FiguraRepository;
import com.manitascrochet.backend.repository.VisualizacionRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class DashboardSevice {

        private final FiguraRepository figuraRepository;
        private final VisualizacionRepository visualizacionRepository;
        private final FavoritoRepository favoritoRepository;
        private final ComentarioRepository comentarioRepository;
        private final MongoTemplate mongoTemplate;

        public DashboardResponseDto getKpis() {

                Double puntuacionPromedio = obtenerPuntuacionPromedio();

                List<Top10> top10Visualizaciones = enriquecer(
                                        agruparYOrdenar(Visualizacion.class, "figuraId", "visualizacion"));

                        List<Top10> top10Favoritos = enriquecer(
                                        agruparYOrdenar(Favorito.class, "figuraId", "favoritos"));

                        List<Top10> top10Comentarios = enriquecer(
                                        agruparYOrdenar(Comentario.class, "figuraId", "comentarios"));

                        // Este ya viene de otra colección/campo distinto (puntuacion en vez de conteo)
                        List<Top10> top10Valoracion = enriquecer(top10PorValoracion());

                List<Document> trending = trendingFigures();

                trending.forEach(doc -> {

                        String figuraId = doc.getString("_id");

                        figuraRepository.findById(figuraId)
                                        .ifPresent(figura -> doc.put("figura", figura.getNombre()));
                        doc.remove("_id");
                });

                // Calculo de evoluciones mensuales

                List<Document> evolucionVisualizaciones = evolucionMensual(
                                Visualizacion.class,
                                "visualizaciones",
                                "fecha");

                List<Document> evolucionFavoritos = evolucionMensual(
                                Favorito.class,
                                "favoritos",
                                "fechaAlta",
                                Criteria.where("activo").is(true));

                List<Document> evolucionComentarios = evolucionMensual(
                                Comentario.class,
                                "comentarios",
                                "fechaModificacion");

                List<Document> evolucionValoraciones = evolucionMensual(
                                Valoracion.class,
                                "valoraciones",
                                "fecha");

                return new DashboardResponseDto(
                                figuraRepository.count(),
                                visualizacionRepository.count(),
                                favoritoRepository.count(),
                                comentarioRepository.count(),
                                Math.round(puntuacionPromedio * 100) / (double) 100,
                                top10Visualizaciones,
                                top10Favoritos,
                                top10Valoracion,
                                top10Comentarios,
                                trending,
                                evolucionVisualizaciones,
                                evolucionFavoritos,
                                evolucionComentarios,
                                evolucionValoraciones);
        }

        // ---------- Agregaciones "count por figuraId", genérica para Visualizacion y
        // Favorito ----------

        private <T> List<Document> agruparYOrdenar(
                        Class<T> coleccion,
                        String campoAgrupacion,
                        String alias) {

                Aggregation aggregation = Aggregation.newAggregation(
                                Aggregation.group(campoAgrupacion)
                                                .count()
                                                .as(alias),

                                Aggregation.sort(
                                                Sort.by(
                                                                Sort.Order.desc(alias),
                                                                Sort.Order.asc("_id"))),

                                Aggregation.limit(10));

                return mongoTemplate.aggregate(
                                aggregation,
                                coleccion,
                                Document.class)
                                .getMappedResults();
        }

        // ---------- Top 10 por promedio de valoración (distinto: es un avg, no un
        // count) ----------

        private List<Document> top10PorValoracion() {

                Aggregation aggregation = Aggregation.newAggregation(
                                Aggregation.group("figuraId")
                                                .avg("puntuacion").as("valoracion")
                                                .count().as("numValoraciones"),

                                Aggregation.match(
                                                Criteria.where("numValoraciones").gt(2)),

                                Aggregation.sort(
                                                Sort.by(
                                                                Sort.Order.desc("valoracion"),
                                                                Sort.Order.asc("_id"))),

                                Aggregation.limit(10));

                return mongoTemplate.aggregate(
                                aggregation,
                                Valoracion.class,
                                Document.class)
                                .getMappedResults();
        }

        // ---------- Top 10 por vistas los ultimos 30 días

        private List<Document> trendingFigures() {

                Aggregation aggregation = Aggregation.newAggregation(

                                Aggregation.match(
                                                Criteria.where("fecha")
                                                                .gte(LocalDateTime.now().minusDays(30))),

                                Aggregation.group("figuraId")
                                                .count()
                                                .as("visualizaciones"),

                                Aggregation.sort(
                                                Sort.by(
                                                                Sort.Order.desc("visualizaciones"),
                                                                Sort.Order.asc("_id"))),

                                Aggregation.limit(10));

                return mongoTemplate.aggregate(
                                aggregation,
                                Visualizacion.class,
                                Document.class)
                                .getMappedResults();
        }

        // ----------- Evoluciones mensuales

        private <T> List<Document> evolucionMensual(
                        Class<T> coleccion,
                        String alias,
                        String campoFecha,
                        Criteria... filtrosExtra) {

                LocalDateTime periodo = LocalDateTime.now().minusMonths(3);

                List<Criteria> criterios = new ArrayList<>();

                criterios.add(
                                Criteria.where(campoFecha)
                                                .gte(periodo));

                Collections.addAll(criterios, filtrosExtra);

                Aggregation aggregation = Aggregation.newAggregation(

                                Aggregation.match(
                                                new Criteria().andOperator(
                                                                criterios.toArray(new Criteria[0]))),

                                Aggregation.project()
                                                .and(campoFecha).extractYear().as("anio")
                                                .and(campoFecha).extractMonth().as("mes"),

                                Aggregation.group("anio", "mes")
                                                .count()
                                                .as(alias),

                                Aggregation.sort(
                                                Sort.Direction.ASC,
                                                "_id.anio",
                                                "_id.mes"));

                List<Document> resultados = mongoTemplate.aggregate(
                                aggregation,
                                coleccion,
                                Document.class)
                                .getMappedResults();

                resultados.forEach(doc -> {

                        Document id = (Document) doc.get("_id");

                        Integer anio = id.getInteger("anio");
                        Integer mes = id.getInteger("mes");

                        doc.put(
                                        "periodo",
                                        anio + "-" + String.format("%02d", mes));

                        doc.remove("_id");
                });

                return resultados;
        }

        // ---------- Enriquecimiento común: agrega nombre + las 3 métricas que falten
        // ----------

        private List<Top10> enriquecer(List<Document> docs) {

                return docs.stream()
                                .map(doc -> {
                                        String figuraId = doc.getString("_id");

                                        String nombre = figuraRepository.findById(figuraId)
                                                        .map(f -> f.getNombre())
                                                        .orElse("Desconocido");

                                        long visualizaciones = doc.containsKey("visualizacion")
                                                        ? ((Number) doc.get("visualizacion")).longValue()
                                                        : visualizacionRepository.countByFiguraId(figuraId);

                                        long favoritos = doc.containsKey("favoritos")
                                                        ? ((Number) doc.get("favoritos")).longValue()
                                                        : favoritoRepository.countByFiguraId(figuraId);

                                        long comentarios = doc.containsKey("comentarios")
                                                        ? ((Number) doc.get("comentarios")).longValue()
                                                        : comentarioRepository.countByFiguraId(figuraId);

                                        double valoracion = doc.containsKey("valoracion")
                                                        ? Math.round(((Number) doc.get("valoracion")).doubleValue()
                                                                        * 100) / 100.0
                                                        : Math.round(obtenerPromedioValoracionPorFigura(figuraId) * 100)
                                                                        / 100.0;

                                        return new Top10(nombre, visualizaciones, favoritos, comentarios, valoracion);
                                })
                                .toList();
        }

        private Double obtenerPuntuacionPromedio() {
                Aggregation aggregation = Aggregation.newAggregation(
                                Aggregation.group().avg("puntuacion").as("promedio"));

                Document result = mongoTemplate.aggregate(aggregation, Valoracion.class, Document.class)
                                .getUniqueMappedResult();

                if (result == null)
                        return 0.0;
                Double promedio = result.getDouble("promedio");
                return promedio != null ? promedio : 0.0;
        }

        private Double obtenerPromedioValoracionPorFigura(String figuraId) {
                Aggregation aggregation = Aggregation.newAggregation(
                                Aggregation.match(Criteria.where("figuraId").is(figuraId)),
                                Aggregation.group().avg("puntuacion").as("promedio"));

                AggregationResults<Document> results = mongoTemplate.aggregate(aggregation, "valoraciones",
                                Document.class);

                if (results == null)
                        return 0.0;

                Document document = results.getUniqueMappedResult();

                return document != null ? document.getDouble("promedio") : 0.0;
        }
}