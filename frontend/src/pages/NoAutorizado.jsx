import Header from "../components/Header";
import Footer from "../components/Footer";

function NoAutorizado() {
    return (
        <div style={{ display: "flex", flexDirection: "column", minHeight: "100vh" }}>
            <Header />

            <div
                style={{
                    flex: 1,
                    display: "flex",
                    flexDirection: "column",
                    justifyContent: "center",
                    alignItems: "center",
                    textAlign: "center"
                }}
            >
                <h2>403 - Acceso no autorizado</h2>
                <p>No tienes permisos para ver esta página.</p>
            </div>

            <Footer />
        </div>
    );
}

export default NoAutorizado;