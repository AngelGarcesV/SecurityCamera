import { useState, useEffect } from "react";
import { NavLink, Outlet, useNavigate } from "react-router-dom";
import "@/styles/layout.css";

function AppLayout() {
  const [rol, setRol] = useState("");
  const [usuario, setUsuario] = useState("");
  const [loaded, setLoaded] = useState(false);
  const navigate = useNavigate();

  useEffect(() => {
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

// ✅ Evitar que un usuario que no sea admin entre a /usuarios
    const path = window.location.pathname;
    if (userRol !== "admin" && path.startsWith("/usuarios")) {
      navigate("/reportes", { replace: true });
    }
  }, [navigate]);

  const handleLogout = () => {
    setRol("");
    setUsuario("");
    setLoaded(false);
    localStorage.clear();
    navigate("/", { replace: true });
  };

  if (!loaded) return <div className="loading">Cargando...</div>;

  return (
      <div className="app-layout">
        <aside className="sidebar">
          <div className="sidebar-logo">
            <h2>CamGestor</h2>
            <p>Rol: {rol || "Sin rol"}</p>
            {usuario && <p>Usuario: {usuario}</p>}
          </div>
          <ul className="sidebar-menu">
            <li><NavLink to="/reportes" className={({ isActive }) => (isActive ? "active" : "")}>Reportes</NavLink></li>
            <li><NavLink to="/camaras" className={({ isActive }) => (isActive ? "active" : "")}>Cámaras</NavLink></li>
            <li><NavLink to="/ubicacion" className={({ isActive }) => (isActive ? "active" : "")}>Ubicación Cámaras</NavLink></li>
            {rol === "admin" && (
                <li><NavLink to="/usuarios" className={({ isActive }) => (isActive ? "active" : "")}>Usuarios</NavLink></li>
            )}
            <li><button onClick={handleLogout} className="button-logout">Cerrar sesión</button></li>
          </ul>
        </aside>
        <main className="main-content">
          <Outlet />
        </main>
      </div>
  );
}

export default AppLayout;