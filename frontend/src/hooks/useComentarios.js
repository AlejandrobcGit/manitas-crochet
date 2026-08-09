import { useEffect, useState } from "react";
import { useApiFetch } from "../api/useApiFetch";

export function useComentarios(figuraId) {

    const authFetch = useApiFetch();

    const [comentarios, setComentarios] = useState([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState(null);
    const [recargar, setRecargar] = useState(0);

    useEffect(() => {

        if (!figuraId) return;

        let cancelado = false;

        async function cargarComentarios() {

            setLoading(true);
            setError(null);

            try {

                const res = await authFetch(`/api/comentarios/figura/${figuraId}`);
                const data = await res.json();

                if (!cancelado) {
                    setComentarios(data);
                }

            } catch (apiError) {

                if (!cancelado) {
                    setError(apiError);
                }

            } finally {

                if (!cancelado) {
                    setLoading(false);
                }

            }

        }

        cargarComentarios();

        return () => {
            cancelado = true;
        };

        // eslint-disable-next-line react-hooks/exhaustive-deps
    }, [figuraId, recargar]);

    function refetch() {
        setRecargar(prev => prev + 1);
    }

    return { comentarios, loading, error, refetch };

}
