import { useEffect, useState } from "react";
import api from "../../axiosConfig";
import "@/styles/layout.css";
import "@/styles/usuarios.css";

function VerUsuarios() {
  const [usuarios, setUsuarios] = useState([]);
  const [busqueda, setBusqueda] = useState("");
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  useEffect(() => {
    cargarUsuarios();
  }, []);

  const cargarUsuarios = async () => {
    try {
      setLoading(true);
      // Modificado para usar el endpoint correcto
      const res = await api.get("/usuario/all");
      setUsuarios(res.data);
      setLoading(false);
    } catch (error) {
      console.error("Error al cargar usuarios:", error);
      setError("No se pudieron obtener los usuarios");
      setLoading(false);
    }
  };

  const usuariosFiltrados = usuarios.filter((u) =>
      u.nombre.toLowerCase().includes(busqueda.toLowerCase()) ||
      u.correo.toLowerCase().includes(busqueda.toLowerCase()) ||
      String(u.id).includes(busqueda)
  );

  if (loading) return <div className="loading">Cargando usuarios</div>;

  return (
      <div>
        <h3 className="text-xl font-semibold mb-4">Lista de Usuarios</h3>

        {error && <div className="alert alert-danger">{error}</div>}

        <input
            type="text"
            placeholder="Buscar por nombre, correo o ID..."
            value={busqueda}
            onChange={(e) => setBusqueda(e.target.value)}
            className="search-box"
        />

        <table className="usuarios-table">
          <thead>
          <tr>
            <th>ID</th>
            <th>Nombre</th>
            <th>Correo</th>
            <th>Rol</th>
          </tr>
          </thead>
          <tbody>
          {usuariosFiltrados.length > 0 ? (
              usuariosFiltrados.map((u) => (
                  <tr key={u.id}>
                    <td>{u.id}</td>
                    <td>{u.nombre}</td>
                    <td>{u.correo}</td>
                    <td><span className={u.rol === 'admin' ? 'text-primary' : ''}>{u.rol}</span></td>
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