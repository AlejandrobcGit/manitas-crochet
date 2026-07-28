import { useState } from "react";
import { useNavigate, useLocation } from "react-router-dom";
import { useUser } from "../hooks/useUser";
import "./LoginForm.css";

function LoginForm() {
    const { login } = useUser();
    const navigate = useNavigate();
    const location = useLocation();

    const [username, setUsername] = useState("");
    const [password, setPassword] = useState("");
    const [error, setError] = useState(null);
    const [submitting, setSubmitting] = useState(false);

    // Si llegamos aquí redirigidos desde una ruta protegida,
    // React Router guarda la ruta original en location.state.from
    const from = location.state?.from || "/";

    const handleSubmit = async (event) => {
        event.preventDefault();
        setError(null);
        setSubmitting(true);

        try {
            await login(username, password);
            navigate(from, { replace: true });
        } catch (apiError) {
            setError(apiError.mensaje || "No se pudo iniciar sesión. Inténtalo de nuevo.");
        } finally {
            setSubmitting(false);
        }
    };

    return (
        <div className="login-page">
            <form className="login-form" onSubmit={handleSubmit}>

                <h1 className="login-form__title">Iniciar sesión</h1>

                {error && (
                    <p className="login-form__error" role="alert">
                        {error}
                    </p>
                )}

                <label className="login-form__label" htmlFor="username">
                    Usuario
                </label>
                <input
                    id="username"
                    className="login-form__input"
                    type="text"
                    value={username}
                    onChange={(event) => setUsername(event.target.value)}
                    autoComplete="username"
                    required
                />

                <label className="login-form__label" htmlFor="password">
                    Contraseña
                </label>
                <input
                    id="password"
                    className="login-form__input"
                    type="password"
                    value={password}
                    onChange={(event) => setPassword(event.target.value)}
                    autoComplete="current-password"
                    required
                />

                <button
                    className="login-form__submit"
                    type="submit"
                    disabled={submitting}
                >
                    {submitting ? "Entrando..." : "Entrar"}
                </button>

            </form>
        </div>
    );
}

export default LoginForm;
