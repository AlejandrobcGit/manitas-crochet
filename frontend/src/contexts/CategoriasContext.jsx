import { createContext, useEffect, useState } from "react";
import { useApiFetch } from "../api/useApiFetch";

export const CategoriasContext = createContext();

export function CategoriasProvider({ children }) {

    const apiFetch = useApiFetch();

    const [categorias, setCategorias] = useState([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState(null);

    const cargarCategorias = async () => {

        try {

            setLoading(true);
            setError(null);

            const response =
                await apiFetch("/api/categorias");

            const data =
                await response.json();

            setCategorias(data);

        } catch (error) {

            setError(error);

        } finally {

            setLoading(false);

        }
    };

    useEffect(() => {
        cargarCategorias();
    }, []);

    return (
        <CategoriasContext.Provider
            value={{
                categorias,
                loading,
                error
            }}
        >
            {children}
        </CategoriasContext.Provider>
    );
}