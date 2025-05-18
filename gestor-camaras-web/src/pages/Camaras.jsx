// src/pages/Camaras.jsx
import { useEffect, useState } from "react";
import api from "../axiosConfig";
import "@/styles/layout.css";
import "@/styles/camaras.css";

function Camaras() {
  const [camaras, setCamaras] = useState([]);

  useEffect(() => {
    const token = localStorage.getItem("token");

    api
      .get("/camaras/mis-camaras")
      .then((res) => setCamaras(res.data))
      .catch(() => alert("Error al cargar tus cámaras"));
  }, []);

  return (
    <div className="main-content">
      <div className="page-header">
        <div>
          <h2 className="page-title">Mis Cámaras</h2>
          <p className="page-subtitle">Listado de cámaras registradas</p>
        </div>
      </div>

      <div className="table-container">
        <table className="data-table">
          <thead>
            <tr>
              <th>Nombre</th>
              <th>Dirección</th>
              <th>Coordenada X</th>
              <th>Coordenada Y</th>
              <th>Acciones</th>
            </tr>
          </thead>
          <tbody>
            {camaras.map((cam) => (
              <tr key={cam.id}>
                <td>{cam.nombre}</td>
                <td>{cam.direccion}</td>
                <td>{cam.coordenadaX}</td>
                <td>{cam.coordenadaY}</td>
                <td>
                  <button className="button-primary">Ver</button>
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
