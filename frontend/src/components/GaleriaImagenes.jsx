import { useState } from "react";

// TODO: ajusta esto a como resuelve las URLs tu FiguraCard actual
// (ej. import.meta.env.VITE_API_URL + "/uploads/")

const API_URL = "http://localhost:8080";
const API_IMG_URL = "/api/imagenes/";

function GaleriaImagenes({ imagenPrincipal, imagenesSecundarias = [], nombre }) {

    const imagenes = [imagenPrincipal, ...imagenesSecundarias].filter(Boolean);

    const [seleccionada, setSeleccionada] = useState(0);

    return (

        <div className="galeria">

            <div className="galeria__principal">
                <img
                    src={API_URL + API_IMG_URL + imagenes[seleccionada]}
                    alt={nombre}
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
                                src={API_URL + API_IMG_URL + img}
                                alt={`${nombre} vista ${index + 1}`}
                            />
                        </button>

                    ))}

                </div>

            )}

        </div>

    );
}

export default GaleriaImagenes;