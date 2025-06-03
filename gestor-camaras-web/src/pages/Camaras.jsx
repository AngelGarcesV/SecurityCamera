import { useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";
import api from "../axiosConfig";
import "@/styles/layout.css";
import "@/styles/camaras.css";

function Camaras() {
  const [camaras, setCamaras] = useState([]);
  const [mostrarFormulario, setMostrarFormulario] = useState(false);
  const [coordenadas, setCoordenadas] = useState({ x: "", y: "" });
  const [nuevaCamara, setNuevaCamara] = useState({
    ip: "",
    puerto: "",
    descripcion: "",
    resolucion: "",
    coordenadax: "",
    coordenaday: ""
  });
  const [loading, setLoading] = useState(true);
  const [ubicacionObtenida, setUbicacionObtenida] = useState(false);

  // ✅ Estados para edición
  const [camaraEditando, setCamaraEditando] = useState(null);
  const [modoEdicion, setModoEdicion] = useState(false);

  const navigate = useNavigate();

  useEffect(() => {
    fetchCamaras();
    obtenerUbicacionActual();
  }, []);

  const fetchCamaras = async () => {
    try {
      const userId = localStorage.getItem("userId");
      const rol = localStorage.getItem("rol");
      if (!userId || !rol) {
        alert("No se encontró el usuario. Intenta volver a iniciar sesión.");
        return;
      }

      const endpoint = rol === "admin"
          ? "/camara/all"
          : `/camara/usuario/${userId}`;

      const response = await api.get(endpoint);
      setCamaras(response.data);
    } catch (error) {
      console.error("Error al cargar las cámaras:", error);
    } finally {
      setLoading(false);
    }
  };

  const obtenerUbicacionActual = () => {
    if (navigator.geolocation) {
      navigator.geolocation.getCurrentPosition(
          (pos) => {
            const coords = {
              x: pos.coords.latitude,
              y: pos.coords.longitude
            };
            setCoordenadas(coords);
            if (!modoEdicion) {
              setNuevaCamara(prev => ({
                ...prev,
                coordenadax: coords.x.toString(),
                coordenaday: coords.y.toString()
              }));
            }
            setUbicacionObtenida(true);
          },
          (err) => {
            console.error("No se pudo obtener ubicación:", err);
            const defaultCoords = { x: 4.1502603, y: -73.6182865 };
            setCoordenadas(defaultCoords);
            if (!modoEdicion) {
              setNuevaCamara(prev => ({
                ...prev,
                coordenadax: defaultCoords.x.toString(),
                coordenaday: defaultCoords.y.toString()
              }));
            }
            setUbicacionObtenida(true);
          }
      );
    } else {
      alert("Geolocalización no soportada por tu navegador.");
      const defaultCoords = { x: 4.1502603, y: -73.6182865 };
      setCoordenadas(defaultCoords);
      if (!modoEdicion) {
        setNuevaCamara(prev => ({
          ...prev,
          coordenadax: defaultCoords.x.toString(),
          coordenaday: defaultCoords.y.toString()
        }));
      }
      setUbicacionObtenida(true);
    }
  };

  const handleInputChange = (e) => {
    const { name, value } = e.target;
    console.log(`📝 Campo ${name} cambiado a:`, value);

    if (modoEdicion) {
      setCamaraEditando((prev) => ({ ...prev, [name]: value }));
    } else {
      setNuevaCamara((prev) => ({ ...prev, [name]: value }));
    }
  };

  // ✅ Función para iniciar edición
  const handleEditarCamara = (camara) => {
    setCamaraEditando({
      id: camara.id,
      ip: camara.ip || "",
      puerto: camara.puerto ? camara.puerto.toString() : "",
      descripcion: camara.descripcion || "",
      resolucion: camara.resolucion || "",
      coordenadax: camara.coordenadax ? camara.coordenadax.toString() : "",
      coordenaday: camara.coordenaday ? camara.coordenaday.toString() : "",
      usuarioId: camara.usuarioId
    });
    setModoEdicion(true);
    setMostrarFormulario(true);
  };

  // ✅ Función para cancelar edición
  const handleCancelarEdicion = () => {
    setCamaraEditando(null);
    setModoEdicion(false);
    setMostrarFormulario(false);
    setNuevaCamara({
      ip: "",
      puerto: "",
      descripcion: "",
      resolucion: "",
      coordenadax: coordenadas.x.toString(),
      coordenaday: coordenadas.y.toString()
    });
  };

  // ✅ Función para actualizar cámara
  const handleActualizarCamara = async (e) => {
    e.preventDefault();

    if (!camaraEditando.ip || !camaraEditando.puerto) {
      alert("IP y Puerto son campos obligatorios");
      return;
    }

    if (!camaraEditando.coordenadax || !camaraEditando.coordenaday) {
      alert("Las coordenadas son obligatorias");
      return;
    }

    const camaraData = {
      id: camaraEditando.id,
      ip: camaraEditando.ip.trim(),
      puerto: parseInt(camaraEditando.puerto, 10),
      descripcion: camaraEditando.descripcion.trim(),
      resolucion: camaraEditando.resolucion.trim(),
      coordenadax: parseFloat(camaraEditando.coordenadax),
      coordenaday: parseFloat(camaraEditando.coordenaday),
      usuarioId: camaraEditando.usuarioId
    };

    console.log("📡 Actualizando cámara:", camaraData);

    try {
      const response = await api.put("/camara/update", camaraData, {
        headers: {
          'Content-Type': 'application/json'
        }
      });

      console.log("✅ Cámara actualizada exitosamente:", response.data);

      handleCancelarEdicion();
      fetchCamaras();
      alert("Cámara actualizada exitosamente");

    } catch (error) {
      console.error("❌ Error al actualizar cámara:", error);
      alert(`Error al actualizar cámara: ${error.response?.data?.message || error.message}`);
    }
  };

  // ✅ Función para eliminar cámara
  const handleEliminarCamara = async (id, descripcion) => {
    const confirmacion = window.confirm(
        `¿Estás seguro de que quieres eliminar la cámara "${descripcion}"?\n\nEsta acción no se puede deshacer.`
    );

    if (!confirmacion) return;

    try {
      await api.delete(`/camara/${id}`);
      console.log("✅ Cámara eliminada exitosamente");

      fetchCamaras();
      alert("Cámara eliminada exitosamente");

    } catch (error) {
      console.error("❌ Error al eliminar cámara:", error);

      if (error.response?.status === 404) {
        alert("Error: Cámara no encontrada");
      } else {
        alert(`Error al eliminar cámara: ${error.response?.data?.message || error.message}`);
      }
    }
  };

  const handleAgregarCamara = async (e) => {
    e.preventDefault();
    const userId = localStorage.getItem("userId");
    if (!userId) {
      alert("No se encontró el usuario. Intenta volver a iniciar sesión.");
      return;
    }

    if (!nuevaCamara.ip || !nuevaCamara.puerto) {
      alert("IP y Puerto son campos obligatorios");
      return;
    }

    if (!nuevaCamara.coordenadax || !nuevaCamara.coordenaday) {
      alert("Las coordenadas son obligatorias");
      return;
    }

    const camaraData = {
      ip: nuevaCamara.ip.trim(),
      puerto: parseInt(nuevaCamara.puerto, 10),
      descripcion: nuevaCamara.descripcion.trim(),
      resolucion: nuevaCamara.resolucion.trim(),
      coordenadax: parseFloat(nuevaCamara.coordenadax),
      coordenaday: parseFloat(nuevaCamara.coordenaday),
      usuarioId: parseInt(userId, 10)
    };

    console.log("📡 Enviando datos de cámara:", camaraData);

    try {
      const response = await api.post("/camara/save", camaraData, {
        headers: {
          'Content-Type': 'application/json'
        }
      });

      console.log("✅ Cámara creada exitosamente:", response.data);

      setNuevaCamara({
        ip: "",
        puerto: "",
        descripcion: "",
        resolucion: "",
        coordenadax: coordenadas.x.toString(),
        coordenaday: coordenadas.y.toString()
      });

      setMostrarFormulario(false);
      fetchCamaras();
      alert("Cámara agregada exitosamente");

    } catch (error) {
      console.error("❌ Error al crear cámara:", error);
      console.error("❌ Respuesta del servidor:", error.response?.data);

      if (error.response?.status === 404) {
        alert("Error 404: No se encontró el endpoint. Verifica la URL del servidor.");
      } else if (error.response?.status === 400) {
        alert(`Error de validación: ${error.response?.data?.message || 'Datos inválidos'}`);
      } else {
        alert(`Error al crear cámara: ${error.response?.data?.message || error.message}`);
      }
    }
  };

  const obtenerNuevaUbicacion = () => {
    setUbicacionObtenida(false);
    obtenerUbicacionActual();
  };

  // ✅ Determinar qué datos del formulario usar
  const datosFormulario = modoEdicion ? camaraEditando : nuevaCamara;

  if (loading) {
    return (
        <div className="camaras-container">
          <div className="camaras-header">
            <div className="page-header">
              <h2 className="page-title">Mis Cámaras</h2>
              <p className="page-subtitle">Listado de cámaras registradas</p>
            </div>
          </div>
          <div className="loading-state">
            <h3>Cargando cámaras...</h3>
            <p>Por favor espera mientras obtenemos los datos.</p>
          </div>
        </div>
    );
  }

  return (
      <div className="camaras-container">
        <div className="camaras-header">
          <div className="page-header">
            <div>
              <h2 className="page-title">Mis Cámaras</h2>
              <p className="page-subtitle">Listado de cámaras registradas</p>
            </div>
            <button
                className="button-primary"
                onClick={() => {
                  if (modoEdicion) {
                    handleCancelarEdicion();
                  } else {
                    setMostrarFormulario(!mostrarFormulario);
                  }
                }}
            >
              {mostrarFormulario
                  ? (modoEdicion ? "Cancelar Edición" : "Cancelar")
                  : "Agregar Cámara"
              }
            </button>
          </div>
        </div>

        <div className="camaras-content">
          {mostrarFormulario && (
              <div className="form-section">
                <h3 className="section-title">
                  {modoEdicion ? "Editar Cámara" : "Nueva Cámara"}
                </h3>
                <form onSubmit={modoEdicion ? handleActualizarCamara : handleAgregarCamara} className="form-container">
                  <div className="form-row">
                    <div className="form-group">
                      <label>IP *</label>
                      <input
                          name="ip"
                          type="text"
                          value={datosFormulario?.ip || ""}
                          onChange={handleInputChange}
                          placeholder="192.168.1.100"
                          required
                          minLength="7"
                          maxLength="15"
                          pattern="^(?:[0-9]{1,3}\.){3}[0-9]{1,3}$"
                          title="Ingresa una IP válida (ej: 192.168.1.100)"
                      />
                    </div>
                    <div className="form-group">
                      <label>Puerto *</label>
                      <input
                          name="puerto"
                          type="number"
                          value={datosFormulario?.puerto || ""}
                          onChange={handleInputChange}
                          placeholder="8080"
                          required
                          min="1"
                          max="65535"
                          title="Ingresa un puerto válido (1-65535)"
                      />
                    </div>
                  </div>

                  <div className="form-group">
                    <label>Descripción *</label>
                    <input
                        name="descripcion"
                        value={datosFormulario?.descripcion || ""}
                        onChange={handleInputChange}
                        placeholder="Cámara entrada principal"
                        required
                    />
                  </div>

                  <div className="form-group">
                    <label>Resolución *</label>
                    <input
                        name="resolucion"
                        value={datosFormulario?.resolucion || ""}
                        onChange={handleInputChange}
                        placeholder="1920x1080"
                        required
                    />
                  </div>

                  <div className="coordenadas-section">
                    <div className="coordenadas-header">
                      <h4>Coordenadas de Ubicación</h4>
                      {!modoEdicion && (
                          <button
                              type="button"
                              className="button-secondary"
                              onClick={obtenerNuevaUbicacion}
                              disabled={!ubicacionObtenida}
                          >
                            📍 Actualizar Ubicación
                          </button>
                      )}
                    </div>
                    <div className="form-row">
                      <div className="form-group">
                        <label>Latitud (X) *</label>
                        <input
                            name="coordenadax"
                            type="number"
                            step="any"
                            value={datosFormulario?.coordenadax || ""}
                            onChange={handleInputChange}
                            placeholder="4.1502603"
                            required
                        />
                      </div>
                      <div className="form-group">
                        <label>Longitud (Y) *</label>
                        <input
                            name="coordenaday"
                            type="number"
                            step="any"
                            value={datosFormulario?.coordenaday || ""}
                            onChange={handleInputChange}
                            placeholder="-73.6182865"
                            required
                        />
                      </div>
                    </div>
                    {ubicacionObtenida && !modoEdicion && (
                        <p className="ubicacion-info">
                          ✅ Ubicación obtenida automáticamente. Puedes modificar las coordenadas si es necesario.
                        </p>
                    )}
                  </div>

                  <div className="form-actions">
                    <button type="submit" className="button-primary button-submit">
                      {modoEdicion ? "Actualizar Cámara" : "Guardar Cámara"}
                    </button>
                    {modoEdicion && (
                        <button
                            type="button"
                            className="button-secondary"
                            onClick={handleCancelarEdicion}
                        >
                          Cancelar
                        </button>
                    )}
                  </div>
                </form>
              </div>
          )}

          <div className="table-section">
            <h3 className="section-title">Cámaras Registradas ({camaras.length})</h3>
            <div className="table-container">
              <table className="data-table">
                <thead>
                <tr>
                  <th>IP</th>
                  <th>Puerto</th>
                  <th>Descripción</th>
                  <th>Resolución</th>
                  <th>Coordenada X</th>
                  <th>Coordenada Y</th>
                  <th>Acciones</th>
                </tr>
                </thead>
                <tbody>
                {camaras.length > 0 ? (
                    camaras.map((cam) => (
                        <tr key={cam.id}>
                          <td>{cam.ip || 'N/A'}</td>
                          <td>{cam.puerto || 'N/A'}</td>
                          <td>{cam.descripcion}</td>
                          <td>{cam.resolucion}</td>
                          <td>{cam.coordenadax?.toFixed(6) || 'N/A'}</td>
                          <td>{cam.coordenaday?.toFixed(6) || 'N/A'}</td>
                          <td>
                            <div className="action-buttons">
                              <button
                                  className="button-primary button-small"
                                  onClick={() => navigate(`/camaras/${cam.id}/galeria`)}
                                  title="Ver galería"
                              >
                                📷 Ver
                              </button>
                              <button
                                  className="button-secondary button-small"
                                  onClick={() => handleEditarCamara(cam)}
                                  title="Editar cámara"
                              >
                                ✏️ Editar
                              </button>
                              <button
                                  className="button-danger button-small"
                                  onClick={() => handleEliminarCamara(cam.id, cam.descripcion)}
                                  title="Eliminar cámara"
                              >
                                🗑️ Eliminar
                              </button>
                            </div>
                          </td>
                        </tr>
                    ))
                ) : (
                    <tr>
                      <td colSpan="7" className="no-data">
                        No hay cámaras registradas. Agrega tu primera cámara usando el botón "Agregar Cámara".
                      </td>
                    </tr>
                )}
                </tbody>
              </table>
            </div>
          </div>
        </div>
      </div>
  );
}

export default Camaras;