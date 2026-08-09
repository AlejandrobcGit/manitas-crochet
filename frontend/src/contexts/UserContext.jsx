import { useCallback, useState, useEffect } from "react";
import { useNavigate } from "react-router-dom";
import { parseApiError } from "../api/parseApiError";
import { UserContext } from "./UserContextDefinition";

const API_URL = import.meta.env.VITE_API_URL;

function UserProvider(props) {

    // undefined = comprobando sesión
    // null = no autenticado
    // objeto = autenticado
    const [user, setUser] = useState(undefined);

    const navigate = useNavigate();

    const buildUserFromResponse = (data) => ({
        token: data.accessToken,
        username: data.nombre,
        email: data.email,
        rol: data.rol,
        id: data.id,
        emailVerificado: data.emailVerificado
    });

    const login = async (username, password) => {

        const respuesta = await fetch(`${API_URL}/auth/signin`, {
            method: "POST",
            headers: { "Content-Type": "application/json" },
            credentials: "include",
            body: JSON.stringify({ username, password })
        });

        if (!respuesta.ok) {
            const apiError = await parseApiError(respuesta);
            throw apiError;
        }

        const data = await respuesta.json();

        setUser(buildUserFromResponse(data));
    };

    const refresh = useCallback(async () => {
        try {
            const respuesta = await fetch(`${API_URL}/auth/refresh`, {
                method: "POST",
                credentials: "include"
            });

            if (!respuesta.ok) {
                setUser(null);
                return null;
            }

            const data = await respuesta.json();

            setUser(buildUserFromResponse(data));

            return data.accessToken;

        } catch {
            setUser(null);
            return null;
        }
    }, []);

    useEffect(() => {
        refresh();
    }, [refresh]);

    const logout = async () => {
        try {
            await fetch(`${API_URL}/auth/logout`, {
                method: "POST",
                credentials: "include"
            });
        } catch (error) {
            console.error("Error al cerrar sesión en el servidor:", error);
        } finally {
            setUser(null);
            navigate("/");
        }
    };

    const signup = async ({ username, email, password }) => {
        const respuesta = await fetch(`${API_URL}/auth/signup`, {
            method: "POST",
            headers: { "Content-Type": "application/json" },
            credentials: "include",
            body: JSON.stringify({ username, email, password })
        });

        if (!respuesta.ok) {
            const apiError = await parseApiError(respuesta);
            throw apiError;
        }

        return await respuesta.json();
    };

    // Esperando respuesta del refresh
    if (user === undefined) {
        return <div>Cargando...</div>;
    }

    return (
        <UserContext.Provider
            value={{
                user,
                setUser,
                login,
                refresh,
                logout,
                signup
            }}
        >
            {props.children}
        </UserContext.Provider>
    );
}

export { UserProvider };