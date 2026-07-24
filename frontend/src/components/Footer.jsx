import "./Footer.css";

function Footer() {
    const year = new Date().getFullYear();

    return (
        <footer className="footer" id="contacto">
            <div className="footer__inner">

                <div className="footer__brand">
                    <span className="footer__logo-mark" aria-hidden="true">🧶</span>
                    <span className="footer__logo-text">Manitas Crochet</span>
                    <p className="footer__tagline">
                        Figuras de crochet hechas a mano, una puntada a la vez.
                    </p>
                </div>
            </div>

            <p className="footer__copy">
                © {year} Manitas Crochet. Todos los derechos reservados.
            </p>
        </footer>
    );
}

export default Footer;
