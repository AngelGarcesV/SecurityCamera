import { useEffect, useState } from "react";
import api from "../../axiosConfig";
import "@/styles/layout.css";
import "@/styles/usuarios.css";

function EliminarUsuario() {
  const [usuarios, setUsuarios] = useState([]);
  const [usuarioSeleccionado, setUsuarioSeleccionado] = useState(null);
  const [confirmando, setConfirmando] = useState(false);
  const [loading, setLoading] = useState(true);
  const [deleting, setDeleting] = useState(false);
  const [message, setMessage] = useState({ text: "", type: "" });

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
    } catch (err) {
      console.error("Error al cargar usuarios:", err);
      setMessage({ text: "Error al cargar usuarios", type: "danger" });
      setLoading(false);
    }
  };

  const seleccionarParaEliminar = (usuario) => {
    setUsuarioSeleccionado(usuario);
    setConfirmando(true);
    setMessage({ text: "", type: "" });
  };

  const cancelarEliminacion = () => {
    setConfirmando(false);
    setUsuarioSeleccionado(null);
  };

  const confirmarEliminacion = async () => {
    if (!usuarioSeleccionado) return;

    setDeleting(true);

    try {
      // Modificado para usar el endpoint correcto
      await api.delete(`/usuario/${usuarioSeleccionado.id}`);
      setMessage({ text: "Usuario eliminado con éxito", type: "success" });
      setConfirmando(false);
      setUsuarioSeleccionado(null);
      cargarUsuarios();
      setDeleting(false);
    } catch (err) {
      console.error("Error al eliminar:", err);
      setMessage({ text: "Error al eliminar el usuario", type: "danger" });
      setDeleting(false);
    }
  };

  if (loading) return <div className="loading">Cargando usuarios</div>;

  return (
      <div>
        <h3 className="text-xl font-semibold mb-4">Eliminar Usuario</h3>

        {message.text && (
            <div className={`alert alert-${message.type}`}>{message.text}</div>
        )}

        {confirmando && usuarioSeleccionado ? (
            <div className="confirmation-dialog">
              <h4 className="text-lg font-semibold mb-3">Confirmar eliminación</h4>
              <p>¿Estás seguro de que deseas eliminar al usuario {usuarioSeleccionado.nombre}?</p>
              <p className="text-danger mb-4">Esta acción no se puede deshacer.</p>

              <div className="actions mt-4">
                <button
                    onClick={confirmarEliminacion}
                    className="btn btn-danger"
                    disabled={deleting}
                >
                  {deleting ? "Eliminando..." : "Sí, eliminar"}
                </button>
                <button
                    onClick={cancelarEliminacion}
                    className="btn btn-secondary"
                    disabled={deleting}
                >
                  Cancelar
                </button>
              </div>
            </div>
        ) : (
            <div className="user-list">
              {usuarios.map((u) => (
                  <div key={u.id} className="usuario-item">
                    <div>
                      <strong>{u.nombre}</strong> ({u.correo})
                      <div><span className={u.rol === 'admin' ? 'text-primary' : ''}>{u.rol}</span></div>
                    </div>
                    <button
                        onClick={() => seleccionarParaEliminar(u)}
                        className="btn btn-danger"
                    >
                      Eliminar
                    </button>
                  </div>
              ))}

              {usuarios.length === 0 && (
                  <p className="text-muted">No hay usuarios disponibles</p>
              )}
            </div>
        )}
      </div>
  );
}

export default EliminarUsuario;
