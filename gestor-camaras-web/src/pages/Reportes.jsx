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

  const rol = localStorage.getItem('rol');
  const userId = localStorage.getItem('userId');

  useEffect(() => {
    const fetchDatos = async () => {
      try {
        const token = localStorage.getItem('token');
        const headers = {
          Authorization: `Bearer ${token}`,
        };
        const urls = {
          camaras: rol === 'admin'
              ? '/api/camara/all'
              : `/api/camara/usuario/${userId}`,
          imagenes: rol === 'admin'
              ? '/api/imagen/all'
              : `/api/imagen/usuario/${userId}`,
          videos: rol === 'admin'
              ? '/api/video/all'
              : `/api/video/usuario/${userId}`,
        };

        const [resCamaras, resImagenes, resVideos] = await Promise.all([
          axios.get(urls.camaras, { headers }),
          axios.get(urls.imagenes, { headers }),
          axios.get(urls.videos, { headers }),
        ]);

        setDatos({
          camaras: resCamaras.data.length,
          imagenes: resImagenes.data.length,
          videos: resVideos.data.length,
        });
      } catch (error) {
        console.error('Error al obtener datos del reporte:', error);
      }
    };

    fetchDatos();
  }, [rol, userId]);

  const datosGrafico = [
    { nombre: 'Cámaras Activas', cantidad: datos.camaras },
    { nombre: 'Imágenes Tomadas', cantidad: datos.imagenes },
    { nombre: 'Videos Grabados', cantidad: datos.videos },
  ];

  const buscarPorId = async () => {
    try {
      const token = localStorage.getItem('token');
      const headers = {
        Authorization: `Bearer ${token}`,
      };
      const [resCam, resImg, resVid] = await Promise.all([
        axios.get(`/api/camara/${busqueda}`, { headers }),
        axios.get(`/api/imagen/camara/${busqueda}`, { headers }),
        axios.get(`/api/video/camara/${busqueda}`, { headers }),
      ]);

      setDatosPorId({
        camaras: resCam.data ? 1 : 0,
        imagenes: resImg.data.length,
        videos: resVid.data.length,
      });
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
              <p>Cámara encontrada: {datosPorId.camaras ? 'Sí' : 'No'}</p>
              <p>Imágenes Tomadas: {datosPorId.imagenes}</p>
              <p>Videos Grabados: {datosPorId.videos}</p>
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