import { useState } from "react";
import { useNavigate, useSearchParams } from "react-router-dom";
import { useApiFetch } from "../api/useApiFetch";
import Header from "../components/Header";
import Footer from "../components/Footer";
import "./ResetPasswordForm.css";

const MIN_PASSWORD_LENGTH = 8;

function ResetPasswordForm() {
    const apiFetch = useApiFetch();
    const navigate = useNavigate();
    const [searchParams] = useSearchParams();
    const token = searchParams.get("token");

    const [password, setPassword] = useState("");
    const [confirmPassword, setConfirmPassword] = useState("");
    const [error, setError] = useState(null);
    const [submitting, setSubmitting] = useState(false);
    const [success, setSuccess] = useState(false);

    const handleSubmit = async (event) => {
        event.preventDefault();
        setError(null);

        if (!token) {
            setError("El enlace de recuperación no es válido o ha expirado.");
            return;
        }

        if (password.length < MIN_PASSWORD_LENGTH) {
            setError(`La contraseña debe tener al menos ${MIN_PASSWORD_LENGTH} caracteres.`);
            return;
        }

        if (password !== confirmPassword) {
            setError("Las contraseñas no coinciden.");
            return;
        }

        setSubmitting(true);

        try {
            await apiFetch("/auth/restablecer-contrasena", {
                method: "POST",
                headers: { "Content-Type": "application/json" },
                body: JSON.stringify({ token, nuevaContrasena: password }),
            });

            setSuccess(true);
        } catch (apiError) {
            setError(
                apiError.mensaje ||
                    "No se pudo restablecer la contraseña. El enlace puede haber expirado."
            );
        } finally {
            setSubmitting(false);
            setPassword("");
        }
    };

    return (
        <>
            <Header />

            <div className="reset-password-page">
                {!token ? (
                    <p className="reset-password-form__error" role="alert">
                        El enlace de recuperación no es válido. Solicita uno nuevo desde la
                        página de inicio de sesión.
                    </p>
                ) : success ? (
                    <div className="reset-password-form__success">
                        <p role="status">
                            Tu contraseña se ha actualizado correctamente.
                        </p>
                        <button
                            type="button"
                            className="login-form__submit"
                            onClick={() => navigate("/login", { replace: true })}
                        >
                            Ir a iniciar sesión
                        </button>
                    </div>
                ) : (
                    <form className="reset-password-form" onSubmit={handleSubmit}>
                        <h1 className="reset-password-form__title">
                            Restablecer contraseña
                        </h1>

                        {error && (
                            <p className="reset-password-form__error" role="alert">
                                {error}
                            </p>
                        )}

                        <label className="login-form__label" htmlFor="new-password">
                            Nueva contraseña
                        </label>
                        <input
                            id="new-password"
                            className="login-form__input"
                            type="password"
                            value={password}
                            onChange={(event) => setPassword(event.target.value)}
                            autoComplete="new-password"
                            minLength={MIN_PASSWORD_LENGTH}
                            required
                        />

                        <label className="login-form__label" htmlFor="confirm-password">
                            Confirmar contraseña
                        </label>
                        <input
                            id="confirm-password"
                            className="login-form__input"
                            type="password"
                            value={confirmPassword}
                            onChange={(event) => setConfirmPassword(event.target.value)}
                            autoComplete="new-password"
                            minLength={MIN_PASSWORD_LENGTH}
                            required
                        />

                        <button
                            className="login-form__submit"
                            type="submit"
                            disabled={submitting}
                        >
                            {submitting ? "Guardando..." : "Enviar"}
                        </button>
                    </form>
                )}
            </div>

            <Footer />
        </>
    );
}

export default ResetPasswordForm;