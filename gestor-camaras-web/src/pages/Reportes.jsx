import React, { useEffect, useState } from 'react';
import api from '../axiosConfig'; // Usar api en lugar de axios directo
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
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  const rol = localStorage.getItem('rol');
  const userId = localStorage.getItem('userId');

  useEffect(() => {
    const fetchDatos = async () => {
      try {
        setLoading(true);
        const token = localStorage.getItem('token');
        const headers = { Authorization: `Bearer ${token}` };

        console.log('🔍 Usuario actual:', { rol, userId });

        // Usar exactamente las mismas URLs que funcionan en Camaras.jsx
        let camarasCount = 0;
        let imagenesCount = 0;
        let videosCount = 0;

        // 1. Obtener cámaras (sabemos que esto funciona)
        try {
          const camarasUrl = rol === 'admin' ? '/camara/all' : `/camara/usuario/${userId}`;
          console.log('🎯 Obteniendo cámaras de:', camarasUrl);

          const resCamaras = await api.get(camarasUrl, { headers });
          console.log('✅ Respuesta cámaras:', resCamaras.data);

          camarasCount = Array.isArray(resCamaras.data) ? resCamaras.data.length : 0;
        } catch (error) {
          console.error('❌ Error cámaras:', error);
          camarasCount = 0;
        }

        // 2. Para imágenes y videos, vamos a probar múltiples endpoints
        const imagenesEndpoints = [
          '/imagenes/all',
          '/imagen/all',
          '/images/all',
          '/image/all'
        ];

        for (const endpoint of imagenesEndpoints) {
          try {
            console.log('🎯 Probando endpoint imágenes:', endpoint);
            const resImagenes = await api.get(endpoint, { headers });
            console.log('✅ Respuesta imágenes:', resImagenes.data);
            imagenesCount = Array.isArray(resImagenes.data) ? resImagenes.data.length : 0;
            break; // Si funciona, salir del loop
          } catch (error) {
            console.log('❌ Falló endpoint:', endpoint, error.response?.status);
          }
        }

        const videosEndpoints = [
          '/video/all',
          '/video',
          '/videos/all',
          '/videos'
        ];

        for (const endpoint of videosEndpoints) {
          try {
            console.log('🎯 Probando endpoint videos:', endpoint);
            const resVideos = await api.get(endpoint, { headers });
            console.log('✅ Respuesta videos:', resVideos.data);
            videosCount = Array.isArray(resVideos.data) ? resVideos.data.length : 0;
            break; // Si funciona, salir del loop
          } catch (error) {
            console.log('❌ Falló endpoint:', endpoint, error.response?.status);
          }
        }

        console.log('📊 Conteos finales:', { camarasCount, imagenesCount, videosCount });

        setDatos({
          camaras: camarasCount,
          imagenes: imagenesCount,
          videos: videosCount,
        });
        setError(null);
      } catch (error) {
        console.error('💥 Error general:', error);
        setError('Error al cargar los reportes');
        setDatos({ camaras: 0, imagenes: 0, videos: 0 });
      } finally {
        setLoading(false);
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
    if (!busqueda.trim()) {
      alert('Por favor ingresa un ID de cámara válido');
      return;
    }

    try {
      const token = localStorage.getItem('token');
      const headers = { Authorization: `Bearer ${token}` };

      // URLs corregidas para búsqueda específica
      const [resCam, resImg, resVid] = await Promise.all([
        api.get(`/camara/${busqueda}`, { headers }),
        api.get(`/imagen/camara/${busqueda}`, { headers }),  // Cambiado a imagen singular
        api.get(`/video/camara/${busqueda}`, { headers }),
      ]);

      setDatosPorId({
        camaras: resCam.data ? 1 : 0,
        imagenes: Array.isArray(resImg.data) ? resImg.data.length : 0,
        videos: Array.isArray(resVid.data) ? resVid.data.length : 0,
      });
    } catch (error) {
      console.error('No se encontró información para el ID ingresado:', error);
      setDatosPorId(null);
      alert('No se encontró información para el ID ingresado');
    }
  };

  const limpiarBusqueda = () => {
    setBusqueda('');
    setDatosPorId(null);
  };

  if (loading) {
    return (
        <div className="main-content reportes-container">
          <div className="loading-state">
            <h2>Cargando reportes...</h2>
            <p>Por favor espera mientras obtenemos los datos.</p>
          </div>
        </div>
    );
  }

  if (error) {
    return (
        <div className="main-content reportes-container">
          <div className="error-state">
            <h2>Error al cargar reportes</h2>
            <p>{error}</p>
            <button onClick={() => window.location.reload()} className="retry-button">
              Reintentar
            </button>
          </div>
        </div>
    );
  }

  return (
      <div className="main-content reportes-container">
        {/* Header de la página */}
        <div className="reportes-header">
          <div className="page-header">
            <h1 className="page-title">Panel de Reportes</h1>
            <p className="page-subtitle">Estadísticas generales del sistema</p>
          </div>
        </div>

        {/* Contenido principal con scroll */}
        <div className="reportes-content">
          {/* Sección de búsqueda para admins */}
          {rol === 'admin' && (
              <div className="busqueda-section">
                <h3 className="section-title">Búsqueda por Cámara</h3>
                <div className="busqueda-controls">
                  <input
                      type="text"
                      placeholder="Ingresa el ID de la cámara"
                      value={busqueda}
                      onChange={(e) => setBusqueda(e.target.value)}
                      className="busqueda-input"
                      onKeyPress={(e) => e.key === 'Enter' && buscarPorId()}
                  />
                  <button onClick={buscarPorId} className="busqueda-boton">
                    Buscar
                  </button>
                  {datosPorId && (
                      <button onClick={limpiarBusqueda} className="limpiar-boton">
                        Limpiar
                      </button>
                  )}
                </div>
              </div>
          )}

          {/* Resultados de búsqueda específica */}
          {datosPorId && (
              <div className="resultados-busqueda">
                <h3 className="section-title">Resultados para Cámara ID: {busqueda}</h3>
                <div className="stats-grid">
                  <div className="stat-card resultado">
                    <h4>Estado de la Cámara</h4>
                    <div className="stat-value">
                      {datosPorId.camaras ? '✅ Encontrada' : '❌ No encontrada'}
                    </div>
                  </div>
                  <div className="stat-card resultado">
                    <h4>Imágenes Tomadas</h4>
                    <div className="stat-value">{datosPorId.imagenes}</div>
                  </div>
                  <div className="stat-card resultado">
                    <h4>Videos Grabados</h4>
                    <div className="stat-value">{datosPorId.videos}</div>
                  </div>
                </div>
              </div>
          )}

          {/* Estadísticas generales */}
          <div className="estadisticas-generales">
            <h3 className="section-title">Estadísticas Generales</h3>
            <div className="stats-grid">
              <div className="stat-card">
                <h4>Cámaras Activas</h4>
                <div className="stat-value">{datos.camaras}</div>
                <p className="stat-description">Total de cámaras registradas</p>
              </div>
              <div className="stat-card">
                <h4>Imágenes Tomadas</h4>
                <div className="stat-value">{datos.imagenes}</div>
                <p className="stat-description">Capturas realizadas</p>
              </div>
              <div className="stat-card">
                <h4>Videos Grabados</h4>
                <div className="stat-value">{datos.videos}</div>
                <p className="stat-description">Grabaciones completadas</p>
              </div>
            </div>
          </div>

          {/* Gráfico de actividad */}
          <div className="grafico-section">
            <h3 className="section-title">Gráfico de Actividad</h3>
            <div className="chart-container">
              <ResponsiveContainer width="100%" height={350}>
                <BarChart
                    data={datosGrafico}
                    margin={{ top: 20, right: 30, left: 20, bottom: 60 }}
                >
                  <CartesianGrid strokeDasharray="3 3" stroke="#e0e0e0" />
                  <XAxis
                      dataKey="nombre"
                      angle={-45}
                      textAnchor="end"
                      height={60}
                      fontSize={12}
                  />
                  <YAxis allowDecimals={false} fontSize={12} />
                  <Tooltip
                      contentStyle={{
                        backgroundColor: '#f8f9fa',
                        border: '1px solid #e9ecef',
                        borderRadius: '8px',
                        boxShadow: '0 2px 8px rgba(0,0,0,0.1)'
                      }}
                  />
                  <Bar
                      dataKey="cantidad"
                      fill="#3949ab"
                      radius={[4, 4, 0, 0]}
                  />
                </BarChart>
              </ResponsiveContainer>
            </div>
          </div>

          {/* Resumen rápido */}
          <div className="resumen-section">
            <h3 className="section-title">Resumen Rápido</h3>
            <div className="resumen-grid">
              <div className="resumen-item">
                <span className="resumen-label">Total de elementos:</span>
                <span className="resumen-value">{datos.camaras + datos.imagenes + datos.videos}</span>
              </div>
              <div className="resumen-item">
                <span className="resumen-label">Promedio por cámara:</span>
                <span className="resumen-value">
                  {datos.camaras > 0 ? Math.round((datos.imagenes + datos.videos) / datos.camaras) : 0}
                </span>
              </div>
              <div className="resumen-item">
                <span className="resumen-label">Última actualización:</span>
                <span className="resumen-value">{new Date().toLocaleString()}</span>
              </div>
            </div>
          </div>
        </div>
      </div>
  );
}

export default Reportes;