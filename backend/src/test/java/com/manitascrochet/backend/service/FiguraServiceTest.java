package com.manitascrochet.backend.service;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.bson.Document;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationOperation;
import org.springframework.data.mongodb.core.aggregation.AggregationResults;
import org.springframework.data.mongodb.core.aggregation.SortOperation;
import org.springframework.data.mongodb.core.convert.MongoConverter;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.util.ReflectionTestUtils;

import com.manitascrochet.backend.dto.FiguraDetalleDto;
import com.manitascrochet.backend.dto.FiguraListadoDto;
import com.manitascrochet.backend.dto.ImageUploadResultDto;
import com.manitascrochet.backend.dto.ResumenValoracionDto;
import com.manitascrochet.backend.dto.ValoracionDto;
import com.manitascrochet.backend.exception.GlobalExceptionHandler.CategoriaNoEncontradaException;
import com.manitascrochet.backend.exception.GlobalExceptionHandler.ColorNoEncontradoException;
import com.manitascrochet.backend.exception.GlobalExceptionHandler.FiguraNoEncontradaException;
import com.manitascrochet.backend.model.Categoria;
import com.manitascrochet.backend.model.Color;
import com.manitascrochet.backend.model.Dificultad;
import com.manitascrochet.backend.model.Figura;
import com.manitascrochet.backend.model.Valoracion;
import com.manitascrochet.backend.repository.CategoriaRepository;
import com.manitascrochet.backend.repository.ColorRepository;
import com.manitascrochet.backend.repository.FiguraRepository;
import com.manitascrochet.backend.repository.ValoracionRepository;
import com.manitascrochet.backend.security.UserDetailsImpl;

@ExtendWith(MockitoExtension.class)
class FiguraServiceTest {

    @Mock
    FiguraRepository figuras;

    @Mock
    CategoriaRepository categorias;

    @Mock
    ColorRepository colores;

    @Mock
    ImageService imageService;

    @Mock
    ValoracionService ratings;

    @Mock
    ComentarioService comments;

    @Mock
    ValoracionRepository valoracionesRepo;

    @Mock
    com.manitascrochet.backend.repository.VisualizacionRepository visualizacionesRepo;

    @Mock
    MongoTemplate mongo;

    @Mock
    MongoConverter mongoConverter;

    // Respaldodel stub de mongoConverter.read (declarado en setUp): _id -> Figura,
    // rellenado por facetResultados(...).
    private final Map<String, Figura> figurasDelFacet = new HashMap<>();

    @Mock
    com.manitascrochet.backend.repository.FavoritoRepository favoritosRepo;

    @InjectMocks
    FiguraService service;

    private Figura figura() {
        Figura f = new Figura();
        f.setId("f1");
        f.setNombre("Oso");
        f.setDescripcion("Oso tejido a crochet");
        f.setCategoriaId("c1");
        f.setImagenPrincipal("");
        f.setImagenesSecundarias(List.of());
        f.setAltura(10);
        f.setAncho(8);
        f.setPeso(3);
        f.setAutor("Alejo");
        f.setDificultad(Dificultad.PRINCIPIANTE);
        f.setColoresIds(List.of());
        return f;
    }

    private Categoria categoria() {
        Categoria c = new Categoria();
        c.setId("c1");
        c.setNombre("Animales");
        return c;
    }

    private Color color(String id, String nombre, String codigo) {
        Color color = new Color();
        color.setId(id);
        color.setNombre(nombre);
        color.setCodigo(codigo);
        return color;
    }

    private ResumenValoracionDto resumen(double media, long total) {
        return new ResumenValoracionDto(media, total);
    }

    private Valoracion valoracion(String figuraId, int puntuacion) {
        Valoracion v = new Valoracion();
        v.setFiguraId(figuraId);
        v.setPuntuacion(puntuacion);
        return v;
    }

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(service, "imageUrl", "https://ik.imagekit.io/8hlhxb9hx");
        ReflectionTestUtils.setField(service, "imageFolder", "manitas-Crochet");
        // El servicio serializa cada Document de "pagina" del $facet con mongoConverter.read.
        // Un único stub lenient resuelto por _id, rellenado por facetResultados(...).
        lenient().when(mongoConverter.read(eq(Figura.class), any(Document.class)))
                .thenAnswer(inv -> figurasDelFacet.get(((Document) inv.getArgument(1)).getString("_id")));
        lenient().when(valoracionesRepo.findByFiguraIdIn(anyList())).thenReturn(List.of());
    }
    // ---------------------------------------------------------------
    // obtenerTodasDto
    // ---------------------------------------------------------------

    @Test
    void obtenerTodasDtoSinFiltros_devuelveListaMapeada() {
        Figura f = figura();
        f.setImagenPrincipal("oso.png");

        when(mongo.aggregate(any(Aggregation.class), eq(Figura.class), eq(Document.class)))
                .thenReturn(facetResultados(List.of(f)));
        when(categorias.findAllById(any())).thenReturn(List.of(categoria()));
        when(valoracionesRepo.findByFiguraIdIn(List.of("f1"))).thenReturn(List.of(
                valoracion("f1", 5), valoracion("f1", 5), valoracion("f1", 5), valoracion("f1", 5), valoracion("f1", 5),
                valoracion("f1", 4), valoracion("f1", 4), valoracion("f1", 4), valoracion("f1", 4), valoracion("f1", 4)));

        var resultado = service.obtenerTodasDto(null, null, false, 0, 12, "recientes", null);

        assertThat(resultado.getContenido()).hasSize(1);
        FiguraListadoDto dto = resultado.getContenido().get(0);
        assertThat(dto.getId()).isEqualTo("f1");
        assertThat(dto.getCategoria()).isEqualTo("Animales");
        assertThat(dto.getImagenPrincipal()).isEqualTo("oso.png");
        assertThat(dto.getValoracionMedia()).isEqualTo(4.5);
        assertThat(dto.getTotalValoraciones()).isEqualTo(10L);
        assertThat(dto.isEsFavorito()).isFalse();
        assertThat(resultado.getPaginaActual()).isEqualTo(0);
        assertThat(resultado.getTotalElementos()).isEqualTo(1L);
        assertThat(resultado.getTotalPaginas()).isEqualTo(1);
    }

    @Test
    void obtenerTodasDtoUsaImagenPorDefectoCuandoEsBlank() {
        Figura f = figura();
        f.setImagenPrincipal("   ");

        when(mongo.aggregate(any(Aggregation.class), eq(Figura.class), eq(Document.class)))
                .thenReturn(facetResultados(List.of(f)));
        when(categorias.findAllById(any())).thenReturn(List.of(categoria()));

        var resultado = service.obtenerTodasDto(null, null, false, 0, 12, "recientes", null);

        assertThat(resultado.getContenido().get(0).getImagenPrincipal())
                .isEqualTo("https://ik.imagekit.io/8hlhxb9hx/manitas-Crochet/default.webp");
    }

    @Test
    void obtenerTodasDtoConFiltros_delegaEnMongoTemplate() {
        when(mongo.aggregate(any(Aggregation.class), eq(Figura.class), eq(Document.class)))
                .thenReturn(facetVacio());

        var resultado = service.obtenerTodasDto("oso", "c1", false, 0, 12, "recientes", null);

        assertThat(resultado.getContenido()).isEmpty();
        verify(mongo, times(1)).aggregate(any(Aggregation.class), eq(Figura.class), eq(Document.class));
    }

    @Test
    void obtenerTodasDtoListaVacia_noConsultaCategoriaNiValoraciones() {
        when(mongo.aggregate(any(Aggregation.class), eq(Figura.class), eq(Document.class)))
                .thenReturn(facetVacio());

        var resultado = service.obtenerTodasDto("", "", false, 0, 12, "recientes", null);

        assertThat(resultado.getContenido()).isEmpty();
        verify(categorias, never()).findById(anyString());
        verify(ratings, never()).obtenerResumenValoraciones(anyString());
    }

    @Test
    void obtenerTodasDtoResultadoVacio_devuelveListaVacia() {
        @SuppressWarnings("unchecked")
        AggregationResults<Document> vacio = mock(AggregationResults.class);
        when(vacio.getMappedResults()).thenReturn(List.of());
        when(mongo.aggregate(any(Aggregation.class), eq(Figura.class), eq(Document.class)))
                .thenReturn(vacio);

        var resultado = service.obtenerTodasDto(null, null, false, 0, 12, "recientes", null);

        assertThat(resultado.getContenido()).isEmpty();
        assertThat(resultado.getTotalElementos()).isZero();
        assertThat(resultado.getTotalPaginas()).isZero();
    }

    @Test
    void obtenerTodasDtoFallaSiCategoriaNoExiste() {
        when(mongo.aggregate(any(Aggregation.class), eq(Figura.class), eq(Document.class)))
                .thenReturn(facetResultados(List.of(figura())));
        when(categorias.findAllById(any())).thenReturn(List.of());

        assertThatThrownBy(() -> service.obtenerTodasDto(null, null, false, 0, 12, "recientes", null))
                .isInstanceOf(CategoriaNoEncontradaException.class);
    }

    @Test
    void obtenerTodasDtoFueraDeRango_devuelveListaVacia() {
        when(mongo.aggregate(any(Aggregation.class), eq(Figura.class), eq(Document.class)))
                .thenReturn(facetResultados(10, List.of()));

        var resultado = service.obtenerTodasDto(null, null, false, 5, 12, "recientes", null);

        assertThat(resultado.getContenido()).isEmpty();
        assertThat(resultado.getPaginaActual()).isEqualTo(5);
        assertThat(resultado.getTotalElementos()).isEqualTo(10L);
        assertThat(resultado.getTotalPaginas()).isEqualTo(1);
        // No se debe consultar la página (find) si está fuera de rango
        verify(mongo, never()).find(any(Query.class), eq(Figura.class));
    }

    @Test
    void obtenerTodasDtoSoloFavoritos_conUsuarioYFavoritos() {
        Figura f = figura();

        UserDetailsImpl user = mock(UserDetailsImpl.class);
        when(user.getUsername()).thenReturn("u1");
        when(favoritosRepo.findByUsuarioIdAndActivoTrue("u1")).thenReturn(List.of(favorito("f1")));
        when(mongo.aggregate(any(Aggregation.class), eq(Figura.class), eq(Document.class)))
                .thenReturn(facetResultados(List.of(f)));
        when(categorias.findAllById(any())).thenReturn(List.of(categoria()));
        when(valoracionesRepo.findByFiguraIdIn(List.of("f1"))).thenReturn(List.of());
        when(favoritosRepo.findByUsuarioIdAndFiguraIdInAndActivoTrue("u1", List.of("f1"))).thenReturn(List.of(favorito("f1")));

        var resultado = service.obtenerTodasDto(null, null, true, 0, 12, "recientes", user);

        assertThat(resultado.getContenido()).hasSize(1);
        assertThat(resultado.getContenido().get(0).isEsFavorito()).isTrue();
    }

    @Test
    void obtenerTodasDtoSoloFavoritos_sinUsuario_devuelveVacio() {
        var resultado = service.obtenerTodasDto(null, null, true, 0, 12, "recientes", null);

        assertThat(resultado.getContenido()).isEmpty();
        verify(mongo, never()).find(any(Query.class), eq(Figura.class));
    }

    @Test
    void obtenerTodasDtoSoloFavoritos_usuarioSinFavoritos_devuelveVacio() {
        UserDetailsImpl user = mock(UserDetailsImpl.class);
        when(user.getUsername()).thenReturn("u1");
        when(favoritosRepo.findByUsuarioIdAndActivoTrue("u1")).thenReturn(List.of());

        var resultado = service.obtenerTodasDto(null, null, true, 0, 12, "recientes", user);

        assertThat(resultado.getContenido()).isEmpty();
        verify(mongo, never()).find(any(Query.class), eq(Figura.class));
    }

    // ---------------------------------------------------------------
    // ordenación dinámica (sortBy)
    // ---------------------------------------------------------------

    @Test
    void ordenarAntiguos_aplicaAscPorFechaCreacion() {
        when(mongo.aggregate(any(Aggregation.class), eq(Figura.class), eq(Document.class)))
                .thenReturn(facetResultados(List.of(figura())));
        when(categorias.findAllById(any())).thenReturn(List.of(categoria()));

        service.obtenerTodasDto(null, null, false, 0, 12, "antiguos", null);

        ArgumentCaptor<Aggregation> captor = ArgumentCaptor.forClass(Aggregation.class);
        verify(mongo).aggregate(captor.capture(), eq(Figura.class), eq(Document.class));
        Sort sortNativo = sortDelFacet(captor.getValue());
        assertThat(sortNativo.getOrderFor("fechaCreacion").getDirection())
                .isEqualTo(Sort.Direction.ASC);
    }

    @Test
    void ordenarRecientes_aplicaDescPorFechaCreacion() {
        when(mongo.aggregate(any(Aggregation.class), eq(Figura.class), eq(Document.class)))
                .thenReturn(facetResultados(List.of(figura())));
        when(categorias.findAllById(any())).thenReturn(List.of(categoria()));

        service.obtenerTodasDto(null, null, false, 0, 12, "recientes", null);

        ArgumentCaptor<Aggregation> captor = ArgumentCaptor.forClass(Aggregation.class);
        verify(mongo).aggregate(captor.capture(), eq(Figura.class), eq(Document.class));
        Sort sortNativo = sortDelFacet(captor.getValue());
        assertThat(sortNativo.getOrderFor("fechaCreacion").getDirection())
                .isEqualTo(Sort.Direction.DESC);
    }

    @Test
    void ordenarValorados_ordenaPorMediaDescSobreTodasLasFiguras() {
        Figura top = figura();
        top.setId("f1");
        top.setNombre("Top");
        Figura flop = figura();
        flop.setId("f2");
        flop.setNombre("Flop");

        when(mongo.aggregate(any(Aggregation.class), eq(Figura.class), eq(Document.class)))
                .thenReturn(facetResultados(List.of(top, flop)));
        when(categorias.findAllById(any())).thenReturn(List.of(categoria()));
        when(valoracionesRepo.findByFiguraIdIn(anyList())).thenReturn(List.of(
                valoracion("f1", 5), valoracion("f1", 5),
                valoracion("f2", 1)));

        var resultado = service.obtenerTodasDto(null, null, false, 0, 12, "valorados", null);

        assertThat(resultado.getContenido()).hasSize(2);
        assertThat(resultado.getContenido().get(0).getId()).isEqualTo("f1");
        assertThat(resultado.getContenido().get(1).getId()).isEqualTo("f2");
        assertThat(resultado.getContenido().get(0).getValoracionMedia()).isEqualTo(5.0);
        assertThat(resultado.getContenido().get(1).getValoracionMedia()).isEqualTo(1.0);

        ArgumentCaptor<Aggregation> captor = ArgumentCaptor.forClass(Aggregation.class);
        verify(mongo).aggregate(captor.capture(), eq(Figura.class), eq(Document.class));
        Sort sortNativo = sortDelFacet(captor.getValue());
        assertThat(sortNativo.getOrderFor("puntuacionMedia").getDirection())
                .isEqualTo(Sort.Direction.DESC);
    }

    @Test
    void ordenarPopulares_ordenaPorNumeroDeVisualizacionesDesc() {
        Figura popular = figura();
        popular.setId("f1");
        Figura menosPopular = figura();
        menosPopular.setId("f2");

        when(mongo.aggregate(any(Aggregation.class), eq(Figura.class), eq(Document.class)))
                .thenReturn(facetResultados(List.of(popular, menosPopular)));
        when(categorias.findAllById(any())).thenReturn(List.of(categoria()));
        when(valoracionesRepo.findByFiguraIdIn(anyList())).thenReturn(List.of());

        var resultado = service.obtenerTodasDto(null, null, false, 0, 12, "populares", null);

        assertThat(resultado.getContenido()).hasSize(2);
        assertThat(resultado.getContenido().get(0).getId()).isEqualTo("f1");
        assertThat(resultado.getContenido().get(1).getId()).isEqualTo("f2");

        ArgumentCaptor<Aggregation> captor = ArgumentCaptor.forClass(Aggregation.class);
        verify(mongo).aggregate(captor.capture(), eq(Figura.class), eq(Document.class));
        Sort sortNativo = sortDelFacet(captor.getValue());
        assertThat(sortNativo.getOrderFor("numVisualizaciones").getDirection())
                .isEqualTo(Sort.Direction.DESC);
    }

    @Test
    void ordenarValorados_sinResultados_devuelveListaVacia() {
        when(mongo.aggregate(any(Aggregation.class), eq(Figura.class), eq(Document.class)))
                .thenReturn(facetVacio());

        var resultado = service.obtenerTodasDto(null, null, false, 0, 12, "valorados", null);

        assertThat(resultado.getContenido()).isEmpty();
        assertThat(resultado.getTotalElementos()).isZero();
        verify(visualizacionesRepo, never()).contarAgrupadasPorFiguraId(anyList());
    }

    // Extrae el Sort del SortOperation que se inyecta en el pipeline del $facet.
    private Sort sortDelFacet(Aggregation aggregation) {
        List<AggregationOperation> pipeline = aggregation.getPipeline().getOperations();
        // El $facet suele añadir operaciones tras el sort; el SortOperation suele ser el primero
        // que aparece recorriendo el pipeline en orden.
        for (AggregationOperation op : pipeline) {
            if (op instanceof SortOperation sortOp) {
                Document sortDoc = sortOp.toDocument(ctxDePrueba());
                Object sortSpec = sortDoc.get("$sort");
                if (sortSpec instanceof Document d && !d.isEmpty()) {
                    String campo = d.keySet().iterator().next();
                    int valor = ((Number) d.get(campo)).intValue();
                    return valor == 1 ? Sort.by(Sort.Direction.ASC, campo)
                            : Sort.by(Sort.Direction.DESC, campo);
                }
            }
        }
        throw new AssertionError("Sin SortOperation en el $facet");
    }

    // Contexto de serialización que deja los campos tal cual (referencia "$campo").
    private org.springframework.data.mongodb.core.aggregation.AggregationOperationContext ctxDePrueba() {
        org.springframework.data.mongodb.core.aggregation.AggregationOperationContext ctx = org.mockito.Mockito
                .mock(org.springframework.data.mongodb.core.aggregation.AggregationOperationContext.class);
        lenient().when(ctx.getMappedObject(any(Document.class), any())).thenAnswer(inv -> inv.getArgument(0));
        lenient().when(ctx.getReference(any(org.springframework.data.mongodb.core.aggregation.Field.class)))
                .thenAnswer(inv -> {
                    String raw = ((org.springframework.data.mongodb.core.aggregation.Field) inv.getArgument(0)).getTarget();
                    return refDePrueba(raw);
                });
        lenient().when(ctx.getReference(any(String.class)))
                .thenAnswer(inv -> refDePrueba(inv.getArgument(0)));
        return ctx;
    }

    private org.springframework.data.mongodb.core.aggregation.ExposedFields.FieldReference refDePrueba(String raw) {
        org.springframework.data.mongodb.core.aggregation.ExposedFields.FieldReference ref = org.mockito.Mockito
                .mock(org.springframework.data.mongodb.core.aggregation.ExposedFields.FieldReference.class);
        lenient().when(ref.getRaw()).thenReturn(raw);
        lenient().when(ref.getReferenceValue()).thenReturn("$" + raw);
        return ref;
    }

    // Simula el resultado del $facet: un único Document con "totales" (count)
    // y "pagina" (docs de la página), tal y como lo devuelve mongoTemplate.aggregate.
    // Además deja listo mongoConverter.read para devolver la Figura de cada doc de página.
    private AggregationResults<Document> facetResultados(long total, List<Figura> figuras) {
        // El $count de MongoDB devuelve Int32 (Integer), no Long: así se simula el dato real.
        List<Document> totales = List.of(new Document("total", (int) total));
        List<Document> paginaDocs = figuras.stream()
                .map(f -> new Document("_id", f.getId()))
                .toList();
        Document resultado = new Document()
                .append("totales", totales)
                .append("pagina", paginaDocs);
        // Registra en el Map que respalda el stub de mongoConverter.read (declarado en setUp).
        figurasDelFacet.clear();
        figuras.forEach(f -> figurasDelFacet.put(f.getId(), f));
        // AggregationResults tiene constructor público: evita mockear getMappedResults(),
        // que provocaria UnfinishedStubbing al evaluarse dentro de another "when(...)".
        return new AggregationResults<>(List.of(resultado), resultado);
    }

    private AggregationResults<Document> facetResultados(List<Figura> figuras) {
        return facetResultados(figuras.size(), figuras);
    }

    // result -> mock(AggregationResults) cuyo getMappedResults devuelve UNA página vacía (total 0).
    private AggregationResults<Document> facetVacio() {
        return facetResultados(0, List.of());
    }

    private com.manitascrochet.backend.model.Favorito favorito(String figuraId) {
        com.manitascrochet.backend.model.Favorito f = new com.manitascrochet.backend.model.Favorito();
        f.setId("fav-" + figuraId);
        f.setUsuarioId("u1");
        f.setFiguraId(figuraId);
        f.setActivo(true);
        return f;
    }

    // ---------------------------------------------------------------
    // obtenerPorId
    // ---------------------------------------------------------------

    @Test
    void obtienePorIdYFallaAusente() {
        Figura f = figura();

        when(figuras.findById("f1")).thenReturn(Optional.of(f));
        assertThat(service.obtenerPorId("f1")).isSameAs(f);

        when(figuras.findById("x")).thenReturn(Optional.empty());
        assertThatThrownBy(() -> service.obtenerPorId("x"))
                .isInstanceOf(FiguraNoEncontradaException.class);
    }

    // ---------------------------------------------------------------
    // obtenerPorIdDto
    // ---------------------------------------------------------------

    @Test
    void obtenerPorIdDtoConUsuarioAutenticado_incluyeValoracionUsuario() {
        Figura f = figura();
        f.setColoresIds(List.of("rojo"));

        UserDetailsImpl userDetails = mock(UserDetailsImpl.class);
        when(userDetails.getId()).thenReturn("u1");

        when(figuras.findById("f1")).thenReturn(Optional.of(f));
        when(categorias.findById("c1")).thenReturn(Optional.of(categoria()));
        when(colores.findAllById(anyList())).thenReturn(List.of(color("rojo", "Rojo", "#ff0000")));
        when(ratings.obtenerResumenValoraciones("f1")).thenReturn(resumen(4.0, 5L));
        when(ratings.obtenerValoracionUsuario("u1", "f1")).thenReturn(new ValoracionDto(5));

        FiguraDetalleDto dto = service.obtenerPorIdDto("f1", userDetails);

        assertThat(dto.getColores()).hasSize(1);
        assertThat(dto.getValoracionUsuario()).isEqualTo(5);
        assertThat(dto.getValoracionMedia()).isEqualTo(4.0);
    }

    @Test
    void obtenerPorIdDtoSinUsuario_valoracionUsuarioCero() {
        Figura f = figura();

        when(figuras.findById("f1")).thenReturn(Optional.of(f));
        when(categorias.findById("c1")).thenReturn(Optional.of(categoria()));
        when(ratings.obtenerResumenValoraciones("f1")).thenReturn(resumen(0.0, 0L));

        FiguraDetalleDto dto = service.obtenerPorIdDto("f1", null);

        assertThat(dto.getValoracionUsuario()).isEqualTo(0);
        verify(ratings, never()).obtenerValoracionUsuario(anyString(), anyString());
    }

    @Test
    void obtenerPorIdDtoOmiteColorInexistente() {
        Figura f = figura();
        f.setColoresIds(List.of("rojo", "verde"));

        when(figuras.findById("f1")).thenReturn(Optional.of(f));
        when(categorias.findById("c1")).thenReturn(Optional.of(categoria()));
        when(colores.findAllById(anyList())).thenReturn(List.of(color("rojo", "Rojo", "#ff0000")));
        when(ratings.obtenerResumenValoraciones("f1")).thenReturn(resumen(0.0, 0L));

        FiguraDetalleDto dto = service.obtenerPorIdDto("f1", null);

        assertThat(dto.getColores()).hasSize(1);
    }

    @Test
    void obtenerPorIdDtoFallaSiFiguraNoExiste() {
        when(figuras.findById("x")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.obtenerPorIdDto("x", null))
                .isInstanceOf(FiguraNoEncontradaException.class);
    }

    @Test
    void obtenerPorIdDtoFallaSiCategoriaNoExiste() {
        Figura f = figura();

        when(figuras.findById("f1")).thenReturn(Optional.of(f));
        when(categorias.findById("c1")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.obtenerPorIdDto("f1", null))
                .isInstanceOf(CategoriaNoEncontradaException.class);
    }

    // ---------------------------------------------------------------
    // eliminar
    // ---------------------------------------------------------------

    @Test
    void eliminarFiguraYSusDependencias() {
        Figura f = figura();
        f.setImagenPrincipal("main.png");
        f.setFileId_imagenPrincipal("main-file-id");
        f.setImagenesSecundarias(List.of("a.png", "b.png"));
        f.setFileId_imagenesSecundarias(List.of("a-file-id", "b-file-id"));

        when(figuras.findById("f1")).thenReturn(Optional.of(f));

        service.eliminar("f1");

        verify(figuras).deleteById("f1");
        verify(ratings).eliminarValoracionesPorFigura("f1");
        verify(comments).eliminarComentariosPorFigura("f1");
        verify(imageService).deleteImage("main-file-id");
        verify(imageService).deleteImage("a-file-id");
        verify(imageService).deleteImage("b-file-id");
    }

    @Test
    void eliminarSinImagenesNoLlamaDeleteImage() {
        Figura f = figura();
        f.setFileId_imagenPrincipal(null);
        f.setFileId_imagenesSecundarias(null);

        when(figuras.findById("f1")).thenReturn(Optional.of(f));

        service.eliminar("f1");

        verify(imageService, never()).deleteImage(anyString());
        verify(figuras).deleteById("f1");
        verify(ratings).eliminarValoracionesPorFigura("f1");
        verify(comments).eliminarComentariosPorFigura("f1");
    }

    @Test
    void eliminarFallaSiFiguraNoExiste() {
        when(figuras.findById("x")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.eliminar("x"))
                .isInstanceOf(FiguraNoEncontradaException.class);

        verify(figuras, never()).deleteById(anyString());
        verify(ratings, never()).eliminarValoracionesPorFigura(anyString());
        verify(comments, never()).eliminarComentariosPorFigura(anyString());
        verify(imageService, never()).deleteImage(anyString());
    }

    // ---------------------------------------------------------------
    // crear
    // ---------------------------------------------------------------

    @Test
    void crearSinImagenesNoLlamaStorage() {
        Figura f = figura();
        f.setColoresIds(List.of());

        when(categorias.findById("c1")).thenReturn(Optional.of(categoria()));

        when(figuras.save(any(Figura.class))).thenAnswer(invocation -> {
            Figura saved = invocation.getArgument(0);
            saved.setId("f1");
            return saved;
        });

        when(ratings.obtenerResumenValoraciones("f1")).thenReturn(resumen(0.0, 0L));

        var result = service.crear(f, null, null);

        assertThat(result).isNotNull();
        assertThat(result.getImagenPrincipal())
                .isEqualTo("https://ik.imagekit.io/8hlhxb9hx/manitas-Crochet/default.webp");

        verify(imageService, never()).uploadImage(anyString(), anyString(), any());
        verify(figuras, times(2)).save(any(Figura.class));
    }

    @Test
    void crearConPrincipalVaciaYSecundariasVaciasNoGuardaArchivos() {
        Figura f = figura();

        MockMultipartFile principalVacia = new MockMultipartFile(
                "principal",
                "",
                "image/png",
                new byte[0]);

        when(categorias.findById("c1")).thenReturn(Optional.of(categoria()));

        when(figuras.save(any(Figura.class))).thenAnswer(invocation -> {
            Figura saved = invocation.getArgument(0);
            saved.setId("f1");
            return saved;
        });

        when(ratings.obtenerResumenValoraciones("f1")).thenReturn(resumen(0.0, 0L));

        var result = service.crear(f, principalVacia, List.of());

        assertThat(result).isNotNull();
        verify(imageService, never()).uploadImage(anyString(), anyString(), any());
    }

    @Test
    void rechazaCategoriaYColorInexistentesAlCrear() {
        Figura f = figura();
        f.setColoresIds(List.of("rojo"));

        when(categorias.findById("c1")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.crear(f, null, null))
                .isInstanceOf(CategoriaNoEncontradaException.class);

        when(categorias.findById("c1")).thenReturn(Optional.of(categoria()));
        when(colores.findById("rojo")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.crear(f, null, null))
                .isInstanceOf(ColorNoEncontradoException.class);
    }

    @Test
    void crearGuardaImagenPrincipalYSecundariasNoVacias() {
        Figura f = figura();
        f.setColoresIds(List.of("rojo"));

        MockMultipartFile principal = new MockMultipartFile(
                "principal",
                "principal.png",
                "image/png",
                new byte[] { 1 });

        MockMultipartFile secundaria = new MockMultipartFile(
                "secundaria",
                "secundaria.png",
                "image/png",
                new byte[] { 2 });

        when(categorias.findById("c1")).thenReturn(Optional.of(categoria()));
        when(colores.findById("rojo")).thenReturn(Optional.of(color("rojo", "Rojo", "#ff0000")));

        when(figuras.save(any(Figura.class))).thenAnswer(invocation -> {
            Figura saved = invocation.getArgument(0);
            saved.setId("f1");
            return saved;
        });

        when(imageService.uploadImage(anyString(), anyString(), any()))
                .thenReturn(new ImageUploadResultDto("imagen.png", "file-id-1"));
        when(ratings.obtenerResumenValoraciones("f1")).thenReturn(resumen(0.0, 0L));

        assertThat(service.crear(f, principal, List.of(secundaria))).isNotNull();

        verify(imageService).uploadImage(anyString(), anyString(), eq(principal));
        verify(imageService).uploadImage(anyString(), anyString(), eq(secundaria));
        verify(figuras, times(2)).save(any(Figura.class));
    }

    // ---------------------------------------------------------------
    // actualizar
    // ---------------------------------------------------------------

    @Test
    void actualizarReemplazaImagenesYEliminaLasAnteriores() {
        Figura actual = figura();
        actual.setImagenPrincipal("anterior.png");
        actual.setFileId_imagenPrincipal("anterior-file-id");
        actual.setImagenesSecundarias(List.of("sec-a.png"));
        actual.setFileId_imagenesSecundarias(List.of("prev-sec-a-file-id"));

        Figura cambios = figura();
        cambios.setNombre("Gato");
        cambios.setColoresIds(List.of());

        MockMultipartFile principal = new MockMultipartFile(
                "principal",
                "principal.png",
                "image/png",
                new byte[] { 1 });

        MockMultipartFile secundaria = new MockMultipartFile(
                "secundaria",
                "secundaria.png",
                "image/png",
                new byte[] { 2 });

        when(figuras.findById("f1")).thenReturn(Optional.of(actual));
        when(categorias.findById("c1")).thenReturn(Optional.of(categoria()));
        when(imageService.uploadImage(anyString(), anyString(), any()))
                .thenReturn(new ImageUploadResultDto("nueva.png", "new-file-id"));
        when(figuras.save(actual)).thenReturn(actual);
        when(ratings.obtenerResumenValoraciones("f1")).thenReturn(resumen(0.0, 0L));

        assertThat(service.actualizar("f1", cambios, principal, List.of(secundaria))).isNotNull();

        verify(imageService).deleteImage("anterior-file-id");
        verify(imageService).deleteImage("prev-sec-a-file-id");
        verify(imageService).uploadImage(anyString(), anyString(), eq(principal));
        verify(imageService).uploadImage(anyString(), anyString(), eq(secundaria));
        verify(figuras).save(actual);
    }

    @Test
    void actualizarValidaColoresExistentesYActualizaSinImagenes() {
        Figura actual = figura();

        Figura cambios = figura();
        cambios.setNombre("Conejo");
        cambios.setDescripcion("Conejo actualizado");
        cambios.setColoresIds(List.of("azul"));

        when(figuras.findById("f1")).thenReturn(Optional.of(actual));
        when(categorias.findById("c1")).thenReturn(Optional.of(categoria()));
        when(colores.findById("azul")).thenReturn(Optional.of(color("azul", "Azul", "#0000ff")));
        when(figuras.save(actual)).thenReturn(actual);
        when(ratings.obtenerResumenValoraciones("f1")).thenReturn(resumen(0.0, 0L));

        var result = service.actualizar("f1", cambios, null, null);

        assertThat(result).isNotNull();
        assertThat(result.getNombre()).isEqualTo("Conejo");

        verify(imageService, never()).uploadImage(anyString(), anyString(), any());
        verify(imageService, never()).deleteImage(anyString());
        verify(figuras).save(actual);
    }

    @Test
    void actualizarFallaSiFiguraNoExiste() {
        Figura cambios = figura();

        when(figuras.findById("x")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.actualizar("x", cambios, null, null))
                .isInstanceOf(FiguraNoEncontradaException.class);

        verify(categorias, never()).findById(anyString());
        verify(figuras, never()).save(any(Figura.class));
    }

    @Test
    void actualizarFallaSiCategoriaNoExiste() {
        Figura actual = figura();
        Figura cambios = figura();

        when(figuras.findById("f1")).thenReturn(Optional.of(actual));
        when(categorias.findById("c1")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.actualizar("f1", cambios, null, null))
                .isInstanceOf(CategoriaNoEncontradaException.class);

        verify(figuras, never()).save(any(Figura.class));
    }

    @Test
    void actualizarFallaSiColorNoExiste() {
        Figura actual = figura();

        Figura cambios = figura();
        cambios.setColoresIds(List.of("verde"));

        when(figuras.findById("f1")).thenReturn(Optional.of(actual));
        when(categorias.findById("c1")).thenReturn(Optional.of(categoria()));
        when(colores.findById("verde")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.actualizar("f1", cambios, null, null))
                .isInstanceOf(ColorNoEncontradoException.class);

        verify(figuras, never()).save(any(Figura.class));
        verify(imageService, never()).uploadImage(anyString(), anyString(), any());
    }

    @Test
    void actualizarConImagenPrincipalNuevaPeroAnteriorNullNoBorraAnterior() {
        Figura actual = figura();
        actual.setImagenPrincipal(null);

        Figura cambios = figura();
        cambios.setNombre("Gato");

        MockMultipartFile principal = new MockMultipartFile(
                "principal",
                "principal.png",
                "image/png",
                new byte[] { 1 });

        when(figuras.findById("f1")).thenReturn(Optional.of(actual));
        when(categorias.findById("c1")).thenReturn(Optional.of(categoria()));
        when(imageService.uploadImage(anyString(), anyString(), any()))
                .thenReturn(new ImageUploadResultDto("gato.png", "gato-file-id"));
        when(figuras.save(actual)).thenReturn(actual);
        when(ratings.obtenerResumenValoraciones("f1")).thenReturn(resumen(0.0, 0L));

        var result = service.actualizar("f1", cambios, principal, null);

        assertThat(result.getImagenPrincipal()).isEqualTo("gato.png");

        verify(imageService, never()).deleteImage(anyString());
        verify(imageService).uploadImage(anyString(), anyString(), eq(principal));
    }

    @Test
    void actualizarConSecundariasNuevasPeroAnterioresNullNoBorraAnteriores() {
        Figura actual = figura();
        actual.setImagenesSecundarias(null);

        Figura cambios = figura();
        cambios.setNombre("Zorro");

        MockMultipartFile secundaria = new MockMultipartFile(
                "secundaria",
                "secundaria.png",
                "image/png",
                new byte[] { 2 });

        when(figuras.findById("f1")).thenReturn(Optional.of(actual));
        when(categorias.findById("c1")).thenReturn(Optional.of(categoria()));
        when(imageService.uploadImage(anyString(), anyString(), any()))
                .thenReturn(new ImageUploadResultDto("zorro-1.png", "zorro-file-id"));
        when(figuras.save(actual)).thenReturn(actual);
        when(ratings.obtenerResumenValoraciones("f1")).thenReturn(resumen(0.0, 0L));

        var result = service.actualizar("f1", cambios, null, List.of(secundaria));

        assertThat(result.getImagenesSecundarias()).containsExactly("zorro-1.png");

        verify(imageService, never()).deleteImage(anyString());
        verify(imageService).uploadImage(anyString(), anyString(), eq(secundaria));
    }

    @Test
    void actualizarConSecundariaVaciaNoLaGuarda() {
        Figura actual = figura();
        actual.setImagenesSecundarias(List.of("antigua.png"));
        actual.setFileId_imagenesSecundarias(List.of("antigua-file-id"));

        Figura cambios = figura();
        cambios.setNombre("Pato");

        MockMultipartFile vacia = new MockMultipartFile(
                "vacia",
                "",
                "image/png",
                new byte[0]);

        when(figuras.findById("f1")).thenReturn(Optional.of(actual));
        when(categorias.findById("c1")).thenReturn(Optional.of(categoria()));
        when(figuras.save(actual)).thenReturn(actual);
        when(ratings.obtenerResumenValoraciones("f1")).thenReturn(resumen(0.0, 0L));

        var result = service.actualizar("f1", cambios, null, List.of(vacia));

        assertThat(result.getImagenesSecundarias()).isEmpty();

        verify(imageService).deleteImage("antigua-file-id");
        verify(imageService, never()).uploadImage(anyString(), anyString(), any());
    }

    @Test
    void actualizarConListaSecundariasVaciaNoBorraNiGuardaSecundarias() {
        Figura actual = figura();
        actual.setImagenPrincipal("main.png");
        actual.setImagenesSecundarias(List.of("antigua.png"));
        actual.setFileId_imagenesSecundarias(List.of("antigua-file-id"));

        Figura cambios = figura();
        cambios.setNombre("Pez");

        when(figuras.findById("f1")).thenReturn(Optional.of(actual));
        when(categorias.findById("c1")).thenReturn(Optional.of(categoria()));
        when(figuras.save(actual)).thenReturn(actual);
        when(ratings.obtenerResumenValoraciones("f1")).thenReturn(resumen(0.0, 0L));

        var result = service.actualizar("f1", cambios, null, List.of());

        assertThat(result).isNotNull();
        assertThat(result.getImagenesSecundarias()).containsExactly("antigua.png");

        verify(imageService, never()).deleteImage("antigua-file-id");
        verify(imageService, never()).uploadImage(anyString(), anyString(), any());
    }
}