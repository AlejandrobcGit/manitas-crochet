package com.manitascrochet.backend.service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationOperation;
import org.springframework.data.mongodb.core.aggregation.AggregationResults;
import org.springframework.data.mongodb.core.convert.MongoConverter;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import org.bson.Document;

import com.manitascrochet.backend.dto.ColorResponseDto;
import com.manitascrochet.backend.dto.FiguraDetalleDto;
import com.manitascrochet.backend.dto.FiguraListadoDto;
import com.manitascrochet.backend.dto.ImageUploadResultDto;
import com.manitascrochet.backend.dto.PaginaFigurasDto;
import com.manitascrochet.backend.dto.ResumenValoracionDto;
import com.manitascrochet.backend.dto.ValoracionDto;
import com.manitascrochet.backend.dto.VisualizacionesPorFiguraDto;
import com.manitascrochet.backend.exception.GlobalExceptionHandler.CategoriaNoEncontradaException;
import com.manitascrochet.backend.exception.GlobalExceptionHandler.ColorNoEncontradoException;
import com.manitascrochet.backend.exception.GlobalExceptionHandler.FiguraNoEncontradaException;
import com.manitascrochet.backend.model.Categoria;
import com.manitascrochet.backend.model.Color;
import com.manitascrochet.backend.model.Favorito;
import com.manitascrochet.backend.model.Figura;
import com.manitascrochet.backend.model.Valoracion;
import com.manitascrochet.backend.repository.CategoriaRepository;
import com.manitascrochet.backend.repository.ColorRepository;
import com.manitascrochet.backend.repository.FavoritoRepository;
import com.manitascrochet.backend.repository.FiguraRepository;
import com.manitascrochet.backend.repository.ValoracionRepository;
import com.manitascrochet.backend.repository.VisualizacionRepository;
import com.manitascrochet.backend.security.UserDetailsImpl;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class FiguraService {

        private final FiguraRepository figuraRepository;
        private final CategoriaRepository categoriaRepository;
        private final ColorRepository colorRepository;
        private final ValoracionRepository valoracionRepository;
        private final FavoritoRepository favoritoRepository;
        private final VisualizacionRepository visualizacionRepository;
        private final ImageService imageService;
        // si elimnara el FileStorageService, cuando imageUpload esta lito
        // private final FileStorageService fileStorageService;
        private final ValoracionService valoracionService;
        private final ComentarioService comentarioService;
        @Value("${imagekit.url.endpoint}")
        private String imageUrl;
        @Value("${imagekit.folder}")
        private String imageFolder;

        /*
         * permite trabajar directamente con MongoDB sin pasar por un repositorio
         * (MongoRepository).
         * Es la implementación principal de la interfaz MongoOperations y proporciona
         * operaciones para crear, consultar, actualizar y borrar documentos.
         */

        private final MongoTemplate mongoTemplate;

        // Conversor de Documentos BSON (del $facet) a objetos Figura
        private final MongoConverter mongoConverter;

        // Obtener todas las figuras en formato DTO con paginación
        public PaginaFigurasDto obtenerTodasDto(
                        String nombre,
                        String categoriaId,
                        boolean soloFavoritos,
                        int page,
                        int size,
                        String sortBy,
                        UserDetailsImpl userDetails) {

                Query query = new Query();
                List<Criteria> criterios = new ArrayList<>();

                if (nombre != null && !nombre.isBlank()) {
                        criterios.add(
                                        Criteria.where("nombre")
                                                        .regex(Pattern.quote(nombre.trim()), "i"));
                }

                if (categoriaId != null && !categoriaId.isBlank()) {
                        criterios.add(
                                        Criteria.where("categoriaId")
                                                        .is(categoriaId));
                }

                // Filtro de solo favoritos: solo figuras que el usuario tiene marcadas.
                // Los favoritos se guardan con el USERNAME como usuarioId (ver FavoritoController).
                String usuarioId = (userDetails != null) ? userDetails.getUsername() : null;
                if (soloFavoritos) {
                        // Un usuario anónimo no tiene favoritos → lista vacía
                        if (usuarioId == null) {
                                return new PaginaFigurasDto(List.of(), page, 0, 0, size);
                        }
                        List<String> favoritosIds = favoritoRepository
                                        .findByUsuarioIdAndActivoTrue(usuarioId)
                                        .stream()
                                        .map(Favorito::getFiguraId)
                                        .toList();
                        // Sin favoritos → lista vacía
                        if (favoritosIds.isEmpty()) {
                                return new PaginaFigurasDto(List.of(), page, 0, 0, size);
                        }
                        criterios.add(
                                        Criteria.where("id").in(favoritosIds));
                }

                if (!criterios.isEmpty()) {
                        query.addCriteria(
                                        new Criteria().andOperator(
                                                        criterios.toArray(Criteria[]::new)));
                }

                        // -----------------------------------------------------------------
                        // ORDENACIÓN / PAGINACIÓN EN UNA SOLA IDA A MONGODB (MEJORA 1 + 3)
                        // -----------------------------------------------------------------
                        // Se usa $facet: en respuesta única obtenemos el TOTAL (count) y los
                        // DOCUMENTOS de la página. Así evitamos el patrón anterior de
                        // (1) count + (2) find = 2 round-trips por petición.
                        //
                        // Ordenación por VALORADOS / POPULARES: a partir de la MEJORA 3
                        // (métricas denormalizadas en la propia Figura) ahora se ordena con
                        // Sort NATIVO de MongoDB sobre los campos puntuacionMedia /
                        // numVisualizaciones, en lugar de cargar todos los IDs en memoria,
                        // calcular la clave en Java y paginar manualmente (era ~400-500 ms).
                        boolean esValorados = "valorados".equals(sortBy);
                        boolean esPopulares = "populares".equals(sortBy);

                        String campoOrden = esValorados ? "puntuacionMedia"
                                        : esPopulares ? "numVisualizaciones"
                                                        : "fechaCreacion";

                        // Recientes (default) / antiguos → fechaCreacion DESC/ASC
                        Sort sort;
                        if (esValorados) {
                                sort = Sort.by(Sort.Order.desc("puntuacionMedia"));
                        } else if (esPopulares) {
                                sort = Sort.by(Sort.Order.desc("numVisualizaciones"));
                        } else if ("antiguos".equals(sortBy)) {
                                sort = Sort.by(Sort.Order.asc("fechaCreacion"));
                        } else {
                                sort = Sort.by(Sort.Order.desc("fechaCreacion"));
                        }

                        // Aplicar el sort ANTES del $facet (las dos sub-pipelines lo heredan)
                        query.with(sort);

                        // Filtro combinado (nombre, categoría, favoritos) aplicado ANTES del
                        // $facet: así count y página evalúan exactamente los mismos documentos.
                        // Un Query/Criteria único se reutiliza en ambas sub-pipelines (MEJORA 1).
                        Criteria criterioFiltro = criterios.isEmpty()
                                        ? new Criteria()
                                        : new Criteria().andOperator(
                                                        criterios.toArray(Criteria[]::new));

                        // $facet: en UNA sola consulta devuelve el total (count) y la página
                        // (skip+limit), evitando el patrón count+find = 2 idas a Mongo (MEJORA 1).
                        AggregationResults<Document> resultados = mongoTemplate.aggregate(
                                        Aggregation.newAggregation(
                                                        Aggregation.match(criterioFiltro),
                                                        Aggregation.sort(sort),
                                                        Aggregation.facet(
                                                                        Aggregation.count().as("total"))
                                                                                        .as("totales")
                                                                                        .and(
                                                                                                        Aggregation.skip((long) page * size),
                                                                                                        Aggregation.limit(size))
                                                                                        .as("pagina")),
                                        Figura.class,
                                        Document.class);

                        List<Document> documentos = resultados.getMappedResults();
                        if (documentos.isEmpty()) {
                                return new PaginaFigurasDto(List.of(), page, 0, 0, size);
                        }

                        // totales → [{total: N}] — $count devuelve Integer (Int32), no Long
                        List<Document> totalesDocs = (List<Document>) documentos.get(0).get("totales");
                        long totalElementos = totalesDocs.isEmpty() ? 0
                                        : ((Number) totalesDocs.get(0).get("total")).longValue();

                        if (totalElementos == 0 || (long) page * size >= totalElementos) {
                                return new PaginaFigurasDto(List.of(), page,
                                                totalPaginas(totalElementos, size), totalElementos, size);
                        }

                        // página → [{...figura...}]
                        List<Document> paginaDocs = (List<Document>) documentos.get(0).get("pagina");
                        List<Figura> figurasPagina = paginaDocs.stream()
                                        .map(doc -> mongoConverter.read(Figura.class, doc))
                                        .toList();

                        return construirPaginaFigurasDto(
                                        figurasPagina, page, size, totalElementos, usuarioId);
                }

        // Ordenación por valoración media o popularidad (visualizaciones)
        // Se obtienen todos los IDs filtrados, se calcula la clave de orden en memoria,
        // se ordenan y se pagina manualmente.
        private PaginaFigurasDto obtenerTodasDtoOrdenJava(
                        Query query,
                        int page,
                        int size,
                        String sortBy,
                        String usuarioId) {

                // 1. Obtener todos los IDs que cumplen los filtros (sin sort, sin paginación)
                Query queryIds = Query.of(query);
                queryIds.fields().include("_id");
                List<String> todosIds = mongoTemplate.find(queryIds, Figura.class)
                                .stream()
                                .map(Figura::getId)
                                .toList();

                long totalElementos = todosIds.size();

                if (totalElementos == 0) {
                        return new PaginaFigurasDto(List.of(), page, 0, 0, size);
                }

                // 2. Calcular la clave de ordenación para cada ID
                Map<String, Double> sortKeys;
                if ("valorados".equals(sortBy)) {
                        sortKeys = calcularSortKeysValoracion(todosIds);
                } else {
                        sortKeys = calcularSortKeysPopularidad(todosIds);
                }

                // 3. Ordenar IDs por clave DESC (empates se resuelven por fechaCreacion DESC)
                Map<String, LocalDateTime> fechasMap = obtenerFechasCreacion(todosIds);

                List<String> idsOrdenados = todosIds.stream()
                                .sorted((a, b) -> {
                                        double ka = sortKeys.getOrDefault(a, 0.0);
                                        double kb = sortKeys.getOrDefault(b, 0.0);
                                        int cmp = Double.compare(kb, ka); // DESC
                                        if (cmp != 0) return cmp;
                                        // Empate: más reciente primero
                                        LocalDateTime fa = fechasMap.getOrDefault(a, LocalDateTime.MIN);
                                        LocalDateTime fb = fechasMap.getOrDefault(b, LocalDateTime.MIN);
                                        return fb.compareTo(fa);
                                })
                                .toList();

                // 4. Paginar los IDs
                int totalPaginas = totalPaginas(totalElementos, size);
                if (page >= totalPaginas) {
                        return new PaginaFigurasDto(List.of(), page, totalPaginas, totalElementos, size);
                }

                int from = page * size;
                int to = (int) Math.min((long) from + size, totalElementos);
                List<String> paginaIds = idsOrdenados.subList(from, to);

                // 5. Fetch de las figuras de la página manteniendo el orden
                List<Figura> figurasPagina = fetchFigurasOrdenadas(paginaIds);

                return construirPaginaFigurasDto(figurasPagina, page, size, totalElementos, usuarioId);
        }

        private Map<String, Double> calcularSortKeysValoracion(List<String> figuraIds) {
                List<Valoracion> valoraciones = valoracionRepository.findByFiguraIdIn(figuraIds);
                return valoraciones.stream()
                                .collect(Collectors.groupingBy(
                                                Valoracion::getFiguraId,
                                                Collectors.averagingInt(Valoracion::getPuntuacion)));
        }

        private Map<String, Double> calcularSortKeysPopularidad(List<String> figuraIds) {
                if (figuraIds.isEmpty()) return Map.of();
                return visualizacionRepository.contarAgrupadasPorFiguraId(figuraIds)
                                .stream()
                                .collect(Collectors.toMap(
                                                VisualizacionesPorFiguraDto::getFiguraId,
                                                dto -> (double) dto.getTotal()));
        }

        private Map<String, LocalDateTime> obtenerFechasCreacion(List<String> figuraIds) {
                if (figuraIds.isEmpty()) return Map.of();
                Query queryFechas = Query.query(Criteria.where("id").in(figuraIds));
                queryFechas.fields().include("_id").include("fechaCreacion");
                List<Figura> figuras = mongoTemplate.find(queryFechas, Figura.class);
                return figuras.stream()
                                .collect(Collectors.toMap(
                                                Figura::getId,
                                                f -> f.getFechaCreacion() != null
                                                                ? f.getFechaCreacion()
                                                                : LocalDateTime.MIN));
        }

        // Fetch de figuras por IDs manteniendo el orden dado
        private List<Figura> fetchFigurasOrdenadas(List<String> ids) {
                if (ids.isEmpty()) return List.of();
                List<Figura> todas = mongoTemplate.find(
                                Query.query(Criteria.where("id").in(ids)),
                                Figura.class);
                Map<String, Figura> byId = todas.stream()
                                .collect(Collectors.toMap(Figura::getId, f -> f));
                return ids.stream()
                                .map(byId::get)
                                .filter(java.util.Objects::nonNull)
                                .toList();
        }

        // Construir PaginaFigurasDto a partir de una lista de figuras de la página
        private PaginaFigurasDto construirPaginaFigurasDto(
                        List<Figura> figuras,
                        int page,
                        int size,
                        long totalElementos,
                        String usuarioId) {

                // Obtener todas las categorías necesarias en una sola consulta
                Set<String> categoriaIds = figuras.stream()
                                .map(Figura::getCategoriaId)
                                .collect(Collectors.toSet());

                Map<String, String> categoriasMap = categoriaRepository.findAllById(categoriaIds)
                                .stream()
                                .collect(Collectors.toMap(
                                                Categoria::getId,
                                                Categoria::getNombre));

                // Obtener valoraciones en una sola consulta
                List<String> figuraIds = figuras.stream()
                                .map(Figura::getId)
                                .toList();

                List<Valoracion> valoraciones = valoracionRepository.findByFiguraIdIn(figuraIds);

                // Agrupamos las valoraciones por figura
                Map<String, List<Valoracion>> valoracionesPorFigura = valoraciones.stream()
                                .collect(Collectors.groupingBy(
                                                Valoracion::getFiguraId));

                // Calcular promedio
                Map<String, ResumenValoracionDto> resumenValoracionesMap = valoracionesPorFigura.entrySet()
                                .stream()
                                .collect(Collectors.toMap(
                                                Map.Entry::getKey,
                                                entry -> {
                                                        List<Valoracion> lista = entry.getValue();
                                                        double media = lista.stream()
                                                                        .mapToInt(Valoracion::getPuntuacion)
                                                                        .average()
                                                                        .orElse(0.0);
                                                        return new ResumenValoracionDto(
                                                                        media,
                                                                        (long) lista.size());
                                                }));

                // Favoritos de la página actual en una sola consulta (usuario autenticado)
                Set<String> favoritosPagina = (usuarioId == null)
                                ? Set.of()
                                : favoritoRepository
                                                .findByUsuarioIdAndFiguraIdInAndActivoTrue(usuarioId, figuraIds)
                                                .stream()
                                                .map(Favorito::getFiguraId)
                                                .collect(Collectors.toSet());

                List<FiguraListadoDto> resultado = figuras.stream()
                                .map(figura -> convertirFiguraListadoDto(
                                                figura,
                                                categoriasMap,
                                                resumenValoracionesMap,
                                                favoritosPagina.contains(figura.getId())))
                                .toList();

                return new PaginaFigurasDto(
                                resultado,
                                page,
                                totalPaginas(totalElementos, size),
                                totalElementos,
                                size);
        }

        private int totalPaginas(long totalElementos, int size) {
                return (int) Math.ceil((double) totalElementos / size);
        }

        // Convertir Figura a FiguraListadoDto
        private FiguraListadoDto convertirFiguraListadoDto(
                        Figura figura,
                        Map<String, String> categoriasMap,
                        Map<String, ResumenValoracionDto> resumenValoracionesMap,
                        boolean esFavorito) {

                String categoria = categoriasMap.get(figura.getCategoriaId());

                if (categoria == null) {
                        throw new CategoriaNoEncontradaException(
                                        figura.getCategoriaId());
                }

                ResumenValoracionDto resumen = resumenValoracionesMap.getOrDefault(
                                figura.getId(),
                                new ResumenValoracionDto(0.0, 0L));

                String imagenPrincipal = figura.getImagenPrincipal() == null
                                || figura.getImagenPrincipal().isBlank()
                                                ? imageUrl + "/" + imageFolder + "/default.webp"
                                                : figura.getImagenPrincipal();

                return new FiguraListadoDto(
                                figura.getId(),
                                figura.getNombre(),
                                categoria,
                                imagenPrincipal,
                                figura.getAltura(),
                                figura.getAncho(),
                                resumen.getValoracionMedia(),
                                resumen.getTotalValoraciones(),
                                esFavorito);
        }

        // Obtener figura por id
        public Figura obtenerPorId(String id) {
                return figuraRepository.findById(id)
                                .orElseThrow(() -> new FiguraNoEncontradaException(id));
        }

        // Obtener figura por id en formato DTO
        public FiguraDetalleDto obtenerPorIdDto(String id, UserDetailsImpl userDetails) {

                return figuraRepository.findById(id)
                                .map(figura -> convertirFiguraDetalleDto(figura, userDetails))
                                .orElseThrow(() -> new FiguraNoEncontradaException(id));
        }

        // Convertir Figura a FiguraDetalleDto
        private FiguraDetalleDto convertirFiguraDetalleDto(Figura figura, UserDetailsImpl userDetails) {

                String categoria = categoriaRepository
                                .findById(figura.getCategoriaId())
                                .map(Categoria::getNombre)
                                .orElseThrow(() -> new CategoriaNoEncontradaException(figura.getCategoriaId()));

                // Colores del detalle en UNA SOLA consulta (MEJORA 2: batch findAllById).
                // Antes cada color era un findById → patrón N+1 (una ida a Mongo por color).
                // Con el batch de la Figura (3-4 colores típicos) se hace 1 única ida.
                List<ColorResponseDto> colores = colorRepository
                                .findAllById(figura.getColoresIds())
                                .stream()
                                .map(color -> new ColorResponseDto(
                                                color.getNombre(),
                                                color.getCodigo()))
                                .toList();

                if (figura.getImagenPrincipal() == null || figura.getImagenPrincipal().isBlank()) {
                        figura.setImagenPrincipal(imageUrl + "/" + imageFolder + "/default.webp");
                }

                ResumenValoracionDto resumenValoracionDto = valoracionService
                                .obtenerResumenValoraciones(figura.getId());

                ValoracionDto valoracionUsuario = (userDetails == null)
                                ? new ValoracionDto(0)
                                : valoracionService.obtenerValoracionUsuario(userDetails.getId(), figura.getId());
                return new FiguraDetalleDto(
                                figura.getId(),
                                figura.getNombre(),
                                figura.getDescripcion(),
                                categoria,
                                figura.getDificultad(),
                                figura.getAutor(),
                                figura.getImagenPrincipal(),
                                figura.getImagenesSecundarias(),
                                colores,
                                figura.getAltura(),
                                figura.getAncho(),
                                figura.getPeso(),
                                resumenValoracionDto.getValoracionMedia(),
                                valoracionUsuario.getPuntuacion(),
                                resumenValoracionDto.getTotalValoraciones());
        }

        // Crear figura
        public FiguraDetalleDto crear(Figura figura, MultipartFile imagenPrincipal,
                        List<MultipartFile> imagenesSecundarias) {

                categoriaRepository.findById(figura.getCategoriaId())
                                .orElseThrow(() -> new CategoriaNoEncontradaException(
                                                figura.getCategoriaId()));

                for (String colorId : figura.getColoresIds()) {

                        colorRepository.findById(colorId)
                                        .orElseThrow(() -> new ColorNoEncontradoException(
                                                        colorId));
                }

                figura.setFechaCreacion(LocalDateTime.now());
                figura.setFechaModificacion(LocalDateTime.now());

                // Guardamos primero SIN imágenes para que Mongo genere el ID.
                Figura figuraGuardada = figuraRepository.save(figura);

                // Guardar imagen principal
                if (imagenPrincipal != null && !imagenPrincipal.isEmpty()) {

                        /*
                         * String filename = fileStorageService.store(
                         * imagenPrincipal,
                         * figuraGuardada.getId(),
                         * figuraGuardada.getNombre());
                         */

                        ImageUploadResultDto imageUploadResultDto = imageService.uploadImage(
                                        figuraGuardada.getId(),
                                        figuraGuardada.getNombre(),
                                        imagenPrincipal);

                        figuraGuardada.setImagenPrincipal(imageUploadResultDto.getUrl());
                        figuraGuardada.setFileId_imagenPrincipal(imageUploadResultDto.getFileId());
                }

                // Guardar imágenes secundarias, cada una con un sufijo -1, -2, -3...
                if (imagenesSecundarias != null && !imagenesSecundarias.isEmpty()) {

                        List<String> nombresImagenes = new ArrayList<>();
                        List<String> fileIdImagenes = new ArrayList<>();
                        int indice = 1;

                        for (MultipartFile imagen : imagenesSecundarias) {

                                if (!imagen.isEmpty()) {

                                        String nombreDiferenciado = figuraGuardada.getNombre() + "-" + indice;

                                        /*
                                         * String filename = fileStorageService.store(
                                         * imagen,
                                         * figuraGuardada.getId(),
                                         * nombreDiferenciado);
                                         */
                                        ImageUploadResultDto imageUploadResultDto = imageService.uploadImage(
                                                        figuraGuardada.getId(),
                                                        nombreDiferenciado,
                                                        imagen);

                                        nombresImagenes.add(imageUploadResultDto.getUrl());
                                        fileIdImagenes.add(imageUploadResultDto.getFileId());
                                        indice++;
                                }
                        }

                        figuraGuardada.setImagenesSecundarias(nombresImagenes);
                        figuraGuardada.setFileId_imagenesSecundarias(fileIdImagenes);

                }

                // Segundo save: ahora sí con los nombres de archivo ya calculados.
                return convertirFiguraDetalleDto(figuraRepository.save(figuraGuardada), null);
        }

        // Actualizar figura
        public FiguraDetalleDto actualizar(
                        String id,
                        Figura figuraActualizada,
                        MultipartFile imagenPrincipal,
                        List<MultipartFile> imagenesSecundarias) {

                Figura figura = figuraRepository.findById(id)
                                .orElseThrow(() -> new FiguraNoEncontradaException(id));

                categoriaRepository.findById(
                                figuraActualizada.getCategoriaId())
                                .orElseThrow(() -> new CategoriaNoEncontradaException(
                                                figuraActualizada.getCategoriaId()));

                for (String colorId : figuraActualizada.getColoresIds()) {

                        colorRepository.findById(colorId)
                                        .orElseThrow(() -> new ColorNoEncontradoException(
                                                        colorId));
                }

                // ----------------------------------------------------
                // DATOS BÁSICOS
                // ----------------------------------------------------

                figura.setNombre(figuraActualizada.getNombre());
                figura.setDescripcion(figuraActualizada.getDescripcion());
                figura.setCategoriaId(figuraActualizada.getCategoriaId());
                figura.setDificultad(figuraActualizada.getDificultad());
                figura.setAltura(figuraActualizada.getAltura());
                figura.setAncho(figuraActualizada.getAncho());
                figura.setPeso(figuraActualizada.getPeso());
                figura.setAutor(figuraActualizada.getAutor());
                figura.setColoresIds(figuraActualizada.getColoresIds());

                // ----------------------------------------------------
                // IMAGEN PRINCIPAL
                // ----------------------------------------------------

                if (imagenPrincipal != null && !imagenPrincipal.isEmpty()) {

                        // Guardar nuevo y borrar anterior
                        String previousFileId = figura.getFileId_imagenPrincipal();

                        ImageUploadResultDto imageUploadResultDto = imageService.uploadImage(
                                        figura.getId(),
                                        figura.getNombre(),
                                        imagenPrincipal);

                        figura.setImagenPrincipal(imageUploadResultDto.getUrl());
                        figura.setFileId_imagenPrincipal(imageUploadResultDto.getFileId());

                        if (previousFileId != null && !previousFileId.isBlank()) {
                                imageService.deleteImage(previousFileId);
                        }
                }

                // ----------------------------------------------------
                // IMÁGENES SECUNDARIAS
                // ----------------------------------------------------

                if (imagenesSecundarias != null && !imagenesSecundarias.isEmpty()) {

                        List<String> previousFileIds = figura.getFileId_imagenesSecundarias() == null
                                        ? new ArrayList<>()
                                        : new ArrayList<>(figura.getFileId_imagenesSecundarias());

                        List<String> nombresImagenes = new ArrayList<>();
                        List<String> fileIdImagenes = new ArrayList<>();
                        int indice = 1;

                        for (MultipartFile imagen : imagenesSecundarias) {

                                if (!imagen.isEmpty()) {

                                        String nombreDiferenciado = figura.getNombre() + "-" + indice;

                                        ImageUploadResultDto imageUploadResultDto = imageService.uploadImage(
                                                        figura.getId(),
                                                        nombreDiferenciado,
                                                        imagen);

                                        nombresImagenes.add(imageUploadResultDto.getUrl());
                                        fileIdImagenes.add(imageUploadResultDto.getFileId());
                                        indice++;
                                }
                        }

                        figura.setImagenesSecundarias(nombresImagenes);
                        figura.setFileId_imagenesSecundarias(fileIdImagenes);

                        // Borrar imágenes secundarias anteriores
                        for (String imagenId : previousFileIds) {
                                imageService.deleteImage(imagenId);
                        }

                }

                // ----------------------------------------------------
                // FECHA MODIFICACIÓN
                // ----------------------------------------------------

                figura.setFechaModificacion(LocalDateTime.now());

                return convertirFiguraDetalleDto(figuraRepository.save(figura), null);
        }

        // Eliminar figura
        public void eliminar(String id) {

                Figura figura = figuraRepository.findById(id)
                                .orElseThrow(() -> new FiguraNoEncontradaException(id));

                // 1. Borrar imagen principal de ImageKit
                if (figura.getFileId_imagenPrincipal() != null
                                && !figura.getFileId_imagenPrincipal().isBlank()) {
                        imageService.deleteImage(figura.getFileId_imagenPrincipal());
                }

                // 2. Borrar imágenes secundarias de ImageKit
                if (figura.getFileId_imagenesSecundarias() != null) {
                        for (String fileId : figura.getFileId_imagenesSecundarias()) {
                                if (fileId != null && !fileId.isBlank()) {
                                        imageService.deleteImage(fileId);
                                }
                        }
                }

                // 3. Borrar datos relacionados
                valoracionService.eliminarValoracionesPorFigura(id);
                comentarioService.eliminarComentariosPorFigura(id);

                // 4. Borrar figura
                figuraRepository.deleteById(id);
        }
}