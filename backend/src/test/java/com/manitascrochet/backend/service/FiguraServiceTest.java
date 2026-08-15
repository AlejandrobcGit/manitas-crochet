package com.manitascrochet.backend.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.mock.web.MockMultipartFile;

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
import com.manitascrochet.backend.repository.CategoriaRepository;
import com.manitascrochet.backend.repository.ColorRepository;
import com.manitascrochet.backend.repository.FiguraRepository;
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
    MongoTemplate mongo;

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

    // ---------------------------------------------------------------
    // obtenerTodasDto
    // ---------------------------------------------------------------

    @Test
    void obtenerTodasDtoSinFiltros_devuelveListaMapeada() {
        Figura f = figura();
        f.setImagenPrincipal("oso.png");

        when(mongo.find(any(Query.class), eq(Figura.class))).thenReturn(List.of(f));
        when(categorias.findById("c1")).thenReturn(Optional.of(categoria()));
        when(ratings.obtenerResumenValoraciones("f1")).thenReturn(resumen(4.5, 10L));

        List<FiguraListadoDto> resultado = service.obtenerTodasDto(null, null);

        assertThat(resultado).hasSize(1);
        FiguraListadoDto dto = resultado.get(0);
        assertThat(dto.getId()).isEqualTo("f1");
        assertThat(dto.getCategoria()).isEqualTo("Animales");
        assertThat(dto.getImagenPrincipal()).isEqualTo("oso.png");
        assertThat(dto.getValoracionMedia()).isEqualTo(4.5);
        assertThat(dto.getTotalValoraciones()).isEqualTo(10L);
    }

    @Test
    void obtenerTodasDtoUsaImagenPorDefectoCuandoEsBlank() {
        Figura f = figura();
        f.setImagenPrincipal("   ");

        when(mongo.find(any(Query.class), eq(Figura.class))).thenReturn(List.of(f));
        when(categorias.findById("c1")).thenReturn(Optional.of(categoria()));
        when(ratings.obtenerResumenValoraciones("f1")).thenReturn(resumen(0.0, 0L));

        List<FiguraListadoDto> resultado = service.obtenerTodasDto(null, null);

        assertThat(resultado.get(0).getImagenPrincipal())
                .isEqualTo("https://ik.imagekit.io/8hlhxb9hx/manitas-Crochet/default.webp");
    }

    @Test
    void obtenerTodasDtoConFiltros_delegaEnMongoTemplate() {
        when(mongo.find(any(Query.class), eq(Figura.class))).thenReturn(List.of());

        List<FiguraListadoDto> resultado = service.obtenerTodasDto("oso", "c1");

        assertThat(resultado).isEmpty();
        verify(mongo, times(1)).find(any(Query.class), eq(Figura.class));
    }

    @Test
    void obtenerTodasDtoListaVacia_noConsultaCategoriaNiValoraciones() {
        when(mongo.find(any(Query.class), eq(Figura.class))).thenReturn(List.of());

        List<FiguraListadoDto> resultado = service.obtenerTodasDto("", "");

        assertThat(resultado).isEmpty();
        verify(categorias, never()).findById(anyString());
        verify(ratings, never()).obtenerResumenValoraciones(anyString());
    }

    @Test
    void obtenerTodasDtoFallaSiCategoriaNoExiste() {
        when(mongo.find(any(Query.class), eq(Figura.class))).thenReturn(List.of(figura()));
        when(categorias.findById("c1")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.obtenerTodasDto(null, null))
                .isInstanceOf(CategoriaNoEncontradaException.class);
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
        when(colores.findById("rojo")).thenReturn(Optional.of(color("rojo", "Rojo", "#ff0000")));
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
        when(colores.findById("rojo")).thenReturn(Optional.of(color("rojo", "Rojo", "#ff0000")));
        when(colores.findById("verde")).thenReturn(Optional.empty());
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
        assertThat(result.getImagenPrincipal()).isEqualTo("https://ik.imagekit.io/8hlhxb9hx/manitas-Crochet/default.webp");

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

        when(imageService.uploadImage(anyString(), anyString(), any())).thenReturn(new ImageUploadResultDto("imagen.png","file-id-1"));
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
        when(imageService.uploadImage(anyString(), anyString(), any())).thenReturn(new ImageUploadResultDto("nueva.png","new-file-id"));
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
        when(imageService.uploadImage(anyString(), anyString(), any())).thenReturn(new ImageUploadResultDto("gato.png","gato-file-id"));
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
        when(imageService.uploadImage(anyString(), anyString(), any())).thenReturn(new ImageUploadResultDto("zorro-1.png","zorro-file-id"));
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