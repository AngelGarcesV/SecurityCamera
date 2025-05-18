// src/pages/usuarios/VerUsuarios.jsx
import { useEffect, useState } from "react";
import api from "../../axiosConfig";

function VerUsuarios() {
  const [usuarios, setUsuarios] = useState([]);
  const [busqueda, setBusqueda] = useState("");

  useEffect(() => {
    cargarUsuarios();
  }, []);

  const cargarUsuarios = async () => {
    try {
      const res = await api.get("/usuarios"); // asegúrate de que esta ruta esté en el backend
      setUsuarios(res.data);
    } catch (error) {
      console.error("Error al cargar usuarios:", error);
      alert("No se pudieron obtener los usuarios");
    }
  };

  const usuariosFiltrados = usuarios.filter((u) =>
    u.nombre.toLowerCase().includes(busqueda.toLowerCase()) ||
    String(u.id).includes(busqueda)
  );

  return (
    <div>
      <h3 className="text-xl font-semibold mb-4">Lista de Usuarios</h3>

      <input
        type="text"
        placeholder="Buscar por nombre o ID..."
        value={busqueda}
        onChange={(e) => setBusqueda(e.target.value)}
        className="border px-3 py-2 rounded w-full max-w-sm mb-4"
      />

      <table className="w-full border-collapse">
        <thead>
          <tr className="bg-gray-200">
            <th className="border px-4 py-2">ID</th>
            <th className="border px-4 py-2">Nombre</th>
            <th className="border px-4 py-2">Correo</th>
            <th className="border px-4 py-2">Rol</th>
          </tr>
        </thead>
        <tbody>
          {usuariosFiltrados.length > 0 ? (
            usuariosFiltrados.map((u) => (
              <tr key={u.id}>
                <td className="border px-4 py-2">{u.id}</td>
                <td className="border px-4 py-2">{u.nombre}</td>
                <td className="border px-4 py-2">{u.correo}</td>
                <td className="border px-4 py-2">{u.rol}</td>
              </tr>
            ))
          ) : (
            <tr>
              <td colSpan="4" className="text-center py-4 text-gray-500">
                No hay usuarios que coincidan con la búsqueda.
              </td>
            </tr>
          )}
        </tbody>
      </table>
    </div>
  );
}

export default VerUsuarios;
