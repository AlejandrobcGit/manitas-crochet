package com.manitascrochet.backend.model;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.CompoundIndexes;
import org.springframework.data.mongodb.core.index.IndexDirection;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import lombok.Data;

@Document(collection = "figuras")
@CompoundIndexes({
                // Catálogo: filtro por categoría + ordenación nativa (MEJORA 1 y 3).
                // MongoDB usa el índice cuando el $facet hereda el sort del catálogo.
                @CompoundIndex(name = "idx_figuras_categoria_fecha", def = "{ 'categoriaId': 1, 'fechaCreacion': -1 }"),
                @CompoundIndex(name = "idx_figuras_categoria_puntuacion", def = "{ 'categoriaId': 1, 'puntuacionMedia': -1 }"),
                @CompoundIndex(name = "idx_figuras_categoria_visualizaciones", def = "{ 'categoriaId': 1, 'numVisualizaciones': -1 }")
})
@Data
public class Figura {

    @Id
    private String id;

    private String nombre;

    private String descripcion;

    // Catálogo: filtro por categoría (covers existsByCategoriaId) y prefijo de los compuestos.
    @Indexed(direction = IndexDirection.ASCENDING)
    private String categoriaId;

    private Dificultad dificultad;

    private String autor;

    private String imagenPrincipal;
    
    private String fileId_imagenPrincipal;

    private List<String> imagenesSecundarias;

    private List<String> fileId_imagenesSecundarias;

    private List<String> coloresIds;

    private Integer altura;

    private Integer ancho;

    private Integer peso;

    // Catálogo SIN filtro de categoría: sort nativo por fechaCreacion (recientes/antiguos).
    @Indexed(name = "idx_fig_fecha", direction = IndexDirection.DESCENDING)
    private LocalDateTime fechaCreacion;

    private LocalDateTime fechaModificacion;

    // METRICAS DENORMALIZADAS (MEJORA 3):
    // Se almacenan en la propia figura para poder ordenar el catálogo con sort nativo de
    // MongoDB (valorados/populares) sin cargar toda la colección en memoria, y para evitar
    // recalcular la media leyendo todas las valoraciones en cada consulta del listado/detalle.
    private Double puntuacionMedia;

    private Long totalValoraciones;

    private Long numVisualizaciones;
}