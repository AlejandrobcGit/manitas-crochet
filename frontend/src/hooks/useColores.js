import { useContext } from "react";
import { ColoresContext } from "../contexts/ColoresContextDefinition";

export function useColores() {
    return useContext(ColoresContext);
}