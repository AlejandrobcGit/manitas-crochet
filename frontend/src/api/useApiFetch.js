import { useContext } from "react";
import { useUser } from "../hooks/useUser";
import { parseApiError } from "./parseApiError";

//const API_URL = import.meta.env.VITE_API_URL;
const API_URL = "http://localhost:8080";

const NO_RETRY_CODES = [
    "BAD_CREDENTIALS",
    "VALIDATION_ERROR",
    "USERNAME_ALREADY_EXISTS",
    "EMAIL_ALREADY_EXISTS"
];

export const useApiFetch = () => {
    const { user, refresh, setUser } = useUser();

    const authFetch = async (url, options = {}) => {
        const headers = {
            ...(options.headers || {}),
            ...(user?.token ? { Authorization: `Bearer ${user.token}` } : {})
        };

        let response = await fetch(`${API_URL}${url}`, {
            ...options,
            headers,
            credentials: "include"
        });

        // ─── 401: intentar refresh token ───────────────────────────────────
        if (response.status === 401) {
            const apiError = await parseApiError(response.clone());

            if (NO_RETRY_CODES.includes(apiError.error)) {
                throw apiError;
            }

            const newToken = await refresh();

            if (!newToken) {
                setUser(null);

                // Si el backend ya devuelve un 401 explicando que no está autorizado,
                // conservamos ese error en lugar de sustituirlo por "SESSION_EXPIRED".
                if (apiError.status === 401 && apiError.error === "UNAUTHORIZED") {
                    throw apiError;
                }

                throw {
                    status: 401,
                    error: "SESSION_EXPIRED",
                    mensaje: "Tu sesión ha expirado. Por favor inicia sesión de nuevo.",
                    timestamp: new Date().toISOString(),
                    fieldErrors: null
                };
            }

            const retryHeaders = {
                ...(options.headers || {}),
                Authorization: `Bearer ${newToken}`
            };

            response = await fetch(`${API_URL}${url}`, {
                ...options,
                headers: retryHeaders,
                credentials: "include"
            });
        }

        // ─── Cualquier error 4xx / 5xx → parseamos y lanzamos ──────────────
        if (!response.ok) {
            const apiError = await parseApiError(response);
            throw apiError;
        }

        return response;
    };

    return authFetch;
};