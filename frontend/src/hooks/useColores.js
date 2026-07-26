import { useContext } from "react";
import { ColoresContext } from "../contexts/ColoresContext";

export function useColores() {
    return useContext(ColoresContext);
}