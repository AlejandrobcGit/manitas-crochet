import "./Footer.css";

function Footer() {
    const year = new Date().getFullYear();

    return (
        <footer className="footer" id="contacto">
            <div className="footer__inner">

                <div className="footer__left">
                    <div className="footer__brand">
                        <span className="footer__logo-text">Arriba Crochet</span>
                        <p className="footer__tagline">
                            Figuras de crochet hechas a mano, una puntada a la vez.
                        </p>
                    </div>

                    <nav className="footer__links" aria-label="Enlaces legales">
                        <a className="footer__link" href="/aviso-legal">
                            Aviso Legal
                        </a>
                        <a className="footer__link" href="/politica-privacidad">
                            Política de Privacidad
                        </a>
                        <a className="footer__link" href="/politica-cookies">
                            Política de Cookies
                        </a>
                    </nav>
                </div>

                <p className="footer__copy">
                    © {year} Arriba Crochet. Todos los derechos reservados.
                </p>
            </div>
        </footer>
    );
}

export default Footer;