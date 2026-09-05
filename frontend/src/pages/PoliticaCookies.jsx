import Footer from "../components/Footer";
import Header from "../components/Header";
import "./PoliticaCookies.css";

export default function PoliticaCookies() {
  return (
    <>
      <Header />
      <main className="cookie-page">
        <section className="cookie-hero">
          <div className="cookie-hero__content">
            <h1>Política de Cookies</h1>
            <p className="cookie-hero__sub">
              Arriba Crochet&nbsp;·&nbsp;Versión 1.0&nbsp;·&nbsp;Última
              actualización: agosto de 2026
            </p>
          </div>
        </section>

        <section className="cookie-card">
          <h2>1. ¿Qué son las cookies?</h2>
          <p>
            Las cookies son pequeños archivos que los sitios web pueden almacenar en el
            dispositivo de una persona usuaria para recordar determinada información
            relacionada con la navegación o con las funcionalidades ofrecidas por el sitio.
          </p>
        </section>

        <section className="cookie-card">
          <h2>2. ¿Qué cookies utiliza Arriba Crochet?</h2>
          <p>
            Arriba Crochet utiliza únicamente las cookies necesarias para el correcto
            funcionamiento de determinadas funcionalidades del sitio web.
          </p>
          <p>
            Actualmente no se utilizan cookies publicitarias, de marketing, de análisis ni
            de elaboración de perfiles.
          </p>

          <h3>Cookie técnica de autenticación</h3>
          <p>
            Arriba Crochet utiliza una cookie técnica asociada al sistema de autenticación
            de usuarios registrados.
          </p>
          <p className="cookie-label">Finalidad:</p>
          <ul>
            <li>Mantener la sesión iniciada.</li>
            <li>Gestionar de forma segura la autenticación de la cuenta.</li>
            <li>
              Permitir el funcionamiento de las funcionalidades disponibles para usuarios
              registrados.
            </li>
          </ul>
          <p className="cookie-label">Características generales:</p>
          <ul>
            <li>Cookie propia.</li>
            <li>Cookie técnica.</li>
            <li>Duración: 7 días.</li>
            <li>No utilizada para fines publicitarios.</li>
            <li>No utilizada para seguimiento comercial.</li>
            <li>No utilizada para elaboración de perfiles.</li>
          </ul>
          <p>
            La utilización de esta cookie resulta necesaria para prestar las funcionalidades
            asociadas a las cuentas de usuario.
          </p>
        </section>

        <section className="cookie-card">
          <h2>3. Cookies de terceros</h2>
          <p>
            Arriba Crochet no utiliza cookies de terceros con fines publicitarios, analíticos
            o comerciales.
          </p>
          <p>
            En el momento de la publicación de esta política no se han incorporado al sitio
            web herramientas de seguimiento, publicidad personalizada o analítica basadas en
            cookies de terceros.
          </p>
        </section>

        <section className="cookie-card">
          <h2>4. Gestión de cookies</h2>
          <p>
            La mayoría de navegadores permiten consultar, bloquear o eliminar las cookies
            existentes mediante su configuración.
          </p>
          <p>
            La desactivación de las cookies necesarias para la autenticación puede impedir el
            acceso a determinadas funcionalidades reservadas para usuarios registrados.
          </p>
        </section>

        <section className="cookie-card">
          <h2>5. Cambios en esta política</h2>
          <p>
            La presente Política de Cookies podrá modificarse cuando se produzcan cambios
            técnicos, funcionales o legales que afecten al uso de cookies en Arriba Crochet.
          </p>
          <p>La versión publicada en el sitio web será la vigente en cada momento.</p>
        </section>

        <section className="cookie-card">
          <h2>6. Contacto</h2>
          <p>
            Para cualquier consulta relacionada con esta Política de Cookies puede utilizarse
            la siguiente dirección de contacto:{" "}
            <a className="cookie-link" href="mailto:arribacrochet@gmail.com">
              arribacrochet@gmail.com
            </a>
            .
          </p>
        </section>
      </main>
      <Footer />
    </>
  );
}
