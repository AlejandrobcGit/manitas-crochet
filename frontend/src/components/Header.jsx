import "./Header.css";

function Header() {
    return (
        <header className="header">
            <div className="header__inner">

                <a href="/" className="header__logo">
                    <span className="header__logo-mark" aria-hidden="true">🧶</span>
                    <span className="header__logo-text">Manitas Crochet</span>
                </a>

                <nav className="header__nav">
                    <a className="header__link" href="#catalogo">Catálogo</a>
                    <a className="header__link" href="#sobre-nosotros">Sobre nosotros</a>
                    <a className="header__link" href="#contacto">Contacto</a>
                </nav>

            </div>
        </header>
    );
}

export default Header;
