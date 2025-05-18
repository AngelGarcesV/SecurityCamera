import { useState, useEffect } from "react";
import { NavLink, useNavigate, Outlet } from "react-router-dom";
import "@/styles/layout.css";

function AppLayout() {
  const [rol, setRol] = useState("");
  const navigate = useNavigate();

  useEffect(() => {
    const storedRol = localStorage.getItem("rol");
    if (!storedRol) {
      navigate("/"); // Redirige al login si no hay rol
    } else {
      setRol(storedRol);
    }
  }, [navigate]);

  const handleLogout = () => {
    localStorage.removeItem("token");
    localStorage.removeItem("rol");
    navigate("/");
  };

  return (
    <div className="app-layout">
      {/* Sidebar */}
      <aside className="sidebar">
        <div className="sidebar-logo">
          <h2>CamGestor</h2>
        </div>
        <ul className="sidebar-menu">
          <li>
            <NavLink
              to="/reportes"
              className={({ isActive }) => (isActive ? "active" : "")}
            >
              Reportes
            </NavLink>
          </li>
          <li>
            <NavLink
              to="/camaras"
              className={({ isActive }) => (isActive ? "active" : "")}
            >
              Cámaras
            </NavLink>
          </li>
          <li>
            <NavLink
              to="/ubicacion"
              className={({ isActive }) => (isActive ? "active" : "")}
            >
              Ubicación Cámaras
            </NavLink>
          </li>
          {rol === "admin" && (
            <li>
              <NavLink
                to="/usuarios"
                className={({ isActive }) => (isActive ? "active" : "")}
              >
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
