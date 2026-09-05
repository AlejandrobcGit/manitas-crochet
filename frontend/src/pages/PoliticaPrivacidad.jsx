import Footer from "../components/Footer";
import Header from "../components/Header";
import "./PoliticaPrivacidad.css";

export default function PoliticaPrivacidad() {
  return (
    <>
      <Header />
      <main className="privacidad-page">
        <section className="privacidad-hero">
          <div className="privacidad-hero__content">
            <h1>Política de Privacidad</h1>
            <p className="privacidad-hero__sub">
              Arriba Crochet&nbsp;·&nbsp;Versión 1.1&nbsp;·&nbsp;Última
              actualización: agosto de 2026
            </p>
          </div>
        </section>

        <section className="privacidad-card">
          <h2>1. Introducción</h2>
          <p>
            Arriba Crochet es un proyecto personal de autoaprendizaje con utilidad práctica
            orientado a la publicación y gestión de un catálogo de productos artesanales
            elaborados mediante técnicas de crochet.
          </p>
          <p>
            La presente Política de Privacidad tiene como finalidad informar de manera clara y
            transparente sobre qué información se recopila, cómo se utiliza y qué derechos
            tienen las personas usuarias respecto a sus datos personales.
          </p>
        </section>

        <section className="privacidad-card">
          <h2>2. Responsable del tratamiento</h2>
          <p>
            Los datos personales tratados a través de este sitio web se gestionan en el marco
            del proyecto personal Arriba Crochet.
          </p>
          <p>
            Para cualquier cuestión relacionada con esta Política de Privacidad o con el
            tratamiento de datos personales podrá utilizarse la dirección de contacto habilitada
            para tal fin.
          </p>
        </section>

        <section className="privacidad-card">
          <h2>3. Datos que se recopilan</h2>
          <p>
            Arriba Crochet recopila únicamente los datos necesarios para el funcionamiento de
            las cuentas de usuario y determinadas funcionalidades del sitio.
          </p>

          <h3>Datos de la cuenta</h3>
          <p>
            Cuando una persona usuaria crea una cuenta pueden almacenarse los siguientes datos:
          </p>
          <ul>
            <li>Nombre de usuario.</li>
            <li>Dirección de correo electrónico.</li>
            <li>Credencial de acceso cifrada necesaria para la autenticación.</li>
            <li>Estado de verificación de la dirección de correo electrónico.</li>
          </ul>

          <h3>Datos de uso</h3>
          <p>
            Con el objetivo de ofrecer determinadas funcionalidades y mejorar el funcionamiento
            del catálogo, pueden registrarse:
          </p>
          <ul>
            <li>Figuras marcadas como favoritas.</li>
            <li>Visualizaciones realizadas por usuarios autenticados dentro del catálogo.</li>
            <li>Comentarios publicados en las figuras del catálogo.</li>
            <li>Valoraciones (puntuaciones) realizadas sobre las figuras del catálogo.</li>
            <li>Fecha y hora de determinadas interacciones realizadas dentro del catálogo.</li>
          </ul>

          <h3>Datos técnicos</h3>
          <p>
            Durante el uso del sitio web pueden generarse registros técnicos necesarios para la
            seguridad, mantenimiento y correcto funcionamiento de la plataforma.
          </p>
        </section>

        <section className="privacidad-card">
          <h2>4. Finalidad del tratamiento</h2>
          <p>Los datos recopilados se utilizan exclusivamente para:</p>
          <ul>
            <li>Permitir el registro y autenticación de usuarios.</li>
            <li>Gestionar las cuentas creadas en la plataforma.</li>
            <li>Mantener la seguridad de las sesiones.</li>
            <li>Gestionar favoritos y preferencias asociadas a la cuenta.</li>
            <li>Permitir la publicación de comentarios y valoraciones en el catálogo.</li>
            <li>Mejorar el funcionamiento y la experiencia de uso del catálogo.</li>
            <li>Resolver incidencias técnicas.</li>
            <li>Cumplir las obligaciones legales que resulten aplicables.</li>
          </ul>
          <p>
            Los datos no se utilizan para la venta a terceros ni para fines publicitarios
            personalizados.
          </p>
        </section>

        <section className="privacidad-card">
          <h2>5. Base jurídica del tratamiento</h2>
          <p>La base jurídica para el tratamiento de los datos es:</p>
          <ul>
            <li>La ejecución de los servicios solicitados por las personas usuarias registradas.</li>
            <li>El interés legítimo de mantener la seguridad y el correcto funcionamiento del sitio web.</li>
            <li>El cumplimiento de las obligaciones legales que resulten aplicables.</li>
          </ul>
        </section>

        <section className="privacidad-card">
          <h2>6. Conservación de los datos</h2>
          <p>
            Los datos personales se conservarán mientras resulten necesarios para el
            funcionamiento de las cuentas y de las funcionalidades asociadas al servicio.
          </p>
          <p>
            La persona usuaria podrá solicitar la eliminación de su cuenta y de los datos
            asociados mediante la dirección de contacto indicada en esta Política de Privacidad,
            salvo aquellos que deban conservarse por obligación legal.
          </p>
        </section>

        <section className="privacidad-card">
          <h2>7. Servicios tecnológicos utilizados</h2>
          <p>
            Para el funcionamiento de Arriba Crochet se utilizan servicios tecnológicos
            proporcionados por terceros especializados en alojamiento, infraestructura y
            almacenamiento de datos.
          </p>
          <p>Entre ellos pueden encontrarse:</p>
          <ul>
            <li>Firebase Hosting.</li>
            <li>Google Cloud Platform.</li>
            <li>MongoDB Atlas.</li>
            <li>Cloudflare.</li>
          </ul>
          <p>
            La base de datos utilizada por Arriba Crochet se encuentra alojada en una región
            ubicada dentro de la Unión Europea.
          </p>
          <p>
            Asimismo, el sitio utiliza ImageKit para la optimización y distribución de imágenes
            del catálogo.
          </p>
          <p>Estos servicios se utilizan exclusivamente para posibilitar el funcionamiento de la plataforma.</p>
        </section>

        <section className="privacidad-card">
          <h2>8. Cookies y autenticación</h2>
          <p>
            Arriba Crochet utiliza una cookie técnica necesaria para el funcionamiento de las
            cuentas de usuario registradas. Esta cookie permite:
          </p>
          <ul>
            <li>Mantener la sesión iniciada.</li>
            <li>Gestionar de forma segura la autenticación.</li>
            <li>Garantizar el correcto funcionamiento de las funcionalidades asociadas a la cuenta.</li>
          </ul>
          <p>
            Esta cookie es estrictamente necesaria para la prestación del servicio y no se utiliza
            con fines publicitarios, de marketing o de elaboración de perfiles.
          </p>
          <p>
            Para más información puede consultarse la{" "}
            <a className="privacidad-link" href="/politica-cookies">
              Política de Cookies
            </a>{" "}
            del sitio.
          </p>
        </section>

        <section className="privacidad-card">
          <h2>9. Seguridad de los datos</h2>
          <p>
            Arriba Crochet adopta medidas técnicas y organizativas razonables destinadas a
            proteger la información frente a accesos no autorizados, pérdidas, alteraciones o usos
            indebidos.
          </p>
          <p>
            Las credenciales de acceso de las personas usuarias no se almacenan en texto plano y
            las comunicaciones con el sitio web se realizan mediante conexiones seguras.
          </p>
          <p>
            No obstante, ningún sistema puede garantizar una seguridad absoluta frente a todos los
            riesgos existentes en Internet.
          </p>
        </section>

        <section className="privacidad-card">
          <h2>10. Derechos de las personas usuarias</h2>
          <p>
            Las personas usuarias podrán ejercer los derechos reconocidos por la normativa
            aplicable en materia de protección de datos, incluyendo:
          </p>
          <ul>
            <li>Derecho de acceso.</li>
            <li>Derecho de rectificación.</li>
            <li>Derecho de supresión.</li>
            <li>Derecho de oposición.</li>
            <li>Derecho de limitación del tratamiento.</li>
            <li>Derecho a la portabilidad de los datos cuando resulte aplicable.</li>
          </ul>
          <p>
            Para ejercer cualquiera de estos derechos podrá enviarse una solicitud a{" "}
            <a className="privacidad-link" href="mailto:arribacrochet@gmail.com">
              arribacrochet@gmail.com
            </a>
            .
          </p>
        </section>

        <section className="privacidad-card">
          <h2>11. Modificaciones de esta política</h2>
          <p>
            La presente Política de Privacidad podrá actualizarse cuando resulte necesario para
            adaptarse a cambios legales, técnicos o funcionales del proyecto.
          </p>
          <p>La versión publicada en el sitio web será la vigente en cada momento.</p>
        </section>

        <section className="privacidad-card">
          <h2>12. Contacto</h2>
          <p>
            Para cualquier consulta relacionada con esta Política de Privacidad o con el
            tratamiento de datos personales puede utilizarse la siguiente dirección de contacto:{" "}
            <a className="privacidad-link" href="mailto:arribacrochet@gmail.com">
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
