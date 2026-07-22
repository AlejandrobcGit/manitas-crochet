import { Route, Routes } from "react-router-dom";

import FiguraDetalle from "./pages/FiguraDetalle";
import FiguraCrear from "./pages/FiguraCrear";
import Inicio from "./pages/Inicio";

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
                path="/figura/"
                element={<FiguraCrear />}
            />

        </Routes>
    );
}

export default App;