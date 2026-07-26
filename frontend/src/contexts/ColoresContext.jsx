import { createContext, useEffect, useState } from "react";
import { useApiFetch } from "../api/useApiFetch";

export const ColoresContext = createContext();

export function ColoresProvider({ children }) {

    const apiFetch = useApiFetch();

    const [colores, setColores] = useState([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState(null);

    const cargarColores = async (nombre = "", codigo = "") => {

        try {

            setLoading(true);
            setError(null);

            const params = new URLSearchParams();

            if (nombre) params.set("nombre", nombre);
            if (codigo) params.set("codigo", codigo);

            const queryString = params.toString();
            
            const url = queryString
                ? `/api/color?${queryString}`
                : "/api/color";

            const response =
                await apiFetch(url);

            const data =
                await response.json();

            setColores(data);

        } catch (error) {

            setError(error);

        } finally {

            setLoading(false);

        }
    };

    useEffect(() => {
        cargarColores();
    }, []);

    return (
        <ColoresContext.Provider
            value={{
                colores,
                setColores,
                cargarColores,
                recargarColores: cargarColores,
                loading,
                error
            }}
        >
            {children}
        </ColoresContext.Provider>
    );
}