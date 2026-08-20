import { useState } from "react";
import { useNavigate, useLocation } from "react-router-dom";
import { useUser } from "../hooks/useUser";
import { useApiFetch } from "../api/useApiFetch";
import Header from "../components/Header";
import Footer from "../components/Footer";
import "./LoginForm.css";

function LoginForm() {
    const { login } = useUser();
    const navigate = useNavigate();
    const location = useLocation();
    const apiFetch = useApiFetch(); 

    const [email, setEmail] = useState("");
    const [password, setPassword] = useState("");
    const [error, setError] = useState(null);
    const [submitting, setSubmitting] = useState(false);

    // --- Recuperación de contraseña ---
    const [showForgotPassword, setShowForgotPassword] = useState(false);
    const [recoveryEmail, setRecoveryEmail] = useState("");
    const [recoveryStatus, setRecoveryStatus] = useState(null); // "sending" | "sent" | "error" | null
    const [recoveryError, setRecoveryError] = useState(null);

    // Si llegamos aquí redirigidos desde una ruta protegida,
    // React Router guarda la ruta original en location.state.from
    const from = location.state?.from || "/";

    const handleSubmit = async (event) => {
        event.preventDefault();
        setError(null);
        setSubmitting(true);

        try {
            await login(email, password);
            navigate(from, { replace: true });
        } catch (apiError) {
            setError(apiError.mensaje || "No se pudo iniciar sesión. Inténtalo de nuevo.");
        } finally {
            setSubmitting(false);
        }
    };

    const handleForgotPasswordSubmit = async (event) => {
        event.preventDefault();
        setRecoveryError(null);
        setRecoveryStatus("sending");

        try {

            const params = new URLSearchParams();

            if (recoveryEmail) params.set("email", recoveryEmail);

            const queryString = params.toString();

            /*const response =*/
                await apiFetch(`/auth/enviarCorreoRecuperar-contrasena?${queryString}`, {
                    method: "POST"
                });

            console.log("Simulando envío de correo de recuperación a:", recoveryEmail);


            setRecoveryStatus("sent");
        } catch (apiError) {
            console.error("Error al enviar correo de recuperación:", apiError.mensaje);
            setRecoveryStatus("error");
            setRecoveryError("No se pudo procesar la solicitud. Inténtalo de nuevo más tarde.");
        }
    };

    return (
        <>
            <Header />

            <div className="login-page">
                <form className="login-form" onSubmit={handleSubmit}>

                    <h1 className="login-form__title">Iniciar sesión</h1>

                    {error && (
                        <p className="login-form__error" role="alert">
                            {error}
                        </p>
                    )}

                    <label className="login-form__label" htmlFor="email">
                        Correo Electrónico
                    </label>
                    <input
                        id="email"
                        className="login-form__input"
                        type="text"
                        value={email}
                        onChange={(event) => setEmail(event.target.value)}
                        autoComplete="email"
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

                    <button
                        type="button"
                        className="login-form__forgot-link"
                        onClick={() => {
                            setShowForgotPassword((prev) => !prev);
                            setRecoveryStatus(null);
                            setRecoveryError(null);
                        }}
                    >
                        ¿Olvidaste tu contraseña?
                    </button>

                    {showForgotPassword && (
                        <div className="login-form__forgot-panel">
                            {recoveryStatus === "sent" ? (
                                <p className="login-form__forgot-message" role="status">
                                    Si existe una cuenta asociada a ese correo, te hemos enviado
                                    un email con instrucciones para recuperar tu contraseña.
                                </p>
                            ) : (
                                <>
                                    <label
                                        className="login-form__label"
                                        htmlFor="recovery-email"
                                    >
                                        Correo electrónico
                                    </label>
                                    <input
                                        id="recovery-email"
                                        className="login-form__input"
                                        type="email"
                                        value={recoveryEmail}
                                        onChange={(event) =>
                                            setRecoveryEmail(event.target.value)
                                        }
                                        autoComplete="email"
                                        required
                                    />

                                    {recoveryStatus === "error" && (
                                        <p className="login-form__error" role="alert">
                                            {recoveryError}
                                        </p>
                                    )}

                                    <button
                                        type="button"
                                        className="login-form__submit login-form__submit--secondary"
                                        onClick={handleForgotPasswordSubmit}
                                        disabled={recoveryStatus === "sending"}
                                    >
                                        {recoveryStatus === "sending"
                                            ? "Enviando..."
                                            : "Enviar correo de recuperación"}
                                    </button>
                                </>
                            )}
                        </div>
                    )}
                </form>
            </div>

            <Footer />
        </>
    );
}

export default LoginForm;