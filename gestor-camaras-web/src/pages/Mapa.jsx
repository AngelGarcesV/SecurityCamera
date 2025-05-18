// src/pages/Mapa.jsx
import { useEffect, useState } from "react";
import api from "../axiosConfig";
import { GoogleMap, Marker, useJsApiLoader } from "@react-google-maps/api";
import "@/styles/layout.css";
import "@/styles/mapa.css";

const containerStyle = {
  width: "100%",
  height: "600px",
};

const defaultCenter = {
  lat: 6.25184,
  lng: -75.56359,
};

function Mapa() {
  const [camaras, setCamaras] = useState([]);

  const { isLoaded } = useJsApiLoader({
    googleMapsApiKey: "TU_API_KEY_AQUI", // ⛔ Reemplaza con tu API Key válida
  });

  useEffect(() => {
    const token = localStorage.getItem("token");

    api
      .get("/camaras/mis-camaras", {
        headers: {
          Authorization: `Bearer ${token}`,
        },
      })
      .then((res) => setCamaras(res.data))
      .catch(() => alert("Error al cargar tus cámaras"));
  }, []);

  if (!isLoaded) return <div className="mapa-container">Cargando mapa...</div>;

  return (
    <div className="main-content mapa-container">
      <div className="page-header">
        <div>
          <h2 className="page-title">Ubicación de Cámaras</h2>
          <p className="page-subtitle">Visualiza en el mapa tus cámaras registradas</p>
        </div>
      </div>

      <GoogleMap
        mapContainerStyle={containerStyle}
        center={defaultCenter}
        zoom={14}
      >
        {camaras.map((cam) => (
          <Marker
            key={cam.id}
            position={{ lat: cam.coordenadaX, lng: cam.coordenadaY }}
            title={cam.nombre}
          />
        ))}
      </GoogleMap>
    </div>
  );
}

export default Mapa;
