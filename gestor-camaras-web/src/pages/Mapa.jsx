// src/pages/Mapa.jsx
import { useEffect, useState } from "react";
import { MapContainer, TileLayer, Marker, Popup } from "react-leaflet";
import "leaflet/dist/leaflet.css";
import L from "leaflet";
import api from "../axiosConfig";
import "@/styles/layout.css";
import "@/styles/mapa.css";

// Ícono de cámara personalizado
const camaraIcon = new L.Icon({
    iconUrl: "https://cdn-icons-png.flaticon.com/512/2776/2776067.png",
    iconSize: [32, 32],
    iconAnchor: [16, 32],
    popupAnchor: [0, -32]
});

function Mapa() {
    const [camaras, setCamaras] = useState([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState(null);
    const rol = localStorage.getItem("rol");

    useEffect(() => {
        const fetchCamaras = async () => {
            try {
                setLoading(true);
                const token = localStorage.getItem("token");
                const url = rol === "admin"
                    ? "/camara/all"
                    : `/camara/usuario/${localStorage.getItem("userId")}`;

                const response = await api.get(url, {
                    headers: {
                        Authorization: `Bearer ${token}`,
                    },
                });

                console.log("📡 Cámaras recibidas:", response.data);
                setCamaras(response.data);
                setError(null);
            } catch (error) {
                console.error("Error al obtener cámaras:", error);
                setError("Error al cargar las cámaras");
            } finally {
                setLoading(false);
            }
        };

        fetchCamaras();
    }, [rol]);

    // Función para obtener el centro y zoom del mapa
    const getMapSettings = () => {
        const validCamaras = camaras.filter(cam =>
            cam.coordenadax != null && cam.coordenaday != null
        );

        // Coordenadas exactas del centro de Villavicencio (Plaza Los Libertadores)
        const villavicencioCenter = [4.142, -73.626]; // Centro exacto de Villavicencio

        // SIEMPRE retornar las coordenadas de Villavicencio para forzar la vista inicial
        return {
            center: villavicencioCenter,
            zoom: 12 // Zoom que muestra bien la ciudad completa
        };
    };

    const validCamaras = camaras.filter(cam =>
        cam.coordenadax != null && cam.coordenaday != null
    );

    const mapSettings = getMapSettings();

    if (loading) {
        return (
            <div className="main-content">
                <div className="page-header">
                    <div>
                        <h2 className="page-title">Ubicación de Cámaras</h2>
                        <p className="page-subtitle">Visualiza dónde están tus cámaras registradas.</p>
                    </div>
                </div>
                <div className="loading-container" style={{
                    display: 'flex',
                    justifyContent: 'center',
                    alignItems: 'center',
                    height: '400px',
                    fontSize: '18px',
                    color: '#666'
                }}>
                    Cargando mapa...
                </div>
            </div>
        );
    }

    if (error) {
        return (
            <div className="main-content">
                <div className="page-header">
                    <div>
                        <h2 className="page-title">Ubicación de Cámaras</h2>
                        <p className="page-subtitle">Visualiza dónde están tus cámaras registradas.</p>
                    </div>
                </div>
                <div className="error-container" style={{
                    display: 'flex',
                    justifyContent: 'center',
                    alignItems: 'center',
                    height: '400px',
                    fontSize: '18px',
                    color: '#e74c3c',
                    backgroundColor: '#fdf2f2',
                    border: '1px solid #f5c6cb',
                    borderRadius: '8px',
                    padding: '20px'
                }}>
                    {error}
                </div>
            </div>
        );
    }

    return (
        <div className="main-content mapa-container">
            {/* Header fijo */}
            <div className="mapa-header">
                <div className="page-header">
                    <h2 className="page-title">Ubicación de Cámaras</h2>
                    <p className="page-subtitle">Visualiza dónde están tus cámaras registradas.</p>
                </div>
            </div>

            {/* Contenido principal con scroll */}
            <div className="mapa-content">
                {/* Estadísticas de cámaras */}
                <div className="mapa-stats">
                    <span className="stats-item">
                        Total de cámaras: <strong>{camaras.length}</strong>
                    </span>
                    <span className="stats-item">
                        Con ubicación: <strong>{validCamaras.length}</strong>
                    </span>
                </div>

                {/* Contenedor del mapa */}
                <div className="mapa-wrapper">
                    <MapContainer
                        center={mapSettings.center}
                        zoom={mapSettings.zoom}
                        style={{
                            height: "450px", // Menos altura
                            width: "100%",   // Más ancho
                            borderRadius: "10px",
                            boxShadow: "0 2px 8px rgba(0,0,0,0.1)"
                        }}
                    >
                        <TileLayer
                            attribution='&copy; <a href="https://www.openstreetmap.org/copyright">OpenStreetMap</a> contributors'
                            url="https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png"
                        />

                        {validCamaras.map((cam) => (
                            <Marker
                                key={cam.id}
                                position={[cam.coordenadax, cam.coordenaday]}
                                icon={camaraIcon}
                            >
                                <Popup>
                                    <div style={{ minWidth: '200px' }}>
                                        <strong style={{ fontSize: '16px', color: '#2c3e50' }}>
                                            {cam.nombre || "Cámara"}
                                        </strong>
                                        <br />
                                        <span style={{ color: '#666', fontSize: '14px' }}>
                                            {cam.descripcion || "Sin descripción"}
                                        </span>
                                        <hr style={{ margin: '8px 0', border: 'none', borderTop: '1px solid #eee' }} />
                                        <small style={{ color: '#888', fontSize: '12px' }}>
                                            Lat: {cam.coordenadax?.toFixed(6)}<br />
                                            Lng: {cam.coordenaday?.toFixed(6)}
                                        </small>
                                    </div>
                                </Popup>
                            </Marker>
                        ))}
                    </MapContainer>
                </div>

                {/* Mensaje cuando no hay cámaras con ubicación */}
                {validCamaras.length === 0 && (
                    <div className="no-camaras-message">
                        <p style={{ margin: '0 0 5px 0', fontSize: '16px' }}>
                            📍 No hay cámaras con ubicación registrada
                        </p>
                        <small>Agrega coordenadas a tus cámaras para verlas en el mapa</small>
                    </div>
                )}

                {/* Información adicional del mapa */}
                <div className="mapa-info-section">
                    <h3 className="section-title">Información del Mapa</h3>
                    <div className="info-grid">
                        <div className="info-card">
                            <h4>📍 Ubicación Central</h4>
                            <p>El mapa está centrado en Villavicencio, Meta, Colombia</p>
                            <p><strong>Coordenadas:</strong> 4.142°N, 73.626°W</p>
                        </div>
                        <div className="info-card">
                            <h4>📊 Estadísticas</h4>
                            <p>Total de cámaras registradas: <strong>{camaras.length}</strong></p>
                            <p>Con coordenadas válidas: <strong>{validCamaras.length}</strong></p>
                        </div>
                    </div>
                </div>
            </div>
        </div>
    );
}

export default Mapa;