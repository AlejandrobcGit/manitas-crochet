import { useParams, Link } from "react-router-dom";

import { useFiguraDetalle } from "../hooks/useFiguraDetalle";

import GaleriaImagenes from "../components/GaleriaImagenes";
import Header from "../components/Header";
import Footer from "../components/Footer";

import "./FiguraDetalle.css";

function FiguraDetalle() {

    const { id } = useParams();

    const {
        figura,
        loading,
        error
    } = useFiguraDetalle(id);

    return (

        <div className="app">

            <Header />

            <main className="detail-main">

                {loading && (
                    <p className="catalog-status">
                        Cargando figura...
                    </p>
                )}

                {error && (
                    <p className="catalog-status catalog-status--error">
                        Error al cargar la figura
                    </p>
                )}

                {!loading && !error && !figura && (
                    <p className="catalog-status">
                        No se encontró la figura solicitada.
                    </p>
                )}

                {!loading && !error && figura && (

                    <>

                        <Link to="/#catalogo" className="detail-back">
                            ← Volver al catálogo
                        </Link>

                        <section className="detail">

                            <GaleriaImagenes
                                imagenPrincipal={figura.imagenPrincipal}
                                imagenesSecundarias={figura.imagenesSecundarias}
                                nombre={figura.nombre}
                            />

                            <div className="detail-info">

                                {figura.categoria && (
                                    <p className="detail-info__eyebrow">
                                        {figura.categoria}
                                    </p>
                                )}

                                <h1 className="detail-info__title">
                                    {figura.nombre}
                                </h1>

                                {figura.dificultad && (
                                    <span className="detail-info__badge">
                                        {figura.dificultad}
                                    </span>
                                )}

                                {figura.descripcion && (
                                    <p className="detail-info__desc">
                                        {figura.descripcion}
                                    </p>
                                )}

                                {(figura.altura || figura.ancho) && (
                                    <div className="detail-info__row">
                                        {figura.altura && (
                                            <span>Alto: {figura.altura} cm</span>
                                        )}
                                        {figura.ancho && (
                                            <span>Ancho: {figura.ancho} cm</span>
                                        )}
                                    </div>
                                )}

                                {figura.colores?.length > 0 && (

                                    <div className="detail-info__colores">

                                        {figura.colores.map(color => (

                                            <span
                                                key={color.nombre}
                                                className="detail-info__color"
                                                title={color.nombre}
                                            >
                                                <span
                                                    className="detail-info__swatch"
                                                    style={{ backgroundColor: color.codigo }}
                                                />
                                                {color.nombre}
                                            </span>

                                        ))}

                                    </div>

                                )}

                                {figura.autor && (
                                    <p className="detail-info__autor">
                                        Tejido por {figura.autor}
                                    </p>
                                )}

                            </div>

                        </section>

                    </>

                )}

            </main>

            <Footer />

        </div>

    );
}

export default FiguraDetalle;