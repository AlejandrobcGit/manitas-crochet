import { useMemo, useState } from "react";

import { useFiguras } from "../hooks/useFiguras";

import FiguraCard from "../components/FiguraCard";
import Header from "../components/Header";
import Footer from "../components/Footer";

import "./Inicio.css";

function Inicio() {

    const {
        figuras,
        loading,
        error
    } = useFiguras();

    const [categoria, setCategoria] =
        useState("Todas");

    const categorias = useMemo(() => {

        if (!figuras) return ["Todas"];

        const unicas = [
            ...new Set(
                figuras
                    .map(f => f.categoria)
                    .filter(Boolean)
            )
        ];

        return ["Todas", ...unicas];

    }, [figuras]);

    const figurasFiltradas = useMemo(() => {

        if (!figuras) return [];

        return categoria === "Todas"
            ? figuras
            : figuras.filter(
                f => f.categoria === categoria
            );

    }, [figuras, categoria]);

    return (

        <div className="app">

            <Header />

            <main>

                <section
                    className="catalog-hero"
                    id="catalogo">

                    <p className="catalog-hero__eyebrow">
                        Colección actual
                    </p>

                    <h1 className="catalog-hero__title">
                        Manitas Crochet
                    </h1>

                    <p className="catalog-hero__subtitle">
                        Figuras de crochet tejidas a mano, una a una.
                    </p>

                </section>

                {!loading &&
                    !error &&
                    categorias.length > 1 && (

                        <nav className="catalog-filter">

                            {categorias.map(cat => (

                                <button
                                    key={cat}
                                    className={
                                        "catalog-filter__item" +
                                        (
                                            cat === categoria
                                                ? " catalog-filter__item--active"
                                                : ""
                                        )
                                    }
                                    onClick={() =>
                                        setCategoria(cat)
                                    }
                                >
                                    {cat}
                                </button>

                            ))}

                        </nav>

                    )}

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

                            {figurasFiltradas.map(figura => (
                                <FiguraCard
                                    key={figura.id}
                                    figura={figura}
                                />
                            ))}

                        </div>

                    )}

            </main>

            <Footer />

        </div>

    );
}

export default Inicio;