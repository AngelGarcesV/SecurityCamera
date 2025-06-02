import React, { useEffect, useState } from 'react';
import axios from 'axios';
import {
  BarChart,
  Bar,
  XAxis,
  YAxis,
  Tooltip,
  ResponsiveContainer,
  CartesianGrid,
} from 'recharts';
import '@/styles/layout.css';
import '@/styles/reportes.css';

function Reportes() {
  const [datos, setDatos] = useState({ camaras: 0, imagenes: 0, videos: 0 });
  const [busqueda, setBusqueda] = useState('');
  const [datosPorId, setDatosPorId] = useState(null);
  const [usuario, setUsuario] = useState('');
  const rol = localStorage.getItem('rol');
  const userId = localStorage.getItem('userId');

  useEffect(() => {
    const fetchDatos = async () => {
      try {
        const token = localStorage.getItem('token');
        let url = '';
        if (rol === 'admin') {
          url = 'http://localhost:9000/api/reportes/todos';
        } else {
          url = `http://localhost:9000/api/reportes/usuario/${userId}`;
        }

        const res = await axios.get(url, {
          headers: {
            Authorization: `Bearer ${token}`,
          },
        });

        setDatos(res.data);
      } catch (error) {
        console.error('Error al obtener datos del reporte:', error);
      }
    };

    fetchDatos();
  }, [rol, userId]);

  const datosGrafico = [
    { nombre: 'Cámaras Activas', cantidad: datos.camaras || 0 },
    { nombre: 'Imágenes Tomadas', cantidad: datos.imagenes || 0 },
    { nombre: 'Videos Grabados', cantidad: datos.videos || 0 },
  ];

  const buscarPorId = async () => {
    try {
      const token = localStorage.getItem('token');
      const res = await axios.get(`http://localhost:9000/api/reportes/camara/${busqueda}`, {
        headers: {
          Authorization: `Bearer ${token}`,
        },
      });
      setDatosPorId(res.data);
    } catch (error) {
      console.error('No se encontró información para el ID ingresado:', error);
      setDatosPorId(null);
    }
  };

  return (
      <div className="main-content">
        <div className="page-header">
          <div>
            <h1 className="page-title">Panel de Reportes</h1>
            <p className="page-subtitle">Estadísticas generales del sistema</p>
          </div>
        </div>

        {rol === 'admin' && (
            <div className="busqueda-id">
              <input
                  type="text"
                  placeholder="Buscar reportes por ID de cámara"
                  value={busqueda}
                  onChange={(e) => setBusqueda(e.target.value)}
                  className="busqueda-input"
              />
              <button onClick={buscarPorId} className="busqueda-boton">Buscar</button>
            </div>
        )}

        {datosPorId && (
            <div className="stat-card">
              <h3>Resultados para cámara ID: {busqueda}</h3>
              <p>Cámaras Activas: {datosPorId.camaras}</p>
              <p>Imágenes Tomadas: {datosPorId.imagenes}</p>
              <p>Videos Grabados: {datosPorId.videos}</p>
              {datosPorId.usuario && (
                  <p>Usuario: {datosPorId.usuario}</p>
              )}
            </div>
        )}

        <div className="stats-container">
          <div className="stat-card">
            <h3>Cámaras Activas</h3>
            <div className="stat-value">{datos.camaras}</div>
          </div>
          <div className="stat-card">
            <h3>Imágenes Tomadas</h3>
            <div className="stat-value">{datos.imagenes}</div>
          </div>
          <div className="stat-card">
            <h3>Videos Grabados</h3>
            <div className="stat-value">{datos.videos}</div>
          </div>
        </div>

        <div className="table-container">
          <h2 className="page-title">Gráfico de Actividad</h2>
          <ResponsiveContainer width="100%" height={300}>
            <BarChart data={datosGrafico} margin={{ top: 20, right: 30, left: 20, bottom: 5 }}>
              <CartesianGrid strokeDasharray="3 3" />
              <XAxis dataKey="nombre" />
              <YAxis />
              <Tooltip />
              <Bar dataKey="cantidad" fill="#3949ab" />
            </BarChart>
          </ResponsiveContainer>
        </div>
      </div>
  );
}

export default Reportes;

