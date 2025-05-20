// src/components/AppLayout.jsx
import { useState, useEffect } from "react";
import { NavLink, Outlet, useNavigate } from "react-router-dom";
import "@/styles/layout.css";

function AppLayout() {
  const [rol, setRol] = useState("");
  const [usuario, setUsuario] = useState("");
  const [loaded, setLoaded] = useState(false);
  const navigate = useNavigate();

// Verificar autenticación al cargar
  useEffect(() => {
    const checkAuth = () => {
      const token = localStorage.getItem("token");
      const userRol = localStorage.getItem("rol");
      const userName = localStorage.getItem("usuario");
      if (!token) {
        navigate("/", { replace: true });
        return;
      }

      setRol(userRol || "");
      setUsuario(userName || "");
      setLoaded(true);
    };

    checkAuth();
  }, [navigate]);

// Escuchar cambios al login (cuando se lanza window.dispatchEvent(new Event('storage')))
  useEffect(() => {
    const handleStorageChange = () => {
      const newRol = localStorage.getItem("rol");
      const newUsuario = localStorage.getItem("usuario");
      setRol(newRol || "");
      setUsuario(newUsuario || "");
      setLoaded(true);
    };

    window.addEventListener("storage", handleStorageChange);
    return () => window.removeEventListener("storage", handleStorageChange);
  }, []);

  const handleLogout = () => {
    localStorage.removeItem("token");
    localStorage.removeItem("rol");
    localStorage.removeItem("usuario");
    navigate("/", { replace: true });
  };

  const isAdmin = rol === "admin";

  if (!loaded) {
    return <div className="loading">Cargando...</div>;
  }

  return (
      <div className="app-layout">
        {/* Sidebar */}
        <aside className="sidebar">
          <div className="sidebar-logo">
            <h2>CamGestor</h2>
            <p>Rol: {rol || "Sin rol"}</p>
            {usuario && <p>Usuario: {usuario}</p>}
          </div>
          <ul className="sidebar-menu">
            <li>
              <NavLink to="/reportes" className={({ isActive }) => (isActive ? "active" : "")}>
                Reportes
              </NavLink>
            </li>
            <li>
              <NavLink to="/camaras" className={({ isActive }) => (isActive ? "active" : "")}>
                Cámaras
              </NavLink>
            </li>
            <li>
              <NavLink to="/ubicacion" className={({ isActive }) => (isActive ? "active" : "")}>
                Ubicación Cámaras
              </NavLink>
            </li>
            {isAdmin && (
                <li>
                  <NavLink to="/usuarios" className={({ isActive }) => (isActive ? "active" : "")}>
                    Usuarios
                  </NavLink>
                </li>
            )}
            <li>
              <button onClick={handleLogout} className="button-logout">
                Cerrar sesión
              </button>
            </li>
          </ul>
        </aside>
        {/* Main content */}
        <main className="main-content">
          <Outlet />
        </main>
      </div>
  );
}

export default AppLayout;