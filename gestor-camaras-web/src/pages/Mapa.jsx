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

  useEffect(() => {
    const fetchCamaras = async () => {
      try {
        const response = await api.get("/camaras");
        setCamaras(response.data);
      } catch (error) {
        console.error("Error al obtener cámaras:", error);
      }
    };

    fetchCamaras();
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

