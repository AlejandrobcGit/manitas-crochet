import { useState } from "react";
import { FaStar } from "react-icons/fa";

import { useApiFetch } from "../api/useApiFetch";
import { useComentarios } from "../hooks/useComentarios";
import { useUser } from "../hooks/useUser";

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
    const { user } = useUser();
    const authFetch = useApiFetch();

    const [valoracion, setValoracion] = useState(0);
    const [comentario, setComentario] = useState("");
    const [submitError, setSubmitError] = useState("");
    const [submitting, setSubmitting] = useState(false);

    const handleSubmit = async (event) => {
        event.preventDefault();

        const textoComentario = comentario.trim();
        const hayValoracion = valoracion >= 0;

        if (!textoComentario && !hayValoracion) {
            setSubmitError("Debes escribir un comentario o elegir una valoración antes de enviar.");
            return;
        }

        setSubmitError("");
        setSubmitting(true);

        try {

            const peticiones = [];

            if (textoComentario) {
                peticiones.push(
                    authFetch(`/api/comentarios`, {
                        method: "POST",
                        body: JSON.stringify({
                            figuraId,
                            comentario: textoComentario
                        })
                    })
                );
            }

            if (hayValoracion) {
                console.log("Enviando valoración:", valoracion);
                peticiones.push(
                    authFetch(`/api/valoraciones/${figuraId}`, {
                        method: "POST",
                        body: JSON.stringify({
                            puntuacion: valoracion
                        })
                    })
                );
            }

            await Promise.all(peticiones);

            setValoracion(0);
            setComentario("");
            refetch();

        } catch (apiError) {
            setSubmitError(apiError.mensaje || "No se pudo enviar tu valoración.");
        } finally {
            setSubmitting(false);
        }
    };

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

            {user ? (
                <form className="comentarios__form" onSubmit={handleSubmit}>
                    <div className="comentarios__form-header">
                        <label className="comentarios__label">Tu valoración</label>
                        <span className="comentarios__hint">Opcional</span>
                    </div>

                    <div className="comentarios__rating-picker" role="radiogroup" aria-label="Selecciona una valoración">
                        {[1, 2, 3, 4, 5].map(numero => (
                            <button
                                key={numero}
                                type="button"
                                className={`comentarios__star-button ${valoracion >= numero ? "comentarios__star-button--active" : ""}`}
                                onClick={() => setValoracion(numero)}
                                aria-label={`${numero} estrella${numero > 1 ? "s" : ""}`}
                                aria-pressed={valoracion === numero}
                            >
                                <FaStar />
                            </button>
                        ))}
                    </div>

                    {valoracion > 0 && (
                        <button
                            type="button"
                            className="comentarios__clear-rating"
                            onClick={() => setValoracion(0)}
                        >
                            Quitar valoración
                        </button>
                    )}

                    <label className="comentarios__label" htmlFor="comentario-figura">
                        Comentario
                    </label>
                    <textarea
                        id="comentario-figura"
                        className="comentarios__textarea"
                        value={comentario}
                        onChange={(event) => setComentario(event.target.value)}
                        rows={4}
                        maxLength={500}
                        placeholder="Escribe tu opinión sobre la figura. Puedes dejarlo vacío si solo quieres valorar."
                    />

                    {submitError && (
                        <p className="comentarios__error" role="alert">
                            {submitError}
                        </p>
                    )}

                    <button type="submit" className="comentarios__submit" disabled={submitting}>
                        {submitting ? "Enviando..." : "Enviar valoración"}
                    </button>
                </form>
            ) : (
                <p className="comentarios__login">
                    Inicia sesión para dejar tu valoración y comentario.
                </p>
            )}

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
