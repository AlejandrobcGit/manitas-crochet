import { createContext, useState, useEffect } from "react";
import { useNavigate } from "react-router-dom";
import { parseApiError } from "../api/parseApiError";

const UserContext = createContext();

const API_URL = "http://localhost:8080";

function UserProvider(props) {

    const [user, setUser] = useState(null);
    const [loading, setLoading] = useState(true); // ← bloquea el render inicial
    const navigate = useNavigate();

    // 🔄 Recuperar sesión automáticamente al cargar la app
    useEffect(() => {
        refresh().finally(() => setLoading(false)); // ← cuando termine, desbloquea
    }, []);

    const buildUserFromResponse = (data) => ({
        token: data.accessToken,
        username: data.nombre,
        email: data.email,
        rol: data.rol,
        id: data.id
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
            throw apiError;   // ← el componente lo recibe en el catch
        }

        const data = await respuesta.json();

        setUser(buildUserFromResponse(data));
    };

    const refresh = async () => {
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

        } catch (error) {
            setUser(null);
            return null;
        }
    };

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

    // ⏳ Mientras se restaura la sesión no renderizamos nada
    if (loading) return null;

    return (
        <UserContext.Provider value={{ user, setUser, login, refresh, logout }}>
            {props.children}
        </UserContext.Provider>
    );
}

export { UserContext, UserProvider };