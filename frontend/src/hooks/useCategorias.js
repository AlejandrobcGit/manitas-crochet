import { useContext } from "react";
import { CategoriasContext } from "../contexts/CategoriasContext";

export function useCategorias() {
    return useContext(CategoriasContext);
}