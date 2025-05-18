// src/pages/Mapa.jsx
import { useEffect, useState } from "react";
import { MapContainer, TileLayer, Marker, Popup } from "react-leaflet";
import "leaflet/dist/leaflet.css";
import L from "leaflet";
import api from "../axiosConfig";
import "@/styles/layout.css";
import "@/styles/mapa.css";

// Configurar ícono de marcador personalizado (opcional)
const camaraIcon = new L.Icon({
  iconUrl: "https://cdn-icons-png.flaticon.com/512/2776/2776067.png",
  iconSize: [32, 32],
  iconAnchor: [16, 32],
  popupAnchor: [0, -32]
});

function Mapa() {
  const [camaras, setCamaras] = useState([]);

  useEffect(() => {
    // Datos de prueba para simular respuesta del backend
    const datosPrueba = [
      {
        id: 1,
        nombre: "Cámara Principal",
        direccion: "Calle 1 #1-1",
        coordenadaX: 4.653,
        coordenadaY: -74.083
      },
      {
        id: 2,
        nombre: "Cámara Entrada",
        direccion: "Carrera 2 #2-2",
        coordenadaX: 4.657,
        coordenadaY: -74.078
      },
      {
        id: 3,
        nombre: "Cámara Parqueadero",
        direccion: "Av. 3 #3-3",
        coordenadaX: 4.66,
        coordenadaY: -74.08
      }
    ];

    setTimeout(() => setCamaras(datosPrueba), 500);
  }, []);

  return (
    <div className="main-content mapa-container">
      <h2 className="page-title">Ubicación de mis Cámaras</h2>
      <p className="page-subtitle">Visualiza dónde están tus cámaras registradas.</p>

      <MapContainer
        center={[4.655, -74.08]}
        zoom={14}
        style={{ height: "650px", width: "650px", borderRadius: "10px", marginTop: "20px" }}
      >
        <TileLayer
          attribution='&copy; <a href="https://www.openstreetmap.org/copyright">OpenStreetMap</a> contributors'
          url="https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png"
        />

        {camaras.map((cam) => (
          <Marker
            key={cam.id}
            position={[cam.coordenadaX, cam.coordenadaY]}
            icon={camaraIcon}
          >
            <Popup>
              <strong>{cam.nombre}</strong><br />
              {cam.direccion}
            </Popup>
          </Marker>
        ))}
      </MapContainer>
    </div>
  );
}

export default Mapa;
