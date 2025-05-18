// src/pages/usuarios/CrearUsuario.jsx
import { useState } from "react";
import api from "../../axiosConfig";

function CrearUsuario() {
  const [formData, setFormData] = useState({
    nombre: "",
    correo: "",
    password: "",
    rol: "user", // por defecto será usuario normal
  });

  const handleChange = (e) => {
    const { name, value } = e.target;
    setFormData((prev) => ({ ...prev, [name]: value }));
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    try {
      await api.post("/usuarios", formData); // asegúrate de que esta ruta existe en el backend
      alert("Usuario creado correctamente");
      setFormData({ nombre: "", correo: "", password: "", rol: "user" });
    } catch (error) {
      console.error("Error al crear usuario:", error);
      alert("Hubo un problema al crear el usuario");
    }
  };

  return (
    <div>
      <h3 className="text-xl font-semibold mb-4">Crear Usuario</h3>
      <form onSubmit={handleSubmit} className="space-y-4 max-w-md">
        <div>
          <label className="block font-medium">Nombre</label>
          <input
            type="text"
            name="nombre"
            value={formData.nombre}
            onChange={handleChange}
            className="w-full border rounded px-3 py-2"
            required
          />
        </div>

        <div>
          <label className="block font-medium">Correo</label>
          <input
            type="email"
            name="correo"
            value={formData.correo}
            onChange={handleChange}
            className="w-full border rounded px-3 py-2"
            required
          />
        </div>

        <div>
          <label className="block font-medium">Contraseña</label>
          <input
            type="password"
            name="password"
            value={formData.password}
            onChange={handleChange}
            className="w-full border rounded px-3 py-2"
            required
          />
        </div>

        <div>
          <label className="block font-medium">Rol</label>
          <select
            name="rol"
            value={formData.rol}
            onChange={handleChange}
            className="w-full border rounded px-3 py-2"
          >
            <option value="user">Usuario</option>
            <option value="admin">Administrador</option>
          </select>
        </div>

        <button
          type="submit"
          className="bg-blue-600 text-white px-4 py-2 rounded hover:bg-blue-700"
        >
          Crear
        </button>
      </form>
    </div>
  );
}

export default CrearUsuario;
