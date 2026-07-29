import { useState } from "react";
import { useNavigate } from "react-router-dom";
import { useUser } from "../hooks/useUser";
import "./SignupForm.css";

function SignupForm() {
    const { signup } = useUser();
    const navigate = useNavigate();

    const [username, setUsername] = useState("");
    const [email, setEmail] = useState("");
    const [password, setPassword] = useState("");
    const [error, setError] = useState(null);
    const [submitting, setSubmitting] = useState(false);

    const handleSubmit = async (event) => {
        event.preventDefault();
        setError(null);
        setSubmitting(true);

        try {
            await signup({ username, email, password });
            navigate("/login");
        } catch (apiError) {
            setError(apiError.mensaje || "No se pudo registrar. Inténtalo de nuevo.");
        } finally {
            setSubmitting(false);
        }
    };

    return (
        <div className="signup-page">
            <form className="signup-form" onSubmit={handleSubmit}>

                <h1 className="signup-form__title">Registro</h1>

                {error && (
                    <p className="signup-form__error" role="alert">
                        {error}
                    </p>
                )}

                <label className="signup-form__label" htmlFor="username">
                    Usuario
                </label>
                <input
                    id="username"
                    className="signup-form__input"
                    type="text"
                    value={username}
                    onChange={(e) => setUsername(e.target.value)}
                    autoComplete="username"
                    required
                />

                <label className="signup-form__label" htmlFor="email">
                    Correo
                </label>
                <input
                    id="email"
                    className="signup-form__input"
                    type="email"
                    value={email}
                    onChange={(e) => setEmail(e.target.value)}
                    autoComplete="email"
                    required
                />

                <label className="signup-form__label" htmlFor="password">
                    Contraseña
                </label>
                <input
                    id="password"
                    className="signup-form__input"
                    type="password"
                    value={password}
                    onChange={(e) => setPassword(e.target.value)}
                    autoComplete="new-password"
                    required
                />

                <button
                    className="signup-form__submit"
                    type="submit"
                    disabled={submitting}
                >
                    {submitting ? "Registrando..." : "Registrar"}
                </button>

            </form>
        </div>
    );
}

export default SignupForm;
