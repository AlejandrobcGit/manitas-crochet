import { FaStar } from "react-icons/fa";

import { useComentarios } from "../hooks/useComentarios";

import "./ComentariosFigura.css";

function formatearFecha(fechaISO) {

    if (!fechaISO) return "";

    const fecha = new Date(fechaISO);

    return fecha.toLocaleDateString("es-ES", {
        day: "2-digit",
        month: "short",
        year: "numeric"
    });

}

function ValoracionComentario({ valoracion }) {

    if (valoracion === null || valoracion === undefined || valoracion === 0) {
        return (
            <span className="comentarios__valoracion comentarios__valoracion--vacia">
                Sin valorar
            </span>
        );
    }

    const starFillPercents = Array.from({ length: 5 }, (_, i) => {

        const diff = valoracion - i;

        if (diff >= 1) {
            return 100;
        }

        if (diff <= 0) {
            return 0;
        }

        return diff * 100;

    });

    return (

        <span
            className="comentarios__valoracion"
            aria-label={`Valoración ${valoracion} sobre 5`}
        >

            <span className="comentarios__stars">

                {starFillPercents.map((percent, i) => (

                    <span
                        key={i}
                        className="comentarios__star"
                    >

                        <FaStar className="comentarios__star-bg" />

                        <span
                            className="comentarios__star-fill"
                            style={{
                                width: `${percent}%`
                            }}
                        >
                            <FaStar />
                        </span>

                    </span>

                ))}

            </span>

        </span>

    );

}

function ComentariosFigura({ figuraId }) {

    const { comentarios, loading, error, refetch } = useComentarios(figuraId);

    return (

        <section className="comentarios">

            <h2 className="comentarios__title">
                Comentarios
                {comentarios.length > 0 && (
                    <span className="comentarios__count">
                        ({comentarios.length})
                    </span>
                )}
            </h2>

            {loading && (
                <p className="catalog-status">
                    Cargando comentarios...
                </p>
            )}

            {error && (
                <p className="catalog-status catalog-status--error">
                    {error.mensaje ?? "Error al cargar los comentarios"}
                </p>
            )}

            {!loading && !error && comentarios.length === 0 && (
                <p className="comentarios__vacio">
                    Todavía no hay comentarios para esta figura.
                </p>
            )}

            {!loading && !error && comentarios.length > 0 && (

                <ul className="comentarios__lista">

                    {comentarios.map(c => (

                        <li key={c.id} className="comentarios__item">

                            <div className="comentarios__header">

                                <span className="comentarios__usuario-wrap">

                                    <ValoracionComentario
                                        valoracion={c.valoracion}
                                    />

                                    <span className="comentarios__usuario">
                                        {c.usuario}
                                    </span>

                                </span>

                                <span className="comentarios__fecha">
                                    {formatearFecha(
                                        c.fechaModificacion ?? c.fechaCreacion
                                    )}
                                    {c.fechaModificacion &&
                                        c.fechaModificacion !== c.fechaCreacion && (
                                            <em> (editado)</em>
                                        )}
                                </span>

                            </div>

                            <p className="comentarios__texto">
                                {c.comentario}
                            </p>

                        </li>

                    ))}

                </ul>

            )}

        </section>

    );

}

export default ComentariosFigura;
