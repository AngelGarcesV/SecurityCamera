// Camaras.jsx (modo pruebas con JSON)
import { useEffect, useState } from "react";
import '@/styles/layout.css';
import '@/styles/camaras.css';

function Camaras() {
  const [camaras, setCamaras] = useState([]);

  useEffect(() => {
    // Datos de prueba
    const datosPrueba = [
      {
        id: 1,
        nombre: "Cámara Principal",
        direccion: "Calle 123 #45-67",
        coordenadaX: 4.653,
        coordenadaY: -74.083,
      },
      {
        id: 2,
        nombre: "Cámara Entrada",
        direccion: "Carrera 10 #25-30",
        coordenadaX: 4.657,
        coordenadaY: -74.078,
      },
      {
        id: 3,
        nombre: "Cámara Parqueadero",
        direccion: "Av. Las Palmas #8-15",
        coordenadaX: 4.66,
        coordenadaY: -74.08,
      }
    ];

    // Simula respuesta asincrónica
    setTimeout(() => setCamaras(datosPrueba), 500);
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
