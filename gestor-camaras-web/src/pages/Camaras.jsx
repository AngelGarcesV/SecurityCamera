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
            // Auto-llenar las coordenadas en el formulario
            setNuevaCamara(prev => ({
              ...prev,
              coordenadax: coords.x.toString(),
              coordenaday: coords.y.toString()
            }));
            setUbicacionObtenida(true);
          },
          (err) => {
            console.error("No se pudo obtener ubicación:", err);
            // Coordenadas por defecto de Villavicencio si falla la geolocalización
            const defaultCoords = { x: 4.1502603, y: -73.6182865 };
            setCoordenadas(defaultCoords);
            setNuevaCamara(prev => ({
              ...prev,
              coordenadax: defaultCoords.x.toString(),
              coordenaday: defaultCoords.y.toString()
            }));
            setUbicacionObtenida(true);
          }
      );
    } else {
      alert("Geolocalización no soportada por tu navegador.");
      // Coordenadas por defecto de Villavicencio
      const defaultCoords = { x: 4.1502603, y: -73.6182865 };
      setCoordenadas(defaultCoords);
      setNuevaCamara(prev => ({
        ...prev,
        coordenadax: defaultCoords.x.toString(),
        coordenaday: defaultCoords.y.toString()
      }));
      setUbicacionObtenida(true);
    }
  };

  const handleInputChange = (e) => {
    const { name, value } = e.target;
    setNuevaCamara((prev) => ({ ...prev, [name]: value }));
  };

  const handleAgregarCamara = async (e) => {
    e.preventDefault();
    const userId = localStorage.getItem("userId");
    if (!userId) {
      alert("No se encontró el usuario. Intenta volver a iniciar sesión.");
      return;
    }

    const camaraData = {
      ip: nuevaCamara.ip,
      puerto: parseInt(nuevaCamara.puerto, 10),
      descripcion: nuevaCamara.descripcion,
      resolucion: nuevaCamara.resolucion,
      coordenadax: parseFloat(nuevaCamara.coordenadax),
      coordenaday: parseFloat(nuevaCamara.coordenaday),
      usuarioId: parseInt(userId, 10)
    };

    try {
      await api.post("/camara/save", camaraData);
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
    } catch (error) {
      console.error("❌ Error al crear cámara:", error);
      alert("Error al crear cámara");
    }
  };

  const obtenerNuevaUbicacion = () => {
    setUbicacionObtenida(false);
    obtenerUbicacionActual();
  };

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
        {/* Header fijo */}
        <div className="camaras-header">
          <div className="page-header">
            <div>
              <h2 className="page-title">Mis Cámaras</h2>
              <p className="page-subtitle">Listado de cámaras registradas</p>
            </div>
            <button
                className="button-primary"
                onClick={() => setMostrarFormulario(!mostrarFormulario)}
            >
              {mostrarFormulario ? "Cancelar" : "Agregar Cámara"}
            </button>
          </div>
        </div>

        {/* Contenido principal con scroll */}
        <div className="camaras-content">
          {mostrarFormulario && (
              <div className="form-section">
                <h3 className="section-title">Nueva Cámara</h3>
                <form onSubmit={handleAgregarCamara} className="form-container">
                  <div className="form-row">
                    <div className="form-group">
                      <label>IP *</label>
                      <input
                          name="ip"
                          value={nuevaCamara.ip}
                          onChange={handleInputChange}
                          placeholder="192.168.1.100"
                          required
                      />
                    </div>
                    <div className="form-group">
                      <label>Puerto *</label>
                      <input
                          name="puerto"
                          type="number"
                          value={nuevaCamara.puerto}
                          onChange={handleInputChange}
                          placeholder="8080"
                          required
                      />
                    </div>
                  </div>

                  <div className="form-group">
                    <label>Descripción *</label>
                    <input
                        name="descripcion"
                        value={nuevaCamara.descripcion}
                        onChange={handleInputChange}
                        placeholder="Cámara entrada principal"
                        required
                    />
                  </div>

                  <div className="form-group">
                    <label>Resolución *</label>
                    <input
                        name="resolucion"
                        value={nuevaCamara.resolucion}
                        onChange={handleInputChange}
                        placeholder="1920x1080"
                        required
                    />
                  </div>

                  <div className="coordenadas-section">
                    <div className="coordenadas-header">
                      <h4>Coordenadas de Ubicación</h4>
                      <button
                          type="button"
                          className="button-secondary"
                          onClick={obtenerNuevaUbicacion}
                          disabled={!ubicacionObtenida}
                      >
                        📍 Actualizar Ubicación
                      </button>
                    </div>
                    <div className="form-row">
                      <div className="form-group">
                        <label>Latitud (X) *</label>
                        <input
                            name="coordenadax"
                            type="number"
                            step="any"
                            value={nuevaCamara.coordenadax}
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
                            value={nuevaCamara.coordenaday}
                            onChange={handleInputChange}
                            placeholder="-73.6182865"
                            required
                        />
                      </div>
                    </div>
                    {ubicacionObtenida && (
                        <p className="ubicacion-info">
                          ✅ Ubicación obtenida automáticamente. Puedes modificar las coordenadas si es necesario.
                        </p>
                    )}
                  </div>

                  <button type="submit" className="button-primary button-submit">
                    Guardar Cámara
                  </button>
                </form>
              </div>
          )}

          {/* Tabla de cámaras */}
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
                          <td>{cam.ip}</td>
                          <td>{cam.puerto}</td>
                          <td>{cam.descripcion}</td>
                          <td>{cam.resolucion}</td>
                          <td>{cam.coordenadax?.toFixed(6) || 'N/A'}</td>
                          <td>{cam.coordenaday?.toFixed(6) || 'N/A'}</td>
                          <td>
                            <button
                                className="button-primary button-small"
                                onClick={() => navigate(`/camaras/${cam.id}/galeria`)}
                            >
                              Ver Galería
                            </button>
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