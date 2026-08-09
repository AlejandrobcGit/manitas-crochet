import { useCallback, useEffect, useState } from "react";
import { useApiFetch } from "../api/useApiFetch";
import { CategoriasContext } from "./CategoriasContextDefinition";

export function CategoriasProvider({ children }) {

    const apiFetch = useApiFetch();

    const [categorias, setCategorias] = useState([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState(null);

    const cargarCategorias = useCallback(async (nombre = "") => {

        try {

            setLoading(true);
            setError(null);

             const params = new URLSearchParams();

            if (nombre) params.set("nombre", nombre);

            const queryString = params.toString();
            
            const url = queryString
                ? `/api/categorias?${queryString}`
                : "/api/categorias";

            const response =
                await apiFetch(url);

            const data =
                await response.json();

            setCategorias(data);

        } catch (error) {

            setError(error);

        } finally {

            setLoading(false);

        }
    }, [apiFetch]);

    useEffect(() => {
        cargarCategorias();
    }, [cargarCategorias]);

    return (
        <CategoriasContext.Provider
            value={{
                categorias,
                 recargarCategorias: cargarCategorias,
                loading,
                error
            }}
        >
            {children}
        </CategoriasContext.Provider>
    );
}