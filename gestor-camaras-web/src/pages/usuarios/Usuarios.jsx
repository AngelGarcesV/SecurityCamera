// src/pages/usuarios/Usuarios.jsx
import { useEffect } from "react";
import { useNavigate, useLocation, Outlet } from "react-router-dom";
import "@/styles/layout.css";
import "@/styles/usuarios.css";

function Usuarios() {
    const navigate = useNavigate();
    const location = useLocation();

    useEffect(() => {
        const rol = localStorage.getItem("rol");
        if (rol !== "admin") {
            navigate("/reportes");
        }

        if (location.pathname === "/usuarios") {
            navigate("/usuarios/ver");
        }
    }, [location.pathname, navigate]);

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

            {/* Aquí se renderizan las subrutas */}
            <div className="stat-card">
                <Outlet />
            </div>
        </div>
    );
}

export default Usuarios;

