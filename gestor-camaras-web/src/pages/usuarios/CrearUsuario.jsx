import { useState } from "react";
import api from "../../axiosConfig";
import "@/styles/layout.css";
import "@/styles/usuarios.css";

function CrearUsuario() {
  const [formData, setFormData] = useState({
    nombre: "",
    correo: "",
    password: "",
    rol: "user",
  });
  const [loading, setLoading] = useState(false);
  const [successMessage, setSuccessMessage] = useState(null);
  const [errorMessage, setErrorMessage] = useState(null);

  const handleChange = (e) => {
    const { name, value } = e.target;
    setFormData((prev) => ({ ...prev, [name]: value }));
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    setLoading(true);
    setErrorMessage(null);
    setSuccessMessage(null);

    try {
      // Modificado para usar el endpoint correcto
      await api.post("/usuario/save", formData);
      setSuccessMessage("Usuario creado correctamente");
      setFormData({ nombre: "", correo: "", password: "", rol: "user" });
      setLoading(false);
    } catch (error) {
      console.error("Error al crear usuario:", error);
      setErrorMessage("Hubo un problema al crear el usuario");
      setLoading(false);
    }
  };

  return (
      <div>
        <h3 className="text-xl font-semibold mb-4">Crear Usuario</h3>

        {successMessage && (
            <div className="alert alert-success">{successMessage}</div>
        )}

        {errorMessage && (
            <div className="alert alert-danger">{errorMessage}</div>
        )}

        <form onSubmit={handleSubmit} className="form-container">
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
            <label>Contraseña</label>
            <input
                type="password"
                name="password"
                value={formData.password}
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
              className="btn btn-primary"
              disabled={loading}
          >
            {loading ? "Creando..." : "Crear Usuario"}
          </button>
        </form>
      </div>
  );
}

export default CrearUsuario;
