import { createContext, useEffect, useState } from "react";
import { useUser } from "../hooks/useUser";
import { useApiFetch } from "../api/useApiFetch";

const FavoritosContext = createContext();

function FavoritosProvider({ children }) {

    const { user } = useUser();

    const authFetch = useApiFetch();

    const [favoritos, setFavoritos] = useState([]);

    useEffect(() => {

        if (!user) {
            setFavoritos([]);
            return;
        }

        cargarFavoritos();

    }, [user]);

    const cargarFavoritos = async () => {

        try {

            const response = await authFetch("/api/favorito");

            const data = await response.json();

            setFavoritos(data);

        } catch (error) {

            console.error("Error al cargar favoritos:", error);

            setFavoritos([]);
        }
    };

    const cambiarFavorito = async (figuraId) => {

        try {

            const response = await authFetch(
                `/api/favorito/${figuraId}`,
                {
                    method: "POST"
                }
            );

            const marcado = await response.json();

            if (marcado) {

                setFavoritos(prev => [...prev, figuraId]);

            } else {

                setFavoritos(prev =>
                    prev.filter(id => id !== figuraId)
                );
            }

            return marcado;

        } catch (error) {

            console.error("Error al cambiar favorito:", error);

            throw error;
        }
    };

    return (
        <FavoritosContext.Provider
            value={{
                favoritos,
                cambiarFavorito,
                cargarFavoritos
            }}
        >
            {children}
        </FavoritosContext.Provider>
    );
}

export {
    FavoritosContext,
    FavoritosProvider
};