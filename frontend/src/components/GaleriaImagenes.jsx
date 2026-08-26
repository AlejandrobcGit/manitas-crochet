import { useState } from "react";
import { getGalleryImage, getThumbnailImage } from "../api/imagekit";

function GaleriaImagenes({ imagenPrincipal, imagenesSecundarias = [], nombre }) {

    const imagenes = [imagenPrincipal, ...imagenesSecundarias].filter(Boolean);

    const [seleccionada, setSeleccionada] = useState(0);

    return (

        <div className="galeria">

            <div className="galeria__principal">
                <img
                    src={getGalleryImage(imagenes[seleccionada])}
                    alt={nombre}
                    fetchPriority="high"
                />
            </div>

            {imagenes.length > 1 && (

                <div className="galeria__miniaturas">

                    {imagenes.map((img, index) => (

                        <button
                            key={img + index}
                            type="button"
                            className={
                                "galeria__miniatura" +
                                (
                                    index === seleccionada
                                        ? " galeria__miniatura--activa"
                                        : ""
                                )
                            }
                            onClick={() => setSeleccionada(index)}
                        >
                            <img
                                src={getThumbnailImage(img)}
                                alt={`${nombre} vista ${index + 1}`}
                                loading="lazy"
                            />
                        </button>

                    ))}

                </div>

            )}

        </div>

    );
}

export default GaleriaImagenes;