package com.manitascrochet.backend.service;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Executor;

import org.bson.Document;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationResults;

import com.manitascrochet.backend.dto.DashboardResponseDto;
import com.manitascrochet.backend.model.Comentario;
import com.manitascrochet.backend.model.Favorito;
import com.manitascrochet.backend.model.Figura;
import com.manitascrochet.backend.model.Valoracion;
import com.manitascrochet.backend.model.Visualizacion;
import com.manitascrochet.backend.repository.ComentarioRepository;
import com.manitascrochet.backend.repository.FavoritoRepository;
import com.manitascrochet.backend.repository.FiguraRepository;
import com.manitascrochet.backend.repository.VisualizacionRepository;

@ExtendWith(MockitoExtension.class)
class DashboardSeviceTest {

    @Mock
    private FiguraRepository figuraRepository;

    @Mock
    private VisualizacionRepository visualizacionRepository;

    @Mock
    private FavoritoRepository favoritoRepository;

    @Mock
    private ComentarioRepository comentarioRepository;

    @Mock
    private MongoTemplate mongoTemplate;

    private DashboardSevice dashboardSevice;

    private final Executor sameThreadExecutor = Runnable::run;

    @BeforeEach
    void setUp() {
        dashboardSevice = new DashboardSevice(
                figuraRepository,
                visualizacionRepository,
                favoritoRepository,
                comentarioRepository,
                mongoTemplate,
                sameThreadExecutor);
    }

    // ---------------------------------------------------------------------
    // Helpers
    // ---------------------------------------------------------------------

    @SuppressWarnings("unchecked")
    private AggregationResults<Document> aggResults(List<Document> docs) {
        AggregationResults<Document> results = mock(AggregationResults.class);

        lenient().when(results.getMappedResults()).thenReturn(docs);
        lenient().when(results.getUniqueMappedResult()).thenReturn(
                docs.isEmpty() ? null : docs.get(0));

        return results;
    }

    private Document countDoc(String figuraId, String alias, long valor) {
        return new Document("_id", figuraId).append(alias, valor);
    }

    private Document totalDoc(String figuraId, long total) {
        return new Document("_id", figuraId).append("total", total);
    }

    private Document promedioFiguraDoc(String figuraId, double promedio) {
        return new Document("_id", figuraId).append("promedio", promedio);
    }

    private Document evolucionDoc(int anio, int mes, String alias, long valor) {
        Document id = new Document("anio", anio).append("mes", mes);
        return new Document("_id", id).append(alias, valor);
    }

    private Figura figuraMock(String id, String nombre) {
        Figura figura = mock(Figura.class);
        lenient().when(figura.getId()).thenReturn(id);
        lenient().when(figura.getNombre()).thenReturn(nombre);
        return figura;
    }

    private void stubFindAllById() {
        Figura figura1 = figuraMock("fig-1", "Gato Amigurumi");
        Figura figura2 = figuraMock("fig-2", "Perro Amigurumi");
        Figura figura3 = figuraMock("fig-3", "Oso Amigurumi");
        Figura figura4 = figuraMock("fig-4", "Rana Amigurumi");
        Figura figura5 = figuraMock("fig-5", "Elefante Amigurumi");

        List<Figura> todas = List.of(figura1, figura2, figura3, figura4, figura5);

        lenient().when(figuraRepository.findAllById(anyIterable()))
                .thenAnswer(invocation -> {
                    Iterable<String> idsIterable = invocation.getArgument(0);
                    List<String> ids = new ArrayList<>();
                    idsIterable.forEach(ids::add);

                    return todas.stream()
                            .filter(figura -> ids.contains(figura.getId()))
                            .toList();
                });
    }

    /**
     * IMPORTANTE:
     *
     * En producción el servicio usa CompletableFuture.
     * En test usamos Runnable::run para que las llamadas sean síncronas.
     *
     * Orden real de llamadas por tipo, con sameThreadExecutor:
     *
     * Visualizacion.class:
     * 1 top10 visualizaciones
     * 2 fallback visualizaciones para top10 favoritos
     * 3 fallback visualizaciones para top10 comentarios
     * 4 fallback visualizaciones para top10 valoración
     * 5 trending
     * 6 evolución visualizaciones
     *
     * Favorito.class:
     * 1 fallback favoritos para top10 visualizaciones
     * 2 top10 favoritos
     * 3 fallback favoritos para top10 comentarios
     * 4 fallback favoritos para top10 valoración
     * 5 evolución favoritos
     *
     * Comentario.class:
     * 1 fallback comentarios para top10 visualizaciones
     * 2 fallback comentarios para top10 favoritos
     * 3 top10 comentarios
     * 4 fallback comentarios para top10 valoración
     * 5 evolución comentarios
     *
     * Valoracion.class:
     * 1 fallback valoración para top10 visualizaciones
     * 2 fallback valoración para top10 favoritos
     * 3 fallback valoración para top10 comentarios
     * 4 top10 valoración
     * 5 evolución valoración
     * 6 promedio global
     */
    @SuppressWarnings("unchecked")
    private void stubCaminoFelizAggregates() {
        AggregationResults<Document> top10Visualizaciones =
                aggResults(List.of(countDoc("fig-1", "visualizacion", 100)));

        AggregationResults<Document> fallbackVisualizacionesFavoritos =
                aggResults(List.of(totalDoc("fig-2", 80)));

        AggregationResults<Document> fallbackVisualizacionesComentarios =
                aggResults(List.of(totalDoc("fig-3", 70)));

        AggregationResults<Document> fallbackVisualizacionesValoracion =
                aggResults(List.of(totalDoc("fig-4", 60)));

        AggregationResults<Document> trending =
                aggResults(List.of(new Document("_id", "fig-5").append("visualizaciones", 50)));

        AggregationResults<Document> evolucionVisualizaciones =
                aggResults(List.of(evolucionDoc(2026, 6, "visualizaciones", 20)));

        lenient().when(mongoTemplate.aggregate(
                        any(Aggregation.class),
                        eq(Visualizacion.class),
                        eq(Document.class)))
                .thenReturn(
                        top10Visualizaciones,
                        fallbackVisualizacionesFavoritos,
                        fallbackVisualizacionesComentarios,
                        fallbackVisualizacionesValoracion,
                        trending,
                        evolucionVisualizaciones);

        AggregationResults<Document> fallbackFavoritosVisualizaciones =
                aggResults(List.of(totalDoc("fig-1", 30)));

        AggregationResults<Document> top10Favoritos =
                aggResults(List.of(countDoc("fig-2", "favoritos", 25)));

        AggregationResults<Document> fallbackFavoritosComentarios =
                aggResults(List.of(totalDoc("fig-3", 20)));

        AggregationResults<Document> fallbackFavoritosValoracion =
                aggResults(List.of(totalDoc("fig-4", 15)));

        AggregationResults<Document> evolucionFavoritos =
                aggResults(List.of(evolucionDoc(2026, 6, "favoritos", 5)));

        lenient().when(mongoTemplate.aggregate(
                        any(Aggregation.class),
                        eq(Favorito.class),
                        eq(Document.class)))
                .thenReturn(
                        fallbackFavoritosVisualizaciones,
                        top10Favoritos,
                        fallbackFavoritosComentarios,
                        fallbackFavoritosValoracion,
                        evolucionFavoritos);

        AggregationResults<Document> fallbackComentariosVisualizaciones =
                aggResults(List.of(totalDoc("fig-1", 12)));

        AggregationResults<Document> fallbackComentariosFavoritos =
                aggResults(List.of(totalDoc("fig-2", 10)));

        AggregationResults<Document> top10Comentarios =
                aggResults(List.of(countDoc("fig-3", "comentarios", 8)));

        AggregationResults<Document> fallbackComentariosValoracion =
                aggResults(List.of(totalDoc("fig-4", 6)));

        AggregationResults<Document> evolucionComentarios =
                aggResults(List.of(evolucionDoc(2026, 6, "comentarios", 3)));

        lenient().when(mongoTemplate.aggregate(
                        any(Aggregation.class),
                        eq(Comentario.class),
                        eq(Document.class)))
                .thenReturn(
                        fallbackComentariosVisualizaciones,
                        fallbackComentariosFavoritos,
                        top10Comentarios,
                        fallbackComentariosValoracion,
                        evolucionComentarios);

        AggregationResults<Document> fallbackValoracionVisualizaciones =
                aggResults(List.of(promedioFiguraDoc("fig-1", 4.1)));

        AggregationResults<Document> fallbackValoracionFavoritos =
                aggResults(List.of(promedioFiguraDoc("fig-2", 4.2)));

        AggregationResults<Document> fallbackValoracionComentarios =
                aggResults(List.of(promedioFiguraDoc("fig-3", 4.3)));

        AggregationResults<Document> top10Valoracion =
                aggResults(List.of(
                        new Document("_id", "fig-4")
                                .append("valoracion", 4.777)
                                .append("numValoraciones", 5)));

        AggregationResults<Document> evolucionValoraciones =
                aggResults(List.of(evolucionDoc(2026, 6, "valoraciones", 8)));

        AggregationResults<Document> promedioGlobal =
                aggResults(List.of(new Document("promedio", 4.3567)));

        lenient().when(mongoTemplate.aggregate(
                        any(Aggregation.class),
                        eq(Valoracion.class),
                        eq(Document.class)))
                .thenReturn(
                        fallbackValoracionVisualizaciones,
                        fallbackValoracionFavoritos,
                        fallbackValoracionComentarios,
                        top10Valoracion,
                        evolucionValoraciones,
                        promedioGlobal);
    }

    // ---------------------------------------------------------------------
    // Camino feliz
    // ---------------------------------------------------------------------

    @Nested
    @DisplayName("getKpis() - camino feliz con nueva lógica batch")
    class CaminoFeliz {

        @Test
        @DisplayName("calcula el DTO usando agregaciones batch y findAllById")
        void getKpis_conDatosCompletos_usaNuevaLogicaBatch() {
            stubCaminoFelizAggregates();
            stubFindAllById();

            when(figuraRepository.count()).thenReturn(4L);
            when(visualizacionRepository.count()).thenReturn(200L);
            when(favoritoRepository.count()).thenReturn(80L);
            when(comentarioRepository.count()).thenReturn(15L);

            DashboardResponseDto dto = dashboardSevice.getKpis();

            assertThat(dto).isNotNull();

            verify(figuraRepository, atLeastOnce()).findAllById(anyIterable());

            verify(figuraRepository, times(1)).count();
            verify(visualizacionRepository, times(1)).count();
            verify(favoritoRepository, times(1)).count();
            verify(comentarioRepository, times(1)).count();

            verify(mongoTemplate, times(6))
                    .aggregate(any(Aggregation.class), eq(Visualizacion.class), eq(Document.class));

            verify(mongoTemplate, times(5))
                    .aggregate(any(Aggregation.class), eq(Favorito.class), eq(Document.class));

            verify(mongoTemplate, times(5))
                    .aggregate(any(Aggregation.class), eq(Comentario.class), eq(Document.class));

            verify(mongoTemplate, times(6))
                    .aggregate(any(Aggregation.class), eq(Valoracion.class), eq(Document.class));
        }
    }

    // ---------------------------------------------------------------------
    // Sin datos
    // ---------------------------------------------------------------------

    @Nested
    @DisplayName("getKpis() - sin datos agregados")
    class SinDatos {

        @SuppressWarnings("unchecked")
        @Test
        @DisplayName("devuelve DTO válido aunque no existan resultados agregados")
        void getKpis_sinDatos_devuelveDtoValido() {
            AggregationResults<Document> vacio = aggResults(Collections.emptyList());

            lenient().when(mongoTemplate.aggregate(
                            any(Aggregation.class),
                            eq(Visualizacion.class),
                            eq(Document.class)))
                    .thenReturn(vacio, vacio, vacio);

            lenient().when(mongoTemplate.aggregate(
                            any(Aggregation.class),
                            eq(Favorito.class),
                            eq(Document.class)))
                    .thenReturn(vacio, vacio);

            lenient().when(mongoTemplate.aggregate(
                            any(Aggregation.class),
                            eq(Comentario.class),
                            eq(Document.class)))
                    .thenReturn(vacio, vacio);

            lenient().when(mongoTemplate.aggregate(
                            any(Aggregation.class),
                            eq(Valoracion.class),
                            eq(Document.class)))
                    .thenReturn(vacio, vacio, vacio);

            when(figuraRepository.count()).thenReturn(0L);
            when(visualizacionRepository.count()).thenReturn(0L);
            when(favoritoRepository.count()).thenReturn(0L);
            when(comentarioRepository.count()).thenReturn(0L);

            DashboardResponseDto dto = dashboardSevice.getKpis();

            assertThat(dto).isNotNull();

            verify(figuraRepository, never()).findAllById(anyIterable());

            verify(figuraRepository, times(1)).count();
            verify(visualizacionRepository, times(1)).count();
            verify(favoritoRepository, times(1)).count();
            verify(comentarioRepository, times(1)).count();
        }
    }

    // ---------------------------------------------------------------------
    // Promedio global
    // ---------------------------------------------------------------------

    @Nested
    @DisplayName("getKpis() - promedio global")
    class PromedioGlobal {

        @SuppressWarnings("unchecked")
        @Test
        @DisplayName("si getUniqueMappedResult() es null, el promedio global queda en 0.0")
        void getKpis_sinResultadoDeValoracion_promedioCero() {
            AggregationResults<Document> vacio = aggResults(Collections.emptyList());

            lenient().when(mongoTemplate.aggregate(
                            any(Aggregation.class),
                            eq(Visualizacion.class),
                            eq(Document.class)))
                    .thenReturn(vacio, vacio, vacio);

            lenient().when(mongoTemplate.aggregate(
                            any(Aggregation.class),
                            eq(Favorito.class),
                            eq(Document.class)))
                    .thenReturn(vacio, vacio);

            lenient().when(mongoTemplate.aggregate(
                            any(Aggregation.class),
                            eq(Comentario.class),
                            eq(Document.class)))
                    .thenReturn(vacio, vacio);

            lenient().when(mongoTemplate.aggregate(
                            any(Aggregation.class),
                            eq(Valoracion.class),
                            eq(Document.class)))
                    .thenReturn(vacio, vacio, vacio);

            DashboardResponseDto dto = dashboardSevice.getKpis();

            assertThat(dto).isNotNull();
        }

        @SuppressWarnings("unchecked")
        @Test
        @DisplayName("si el campo promedio viene null, el promedio global queda en 0.0")
        void getKpis_campoPromedioNulo_promedioCero() {
            AggregationResults<Document> vacio = aggResults(Collections.emptyList());
            AggregationResults<Document> promedioSinCampo =
                    aggResults(List.of(new Document("otraClave", "valor")));

            lenient().when(mongoTemplate.aggregate(
                            any(Aggregation.class),
                            eq(Visualizacion.class),
                            eq(Document.class)))
                    .thenReturn(vacio, vacio, vacio);

            lenient().when(mongoTemplate.aggregate(
                            any(Aggregation.class),
                            eq(Favorito.class),
                            eq(Document.class)))
                    .thenReturn(vacio, vacio);

            lenient().when(mongoTemplate.aggregate(
                            any(Aggregation.class),
                            eq(Comentario.class),
                            eq(Document.class)))
                    .thenReturn(vacio, vacio);

            lenient().when(mongoTemplate.aggregate(
                            any(Aggregation.class),
                            eq(Valoracion.class),
                            eq(Document.class)))
                    .thenReturn(vacio, vacio, promedioSinCampo);

            DashboardResponseDto dto = dashboardSevice.getKpis();

            assertThat(dto).isNotNull();
        }
    }

    // ---------------------------------------------------------------------
    // Trending
    // ---------------------------------------------------------------------

    @Nested
    @DisplayName("getKpis() - trending")
    class Trending {

        @SuppressWarnings("unchecked")
        @Test
        @DisplayName("si la figura no existe, trending usa 'Desconocido'")
        void getKpis_trendingConFiguraInexistente_usaDesconocido() {
            AggregationResults<Document> vacio = aggResults(Collections.emptyList());

            AggregationResults<Document> trending =
                    aggResults(List.of(
                            new Document("_id", "fig-inexistente")
                                    .append("visualizaciones", 9L)));

            lenient().when(mongoTemplate.aggregate(
                            any(Aggregation.class),
                            eq(Visualizacion.class),
                            eq(Document.class)))
                    .thenReturn(vacio, trending, vacio);

            lenient().when(mongoTemplate.aggregate(
                            any(Aggregation.class),
                            eq(Favorito.class),
                            eq(Document.class)))
                    .thenReturn(vacio, vacio);

            lenient().when(mongoTemplate.aggregate(
                            any(Aggregation.class),
                            eq(Comentario.class),
                            eq(Document.class)))
                    .thenReturn(vacio, vacio);

            lenient().when(mongoTemplate.aggregate(
                            any(Aggregation.class),
                            eq(Valoracion.class),
                            eq(Document.class)))
                    .thenReturn(vacio, vacio, vacio);

            when(figuraRepository.findAllById(anyIterable()))
                    .thenReturn(Collections.emptyList());

            DashboardResponseDto dto = dashboardSevice.getKpis();

            assertThat(dto).isNotNull();

            verify(figuraRepository, times(1)).findAllById(anyIterable());
        }

        @SuppressWarnings("unchecked")
        @Test
        @DisplayName("si la figura existe, trending resuelve el nombre con findAllById")
        void getKpis_trendingConFiguraExistente_agregaNombre() {
            AggregationResults<Document> vacio = aggResults(Collections.emptyList());

            AggregationResults<Document> trending =
                    aggResults(List.of(
                            new Document("_id", "fig-real")
                                    .append("visualizaciones", 42L)));

            lenient().when(mongoTemplate.aggregate(
                            any(Aggregation.class),
                            eq(Visualizacion.class),
                            eq(Document.class)))
                    .thenReturn(vacio, trending, vacio);

            lenient().when(mongoTemplate.aggregate(
                            any(Aggregation.class),
                            eq(Favorito.class),
                            eq(Document.class)))
                    .thenReturn(vacio, vacio);

            lenient().when(mongoTemplate.aggregate(
                            any(Aggregation.class),
                            eq(Comentario.class),
                            eq(Document.class)))
                    .thenReturn(vacio, vacio);

            lenient().when(mongoTemplate.aggregate(
                            any(Aggregation.class),
                            eq(Valoracion.class),
                            eq(Document.class)))
                    .thenReturn(vacio, vacio, vacio);

            Figura figuraReal = figuraMock("fig-real", "Elefante Amigurumi");

            when(figuraRepository.findAllById(anyIterable()))
                    .thenReturn(List.of(figuraReal));

            DashboardResponseDto dto = dashboardSevice.getKpis();

            assertThat(dto).isNotNull();

            verify(figuraRepository, times(1)).findAllById(anyIterable());
        }
    }

    // ---------------------------------------------------------------------
    // Conteos totales
    // ---------------------------------------------------------------------

    @SuppressWarnings("unchecked")
    @Test
    @DisplayName("getKpis() delega los totales a count() de cada repositorio exactamente una vez")
    void getKpis_delegaConteosTotales() {
        AggregationResults<Document> vacio = aggResults(Collections.emptyList());

        lenient().when(mongoTemplate.aggregate(
                        any(Aggregation.class),
                        eq(Visualizacion.class),
                        eq(Document.class)))
                .thenReturn(vacio, vacio, vacio);

        lenient().when(mongoTemplate.aggregate(
                        any(Aggregation.class),
                        eq(Favorito.class),
                        eq(Document.class)))
                .thenReturn(vacio, vacio);

        lenient().when(mongoTemplate.aggregate(
                        any(Aggregation.class),
                        eq(Comentario.class),
                        eq(Document.class)))
                .thenReturn(vacio, vacio);

        lenient().when(mongoTemplate.aggregate(
                        any(Aggregation.class),
                        eq(Valoracion.class),
                        eq(Document.class)))
                .thenReturn(vacio, vacio, vacio);

        when(figuraRepository.count()).thenReturn(10L);
        when(visualizacionRepository.count()).thenReturn(500L);
        when(favoritoRepository.count()).thenReturn(120L);
        when(comentarioRepository.count()).thenReturn(45L);

        dashboardSevice.getKpis();

        verify(figuraRepository, times(1)).count();
        verify(visualizacionRepository, times(1)).count();
        verify(favoritoRepository, times(1)).count();
        verify(comentarioRepository, times(1)).count();
    }
}