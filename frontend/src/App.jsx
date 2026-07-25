import { Route, Routes } from "react-router-dom";

import FiguraDetalle from "./pages/FiguraDetalle";
import FiguraForm from "./pages/FiguraForm";
import Inicio from "./pages/Inicio";
import AdminPanel from "./pages/AdminPanel";

function App() {
    return (
        <Routes>

            <Route
                path="/"
                element={<Inicio />}
            />

            <Route
                path="/figuras/:id"
                element={<FiguraDetalle />}
            />
            <Route
                path="/figuras/nueva"
                element={<FiguraForm />}
            />

            <Route
                path="/figuras/editar/:id"
                element={<FiguraForm />}
            />

            <Route
                path="/administracion"
                element={<AdminPanel />}
            />

        </Routes>
    );
}

export default App;