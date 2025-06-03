import { useParams, useNavigate } from "react-router-dom";
import { useEffect, useState } from "react";
import api from "../axiosConfig";
import "@/styles/galeria.css";

function Galeria() {
    const { id } = useParams();
    const navigate = useNavigate();
    const [camara, setCamara] = useState(null);
    const [imagenes, setImagenes] = useState([]);
    const [videos, setVideos] = useState([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState(null);

    useEffect(() => {
        const fetchData = async () => {
            try {
                setLoading(true);

                // ✅ Solo 3 endpoints necesarios
                const [resCam, resImg, resVid] = await Promise.all([
                    api.get(`/camara/${id}`),
                    api.get(`/imagenes/camara/${id}`),    // Todas las imágenes (originales + procesadas)
                    api.get(`/video/camara/${id}`)
                ]);

                setCamara(resCam.data);
                setImagenes(Array.isArray(resImg.data) ? resImg.data : []);
                setVideos(Array.isArray(resVid.data) ? resVid.data : []);

            } catch (err) {
                console.error("Error al cargar la galería:", err);
                setError("Error al cargar la galería de la cámara");
            } finally {
                setLoading(false);
            }
        };

        fetchData();
    }, [id]);

    const totalElementos = imagenes.length + videos.filter(v => v.video).length;

    if (loading) {
        return (
            <div className="galeria-container">
                <div className="galeria-header">
                    <div className="page-header">
                        <h2 className="page-title">Cargando Galería...</h2>
                        <button
                            className="button-back"
                            onClick={() => navigate("/camaras")}
                        >
                            ← Volver a Cámaras
                        </button>
                    </div>
                </div>
                <div className="loading-state">
                    <h3>Obteniendo multimedia...</h3>
                    <p>Por favor espera mientras cargamos el contenido.</p>
                </div>
            </div>
        );
    }

    if (error) {
        return (
            <div className="galeria-container">
                <div className="galeria-header">
                    <div className="page-header">
                        <h2 className="page-title">Error</h2>
                        <button
                            className="button-back"
                            onClick={() => navigate("/camaras")}
                        >
                            ← Volver a Cámaras
                        </button>
                    </div>
                </div>
                <div className="error-state">
                    <h3>{error}</h3>
                    <button
                        className="button-primary"
                        onClick={() => navigate("/camaras")}
                    >
                        Volver a Cámaras
                    </button>
                </div>
            </div>
        );
    }

    return (
        <div className="galeria-container">
            {/* Header fijo */}
            <div className="galeria-header">
                <div className="page-header">
                    <div>
                        <h2 className="page-title">Galería - {camara?.descripcion || camara?.nombre || `Cámara ${id}`}</h2>
                        <p className="page-subtitle">
                            Multimedia registrada • {totalElementos} elementos totales
                        </p>
                    </div>
                    <button
                        className="button-back"
                        onClick={() => navigate("/camaras")}
                    >
                        ← Volver a Cámaras
                    </button>
                </div>
            </div>

            {/* Contenido principal con scroll */}
            <div className="galeria-content">
                {/* Información de la cámara */}
                {camara && (
                    <div className="camara-info">
                        <div className="info-grid">
                            <div className="info-item">
                                <span className="info-label">IP:</span>
                                <span className="info-value">{camara.ip || 'N/A'}</span>
                            </div>
                            <div className="info-item">
                                <span className="info-label">Puerto:</span>
                                <span className="info-value">{camara.puerto || 'N/A'}</span>
                            </div>
                            <div className="info-item">
                                <span className="info-label">Resolución:</span>
                                <span className="info-value">{camara.resolucion || 'N/A'}</span>
                            </div>
                            <div className="info-item">
                                <span className="info-label">Ubicación:</span>
                                <span className="info-value">
                                    {camara.coordenadax && camara.coordenaday
                                        ? `${camara.coordenadax.toFixed(4)}, ${camara.coordenaday.toFixed(4)}`
                                        : 'No disponible'
                                    }
                                </span>
                            </div>
                        </div>
                    </div>
                )}

                {/* Sección de Imágenes (Originales + Procesadas) */}
                <div className="media-section">
                    <div className="section-header">
                        <h3 className="section-title">📷 Galería de Imágenes</h3>
                        <span className="section-count">{imagenes.length} elementos</span>
                    </div>
                    {imagenes.length > 0 ? (
                        <div className="media-grid">
                            {imagenes.map((img, index) => (
                                <div key={img.id} className="media-item image-item">
                                    <img
                                        src={`data:image/jpeg;base64,${img.imagen}`}
                                        alt={`Imagen ${index + 1}`}
                                        loading="lazy"
                                        onError={(e) => {
                                            e.target.src = 'data:image/svg+xml;base64,PHN2ZyB3aWR0aD0iMjAwIiBoZWlnaHQ9IjIwMCIgeG1sbnM9Imh0dHA6Ly93d3cudzMub3JnLzIwMDAvc3ZnIj48cmVjdCB3aWR0aD0iMTAwJSIgaGVpZ2h0PSIxMDAlIiBmaWxsPSIjZGRkIi8+PHRleHQgeD0iNTAlIiB5PSI1MCUiIGZvbnQtZmFtaWx5PSJBcmlhbCIgZm9udC1zaXplPSIxNCIgZmlsbD0iIzk5OSIgdGV4dC1hbmNob3I9Im1pZGRsZSIgZHk9Ii4zZW0iPkltYWdlbiBubyBkaXNwb25pYmxlPC90ZXh0Pjwvc3ZnPg==';
                                        }}
                                    />
                                    <div className="media-overlay">
                                        <span className="media-info">
                                            {img.nombre || `Imagen #${img.id}`}
                                        </span>
                                        {img.fecha && (
                                            <span className="media-date">
                                                {new Date(img.fecha).toLocaleDateString()}
                                            </span>
                                        )}
                                        {img.resolucion && (
                                            <span className="media-resolution">
                                                {img.resolucion}
                                            </span>
                                        )}
                                    </div>
                                </div>
                            ))}
                        </div>
                    ) : (
                        <div className="empty-state">
                            <p>📷 No hay imágenes disponibles para esta cámara.</p>
                        </div>
                    )}
                </div>

                {/* Sección de Videos */}
                <div className="media-section">
                    <div className="section-header">
                        <h3 className="section-title">🎥 Videos</h3>
                        <span className="section-count">
                            {videos.filter(v => v.video).length} elementos
                        </span>
                    </div>
                    {videos.length > 0 && videos.some(v => v.video) ? (
                        <div className="media-grid">
                            {videos.map((vid) =>
                                vid.video ? (
                                    <div key={vid.id} className="media-item video-item">
                                        <video
                                            controls
                                            preload="metadata"
                                            style={{ maxWidth: '100%', height: 'auto' }}
                                        >
                                            <source
                                                src={`data:video/mp4;base64,${vid.video}`}
                                                type="video/mp4"
                                            />
                                            Tu navegador no soporta la reproducción de video.
                                        </video>
                                        <div className="media-overlay">
                                            <span className="media-info">
                                                {vid.nombre || `Video #${vid.id}`}
                                            </span>
                                            {vid.duracion && (
                                                <span className="media-duration">
                                                    ⏱️ {vid.duracion}
                                                </span>
                                            )}
                                            {vid.fecha && (
                                                <span className="media-date">
                                                    📅 {new Date(vid.fecha).toLocaleDateString()}
                                                </span>
                                            )}
                                        </div>
                                    </div>
                                ) : null
                            )}
                        </div>
                    ) : (
                        <div className="empty-state">
                            <p>🎥 No hay videos disponibles para esta cámara.</p>
                        </div>
                    )}
                </div>

                {/* Resumen de la galería */}
                <div className="summary-section">
                    <h3 className="section-title">📊 Resumen de la Galería</h3>
                    <div className="summary-grid">
                        <div className="summary-item">
                            <span className="summary-number">{imagenes.length}</span>
                            <span className="summary-label">Imágenes Totales</span>
                        </div>
                        <div className="summary-item">
                            <span className="summary-number">{videos.filter(v => v.video).length}</span>
                            <span className="summary-label">Videos</span>
                        </div>
                        <div className="summary-item total">
                            <span className="summary-number">{totalElementos}</span>
                            <span className="summary-label">Total de Elementos</span>
                        </div>
                        <div className="summary-item">
                            <span className="summary-number">
                                {camara?.coordenadax && camara?.coordenaday ? '📍' : '❌'}
                            </span>
                            <span className="summary-label">Ubicación GPS</span>
                        </div>
                    </div>
                </div>

                {/* Información adicional */}
                {imagenes.length === 0 && videos.length === 0 && (
                    <div className="no-content-message">
                        <div className="no-content-icon">📷</div>
                        <h3>No hay contenido multimedia</h3>
                        <p>Esta cámara aún no tiene imágenes ni videos registrados.</p>
                        <p>El contenido aparecerá aquí cuando esté disponible.</p>
                    </div>
                )}
            </div>
        </div>
    );
}

export default Galeria;