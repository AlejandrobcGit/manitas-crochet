import { Routes, Route } from "react-router-dom";

import Inicio from "./pages/Inicio";
import FiguraDetalle from "./pages/FiguraDetalle";

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

        </Routes>
    );
}

export default App;