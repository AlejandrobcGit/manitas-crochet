import Footer from "../components/Footer";
import Header from "../components/Header";
import "./AboutPage.css";

export default function AboutPage() {
  return (
    <>
      <Header />
      <main className="about-page">
        <section className="about-hero">
          <div className="about-hero__content">
            <h1>Donde las ideas se tejen</h1>
            <p>
              En ArribaCrochet transformamos hilos, creatividad y dedicación en
              piezas únicas hechas a mano. Cada creación refleja la pasión por el
              crochet y el valor de la artesanía.
            </p>
          </div>
        </section>

        <section className="about-section">
          <div className="about-card">
            <h2>Nuestra historia</h2>

            <p>
              Nacimos con la ilusión de crear figuras y accesorios
              tejidos a mano. Lo que comenzó como una afición pronto se convirtió
              en un proyecto dedicado a compartir piezas únicas elaboradas con
              paciencia, creatividad y atención al detalle. Hoy seguimos trabajando 
              con la misma pasión del primer día, apostando por la artesanía y por 
              creaciones que transmitan calidez y personalidad.
            </p>
          </div>
        </section>

        <section className="about-values">
          <h2>Nuestros valores</h2>

          <div className="about-values-grid">
            <article className="value-card">
              <div className="value-icon">🧶</div>
              <h3>Artesanía</h3>
              <p>Cada pieza está hecha con cuidado y dedicación.</p>
            </article>

            <article className="value-card">
              <div className="value-icon">🎨</div>
              <h3>Creatividad</h3>
              <p>Diseños originales inspirados en la imaginación.</p>
            </article>

            <article className="value-card">
              <div className="value-icon">💚</div>
              <h3>Cercanía</h3>
              <p>Valoramos la confianza y el trato cercano.</p>
            </article>

            <article className="value-card">
              <div className="value-icon">⭐</div>
              <h3>Calidad</h3>
              <p>Materiales y acabados cuidados para cada creación.</p>
            </article>
          </div>
        </section>
        <section className="about-cta">
          <h2>Gracias por formar parte de ArribaCrochet</h2>

          <p>
            Cada visita y cada muestra de apoyo nos anima a seguir creando nuevas
            ideas y compartiendo nuestra pasión por el crochet.
          </p>
        </section>
      </main>
      <Footer />
    </>
  );

}