// src/pages/usuarios/EditarUsuario.jsx
import { useEffect, useState } from "react";
import api from "../../axiosConfig";

function EditarUsuario() {
  const [usuarios, setUsuarios] = useState([]);
  const [seleccionado, setSeleccionado] = useState(null);
  const [formData, setFormData] = useState({
    nombre: "",
    correo: "",
    rol: "user",
  });

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

  const handleSeleccionar = (usuario) => {
    setSeleccionado(usuario);
    setFormData({
      nombre: usuario.nombre,
      correo: usuario.correo,
      rol: usuario.rol,
    });
  };

  const handleChange = (e) => {
    const { name, value } = e.target;
    setFormData((prev) => ({ ...prev, [name]: value }));
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    if (!seleccionado) return;

    try {
      await api.put(`/usuarios/${seleccionado.id}`, formData);
      alert("Usuario actualizado con éxito");
      setSeleccionado(null);
      setFormData({ nombre: "", correo: "", rol: "user" });
      cargarUsuarios();
    } catch (err) {
      console.error("Error al actualizar:", err);
      alert("Error al actualizar el usuario");
    }
  };

  return (
    <div>
      <h3 className="text-xl font-semibold mb-4">Editar Usuario</h3>

      <ul className="mb-6 space-y-1">
        {usuarios.map((u) => (
          <li
            key={u.id}
            className={`cursor-pointer p-2 rounded ${
              seleccionado?.id === u.id ? "bg-blue-100" : "hover:bg-gray-100"
            }`}
            onClick={() => handleSeleccionar(u)}
          >
            {u.id} - {u.nombre} ({u.rol})
          </li>
        ))}
      </ul>

      {seleccionado && (
        <form onSubmit={handleSubmit} className="space-y-4 max-w-md">
          <div>
            <label className="block font-medium">Nombre</label>
            <input
              type="text"
              name="nombre"
              value={formData.nombre}
              onChange={handleChange}
              className="w-full border px-3 py-2 rounded"
            />
          </div>

          <div>
            <label className="block font-medium">Correo</label>
            <input
              type="email"
              name="correo"
              value={formData.correo}
              onChange={handleChange}
              className="w-full border px-3 py-2 rounded"
            />
          </div>

          <div>
            <label className="block font-medium">Rol</label>
            <select
              name="rol"
              value={formData.rol}
              onChange={handleChange}
              className="w-full border px-3 py-2 rounded"
            >
              <option value="user">Usuario</option>
              <option value="admin">Administrador</option>
            </select>
          </div>

          <button
            type="submit"
            className="bg-green-600 text-white px-4 py-2 rounded hover:bg-green-700"
          >
            Guardar Cambios
          </button>
        </form>
      )}
    </div>
  );
}

export default EditarUsuario;
