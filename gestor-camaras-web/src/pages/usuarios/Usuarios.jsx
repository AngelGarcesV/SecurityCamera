import { Routes, Route, Link, useNavigate } from "react-router-dom";
import { useEffect } from "react";
import CrearUsuario from "./CrearUsuario";
import VerUsuarios from "./VerUsuarios";
import EditarUsuario from "./EditarUsuario";
import EliminarUsuario from "./EliminarUsuario";
import "@/styles/layout.css";
import "@/styles/usuarios.css";

function Usuarios() {
  const navigate = useNavigate();

  useEffect(() => {
    const rol = localStorage.getItem("rol");
    if (rol !== "admin") {
      // Si no es admin, redirigir a reportes
      navigate("/reportes");
    }
  }, []);

  return (
    <div>
      <h2 className="text-2xl mb-4">Gestión de Usuarios</h2>
      <nav className="space-x-4 mb-4">
        <Link to="crear">Crear</Link>
        <Link to="ver">Ver Usuarios</Link>
        <Link to="editar">Editar</Link>
        <Link to="eliminar">Eliminar</Link>
      </nav>

      <Routes>
        <Route path="crear" element={<CrearUsuario />} />
        <Route path="ver" element={<VerUsuarios />} />
        <Route path="editar" element={<EditarUsuario />} />
        <Route path="eliminar" element={<EliminarUsuario />} />
      </Routes>
    </div>
  );
}

export default Usuarios;

