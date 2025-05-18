<<<<<<< HEAD
// src/pages/usuarios/EditarUsuario.jsx
import { useEffect, useState } from "react";
import api from "../../axiosConfig";
=======
import { useEffect, useState } from "react";
import api from "../../axiosConfig";
import "@/styles/layout.css";
import "@/styles/usuarios.css";
>>>>>>> 3defdf10ad7602be4cbedfe99b997bc93db22203

function EditarUsuario() {
  const [usuarios, setUsuarios] = useState([]);
  const [seleccionado, setSeleccionado] = useState(null);
  const [formData, setFormData] = useState({
    nombre: "",
    correo: "",
    rol: "user",
  });
<<<<<<< HEAD
=======
  const [loading, setLoading] = useState(true);
  const [submitting, setSubmitting] = useState(false);
  const [message, setMessage] = useState({ text: "", type: "" });
>>>>>>> 3defdf10ad7602be4cbedfe99b997bc93db22203

  useEffect(() => {
    cargarUsuarios();
  }, []);

  const cargarUsuarios = async () => {
    try {
<<<<<<< HEAD
      const res = await api.get("/usuarios");
      setUsuarios(res.data);
    } catch (err) {
      console.error("Error al cargar usuarios:", err);
=======
      setLoading(true);
      // Modificado para usar el endpoint correcto
      const res = await api.get("/usuario/all");
      setUsuarios(res.data);
      setLoading(false);
    } catch (err) {
      console.error("Error al cargar usuarios:", err);
      setMessage({ text: "Error al cargar usuarios", type: "danger" });
      setLoading(false);
>>>>>>> 3defdf10ad7602be4cbedfe99b997bc93db22203
    }
  };

  const handleSeleccionar = (usuario) => {
    setSeleccionado(usuario);
    setFormData({
      nombre: usuario.nombre,
      correo: usuario.correo,
      rol: usuario.rol,
    });
<<<<<<< HEAD
=======
    setMessage({ text: "", type: "" });
>>>>>>> 3defdf10ad7602be4cbedfe99b997bc93db22203
  };

  const handleChange = (e) => {
    const { name, value } = e.target;
    setFormData((prev) => ({ ...prev, [name]: value }));
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    if (!seleccionado) return;

<<<<<<< HEAD
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
=======
    setSubmitting(true);
    setMessage({ text: "", type: "" });

    try {
      // Modificado para usar el endpoint correcto
      await api.put(`/usuario/${seleccionado.id}`, formData);
      setMessage({ text: "Usuario actualizado con éxito", type: "success" });
      setSeleccionado(null);
      setFormData({ nombre: "", correo: "", rol: "user" });
      cargarUsuarios();
      setSubmitting(false);
    } catch (err) {
      console.error("Error al actualizar:", err);
      setMessage({ text: "Error al actualizar el usuario", type: "danger" });
      setSubmitting(false);
    }
  };

  if (loading) return <div className="loading">Cargando usuarios</div>;

  return (
      <div>
        <h3 className="text-xl font-semibold mb-4">Editar Usuario</h3>

        {message.text && (
            <div className={`alert alert-${message.type}`}>{message.text}</div>
        )}

        <div className="usuarios-container">
          <div className="user-list">
            {usuarios.map((u) => (
                <div
                    key={u.id}
                    className={`usuario-item ${seleccionado?.id === u.id ? 'bg-light' : ''}`}
                    onClick={() => handleSeleccionar(u)}
                >
                  <span>{u.nombre} ({u.correo})</span>
                  <span className={u.rol === 'admin' ? 'text-primary' : ''}>{u.rol}</span>
                </div>
            ))}
          </div>

          {seleccionado && (
              <form onSubmit={handleSubmit} className="form-container mt-4">
                <div className="form-group">
                  <label>Nombre</label>
                  <input
                      type="text"
                      name="nombre"
                      value={formData.nombre}
                      onChange={handleChange}
                      className="form-control"
                      required
                  />
                </div>

                <div className="form-group">
                  <label>Correo</label>
                  <input
                      type="email"
                      name="correo"
                      value={formData.correo}
                      onChange={handleChange}
                      className="form-control"
                      required
                  />
                </div>

                <div className="form-group">
                  <label>Rol</label>
                  <select
                      name="rol"
                      value={formData.rol}
                      onChange={handleChange}
                      className="form-control"
                  >
                    <option value="user">Usuario</option>
                    <option value="admin">Administrador</option>
                  </select>
                </div>

                <button
                    type="submit"
                    className="btn btn-success"
                    disabled={submitting}
                >
                  {submitting ? "Guardando..." : "Guardar Cambios"}
                </button>
              </form>
          )}
        </div>
      </div>
  );
}

export default EditarUsuario;
>>>>>>> 3defdf10ad7602be4cbedfe99b997bc93db22203
