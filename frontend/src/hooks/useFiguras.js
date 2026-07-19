import { useContext } from "react";
import { FigurasContext } from "../contexts/FigurasContext";

export function useFiguras() {
    return useContext(FigurasContext);
}