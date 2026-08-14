import { useState } from "react";

// ImageKit: tr=w-<ancho>,h-<alto>,fo-auto,q-<calidad>
function getImagenOptimizada(url, size) {
    if (!url) return url;
    const separador = url.includes("?") ? "&" : "?";
    return `${url}${separador}tr=w-${size},h-${size},fo-auto,q-80`;
}

function GaleriaImagenes({ imagenPrincipal, imagenesSecundarias = [], nombre }) {

    const imagenes = [imagenPrincipal, ...imagenesSecundarias].filter(Boolean);

    const [seleccionada, setSeleccionada] = useState(0);

    return (

        <div className="galeria">

            <div className="galeria__principal">
                <img
                    src={getImagenOptimizada(imagenes[seleccionada], 800)}
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
                                src={getImagenOptimizada(img, 150)}
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