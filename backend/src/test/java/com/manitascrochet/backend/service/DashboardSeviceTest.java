package com.manitascrochet.backend.service;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

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

/**
 * Pruebas unitarias de {@link DashboardSevice}.
 *
 * NOTA DE SUPUESTOS (ajustar si no coincide con el código real, no se tuvo
 * acceso a estos ficheros):
 * - DashboardResponseDto y Top10 se asumen "record"-like, construidos
 * posicionalmente tal y como se ven en getKpis().
 * - Figura se asume una clase normal (no record) con getNombre(), por eso es
 * mockeable con Mockito.
 * - Los repositorios extienden MongoRepository y exponen countByFiguraId(String)
 * y count().
 *
 * ESTRATEGIA DE COBERTURA
 * Como toda la lógica de negocio vive en métodos privados, la única puerta de
 * entrada es getKpis(). Cada test configura las respuestas del MongoTemplate
 * mockeado (en el ORDEN exacto en que se invocan dentro de getKpis) para forzar
 * cada rama: promedio nulo / no nulo, claves presentes / ausentes en los
 * Document agregados, Optional presente / vacío en FiguraRepository, y los
 * filtros extra de evolucionMensual (favoritos usa Criteria "activo").
 */
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

    @BeforeEach
    void setUp() {
        dashboardSevice = new DashboardSevice(
                figuraRepository,
                visualizacionRepository,
                favoritoRepository,
                comentarioRepository,
                mongoTemplate);
    }

    // ---------------------------------------------------------------------
    // Helpers
    // ---------------------------------------------------------------------

    @SuppressWarnings("unchecked")
    private AggregationResults<Document> aggResults(List<Document> docs) {
        AggregationResults<Document> results = mock(AggregationResults.class);
        // lenient(): según la llamada de getKpis() que consuma este resultado,
        // el código real usa SOLO getMappedResults() o SOLO
        // getUniqueMappedResult(), nunca ambos. Con strict stubs (el modo por
        // defecto de MockitoExtension) el que no se invoque se marcaría como
        // "unnecessary stubbing" y rompería el test.
        lenient().when(results.getMappedResults()).thenReturn(docs);
        lenient().when(results.getUniqueMappedResult()).thenReturn(docs.isEmpty() ? null : docs.get(0));
        return results;
    }

    private Document countDoc(String figuraId, String alias, long valor) {
        return new Document("_id", figuraId).append(alias, valor);
    }

    private Document evolucionDoc(int anio, int mes, String alias, long valor) {
        Document id = new Document("anio", anio).append("mes", mes);
        return new Document("_id", id).append(alias, valor);
    }

    private Figura figuraMock(String nombre) {
        Figura figura = mock(Figura.class);
        when(figura.getNombre()).thenReturn(nombre);
        return figura;
    }

    /**
     * Configura las respuestas para las 3 llamadas a mongoTemplate.aggregate(...,
     * Valoracion.class, ...) que se hacen SIEMPRE en este orden dentro de
     * getKpis(): (1) obtenerPuntuacionPromedio, (2) top10PorValoracion,
     * (3) evolucionMensual(valoraciones).
     */
    @SuppressWarnings("unchecked")
    private void stubValoracionCalls(AggregationResults<Document> promedioGlobal,
            List<Document> top10Valoracion,
            List<Document> evolucionValoraciones) {
        // IMPORTANTE: los AggregationResults se construyen ANTES de abrir el
        // when(...) externo. Si se llamara a aggResults(...) dentro de los
        // argumentos de thenReturn(...), su when(...) interno se ejecutaría
        // mientras el when(...) externo sigue "abierto" y Mockito lanza
        // UnfinishedStubbingException.
        AggregationResults<Document> top10ValoracionResults = aggResults(top10Valoracion);
        AggregationResults<Document> evolucionValoracionesResults = aggResults(evolucionValoraciones);

        lenient().when(mongoTemplate.aggregate(any(Aggregation.class), eq(Valoracion.class), eq(Document.class)))
            .thenReturn(
                promedioGlobal,
                top10ValoracionResults,
                evolucionValoracionesResults);
    }

    /**
     * Configura las 3 llamadas con Visualizacion.class, en orden: (1)
     * agruparYOrdenar (top10), (2) trendingFigures, (3) evolucionMensual.
     */
    @SuppressWarnings("unchecked")
    private void stubVisualizacionCalls(List<Document> top10Vis, List<Document> trending,
            List<Document> evolucionVis) {
        AggregationResults<Document> top10VisResults = aggResults(top10Vis);
        AggregationResults<Document> trendingResults = aggResults(trending);
        AggregationResults<Document> evolucionVisResults = aggResults(evolucionVis);

        lenient().when(mongoTemplate.aggregate(any(Aggregation.class), eq(Visualizacion.class), eq(Document.class)))
            .thenReturn(
                top10VisResults,
                trendingResults,
                evolucionVisResults);
    }

    /** Configura las 2 llamadas con Favorito.class: agruparYOrdenar y evolucionMensual. */
    @SuppressWarnings("unchecked")
    private void stubFavoritoCalls(List<Document> top10Fav, List<Document> evolucionFav) {
        AggregationResults<Document> top10FavResults = aggResults(top10Fav);
        AggregationResults<Document> evolucionFavResults = aggResults(evolucionFav);

        lenient().when(mongoTemplate.aggregate(any(Aggregation.class), eq(Favorito.class), eq(Document.class)))
            .thenReturn(top10FavResults, evolucionFavResults);
    }

    /** Configura las 2 llamadas con Comentario.class: agruparYOrdenar y evolucionMensual. */
    @SuppressWarnings("unchecked")
    private void stubComentarioCalls(List<Document> top10Com, List<Document> evolucionCom) {
        AggregationResults<Document> top10ComResults = aggResults(top10Com);
        AggregationResults<Document> evolucionComResults = aggResults(evolucionCom);

        lenient().when(mongoTemplate.aggregate(any(Aggregation.class), eq(Comentario.class), eq(Document.class)))
            .thenReturn(top10ComResults, evolucionComResults);
    }

    // ---------------------------------------------------------------------
    // Camino feliz: todos los Document ya traen su métrica -> no hace falta
    // recurrir a los repositorios dentro de enriquecer().
    // ---------------------------------------------------------------------

    @Nested
    @DisplayName("getKpis() - camino feliz")
    class CaminoFeliz {

        @Test
        @DisplayName("calcula el DTO usando los datos agregados, sin fallback a repositorios")
        void getKpis_conDatosCompletos_noUsaFallback() {
            // promedio global
            Document promedioDoc = new Document("promedio", 4.3567);
            AggregationResults<Document> promedioGlobal = aggResults(List.of(promedioDoc));

            List<Document> top10Vis = List.of(countDoc("fig-1", "visualizacion", 100));
            List<Document> trending = List.of(new Document("_id", "fig-1").append("visualizaciones", 50));
            List<Document> evolucionVis = List.of(evolucionDoc(2026, 6, "visualizaciones", 20));

            List<Document> top10Fav = List.of(countDoc("fig-2", "favoritos", 30));
            List<Document> evolucionFav = List.of(evolucionDoc(2026, 6, "favoritos", 5));

            List<Document> top10Com = List.of(countDoc("fig-3", "comentarios", 12));
            List<Document> evolucionCom = List.of(evolucionDoc(2026, 6, "comentarios", 3));

            // top10PorValoracion ya trae la clave "valoracion" -> rama contiene clave
            Document valDoc = new Document("_id", "fig-4")
                    .append("valoracion", 4.777)
                    .append("numValoraciones", 5);
            List<Document> top10Valoracion = List.of(valDoc);
            List<Document> evolucionVal = List.of(evolucionDoc(2026, 6, "valoraciones", 8));

            stubValoracionCalls(promedioGlobal, top10Valoracion, evolucionVal);
            stubVisualizacionCalls(top10Vis, trending, evolucionVis);
            stubFavoritoCalls(top10Fav, evolucionFav);
            stubComentarioCalls(top10Com, evolucionCom);

            // figuraMock(...) se resuelve ANTES del when(...) por el mismo
            // motivo explicado en stubValoracionCalls: no se puede anidar un
            // when() dentro de los argumentos de otro when().thenReturn().
            Figura figura1 = figuraMock("Gato Amigurumi");
            Figura figura2 = figuraMock("Perro Amigurumi");
            Figura figura3 = figuraMock("Oso Amigurumi");
            Figura figura4 = figuraMock("Rana Amigurumi");

            when(figuraRepository.findById("fig-1")).thenReturn(Optional.of(figura1));
            when(figuraRepository.findById("fig-2")).thenReturn(Optional.of(figura2));
            when(figuraRepository.findById("fig-3")).thenReturn(Optional.of(figura3));
            when(figuraRepository.findById("fig-4")).thenReturn(Optional.of(figura4));

            when(figuraRepository.count()).thenReturn(4L);
            when(visualizacionRepository.count()).thenReturn(200L);
            when(favoritoRepository.count()).thenReturn(80L);
            when(comentarioRepository.count()).thenReturn(15L);

            DashboardResponseDto dto = dashboardSevice.getKpis();

            assertThat(dto).isNotNull();

            // Cada top10 (excepto el propio) no trae las otras métricas, por
            // lo que se consultan los conteos por figura 3 veces (fig-2, fig-3, fig-4).
            verify(visualizacionRepository, times(3)).countByFiguraId(anyString());
            verify(favoritoRepository, times(3)).countByFiguraId(anyString());
            verify(comentarioRepository, times(3)).countByFiguraId(anyString());
            // "valoraciones" solo se agrega vía String cuando falta la clave
            // "valoracion" en el Document; aquí top10Vis/Fav/Com sí la
            // necesitan (no la traen) por lo que SÍ se invoca 3 veces (una
            // por cada figuraId de esos tres top10).
            verify(mongoTemplate, times(3))
                    .aggregate(any(Aggregation.class), eq("valoraciones"), eq(Document.class));

            verify(figuraRepository, times(2)).findById("fig-1");
            verify(figuraRepository).findById("fig-2");
            verify(figuraRepository).findById("fig-3");
            verify(figuraRepository).findById("fig-4");
        }
    }

    // ---------------------------------------------------------------------
    // Ramas de fallback dentro de enriquecer(): faltan las claves de conteo
    // en el Document -> debe recurrir a countByFiguraId / promedio individual.
    // Además: Optional vacío en FiguraRepository -> "Desconocido".
    // ---------------------------------------------------------------------

    @Nested
    @DisplayName("getKpis() - ramas de fallback en enriquecer()")
    class RamasFallback {

        @Test
        @DisplayName("usa los repositorios cuando el Document no trae la métrica y usa 'Desconocido' si no existe la figura")
        void getKpis_sinMetricasEnDocument_usaRepositorios() {
            Document promedioDoc = new Document("promedio", 3.0);
            AggregationResults<Document> promedioGlobal = aggResults(List.of(promedioDoc));

            // Documents SIN la clave de conteo -> fuerza el fallback
            List<Document> top10Vis = List.of(new Document("_id", "fig-x"));
            List<Document> trending = Collections.emptyList();
            List<Document> evolucionVis = Collections.emptyList();

            List<Document> top10Fav = List.of(new Document("_id", "fig-x"));
            List<Document> evolucionFav = Collections.emptyList();

            List<Document> top10Com = List.of(new Document("_id", "fig-x"));
            List<Document> evolucionCom = Collections.emptyList();

            // top10PorValoracion también sin la clave "valoracion"
            List<Document> top10Valoracion = List.of(new Document("_id", "fig-x"));
            List<Document> evolucionVal = Collections.emptyList();

            stubValoracionCalls(promedioGlobal, top10Valoracion, evolucionVal);
            stubVisualizacionCalls(top10Vis, trending, evolucionVis);
            stubFavoritoCalls(top10Fav, evolucionFav);
            stubComentarioCalls(top10Com, evolucionCom);

            // figuraRepository devuelve vacío -> rama "Desconocido"
            when(figuraRepository.findById("fig-x")).thenReturn(Optional.empty());

            when(visualizacionRepository.countByFiguraId("fig-x")).thenReturn(7L);
            when(favoritoRepository.countByFiguraId("fig-x")).thenReturn(2L);
            when(comentarioRepository.countByFiguraId("fig-x")).thenReturn(1L);

            // promedio individual: Document no nulo -> rama "document != null"
            Document promedioIndividual = new Document("promedio", 4.5);
            AggregationResults<Document> promedioIndividualResults = aggResults(List.of(promedioIndividual));
            when(mongoTemplate.aggregate(any(Aggregation.class), eq("valoraciones"), eq(Document.class)))
                    .thenReturn(promedioIndividualResults);

            DashboardResponseDto dto = dashboardSevice.getKpis();

            assertThat(dto).isNotNull();

            verify(visualizacionRepository, times(4)).countByFiguraId("fig-x");
            verify(favoritoRepository, times(4)).countByFiguraId("fig-x");
            verify(comentarioRepository, times(4)).countByFiguraId("fig-x");
            // 4 llamadas: top10Vis, top10Fav, top10Com y top10Valoracion,
            // todos referenciando el mismo "fig-x" pero cada uno gatilla su
            // propia llamada de enriquecimiento.
            verify(mongoTemplate, times(4))
                    .aggregate(any(Aggregation.class), eq("valoraciones"), eq(Document.class));
        }

        @Test
        @DisplayName("promedio individual nulo (sin valoraciones para la figura) devuelve 0.0")
        void getKpis_promedioIndividualNulo_devuelveCero() {
            Document promedioDoc = new Document("promedio", 3.0);
            AggregationResults<Document> promedioGlobal = aggResults(List.of(promedioDoc));

            List<Document> top10Vis = List.of(new Document("_id", "fig-y"));
            stubValoracionCalls(promedioGlobal, Collections.emptyList(), Collections.emptyList());
            stubVisualizacionCalls(top10Vis, Collections.emptyList(), Collections.emptyList());
            stubFavoritoCalls(Collections.emptyList(), Collections.emptyList());
            stubComentarioCalls(Collections.emptyList(), Collections.emptyList());

            Figura figuraY = figuraMock("Buho");
            when(figuraRepository.findById("fig-y")).thenReturn(Optional.of(figuraY));
            when(visualizacionRepository.countByFiguraId("fig-y")).thenReturn(0L);
            when(favoritoRepository.countByFiguraId("fig-y")).thenReturn(0L);
            when(comentarioRepository.countByFiguraId("fig-y")).thenReturn(0L);

            // getUniqueMappedResult() == null -> rama "document != null ? ... : 0.0"
            AggregationResults<Document> vacio = aggResults(Collections.emptyList());
            when(mongoTemplate.aggregate(any(Aggregation.class), eq("valoraciones"), eq(Document.class)))
                    .thenReturn(vacio);

            DashboardResponseDto dto = dashboardSevice.getKpis();

            assertThat(dto).isNotNull();
        }
    }

    // ---------------------------------------------------------------------
    // Ramas de obtenerPuntuacionPromedio(): resultado nulo y promedio nulo.
    // ---------------------------------------------------------------------

    @Nested
    @DisplayName("getKpis() - ramas de obtenerPuntuacionPromedio()")
    class PromedioGlobal {

        @Test
        @DisplayName("getUniqueMappedResult() nulo -> promedio global 0.0")
        void getKpis_sinResultadoDeValoracion_promedioCero() {
            AggregationResults<Document> promedioVacio = aggResults(Collections.emptyList());

            stubValoracionCalls(promedioVacio, Collections.emptyList(), Collections.emptyList());
            stubVisualizacionCalls(Collections.emptyList(), Collections.emptyList(), Collections.emptyList());
            stubFavoritoCalls(Collections.emptyList(), Collections.emptyList());
            stubComentarioCalls(Collections.emptyList(), Collections.emptyList());

            DashboardResponseDto dto = dashboardSevice.getKpis();

            assertThat(dto).isNotNull();
        }

        @Test
        @DisplayName("Document presente pero campo 'promedio' nulo -> promedio global 0.0")
        void getKpis_campoPromedioNulo_promedioCero() {
            // Document sin el campo "promedio" -> result.getDouble("promedio") == null
            Document sinPromedio = new Document("otraClave", "valor");
            AggregationResults<Document> promedioConCampoNulo = aggResults(List.of(sinPromedio));

            stubValoracionCalls(promedioConCampoNulo, Collections.emptyList(), Collections.emptyList());
            stubVisualizacionCalls(Collections.emptyList(), Collections.emptyList(), Collections.emptyList());
            stubFavoritoCalls(Collections.emptyList(), Collections.emptyList());
            stubComentarioCalls(Collections.emptyList(), Collections.emptyList());

            DashboardResponseDto dto = dashboardSevice.getKpis();

            assertThat(dto).isNotNull();
        }
    }

    // ---------------------------------------------------------------------
    // trendingFigures(): figura encontrada vs. no encontrada en el forEach.
    // ---------------------------------------------------------------------

    @Nested
    @DisplayName("getKpis() - ramas de trendingFigures()")
    class Trending {

        @Test
        @DisplayName("figura no encontrada -> no se añade la clave 'figura' al Document")
        void getKpis_trendingConFiguraInexistente_noAgregaNombre() {
            Document promedioDoc = new Document("promedio", 1.0);
            AggregationResults<Document> promedioGlobal = aggResults(List.of(promedioDoc));

            List<Document> trending = List.of(new Document("_id", "fig-inexistente").append("visualizaciones", 9L));

            stubValoracionCalls(promedioGlobal, Collections.emptyList(), Collections.emptyList());
            stubVisualizacionCalls(Collections.emptyList(), trending, Collections.emptyList());
            stubFavoritoCalls(Collections.emptyList(), Collections.emptyList());
            stubComentarioCalls(Collections.emptyList(), Collections.emptyList());

            when(figuraRepository.findById("fig-inexistente")).thenReturn(Optional.empty());

            DashboardResponseDto dto = dashboardSevice.getKpis();

            assertThat(dto).isNotNull();
            verify(figuraRepository).findById("fig-inexistente");
        }

        @Test
        @DisplayName("figura encontrada -> se añade su nombre y se elimina el _id")
        void getKpis_trendingConFiguraExistente_agregaNombre() {
            Document promedioDoc = new Document("promedio", 1.0);
            AggregationResults<Document> promedioGlobal = aggResults(List.of(promedioDoc));

            List<Document> trending = List.of(new Document("_id", "fig-real").append("visualizaciones", 42L));

            stubValoracionCalls(promedioGlobal, Collections.emptyList(), Collections.emptyList());
            stubVisualizacionCalls(Collections.emptyList(), trending, Collections.emptyList());
            stubFavoritoCalls(Collections.emptyList(), Collections.emptyList());
            stubComentarioCalls(Collections.emptyList(), Collections.emptyList());

            Figura figuraReal = figuraMock("Elefante Amigurumi");
            when(figuraRepository.findById("fig-real")).thenReturn(Optional.of(figuraReal));

            DashboardResponseDto dto = dashboardSevice.getKpis();

            assertThat(dto).isNotNull();
            verify(figuraRepository).findById("fig-real");
        }
    }

    // ---------------------------------------------------------------------
    // evolucionMensual(): rama con Criteria adicional (favoritos usa
    // Criteria.where("activo").is(true)) y verificación de que el periodo se
    // filtra desde "hace 3 meses" (Criteria.gte).
    // ---------------------------------------------------------------------

    @Nested
    @DisplayName("getKpis() - evolucionMensual() con y sin filtros extra")
    class EvolucionMensual {

        @Test
        @DisplayName("evolución de favoritos aplica el filtro extra 'activo = true' sin romper el flujo")
        void getKpis_evolucionFavoritos_conFiltroActivo() {
            Document promedioDoc = new Document("promedio", 2.5);
            AggregationResults<Document> promedioGlobal = aggResults(List.of(promedioDoc));

            List<Document> evolucionFav = List.of(
                    evolucionDoc(2026, 5, "favoritos", 3),
                    evolucionDoc(2026, 6, "favoritos", 6));

            stubValoracionCalls(promedioGlobal, Collections.emptyList(), Collections.emptyList());
            stubVisualizacionCalls(Collections.emptyList(), Collections.emptyList(), Collections.emptyList());
            stubFavoritoCalls(Collections.emptyList(), evolucionFav);
            stubComentarioCalls(Collections.emptyList(), Collections.emptyList());

            DashboardResponseDto dto = dashboardSevice.getKpis();

            assertThat(dto).isNotNull();
            // Se ejecutan 2 agregaciones sobre Favorito.class: agruparYOrdenar y
            // evolucionMensual (esta última con el Criteria "activo").
            verify(mongoTemplate, times(2))
                    .aggregate(any(Aggregation.class), eq(Favorito.class), eq(Document.class));
        }
    }

    // ---------------------------------------------------------------------
    // Verificación general de conteos totales delegados a los repositorios.
    // ---------------------------------------------------------------------

    @Test
    @DisplayName("getKpis() delega los totales a count() de cada repositorio exactamente una vez")
    void getKpis_delegaConteosTotales() {
        Document promedioDoc = new Document("promedio", 4.0);
        AggregationResults<Document> promedioGlobal = aggResults(List.of(promedioDoc));

        stubValoracionCalls(promedioGlobal, Collections.emptyList(), Collections.emptyList());
        stubVisualizacionCalls(Collections.emptyList(), Collections.emptyList(), Collections.emptyList());
        stubFavoritoCalls(Collections.emptyList(), Collections.emptyList());
        stubComentarioCalls(Collections.emptyList(), Collections.emptyList());

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