import { useCallback } from "react";
import { useApiFetch } from "../api/useApiFetch";
import { FavoritosContext } from "./FavoritosContextDefinition";

function FavoritosProvider({ children }) {

    const authFetch = useApiFetch();

    const cambiarFavorito = useCallback(async (figuraId) => {

        try {

            const response = await authFetch(
                `/api/favorito/${figuraId}`,
                {
                    method: "POST"
                }
            );

            const marcado = await response.json();

            return marcado;

        } catch (error) {

            console.error("Error al cambiar favorito:", error);

            throw error;
        }
    }, [authFetch]);

    return (
        <FavoritosContext.Provider
            value={{
                cambiarFavorito
            }}
        >
            {children}
        </FavoritosContext.Provider>
    );
}

export {
    FavoritosProvider
};
