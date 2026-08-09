import "./Header.css";
import { useUser } from "../hooks/useUser";
import { useApiFetch } from "../api/useApiFetch";

function Header() {
    const { user, logout } = useUser();
    const apiFetch = useApiFetch();

    const emailVerificado = async () => {
        try {
            /*const response = */ await apiFetch("/auth/enviarcorreoverificar");
        } catch (error) {
            console.error("Error verifying email:", error);
        }
    };

    return (
        <header className="header">
            <div className="header__inner">

                <a href="/" className="header__logo">
                    <span className="header__logo-mark" aria-hidden="true">🧶</span>
                    <span className="header__logo-text">Manitas Crochet</span>
                </a>

                <nav className="header__nav">
                    <div className="header__nav-left">
                        <a className="header__link" href="/">Catálogo</a>
                        <a className="header__link" href="#sobre-nosotros">Sobre nosotros</a>
                        <a className="header__link" href="#contacto">Contacto</a>
                    </div>

                    <div className="header__nav-right">
                        {user ? (
                            <>
                                {user.rol === "ROLE_ADMIN" && (
                                    <a className="header__link" href="/administracion">Administración</a>
                                )}
                                <div className="header__user-menu">
                                    <button type="button" className="header__user-trigger">
                                        <span className="header__user-name">{user.username}</span>
                                        <span className="header__user-email">{user.email}</span>
                                    </button>
                                    <div className="header__user-dropdown">
                                        {user.emailVerificado ? (
                                            <span className="header__verification-status">
                                                Correo verificado
                                            </span>
                                        ) : (
                                            <button
                                                type="button"
                                                className="header__link header__link--button"
                                                onClick={emailVerificado}
                                            >
                                                Verificar correo
                                            </button>
                                        )}

                                        <button
                                            type="button"
                                            className="header__link header__link--button"
                                            onClick={logout}
                                        >
                                            Cerrar sesión
                                        </button>
                                    </div>
                                </div>
                            </>
                        ) : (
                            <>
                                <a className="header__link" href="/login">Iniciar sesión</a>
                                <a className="header__link header__link--accent" href="/signup">Crear cuenta</a>
                            </>
                        )}
                    </div>
                </nav>

            </div>
        </header>
    );
}

export default Header;
