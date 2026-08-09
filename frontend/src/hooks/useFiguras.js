import { useContext } from "react";
import { FigurasContext } from "../contexts/FigurasContextDefinition";

export function useFiguras() {
    return useContext(FigurasContext);
}