import { useContext } from "react";
import { UserContext } from "../contexts/UserContextDefinition";

export function useUser() {
    return useContext(UserContext);
}