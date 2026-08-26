import { useState } from "react";
import { FaBars, FaTimes } from "react-icons/fa";

import "./Header.css";
import { useUser } from "../hooks/useUser";
import { useApiFetch } from "../api/useApiFetch";
import { Link } from "react-router-dom";
import logo from '../assets/logo.jpg';

function Header() {
    const { user, logout } = useUser();
    const apiFetch = useApiFetch();
    const [menuAbierto, setMenuAbierto] = useState(false);

    const cerrarMenu = () => setMenuAbierto(false);

    const emailVerificado = async () => {
        try {
            /*const response = */ await apiFetch("/auth/enviarcorreoverificar");
            alert("Correo de verificación enviado. Revisa tu bandeja de entrada.");
        } catch (error) {
            console.error("Error verifying email:", error);
            alert("Ocurrió un error al enviar el correo de verificación.");
        }
    };

    const handleLogout = () => {
        cerrarMenu();
        logout();
    };

    return (
        <header className="header">
            <div className="header__inner">

                <Link to="/" className="header__logo" onClick={cerrarMenu}>
                    <img
                        src={logo}
                        alt="Arriba Crochet"
                        className="header__logo-img"
                    />
                    <span className="header__logo-text">Arriba Crochet</span>
                </Link>

                <button
                    type="button"
                    className="header__toggle"
                    onClick={() => setMenuAbierto((prev) => !prev)}
                    aria-expanded={menuAbierto}
                    aria-controls="header-nav"
                    aria-label={menuAbierto ? "Cerrar menú" : "Abrir menú"}
                >
                    {menuAbierto ? <FaTimes /> : <FaBars />}
                </button>

                <nav
                    id="header-nav"
                    className={`header__nav ${menuAbierto ? "header__nav--open" : ""}`}
                >
                    <div className="header__nav-left">
                        <Link className="header__link" to="/" onClick={cerrarMenu}>Catálogo</Link>
                        <Link className="header__link" to="/sobre-nosotros" onClick={cerrarMenu}>Sobre nosotros</Link>
                        <a className="header__link" href="https://www.instagram.com/ArribaCrochet" target="_blank"
                            rel="noopener noreferrer" onClick={cerrarMenu}
                        >Contacto</a>
                    </div>

                    <div className="header__nav-right">
                        {user ? (
                            <>
                                {user.rol === "ROLE_ADMIN" && (
                                    <Link className="header__link" to="/administracion" onClick={cerrarMenu}>
                                        Administración
                                    </Link>
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
                                            onClick={handleLogout}
                                        >
                                            Cerrar sesión
                                        </button>
                                    </div>
                                </div>
                            </>
                        ) : (
                            <>
                                <a className="header__link" href="/login" onClick={cerrarMenu}>Iniciar sesión</a>
                                <a className="header__link header__link--accent" href="/signup" onClick={cerrarMenu}>Crear cuenta</a>
                            </>
                        )}
                    </div>
                </nav>

            </div>
        </header>
    );
}

export default Header;