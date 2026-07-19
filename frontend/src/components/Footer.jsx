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

                <div className="footer__links">
                    <p className="footer__heading">Contacto</p>
                    <a className="footer__link" href="mailto:hola@manitascrochet.com">
                        hola@manitascrochet.com
                    </a>
                    <a className="footer__link" href="https://instagram.com" target="_blank" rel="noreferrer">
                        Instagram
                    </a>
                    <a className="footer__link" href="https://wa.me/000000000" target="_blank" rel="noreferrer">
                        WhatsApp
                    </a>
                </div>

            </div>

            <p className="footer__copy">
                © {year} Manitas Crochet. Todos los derechos reservados.
            </p>
        </footer>
    );
}

export default Footer;
