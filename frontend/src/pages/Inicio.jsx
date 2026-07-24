import { useEffect, useState } from "react";

import { useFiguras } from "../hooks/useFiguras";
import { useCategorias } from "../hooks/useCategorias";
import { useDebounce } from "../hooks/useDebounce";

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

    const [nombre, setNombre] = useState("");
    const [categoriaId, setCategoriaId] = useState("");

    const nombreDebounced = useDebounce(nombre, 400);

    // Cada vez que cambie la busqueda (debounced) o la categoria, pedimos al backend
    useEffect(() => {
        recargarFiguras(nombreDebounced, categoriaId);
    }, [nombreDebounced, categoriaId]);

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
                                        className="catalog-clear"
                                        onClick={() => {
                                            setNombre("");
                                            setCategoriaId("");
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

                                    {figuras.map(figura => (
                                        <FiguraCard
                                            key={figura.id}
                                            figura={figura}
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
                    </div>
                </section>

            </main>

            <Footer />

        </div>

    );
}

export default Inicio;