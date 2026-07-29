import { useContext } from "react";
import { FavoritosContext } from "../contexts/FavoritosContext";

export function useFavoritos() {
    return useContext(FavoritosContext);
}