import { useEffect, useState, useCallback } from "react";
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

    // Recupera el estado guardado una sola vez al montar (al volver del detalle se restaura)
    const [estadoInicial] = useState(leerEstadoGuardado);

    const [nombre, setNombre] = useState(estadoInicial?.nombre ?? "");
    const [categoriaId, setCategoriaId] = useState(estadoInicial?.categoriaId ?? "");
    const [soloFavoritos, setSoloFavoritos] = useState(estadoInicial?.soloFavoritos ?? false);
    const [paginaActual, setPaginaActual] = useState(estadoInicial?.page ?? 0);
    const [paginaInput, setPaginaInput] = useState("");

    const nombreDebounced = useDebounce(nombre, 400);

    const cargar = useCallback((page) => {
        recargarFiguras({
            nombre: nombreDebounced,
            categoriaId,
            soloFavoritos,
            page,
            size: 12
        });
    }, [recargarFiguras, nombreDebounced, categoriaId, soloFavoritos]);

    // Guarda el estado (filtros + página) cuando cambian, para restaurarlo al volver
    useEffect(() => {
        guardarEstado({
            nombre: nombreDebounced,
            categoriaId,
            soloFavoritos,
            page: paginaActual
        });
    }, [nombreDebounced, categoriaId, soloFavoritos, paginaActual]);

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

    const limpiarFiltros = () => {
        setNombre("");
        setCategoriaId("");
        setSoloFavoritos(false);
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
