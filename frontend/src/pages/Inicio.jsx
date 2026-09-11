import { useEffect, useState, useCallback } from "react";
import { useSearchParams } from "react-router-dom";
import { FaHeart, FaRegHeart } from "react-icons/fa";

import { useFiguras } from "../hooks/useFiguras";
import { useCategorias } from "../hooks/useCategorias";
import { useDebounce } from "../hooks/useDebounce";
import { useFavoritos } from "../hooks/useFavoritos";

import FiguraCard from "../components/FiguraCard";
import Header from "../components/Header";
import Footer from "../components/Footer";

import "./Inicio.css";

const SESSION_KEY = "mcc-catalogo-estado";

function leerEstadoGuardado() {
    try {
        const raw = sessionStorage.getItem(SESSION_KEY);
        return raw ? JSON.parse(raw) : null;
    } catch {
        return null;
    }
}

function guardarEstado(estado) {
    try {
        sessionStorage.setItem(SESSION_KEY, JSON.stringify(estado));
    } catch {
        // almacenamiento no disponible
    }
}

// La URL tiene prioridad (compartible y resistente a refresco).
// Si no hay parámetros (p. ej. al volver vía "Volver al catálogo" -> /#catalogo),
// se recupera el último estado guardado en sessionStorage.
function leerEstadoInicial(searchParams) {
    if (searchParams.toString().length > 0) {
        const page = parseInt(searchParams.get("page"), 10);
        return {
            nombre: searchParams.get("nombre") ?? "",
            categoriaId: searchParams.get("categoriaId") ?? "",
            soloFavoritos: searchParams.get("soloFavoritos") === "true",
            sortBy: searchParams.get("sortBy") ?? "recientes",
            page: isNaN(page) || page < 0 ? 0 : page
        };
    }

    const guardado = leerEstadoGuardado();
    if (guardado && typeof guardado === "object") {
        return {
            nombre: guardado.nombre ?? "",
            categoriaId: guardado.categoriaId ?? "",
            soloFavoritos: guardado.soloFavoritos ?? false,
            sortBy: guardado.sortBy ?? "recientes",
            page: guardado.page ?? 0
        };
    }

    return {
        nombre: "",
        categoriaId: "",
        soloFavoritos: false,
        sortBy: "recientes",
        page: 0
    };
}

function Inicio() {

    const {
        figuras,
        recargarFiguras,
        loading,
        error,
        pagina
    } = useFiguras();

    const {
        categorias,
        loading: loadingCategorias
    } = useCategorias();

    const { cambiarFavorito } = useFavoritos();

    const [searchParams, setSearchParams] = useSearchParams();

    // Recupera el estado una sola vez al montar: URL primero, sessionStorage como respaldo
    const [estadoInicial] = useState(() => leerEstadoInicial(searchParams));

    const [nombre, setNombre] = useState(estadoInicial.nombre);
    const [categoriaId, setCategoriaId] = useState(estadoInicial.categoriaId);
    const [soloFavoritos, setSoloFavoritos] = useState(estadoInicial.soloFavoritos);
    const [ordenarPor, setOrdenarPor] = useState(estadoInicial.sortBy);
    const [paginaActual, setPaginaActual] = useState(estadoInicial.page);
    const [paginaInput, setPaginaInput] = useState("");

    const nombreDebounced = useDebounce(nombre, 400);

    const cargar = useCallback((page) => {
        recargarFiguras({
            nombre: nombreDebounced,
            categoriaId,
            soloFavoritos,
            page,
            size: 12,
            sortBy: ordenarPor
        });
    }, [recargarFiguras, nombreDebounced, categoriaId, soloFavoritos, ordenarPor]);

    // Sincroniza filtros y ordenación en la URL
    useEffect(() => {
        const params = {};
        if (nombre) params.nombre = nombre;
        if (categoriaId) params.categoriaId = categoriaId;
        if (soloFavoritos) params.soloFavoritos = "true";
        if (ordenarPor && ordenarPor !== "recientes") params.sortBy = ordenarPor;
        if (paginaActual > 0) params.page = String(paginaActual);
        setSearchParams(params, { replace: true });
    }, [nombre, categoriaId, soloFavoritos, ordenarPor, paginaActual, setSearchParams]);

    // Guarda el estado (filtros + orden + página) para restaurarlo al volver del detalle
    useEffect(() => {
        guardarEstado({
            nombre,
            categoriaId,
            soloFavoritos,
            sortBy: ordenarPor,
            page: paginaActual
        });
    }, [nombre, categoriaId, soloFavoritos, ordenarPor, paginaActual]);

    // Al montar carga la página guardada; al cambiar un filtro recarga en la página actual
    useEffect(() => {
        cargar(paginaActual);
    }, [cargar, paginaActual]);

    const cambiarNombre = (e) => {
        setNombre(e.target.value);
        setPaginaActual(0);
    };

    const cambiarCategoria = (e) => {
        setCategoriaId(e.target.value);
        setPaginaActual(0);
    };

    const toggleSoloFavoritos = () => {
        setSoloFavoritos((prev) => !prev);
        setPaginaActual(0);
    };

    const cambiarOrden = (e) => {
        setOrdenarPor(e.target.value);
        setPaginaActual(0);
    };

    const limpiarFiltros = () => {
        setNombre("");
        setCategoriaId("");
        setSoloFavoritos(false);
        setOrdenarPor("recientes");
        setPaginaActual(0);
    };

    const onToggleFavorito = async (figuraId) => {
        try {
            await cambiarFavorito(figuraId);
            // Reconsultamos para actualizar el flag esFavorito (y quitar si filtramos favoritos)
            cargar(paginaActual);
        } catch (error) {
            console.error(error);
        }
    };

    const irAPagina = (pagina) => {
        setPaginaActual(pagina);
    };

    const irAPaginaInput = () => {
        const num = parseInt(paginaInput, 10);
        if (isNaN(num)) return;
        // usuario escribe 1..N, internamente es 0..N-1
        irAPagina(Math.min(Math.max(num - 1, 0), pagina.totalPaginas - 1));
    };

    // Sincroniza el input con la página actual cuando cambia externamente
    useEffect(() => {
        setPaginaInput(String(pagina.paginaActual + 1));
    }, [pagina.paginaActual]);

    return (

        <div className="app">

            <Header />

            <main className="catalog-page">

                <section className="catalog-layout" id="catalogo">

                    <aside className="catalog-sidebar">
                        <div className="catalog-panel">
                            <h2 className="catalog-title">Filtrar figuras</h2>

                            <label className="catalog-field">
                                <span>Buscar por nombre</span>
                                <input
                                    type="text"
                                    id="catalog-search"
                                    name="catalogSearch"
                                    className="catalog-search"
                                    placeholder="Escribe un nombre..."
                                    value={nombre}
                                    onChange={cambiarNombre}
                                />
                            </label>

                            {!loadingCategorias && (
                                <>
                                    <label className="catalog-field">
                                        <span>Categoría</span>
                                        <select
                                            id="catalog-category"
                                            name="catalogCategory"
                                            className="catalog-select"
                                            value={categoriaId}
                                            onChange={cambiarCategoria}
                                        >
                                            <option value="">Todas las categorías</option>

                                            {categorias.map((cat) => (
                                                <option key={cat.id} value={cat.id}>
                                                    {cat.nombre}
                                                </option>
                                            ))}

                                        </select>
                                    </label>

                                    <button
                                        type="button"
                                        className={`catalog-favorite-toggle ${soloFavoritos ? "catalog-favorite-toggle--active" : ""}`}
                                        onClick={toggleSoloFavoritos}
                                        aria-pressed={soloFavoritos}
                                    >
                                        {soloFavoritos
                                            ? <FaHeart />
                                            : <FaRegHeart />
                                        }
                                        <span>Favoritos</span>
                                    </button>

                                    <label className="catalog-field">
                                        <span>Ordenar por</span>
                                        <select
                                            id="catalog-sort"
                                            name="catalogSort"
                                            className="catalog-select"
                                            value={ordenarPor}
                                            onChange={cambiarOrden}
                                        >
                                            <option value="recientes">Más recientes</option>
                                            <option value="antiguos">Más antiguos</option>
                                            <option value="valorados">Más valorados</option>
                                            <option value="populares">Más populares</option>
                                        </select>
                                    </label>

                                    <button
                                        type="button"
                                        className="catalog-clear"
                                        onClick={limpiarFiltros}
                                    >
                                        Limpiar filtros
                                    </button>
                                </>
                            )}
                        </div>
                    </aside>

                    <div className="catalog-content">
                        {loading && (
                            <p className="catalog-status">
                                Cargando figuras...
                            </p>
                        )}

                        {error && (
                            <p className="catalog-status catalog-status--error">
                                Error al cargar figuras
                            </p>
                        )}

                        {!loading &&
                            !error && (

                                <div className="catalog-grid">

                                    {figuras.map(figura => (
                                        <FiguraCard
                                            key={figura.id}
                                            figura={figura}
                                            esFavorito={figura.esFavorito}
                                            onToggleFavorito={onToggleFavorito}
                                        />
                                    ))}

                                </div>

                            )}

                        {!loading &&
                            !error &&
                            figuras.length === 0 && (

                                <p className="catalog-status">
                                    No se encontraron figuras.
                                </p>

                            )}

                        {!loading &&
                            !error &&
                            pagina.totalPaginas > 1 && (
                                <div className="paginacion">
                                    <button
                                        type="button"
                                        className="paginacion__btn"
                                        disabled={pagina.paginaActual <= 0}
                                        onClick={() => irAPagina(pagina.paginaActual - 1)}
                                    >
                                        ‹ Anterior
                                    </button>
                                    <div className="paginacion__go">
                                        <label
                                            className="paginacion__label"
                                            htmlFor="paginacion-input"
                                        >
                                            Página
                                        </label>
                                        <input
                                            id="paginacion-input"
                                            type="number"
                                            min="1"
                                            max={pagina.totalPaginas}
                                            className="paginacion__input"
                                            value={paginaInput}
                                            onChange={(e) => setPaginaInput(e.target.value)}
                                            onKeyDown={(e) => {
                                                if (e.key === "Enter") irAPaginaInput();
                                            }}
                                        />
                                        <span className="paginacion__info">
                                            de {pagina.totalPaginas}
                                        </span>
                                        <button
                                            type="button"
                                            className="paginacion__btn"
                                            onClick={irAPaginaInput}
                                        >
                                            Ir
                                        </button>
                                    </div>
                                    <button
                                        type="button"
                                        className="paginacion__btn"
                                        disabled={pagina.paginaActual >= pagina.totalPaginas - 1}
                                        onClick={() => irAPagina(pagina.paginaActual + 1)}
                                    >
                                        Siguiente ›
                                    </button>
                                </div>
                            )}
                    </div>
                </section>

            </main>

            <Footer />

        </div>

    );
}

export default Inicio;
