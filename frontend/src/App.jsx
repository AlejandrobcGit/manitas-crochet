import { Route, Routes } from "react-router-dom";

import FiguraDetalle from "./pages/FiguraDetalle";
import Inicio from "./pages/Inicio";
import AdminPanel from "./pages/AdminPanel";
import LoginForm from "./components/LoginForm"
import ProtectedRoute from "./components/ProtectedRoute";
import NoAutorizado from "./pages/NoAutorizado";
import SignupForm from "./components/SignupForm";
import VerificarEmail from "./pages/VerificarEmail";
import ResetPasswordForm from "./pages/ResetPasswordForm";
import AboutPage from "./pages/AboutPage"
import PoliticaCookies from "./pages/PoliticaCookies"
import PoliticaPrivacidad from "./pages/PoliticaPrivacidad"
import AvisoLegal from "./pages/AvisoLegal"

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
                path="/administracion"
                element={
                    <ProtectedRoute allowedRoles={["ROLE_ADMIN"]}>
                        <AdminPanel />
                    </ProtectedRoute>
                }
            />

            <Route
                path="/login"
                element={<LoginForm />}
            />

            <Route
                path="/no-autorizado"
                element={<NoAutorizado />}
            />

            <Route
                path="/signup"
                element={<SignupForm />}/>

            <Route path="/sobre-nosotros"
                element={<AboutPage />} />

            <Route path="/politica-cookies"
                element={<PoliticaCookies />} />

            <Route path="/politica-privacidad"
                element={<PoliticaPrivacidad />} />

            <Route path="/aviso-legal"
                element={<AvisoLegal />} />

            <Route path="/verificar-email"
                element={<VerificarEmail />} />

            <Route path="/recuperar-contrasena"
                element={<ResetPasswordForm />}
            />
        </Routes>
    );
}

export default App;