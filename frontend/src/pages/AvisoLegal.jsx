import Footer from "../components/Footer";
import Header from "../components/Header";
import "./AvisoLegal.css";

export default function AvisoLegal() {
  return (
    <>
      <Header />
      <main className="aviso-page">
        <section className="aviso-hero">
          <div className="aviso-hero__content">
            <h1>Aviso Legal</h1>
            <p className="aviso-hero__sub">
              Arriba Crochet&nbsp;·&nbsp;Versión 1.0&nbsp;·&nbsp;Última
              actualización: agosto de 2026
            </p>
          </div>
        </section>

        <section className="aviso-card">
          <h2>1. Información general</h2>
          <p>
            Este sitio web, Arriba Crochet, es un proyecto personal de autoaprendizaje con
            utilidad práctica orientado a la publicación y gestión de un catálogo de productos
            artesanales elaborados mediante técnicas de crochet.
          </p>
          <p>
            El acceso y utilización de este sitio web atribuye la condición de persona usuaria e
            implica la aceptación de las condiciones recogidas en el presente Aviso Legal.
          </p>
          <p>
            Para cualquier consulta relacionada con el sitio web puede utilizarse la siguiente
            dirección de contacto:{" "}
            <a className="aviso-link" href="mailto:arribacrochet@gmail.com">
              arribacrochet@gmail.com
            </a>
            .
          </p>
        </section>

        <section className="aviso-card">
          <h2>2. Finalidad del sitio web</h2>
          <p>
            La finalidad de Arriba Crochet es facilitar la consulta de un catálogo de productos
            artesanales y ofrecer información relacionada con dichos contenidos.
          </p>
          <p>
            El proyecto permite, además, la creación de cuentas de usuario para acceder a
            funcionalidades como marcar figuras como favoritas, publicar comentarios o valorar
            las piezas del catálogo.
          </p>
          <p>
            El contenido publicado tiene carácter informativo y podrá actualizarse, modificarse o
            eliminarse en cualquier momento para mejorar el funcionamiento del proyecto.
          </p>
        </section>

        <section className="aviso-card">
          <h2>3. Condiciones de uso</h2>
          <p>
            La persona usuaria se compromete a utilizar el sitio web de forma adecuada, respetando
            la legislación vigente, la buena fe y el presente Aviso Legal.
          </p>
          <p>
            Queda prohibida la utilización del sitio web para actividades ilícitas o que puedan
            perjudicar los derechos o intereses de terceras personas.
          </p>
          <p>
            Asimismo, no está permitido intentar alterar, dañar o interferir en el funcionamiento
            normal del sitio web o de los servicios que lo soportan.
          </p>
        </section>

        <section className="aviso-card">
          <h2>4. Propiedad intelectual e industrial</h2>
          <p>
            Salvo indicación expresa en contrario, los textos, imágenes, fotografías, diseños,
            logotipos y demás contenidos publicados en Arriba Crochet pertenecen al proyecto Arriba
            Crochet o son utilizados con la autorización correspondiente.
          </p>
          <p>
            La reproducción, distribución, transformación o utilización de dichos contenidos con
            fines distintos del uso personal o informativo requerirá autorización previa de su
            titular o de quien ostente los derechos correspondientes.
          </p>
        </section>

        <section className="aviso-card">
          <h2>5. Responsabilidad</h2>
          <p>
            Arriba Crochet realiza esfuerzos razonables para mantener la información publicada
            actualizada y para garantizar el correcto funcionamiento del sitio web.
          </p>
          <p>
            No obstante, no se garantiza la ausencia de errores, interrupciones temporales o
            incidencias técnicas derivadas de factores ajenos al proyecto.
          </p>
          <p>La persona usuaria utiliza el sitio web bajo su propia responsabilidad.</p>
        </section>

        <section className="aviso-card">
          <h2>6. Enlaces a sitios de terceros</h2>
          <p>
            Este sitio web puede contener enlaces a páginas o recursos externos gestionados por
            terceros.
          </p>
          <p>
            Arriba Crochet no controla ni asume responsabilidad sobre los contenidos, políticas o
            servicios ofrecidos por dichos sitios externos.
          </p>
          <p>
            La existencia de estos enlaces tiene únicamente una finalidad informativa o de
            referencia.
          </p>
        </section>

        <section className="aviso-card">
          <h2>7. Protección de datos personales</h2>
          <p>
            El tratamiento de los datos personales que puedan recopilarse a través del sitio web se
            regula mediante la correspondiente{" "}
            <a className="aviso-link" href="/politica-privacidad">
              Política de Privacidad
            </a>
            .
          </p>
          <p>
            Las condiciones relativas al uso de cookies se describen en la correspondiente{" "}
            <a className="aviso-link" href="/politica-cookies">
              Política de Cookies
            </a>
            .
          </p>
        </section>

        <section className="aviso-card">
          <h2>8. Modificaciones</h2>
          <p>
            Arriba Crochet podrá modificar el presente Aviso Legal cuando resulte necesario para
            adaptarlo a cambios legales, técnicos o funcionales del proyecto.
          </p>
          <p>La versión publicada en el sitio web será la vigente en cada momento.</p>
        </section>

        <section className="aviso-card">
          <h2>9. Legislación aplicable</h2>
          <p>
            El presente Aviso Legal se interpretará de acuerdo con la legislación española que
            resulte aplicable.
          </p>
        </section>

        <section className="aviso-card">
          <h2>10. Contacto</h2>
          <p>
            Para cualquier consulta relacionada con este Aviso Legal puede utilizarse la siguiente
            dirección de contacto:{" "}
            <a className="aviso-link" href="mailto:arribacrochet@gmail.com">
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