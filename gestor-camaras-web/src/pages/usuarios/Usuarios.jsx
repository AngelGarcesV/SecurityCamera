import { Routes, Route, NavLink, useLocation, useNavigate } from "react-router-dom";
import { useEffect } from "react";
import CrearUsuario from "./CrearUsuario";
import VerUsuarios from "./VerUsuarios";
import EditarUsuario from "./EditarUsuario";
import EliminarUsuario from "./EliminarUsuario";
import "@/styles/layout.css";
import "@/styles/usuarios.css";

function Usuarios() {
    const navigate = useNavigate();
    const location = useLocation();

    useEffect(() => {
        const rol = localStorage.getItem("rol");
        if (rol !== "admin") {
            // Si no es admin, redirigir a reportes
            navigate("/reportes");
        }

        // Si estamos en /usuarios sin subruta, redirigir a /usuarios/ver
        if (location.pathname === "/usuarios") {
            navigate("/usuarios/ver");
        }
    }, [location.pathname, navigate]);

    // Función para manejar la navegación
    const handleNavigation = (path) => {
        navigate(path);
    };

    return (
        <div>
            <h1 className="page-title">Panel de Usuarios</h1>
            <p className="page-subtitle">Gestión de usuarios del sistema</p>

            <div className="nav-usuarios">
                <button
                    className={location.pathname.includes("/usuarios/crear") ? "active" : ""}
                    onClick={() => handleNavigation("/usuarios/crear")}
                >
                    Crear Usuario
                </button>
                <button
                    className={location.pathname.includes("/usuarios/ver") ? "active" : ""}
                    onClick={() => handleNavigation("/usuarios/ver")}
                >
                    Ver Usuarios
                </button>
                <button
                    className={location.pathname.includes("/usuarios/editar") ? "active" : ""}
                    onClick={() => handleNavigation("/usuarios/editar")}
                >
                    Editar Usuario
                </button>
                <button
                    className={location.pathname.includes("/usuarios/eliminar") ? "active" : ""}
                    onClick={() => handleNavigation("/usuarios/eliminar")}
                >
                    Eliminar Usuario
                </button>
            </div>

            <div className="stat-card">
                <Routes>
                    <Route path="/" element={<VerUsuarios />} />
                    <Route path="crear" element={<CrearUsuario />} />
                    <Route path="ver" element={<VerUsuarios />} />
                    <Route path="editar" element={<EditarUsuario />} />
                    <Route path="eliminar" element={<EliminarUsuario />} />
                </Routes>
            </div>
        </div>
    );
}

export default Usuarios;