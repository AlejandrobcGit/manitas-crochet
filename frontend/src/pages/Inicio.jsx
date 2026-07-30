import { useEffect, useState, useMemo } from "react";
import { FaHeart, FaRegHeart } from "react-icons/fa";

import { useFiguras } from "../hooks/useFiguras";
import { useCategorias } from "../hooks/useCategorias";
import { useDebounce } from "../hooks/useDebounce";
import { useFavoritos } from "../hooks/useFavoritos";

import FiguraCard from "../components/FiguraCard";
import Header from "../components/Header";
import Footer from "../components/Footer";

import "./Inicio.css";

function Inicio() {

    const {
        figuras,
        recargarFiguras,
        loading,
        error
    } = useFiguras();

    const {
        categorias,
        loading: loadingCategorias
    } = useCategorias();

    const { favoritos, cambiarFavorito } = useFavoritos();

    const [nombre, setNombre] = useState("");
    const [categoriaId, setCategoriaId] = useState("");
    const [soloFavoritos, setSoloFavoritos] = useState(false);

    const nombreDebounced = useDebounce(nombre, 400);

    // Cada vez que cambie la busqueda (debounced) o la categoria, pedimos al backend
    useEffect(() => {
        recargarFiguras(nombreDebounced, categoriaId);
    }, [nombreDebounced, categoriaId]);

    // El filtro de favoritos se aplica en el cliente sobre lo que ya trajo el backend
    const figurasMostradas = useMemo(() => {
        if (!soloFavoritos) return figuras;
        return figuras.filter((figura) => favoritos.includes(figura.id));
    }, [figuras, favoritos, soloFavoritos]);

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
                                    className="catalog-search"
                                    placeholder="Escribe un nombre..."
                                    value={nombre}
                                    onChange={(e) => setNombre(e.target.value)}
                                />
                            </label>

                            {!loadingCategorias && (
                                <>
                                    <label className="catalog-field">
                                        <span>Categoría</span>
                                        <select
                                            className="catalog-select"
                                            value={categoriaId}
                                            onChange={(e) => setCategoriaId(e.target.value)}
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
                                        onClick={() => setSoloFavoritos((prev) => !prev)}
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
                                        onClick={() => {
                                            setNombre("");
                                            setCategoriaId("");
                                            setSoloFavoritos(false);
                                        }}
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

                                    {figurasMostradas.map(figura => (
                                        <FiguraCard
                                            key={figura.id}
                                            figura={figura}
                                            esFavorito={favoritos.includes(figura.id)}
                                            onToggleFavorito={cambiarFavorito}
                                        />
                                    ))}

                                </div>

                            )}

                        {!loading &&
                            !error &&
                            figurasMostradas.length === 0 && (

                                <p className="catalog-status">
                                    No se encontraron figuras.
                                </p>

                            )}
                    </div>
                </section>

            </main>

            <Footer />

        </div>

    );
}

export default Inicio;