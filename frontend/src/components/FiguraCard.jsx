import "./FiguraCard.css";
import { useNavigate } from "react-router-dom";
import { useFavoritos } from "../hooks/useFavoritos";
import { FaHeart, FaRegHeart } from "react-icons/fa";

const API_URL = "http://localhost:8080";

function FiguraCard({ figura }) {

    const navigate = useNavigate();

    const { favoritos, cambiarFavorito } = useFavoritos();

    const esFavorito = favoritos.includes(figura.id);

    const imageUrl = figura.imagenPrincipal
        ? `${API_URL}/api/imagenes/${figura.imagenPrincipal}`
        : null;

    const handleFavorito = async (e) => {

        e.stopPropagation();

        try {
            await cambiarFavorito(figura.id);
        } catch (error) {
            console.error(error);
        }
    };

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

                    <p className="figura-card__dimensions">
                        {figura.altura} × {figura.ancho} cm
                    </p>

                </div>

            </div>

        </article>
    );
}

export default FiguraCard;