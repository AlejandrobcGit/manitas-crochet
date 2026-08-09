import { useContext } from "react";
import { FavoritosContext } from "../contexts/FavoritosContextDefinition";

export function useFavoritos() {
    return useContext(FavoritosContext);
}