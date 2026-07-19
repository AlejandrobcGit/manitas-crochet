import "./FiguraCard.css";
import { useNavigate } from "react-router-dom";

const API_URL = "http://localhost:8080";

function FiguraCard({ figura }) {

    const navigate = useNavigate();

    const imageUrl = figura.imagenPrincipal
        ? `${API_URL}/api/imagenes/${figura.imagenPrincipal}`
        : null;

    return (
        <article
            className="figura-card"
            onClick={() => navigate(`/figuras/${figura.id}`)}
        >

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