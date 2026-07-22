import { useEffect, useState } from "react";
import { useApiFetch } from "../api/useApiFetch";

export function useFiguraDetalle(id) {

    const apiFetch = useApiFetch();

    const [figura, setFigura] = useState(null);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState(null);

    useEffect(() => {

        if (!id) return;

        const cargarFigura = async () => {

            try {

                setLoading(true);
                setError(null);

                const response =
                    await apiFetch(`/api/figuras/${id}`);

                const data =
                    await response.json();

                setFigura(data);

            } catch (error) {

                setError(error);

            } finally {

                setLoading(false);

            }
        };

        cargarFigura();

    }, [id]);

    return { figura, loading, error };
}