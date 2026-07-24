import { createContext, useEffect, useState } from "react";
import { useApiFetch } from "../api/useApiFetch";

export const FigurasContext = createContext();

export function FigurasProvider({ children }) {

    const apiFetch = useApiFetch();

    const [figuras, setFiguras] = useState([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState(null);

    const cargarFiguras = async (nombre = "", categoriaId = "") => {

        try {

            setLoading(true);
            setError(null);

            const params = new URLSearchParams();

            if (nombre) params.set("nombre", nombre);
            if (categoriaId) params.set("categoriaId", categoriaId);

            const queryString = params.toString();
            const url = queryString
                ? `/api/figuras?${queryString}`
                : "/api/figuras";
            console.log("url:", url);
            const response =
                await apiFetch(url);

            const data =
                await response.json();

            setFiguras(data);

        } catch (error) {

            setError(error);

        } finally {

            setLoading(false);

        }
    };

    useEffect(() => {
        cargarFiguras();
    }, []);

    return (
        <FigurasContext.Provider
            value={{
                figuras,
                setFiguras,
                cargarFiguras,
                recargarFiguras: cargarFiguras,
                loading,
                error
            }}
        >
            {children}
        </FigurasContext.Provider>
    );
}