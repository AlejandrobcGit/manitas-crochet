package com.manitascrochet.backend.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
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
    FileStorageService files;

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

    @Test
    void obtienePorIdYFallaAusente() {
        Figura f = figura();

        when(figuras.findById("f1")).thenReturn(Optional.of(f));
        assertThat(service.obtenerPorId("f1")).isSameAs(f);

        when(figuras.findById("x")).thenReturn(Optional.empty());
        assertThatThrownBy(() -> service.obtenerPorId("x"))
                .isInstanceOf(FiguraNoEncontradaException.class);
    }

    @Test
    void transformaListadoConCategoriaValoracionEImagenPorDefecto() {
        Figura f = figura();

        when(mongo.find(any(Query.class), eq(Figura.class))).thenReturn(List.of(f));
        when(categorias.findById("c1")).thenReturn(Optional.of(categoria()));
        when(ratings.obtenerResumenValoraciones("f1")).thenReturn(resumen(4.5, 2L));

        var result = service.obtenerTodasDto("oso", "c1");

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getCategoria()).isEqualTo("Animales");
        assertThat(result.get(0).getImagenPrincipal()).isEqualTo("default.png");
        assertThat(result.get(0).getValoracionMedia()).isEqualTo(4.5);
        assertThat(result.get(0).getTotalValoraciones()).isEqualTo(2L);

        verify(mongo).find(any(Query.class), eq(Figura.class));
    }

    @Test
    void obtenerTodasDtoSinFiltrosTambienConsultaMongo() {
        // Cubre las ramas false de nombre != null && !blank,
        // categoriaId != null && !blank y !criterios.isEmpty().
        Figura f = figura();
        f.setImagenPrincipal("oso.png");

        when(mongo.find(any(Query.class), eq(Figura.class))).thenReturn(List.of(f));
        when(categorias.findById("c1")).thenReturn(Optional.of(categoria()));
        when(ratings.obtenerResumenValoraciones("f1")).thenReturn(resumen(3.0, 1L));

        var result = service.obtenerTodasDto(null, "   ");

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getImagenPrincipal()).isEqualTo("oso.png");

        verify(mongo).find(any(Query.class), eq(Figura.class));
    }

    @Test
    void obtenerTodasDtoFallaSiCategoriaNoExisteAlConvertirListado() {
        // Cubre el orElseThrow privado de convertirFiguraListadoDto.
        Figura f = figura();

        when(mongo.find(any(Query.class), eq(Figura.class))).thenReturn(List.of(f));
        when(categorias.findById("c1")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.obtenerTodasDto(null, null))
                .isInstanceOf(CategoriaNoEncontradaException.class);

        verify(ratings, never()).obtenerResumenValoraciones(anyString());
    }

    @Test
    void eliminaFiguraYSusDependencias() {
        Figura f = figura();
        f.setImagenPrincipal("main.png");
        f.setImagenesSecundarias(List.of("a.png", "b.png"));

        when(figuras.findById("f1")).thenReturn(Optional.of(f));

        service.eliminar("f1");

        verify(figuras).deleteById("f1");
        verify(ratings).eliminarValoracionesPorFigura("f1");
        verify(comments).eliminarComentariosPorFigura("f1");
        verify(files).delete("main.png");
        verify(files).delete("a.png");
        verify(files).delete("b.png");
    }

    @Test
    void eliminarFiguraSinImagenesSecundariasNoIteraSecundarias() {
        // Cubre la rama false de imagenesSecundarias != null.
        Figura f = figura();
        f.setImagenPrincipal("main.png");
        f.setImagenesSecundarias(null);

        when(figuras.findById("f1")).thenReturn(Optional.of(f));

        service.eliminar("f1");

        verify(figuras).deleteById("f1");
        verify(files).delete("main.png");
        verify(files, never()).delete("a.png");
        verify(files, never()).delete("b.png");
    }

    @Test
    void eliminarFallaSiFiguraNoExiste() {
        // Cubre el orElseThrow de eliminar.
        when(figuras.findById("x")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.eliminar("x"))
                .isInstanceOf(FiguraNoEncontradaException.class);

        verify(figuras, never()).deleteById(anyString());
        verify(files, never()).delete(anyString());
    }

    @Test
    void obtieneDetalleParaInvitadoYUsuarioConColores() {
        Figura f = figura();
        f.setColoresIds(List.of("rojo", "inexistente"));

        Color rojo = color("rojo", "Rojo", "#ff0000");

        when(figuras.findById("f1")).thenReturn(Optional.of(f));
        when(categorias.findById("c1")).thenReturn(Optional.of(categoria()));
        when(colores.findById("rojo")).thenReturn(Optional.of(rojo));
        when(colores.findById("inexistente")).thenReturn(Optional.empty());
        when(ratings.obtenerResumenValoraciones("f1")).thenReturn(resumen(4.0, 1L));

        assertThat(service.obtenerPorIdDto("f1", null).getColores()).hasSize(1);

        UserDetailsImpl user = new UserDetailsImpl();
        user.setId("u1");

        when(ratings.obtenerValoracionUsuario("u1", "f1")).thenReturn(new ValoracionDto(5));

        assertThat(service.obtenerPorIdDto("f1", user).getValoracionUsuario()).isEqualTo(5);
    }

    @Test
    void obtenerDetalleUsaImagenPorDefectoSiImagenPrincipalEsNull() {
        // Ya cubrías imagen en blanco. Este caso cubre la rama null.
        Figura f = figura();
        f.setImagenPrincipal(null);

        when(figuras.findById("f1")).thenReturn(Optional.of(f));
        when(categorias.findById("c1")).thenReturn(Optional.of(categoria()));
        when(ratings.obtenerResumenValoraciones("f1")).thenReturn(resumen(0.0, 0L));

        var result = service.obtenerPorIdDto("f1", null);

        assertThat(result.getImagenPrincipal()).isEqualTo("default.png");
    }

    @Test
    void obtenerDetalleFallaSiFiguraNoExiste() {
        // Cubre el orElseThrow de obtenerPorIdDto.
        when(figuras.findById("x")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.obtenerPorIdDto("x", null))
                .isInstanceOf(FiguraNoEncontradaException.class);
    }

    @Test
    void obtenerDetalleFallaSiCategoriaNoExiste() {
        // Cubre el orElseThrow privado de convertirFiguraDetalleDto.
        Figura f = figura();

        when(figuras.findById("f1")).thenReturn(Optional.of(f));
        when(categorias.findById("c1")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.obtenerPorIdDto("f1", null))
                .isInstanceOf(CategoriaNoEncontradaException.class);

        verify(ratings, never()).obtenerResumenValoraciones(anyString());
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

        MockMultipartFile vacia = new MockMultipartFile(
                "vacia",
                "",
                "image/png",
                new byte[0]);

        when(categorias.findById("c1")).thenReturn(Optional.of(categoria()));
        when(colores.findById("rojo")).thenReturn(Optional.of(color("rojo", "Rojo", "#ff0000")));

        when(figuras.save(any(Figura.class))).thenAnswer(invocation -> {
            Figura saved = invocation.getArgument(0);
            saved.setId("f1");
            return saved;
        });

        when(files.store(any(), eq("f1"), anyString())).thenReturn("imagen.png");
        when(ratings.obtenerResumenValoraciones("f1")).thenReturn(resumen(0.0, 0L));

        assertThat(service.crear(f, principal, List.of(vacia, secundaria))).isNotNull();

        verify(files).store(principal, "f1", "Oso");
        verify(files).store(secundaria, "f1", "Oso-1");
        verify(figuras, times(2)).save(any(Figura.class));
    }

    @Test
    void crearSinImagenesNoLlamaStorage() {
        // Cubre las ramas false de imagenPrincipal != null && !empty
        // e imagenesSecundarias != null && !empty.
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
        assertThat(result.getImagenPrincipal()).isEqualTo("default.png");

        verify(files, never()).store(any(), anyString(), anyString());
        verify(figuras, times(2)).save(any(Figura.class));
    }

    @Test
    void crearConPrincipalVaciaYSecundariasVaciasNoGuardaArchivos() {
        // Cubre que MultipartFile vacío no se almacena.
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
        verify(files, never()).store(any(), anyString(), anyString());
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
    void actualizarReemplazaImagenesYEliminaLasAnteriores() {
        Figura actual = figura();
        actual.setImagenPrincipal("anterior.png");
        actual.setImagenesSecundarias(List.of("sec-a.png"));

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
        when(files.store(any(), eq("f1"), anyString())).thenReturn("nueva.png");
        when(figuras.save(actual)).thenReturn(actual);
        when(ratings.obtenerResumenValoraciones("f1")).thenReturn(resumen(0.0, 0L));

        assertThat(service.actualizar("f1", cambios, principal, List.of(secundaria))).isNotNull();

        verify(files).delete("anterior.png");
        verify(files).delete("sec-a.png");
        verify(files).store(principal, "f1", "Gato");
        verify(files).store(secundaria, "f1", "Gato-1");
        verify(figuras).save(actual);
    }

    @Test
    void actualizarValidaColoresExistentesYActualizaSinImagenes() {
        // Cubre el for de colores en actualizar y las ramas false de imágenes.
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

        verify(files, never()).store(any(), anyString(), anyString());
        verify(files, never()).delete(anyString());
        verify(figuras).save(actual);
    }

    @Test
    void actualizarFallaSiFiguraNoExiste() {
        // Cubre el orElseThrow inicial de actualizar.
        Figura cambios = figura();

        when(figuras.findById("x")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.actualizar("x", cambios, null, null))
                .isInstanceOf(FiguraNoEncontradaException.class);

        verify(categorias, never()).findById(anyString());
        verify(figuras, never()).save(any(Figura.class));
    }

    @Test
    void actualizarFallaSiCategoriaNoExiste() {
        // Cubre la rama no cubierta de categoría inexistente en actualizar.
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
        // Cubre ColorNoEncontradoException dentro de actualizar.
        Figura actual = figura();

        Figura cambios = figura();
        cambios.setColoresIds(List.of("verde"));

        when(figuras.findById("f1")).thenReturn(Optional.of(actual));
        when(categorias.findById("c1")).thenReturn(Optional.of(categoria()));
        when(colores.findById("verde")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.actualizar("f1", cambios, null, null))
                .isInstanceOf(ColorNoEncontradoException.class);

        verify(figuras, never()).save(any(Figura.class));
        verify(files, never()).store(any(), anyString(), anyString());
    }

    @Test
    void actualizarConImagenPrincipalNuevaPeroAnteriorNullNoBorraAnterior() {
        // Cubre la rama false de figura.getImagenPrincipal() != null.
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
        when(files.store(principal, "f1", "Gato")).thenReturn("gato.png");
        when(figuras.save(actual)).thenReturn(actual);
        when(ratings.obtenerResumenValoraciones("f1")).thenReturn(resumen(0.0, 0L));

        var result = service.actualizar("f1", cambios, principal, null);

        assertThat(result.getImagenPrincipal()).isEqualTo("gato.png");

        verify(files, never()).delete(anyString());
        verify(files).store(principal, "f1", "Gato");
    }

    @Test
    void actualizarConImagenPrincipalNuevaPeroAnteriorBlankNoBorraAnterior() {
        // Cubre la rama false de !figura.getImagenPrincipal().isBlank().
        Figura actual = figura();
        actual.setImagenPrincipal("   ");

        Figura cambios = figura();
        cambios.setNombre("Gato");

        MockMultipartFile principal = new MockMultipartFile(
                "principal",
                "principal.png",
                "image/png",
                new byte[] { 1 });

        when(figuras.findById("f1")).thenReturn(Optional.of(actual));
        when(categorias.findById("c1")).thenReturn(Optional.of(categoria()));
        when(files.store(principal, "f1", "Gato")).thenReturn("gato.png");
        when(figuras.save(actual)).thenReturn(actual);
        when(ratings.obtenerResumenValoraciones("f1")).thenReturn(resumen(0.0, 0L));

        var result = service.actualizar("f1", cambios, principal, null);

        assertThat(result.getImagenPrincipal()).isEqualTo("gato.png");

        verify(files, never()).delete(anyString());
        verify(files).store(principal, "f1", "Gato");
    }

    @Test
    void actualizarConSecundariasNuevasPeroAnterioresNullNoBorraAnteriores() {
        // Cubre la rama false de figura.getImagenesSecundarias() != null.
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
        when(files.store(secundaria, "f1", "Zorro-1")).thenReturn("zorro-1.png");
        when(figuras.save(actual)).thenReturn(actual);
        when(ratings.obtenerResumenValoraciones("f1")).thenReturn(resumen(0.0, 0L));

        var result = service.actualizar("f1", cambios, null, List.of(secundaria));

        assertThat(result.getImagenesSecundarias()).containsExactly("zorro-1.png");

        verify(files, never()).delete(anyString());
        verify(files).store(secundaria, "f1", "Zorro-1");
    }

    @Test
    void actualizarConSecundariaVaciaNoLaGuarda() {
        // Cubre la rama false de if (!imagen.isEmpty()) en actualizar.
        Figura actual = figura();
        actual.setImagenesSecundarias(List.of("antigua.png"));

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

        verify(files).delete("antigua.png");
        verify(files, never()).store(any(), anyString(), anyString());
    }

    @Test
    void actualizarConListaSecundariasVaciaNoBorraNiGuardaSecundarias() {
        // Cubre la rama false de !imagenesSecundarias.isEmpty().
        Figura actual = figura();
        actual.setImagenPrincipal("main.png");
        actual.setImagenesSecundarias(List.of("antigua.png"));

        Figura cambios = figura();
        cambios.setNombre("Pez");

        when(figuras.findById("f1")).thenReturn(Optional.of(actual));
        when(categorias.findById("c1")).thenReturn(Optional.of(categoria()));
        when(figuras.save(actual)).thenReturn(actual);
        when(ratings.obtenerResumenValoraciones("f1")).thenReturn(resumen(0.0, 0L));

        var result = service.actualizar("f1", cambios, null, List.of());

        assertThat(result).isNotNull();
        assertThat(result.getImagenesSecundarias()).containsExactly("antigua.png");

        verify(files, never()).delete("antigua.png");
        verify(files, never()).store(any(), anyString(), anyString());
    }
}