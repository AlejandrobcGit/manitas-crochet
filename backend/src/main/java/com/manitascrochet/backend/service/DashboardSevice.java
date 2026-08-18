package com.manitascrochet.backend.service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.stream.Collectors;

import org.bson.Document;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.stereotype.Service;

import com.manitascrochet.backend.dto.DashboardResponseDto;
import com.manitascrochet.backend.dto.Top10;
import com.manitascrochet.backend.model.Comentario;
import com.manitascrochet.backend.model.Favorito;
import com.manitascrochet.backend.model.Figura;
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

        // Bean definido en DashboardAsyncConfig (ver archivo adjunto).
        // Si no querés paralelizar, podés quitar este campo y llamar los métodos
        // de forma secuencial como en la versión original.
        @Qualifier("dashboardExecutor")
        private final Executor dashboardExecutor;

        @Cacheable(value = "dashboardKpis", key = "'kpis'")
        public DashboardResponseDto getKpis() {

                // ---------- Se disparan todas las agregaciones en paralelo ----------

                CompletableFuture<List<Top10>> fTop10Visualizaciones = CompletableFuture.supplyAsync(
                                () -> enriquecer(agruparYOrdenar(Visualizacion.class, "figuraId", "visualizacion")),
                                dashboardExecutor);

                CompletableFuture<List<Top10>> fTop10Favoritos = CompletableFuture.supplyAsync(
                                () -> enriquecer(agruparYOrdenar(Favorito.class, "figuraId", "favoritos")),
                                dashboardExecutor);

                CompletableFuture<List<Top10>> fTop10Comentarios = CompletableFuture.supplyAsync(
                                () -> enriquecer(agruparYOrdenar(Comentario.class, "figuraId", "comentarios")),
                                dashboardExecutor);

                CompletableFuture<List<Top10>> fTop10Valoracion = CompletableFuture.supplyAsync(
                                () -> enriquecer(top10PorValoracion()),
                                dashboardExecutor);

                CompletableFuture<List<Document>> fTrending = CompletableFuture.supplyAsync(
                                this::trendingFigurasConNombre, dashboardExecutor);

                CompletableFuture<List<Document>> fEvolucionVisualizaciones = CompletableFuture.supplyAsync(
                                () -> evolucionMensual(Visualizacion.class, "visualizaciones", "fecha"),
                                dashboardExecutor);

                CompletableFuture<List<Document>> fEvolucionFavoritos = CompletableFuture.supplyAsync(
                                () -> evolucionMensual(Favorito.class, "favoritos", "fechaAlta",
                                                Criteria.where("activo").is(true)),
                                dashboardExecutor);

                CompletableFuture<List<Document>> fEvolucionComentarios = CompletableFuture.supplyAsync(
                                () -> evolucionMensual(Comentario.class, "comentarios", "fechaModificacion"),
                                dashboardExecutor);

                CompletableFuture<List<Document>> fEvolucionValoraciones = CompletableFuture.supplyAsync(
                                () -> evolucionMensual(Valoracion.class, "valoraciones", "fecha"),
                                dashboardExecutor);

                CompletableFuture<Double> fPuntuacionPromedio = CompletableFuture.supplyAsync(
                                this::obtenerPuntuacionPromedio, dashboardExecutor);

                CompletableFuture<Long> fCountFiguras = CompletableFuture.supplyAsync(
                                figuraRepository::count, dashboardExecutor);
                CompletableFuture<Long> fCountVisualizaciones = CompletableFuture.supplyAsync(
                                visualizacionRepository::count, dashboardExecutor);
                CompletableFuture<Long> fCountFavoritos = CompletableFuture.supplyAsync(
                                favoritoRepository::count, dashboardExecutor);
                CompletableFuture<Long> fCountComentarios = CompletableFuture.supplyAsync(
                                comentarioRepository::count, dashboardExecutor);

                CompletableFuture.allOf(
                                fTop10Visualizaciones, fTop10Favoritos, fTop10Comentarios, fTop10Valoracion,
                                fTrending, fEvolucionVisualizaciones, fEvolucionFavoritos, fEvolucionComentarios,
                                fEvolucionValoraciones, fPuntuacionPromedio,
                                fCountFiguras, fCountVisualizaciones, fCountFavoritos, fCountComentarios)
                                .join();

                Double puntuacionPromedio = fPuntuacionPromedio.join();

                return new DashboardResponseDto(
                                fCountFiguras.join(),
                                fCountVisualizaciones.join(),
                                fCountFavoritos.join(),
                                fCountComentarios.join(),
                                redondear(puntuacionPromedio),
                                fTop10Visualizaciones.join(),
                                fTop10Favoritos.join(),
                                fTop10Valoracion.join(),
                                fTop10Comentarios.join(),
                                fTrending.join(),
                                fEvolucionVisualizaciones.join(),
                                fEvolucionFavoritos.join(),
                                fEvolucionComentarios.join(),
                                fEvolucionValoraciones.join());
        }

        // ---------- Agregaciones "count por figuraId", genérica para Visualizacion,
        // Favorito y Comentario ----------

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

        // ---------- Top 10 por vistas los últimos 30 días, con nombre resuelto en
        // batch (antes: 1 findById por cada uno de los 10 resultados) ----------

        private List<Document> trendingFigurasConNombre() {

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

                List<Document> trending = mongoTemplate.aggregate(
                                aggregation,
                                Visualizacion.class,
                                Document.class)
                                .getMappedResults();

                if (trending.isEmpty()) {
                        return trending;
                }

                List<String> ids = trending.stream().map(d -> d.getString("_id")).toList();
                Map<String, String> nombresPorId = obtenerNombres(ids);

                trending.forEach(doc -> {
                        String figuraId = doc.getString("_id");
                        doc.put("figura", nombresPorId.getOrDefault(figuraId, "Desconocido"));
                        doc.remove("_id");
                });

                return trending;
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

        // ---------- Enriquecimiento común: agrega nombre + las 3 métricas que
        // falten, todo resuelto en batch (antes: hasta 4 queries por CADA una de
        // las 10 figuras, ahora: como máximo 5 queries en total por lista) ----------

        private List<Top10> enriquecer(List<Document> docs) {

                if (docs.isEmpty()) {
                        return List.of();
                }

                List<String> ids = docs.stream().map(d -> d.getString("_id")).toList();

                Map<String, String> nombresPorId = obtenerNombres(ids);

                boolean faltaVisualizaciones = docs.stream().noneMatch(d -> d.containsKey("visualizacion"));
                boolean faltaFavoritos = docs.stream().noneMatch(d -> d.containsKey("favoritos"));
                boolean faltaComentarios = docs.stream().noneMatch(d -> d.containsKey("comentarios"));
                boolean faltaValoracion = docs.stream().noneMatch(d -> d.containsKey("valoracion"));

                Map<String, Long> visualizacionesPorId = faltaVisualizaciones
                                ? contarPorFiguraId(Visualizacion.class, ids)
                                : Map.of();

                Map<String, Long> favoritosPorId = faltaFavoritos
                                ? contarPorFiguraId(Favorito.class, ids)
                                : Map.of();

                Map<String, Long> comentariosPorId = faltaComentarios
                                ? contarPorFiguraId(Comentario.class, ids)
                                : Map.of();

                Map<String, Double> valoracionPorId = faltaValoracion
                                ? promedioValoracionPorFiguraIds(ids)
                                : Map.of();

                return docs.stream()
                                .map(doc -> {
                                        String figuraId = doc.getString("_id");

                                        String nombre = nombresPorId.getOrDefault(figuraId, "Desconocido");

                                        long visualizaciones = doc.containsKey("visualizacion")
                                                        ? ((Number) doc.get("visualizacion")).longValue()
                                                        : visualizacionesPorId.getOrDefault(figuraId, 0L);

                                        long favoritos = doc.containsKey("favoritos")
                                                        ? ((Number) doc.get("favoritos")).longValue()
                                                        : favoritosPorId.getOrDefault(figuraId, 0L);

                                        long comentarios = doc.containsKey("comentarios")
                                                        ? ((Number) doc.get("comentarios")).longValue()
                                                        : comentariosPorId.getOrDefault(figuraId, 0L);

                                        double valoracion = doc.containsKey("valoracion")
                                                        ? redondear(((Number) doc.get("valoracion")).doubleValue())
                                                        : redondear(valoracionPorId.getOrDefault(figuraId, 0.0));

                                        return new Top10(nombre, visualizaciones, favoritos, comentarios, valoracion);
                                })
                                .toList();
        }

        // ---------- Helpers de batch fetching ----------

        private Map<String, String> obtenerNombres(List<String> figuraIds) {
                return figuraRepository.findAllById(figuraIds).stream()
                                .collect(Collectors.toMap(Figura::getId, Figura::getNombre));
        }

        private <T> Map<String, Long> contarPorFiguraId(Class<T> coleccion, List<String> figuraIds) {

                Aggregation aggregation = Aggregation.newAggregation(
                                Aggregation.match(Criteria.where("figuraId").in(figuraIds)),
                                Aggregation.group("figuraId").count().as("total"));

                List<Document> resultados = mongoTemplate.aggregate(
                                aggregation,
                                coleccion,
                                Document.class)
                                .getMappedResults();

                return resultados.stream()
                                .collect(Collectors.toMap(
                                                d -> d.getString("_id"),
                                                d -> ((Number) d.get("total")).longValue()));
        }

        private Map<String, Double> promedioValoracionPorFiguraIds(List<String> figuraIds) {

                Aggregation aggregation = Aggregation.newAggregation(
                                Aggregation.match(Criteria.where("figuraId").in(figuraIds)),
                                Aggregation.group("figuraId").avg("puntuacion").as("promedio"));

                List<Document> resultados = mongoTemplate.aggregate(
                                aggregation,
                                Valoracion.class,
                                Document.class)
                                .getMappedResults();

                return resultados.stream()
                                .collect(Collectors.toMap(
                                                d -> d.getString("_id"),
                                                d -> {
                                                        Double promedio = d.getDouble("promedio");
                                                        return promedio != null ? promedio : 0.0;
                                                }));
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

        private double redondear(double valor) {
                return Math.round(valor * 100) / 100.0;
        }
}
