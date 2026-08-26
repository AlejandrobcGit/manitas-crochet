import "./FiguraCard.css";
import { useNavigate } from "react-router-dom";
import { FaHeart, FaRegHeart, FaStar } from "react-icons/fa";
import { getCatalogImage } from "../api/imagekit";

const API_URL = import.meta.env.VITE_API_URL;

function FiguraCard({ figura, esFavorito, onToggleFavorito }) {

    const navigate = useNavigate();

    const imageUrl = getCatalogImage(figura.imagenPrincipal);

    const handleFavorito = async (e) => {
        e.stopPropagation();

        try {
            await onToggleFavorito(figura.id);
        } catch (error) {
            console.error(error);
        }
    };

    // Porcentaje de "llenado" de estrellas (0 a 100) para cada una de las 5
    const media = figura.valoracionMedia ?? 0;
    const starFillPercents = Array.from({ length: 5 }, (_, i) => {
        const diff = media - i;
        if (diff >= 1) return 100;
        if (diff <= 0) return 0;
        return diff * 100; // parte decimal para estrella parcial
    });

    return (
        <article
            className="figura-card"
            onClick={() => navigate(`/figuras/${figura.id}`)}
        >

            <button
                className="figura-card__favorite"
                onClick={handleFavorito}
                aria-label="Cambiar favorito"
            >
                {esFavorito
                    ? <FaHeart />
                    : <FaRegHeart />
                }
            </button>

            <div className="figura-card__ring" aria-hidden="true" />

            <div className="figura-card__body">

                <div className="figura-card__image-container">

                    {imageUrl ? (
                        <img
                            className="figura-card__image"
                            src={imageUrl}
                            alt={figura.nombre}
                            loading="lazy"
                        />
                    ) : (
                        <div className="figura-card__image-placeholder">
                            Sin imagen
                        </div>
                    )}

                </div>

                <div className="figura-card__content">

                    <p className="figura-card__category">
                        {figura.categoria}
                    </p>

                    <h3 className="figura-card__title">
                        {figura.nombre}
                    </h3>

                    <div className="figura-card__footer">

                        <p className="figura-card__dimensions">
                            {figura.altura} × {figura.ancho} cm
                        </p>

                        <div
                            className="figura-card__rating"
                            aria-label={`Valoración media ${media.toFixed(1).replace(".", ",")} sobre 5, ${figura.totalValoraciones} valoraciones`}
                        >
                            <div className="figura-card__stars">
                                {starFillPercents.map((percent, i) => (
                                    <span key={i} className="figura-card__star">
                                        <FaStar className="figura-card__star-bg" />
                                        <span
                                            className="figura-card__star-fill"
                                            style={{ width: `${percent}%` }}
                                        >
                                            <FaStar />
                                        </span>
                                    </span>
                                ))}
                            </div>

                            {figura.totalValoraciones > 0 ? (
                                <span className="figura-card__rating-text">
                                    {media.toFixed(1).replace(".", ",")} ({figura.totalValoraciones})
                                </span>
                            ) : (
                                <span className="figura-card__rating-text figura-card__rating-text--empty">
                                    Sin valoraciones
                                </span>
                            )}
                        </div>

                    </div>

                </div>

            </div>

        </article>
    );
}

export default FiguraCard;