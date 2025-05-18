// src/pages/usuarios/EliminarUsuario.jsx
import { useEffect, useState } from "react";
import api from "../../axiosConfig";

function EliminarUsuario() {
  const [usuarios, setUsuarios] = useState([]);
  const [seleccionado, setSeleccionado] = useState(null);

  useEffect(() => {
    cargarUsuarios();
  }, []);

  const cargarUsuarios = async () => {
    try {
      const res = await api.get("/usuarios");
      setUsuarios(res.data);
    } catch (err) {
      console.error("Error al cargar usuarios:", err);
    }
  };

  const handleEliminar = async (id) => {
    const confirmacion = window.confirm("¿Estás seguro de que deseas eliminar este usuario?");
    if (!confirmacion) return;

    try {
      await api.delete(`/usuarios/${id}`);
      alert("Usuario eliminado con éxito");
      setSeleccionado(null);
      cargarUsuarios();
    } catch (err) {
      console.error("Error al eliminar:", err);
      alert("Error al eliminar el usuario");
    }
  };

  return (
    <div>
      <h3 className="text-xl font-semibold mb-4">Eliminar Usuario</h3>

      <ul className="space-y-2">
        {usuarios.map((u) => (
          <li
            key={u.id}
            className="flex justify-between items-center border p-2 rounded hover:bg-gray-50"
          >
            <span>{u.id} - {u.nombre} ({u.rol})</span>
            <button
              onClick={() => handleEliminar(u.id)}
              className="bg-red-600 text-white px-3 py-1 rounded hover:bg-red-700"
            >
              Eliminar
            </button>
          </li>
        ))}
      </ul>
    </div>
  );
}

export default EliminarUsuario;
