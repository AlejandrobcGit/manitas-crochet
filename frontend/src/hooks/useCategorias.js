import { useContext } from "react";
import { CategoriasContext } from "../contexts/CategoriasContextDefinition";

export function useCategorias() {
    return useContext(CategoriasContext);
}