import { createContext, useEffect, useState } from "react";
import { useApiFetch } from "../api/useApiFetch";

export const FigurasContext = createContext();

export function FigurasProvider({ children }) {

    const apiFetch = useApiFetch();

    const [figuras, setFiguras] = useState([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState(null);

    const cargarFiguras = async () => {

        try {

            setLoading(true);
            setError(null);

            const response =
                await apiFetch("/api/figuras");

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