import { useState } from "react";

import Header from "../components/Header";
import Footer from "../components/Footer";

import AdminFiguras from "./admin/AdminFiguras";
import AdminCategorias from "./admin/AdminCategorias";
import AdminColores from "./admin/AdminColores";
import DashboardPage from "./admin/DashboardPage";

import "./AdminPanel.css";

function AdminPanel() {

    const [opcionActiva, setOpcionActiva] = useState("figuras");

    const renderContenido = () => {

        switch (opcionActiva) {

            case "figuras":
                return <AdminFiguras />;

            case "categorias":
                return <AdminCategorias />;

            case "colores":
                return <AdminColores />;

            case "estadisticas":
                return <DashboardPage />;

            default:
                return <AdminFiguras />;
        }
    };

    return (
        <div className="app">

            <Header />

            <main className="admin-layout">

                <aside className="admin-menu">
                    <div className="admin-panel">
                        <h2>Administración</h2>

                        <ul>

                            <li
                                className={opcionActiva === "figuras" ? "activo" : ""}
                                onClick={() => setOpcionActiva("figuras")}
                            >
                                Figuras
                            </li>

                            <li
                                className={opcionActiva === "categorias" ? "activo" : ""}
                                onClick={() => setOpcionActiva("categorias")}
                            >
                                Categorías
                            </li>

                            <li
                                className={opcionActiva === "colores" ? "activo" : ""}
                                onClick={() => setOpcionActiva("colores")}
                            >
                                Colores
                            </li>

                            <li
                                className={opcionActiva === "estadisticas" ? "activo" : ""}
                                onClick={() => setOpcionActiva("estadisticas")}
                            >
                                Estadísticas
                            </li>

                        </ul>
                    </div>
                </aside>

                <section className="admin-content">
                    {renderContenido()}
                </section>

            </main>

            <Footer />

        </div>
    );
}

export default AdminPanel;