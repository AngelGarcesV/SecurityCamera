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
    resolucion: ""
  });

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
    }
  };

  const obtenerUbicacionActual = () => {
    if (navigator.geolocation) {
      navigator.geolocation.getCurrentPosition(
          (pos) => {
            setCoordenadas({
              x: pos.coords.latitude,
              y: pos.coords.longitude
            });
          },
          (err) => {
            console.error("No se pudo obtener ubicación:", err);
          }
      );
    } else {
      alert("Geolocalización no soportada por tu navegador.");
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
      coordenadax: coordenadas.x,
      coordenaday: coordenadas.y,
      usuarioId: parseInt(userId, 10)
    };

    try {
      await api.post("/camara/save", camaraData);
      setNuevaCamara({
        ip: "",
        puerto: "",
        descripcion: "",
        resolucion: ""
      });
      setMostrarFormulario(false);
      fetchCamaras();
    } catch (error) {
      console.error("❌ Error al crear cámara:", error);
      alert("Error al crear cámara");
    }
  };

  return (
      <div className="main-content">
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
        {mostrarFormulario && (
            <form onSubmit={handleAgregarCamara} className="form-container">
              <div className="form-group">
                <label>IP</label>
                <input
                    name="ip"
                    value={nuevaCamara.ip}
                    onChange={handleInputChange}
                    required
                />
              </div>
              <div className="form-group">
                <label>Puerto</label>
                <input
                    name="puerto"
                    type="number"
                    value={nuevaCamara.puerto}
                    onChange={handleInputChange}
                    required
                />
              </div>
              <div className="form-group">
                <label>Descripción</label>
                <input
                    name="descripcion"
                    value={nuevaCamara.descripcion}
                    onChange={handleInputChange}
                    required
                />
              </div>
              <div className="form-group">
                <label>Resolución</label>
                <input
                    name="resolucion"
                    value={nuevaCamara.resolucion}
                    onChange={handleInputChange}
                    required
                />
              </div>
              <button type="submit" className="button-primary">
                Guardar Cámara
              </button>
            </form>
        )}

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
            {camaras.map((cam) => (
                <tr key={cam.id}>
                  <td>{cam.ip}</td>
                  <td>{cam.puerto}</td>
                  <td>{cam.descripcion}</td>
                  <td>{cam.resolucion}</td>
                  <td>{cam.coordenadax}</td>
                  <td>{cam.coordenaday}</td>
                  <td>
                    <button
                        className="button-primary"
                        onClick={() => navigate(`/camaras/${cam.id}/galeria`)}
                    >
                      Ver
                    </button>
                  </td>
                </tr>
            ))}
            </tbody>
          </table>
        </div>
      </div>
  );
}

export default Camaras;