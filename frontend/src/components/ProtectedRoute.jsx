
import { Navigate } from "react-router-dom";
import { useUser } from "../hooks/useUser";

function ProtectedRoute({ children, allowedRoles }) {
    const { user } = useUser();

    // No hay sesión iniciada
    if (!user) {
        return <Navigate to="/login" replace />;
    }

    // Hay sesión pero el rol no está permitido
    if (allowedRoles && !allowedRoles.includes(user.rol)) {
        return <Navigate to="/no-autorizado" replace />;
    }

    return children;
}

export default ProtectedRoute;