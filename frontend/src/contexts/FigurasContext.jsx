import { useCallback, useState } from "react";
import { useApiFetch } from "../api/useApiFetch";
import { FigurasContext } from "./FigurasContextDefinition";

export function FigurasProvider({ children }) {

    const apiFetch = useApiFetch();

    const [figuras, setFiguras] = useState([]);
    const [pagina, setPagina] = useState({
        paginaActual: 0,
        totalPaginas: 1,
        totalElementos: 0,
        tamanoPagina: 12
    });
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState(null);

    const cargarFiguras = useCallback(async (params = {}) => {

        try {

            setLoading(true);
            setError(null);

            const { nombre = "", categoriaId = "", soloFavoritos = false, page = 0, size = 12 } = params;

            const qs = new URLSearchParams();

            if (nombre) qs.set("nombre", nombre);
            if (categoriaId) qs.set("categoriaId", categoriaId);
            if (soloFavoritos) qs.set("soloFavoritos", "true");
            qs.set("page", page);
            qs.set("size", size);

            const queryString = qs.toString();
            const url = `/api/figuras?${queryString}`;

            const response =
                await apiFetch(url);

            const data =
                await response.json();

            setFiguras(data.contenido || []);
            setPagina({
                paginaActual: data.paginaActual ?? 0,
                totalPaginas: data.totalPaginas ?? 1,
                totalElementos: data.totalElementos ?? 0,
                tamanoPagina: data.tamanoPagina ?? size
            });

        } catch (error) {

            setError(error);

        } finally {

            setLoading(false);

        }
    }, [apiFetch]);

    return (
        <FigurasContext.Provider
            value={{
                figuras,
                setFiguras,
                cargarFiguras,
                recargarFiguras: cargarFiguras,
                pagina,
                setPagina,
                loading,
                error
            }}
        >
            {children}
        </FigurasContext.Provider>
    );
}