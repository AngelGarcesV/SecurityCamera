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
    const [imagenesProcesadas, setImagenesProcesadas] = useState([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState(null);

    useEffect(() => {
        const fetchData = async () => {
            try {
                setLoading(true);
                const [resCam, resImg, resVid, resProc] = await Promise.all([
                    api.get(`/camara/${id}`),
                    api.get(`/imagenes/camara/${id}`),
                    api.get(`/video/camara/${id}`),
                    api.get(`/imagenesProcesadas/camara/${id}`)
                ]);

                setCamara(resCam.data);
                setImagenes(Array.isArray(resImg.data) ? resImg.data : []);
                setVideos(Array.isArray(resVid.data) ? resVid.data : []);
                setImagenesProcesadas(Array.isArray(resProc.data) ? resProc.data : []);
            } catch (err) {
                console.error("Error al cargar la galería:", err);
                setError("Error al cargar la galería de la cámara");
            } finally {
                setLoading(false);
            }
        };

        fetchData();
    }, [id]);

    const totalElementos = imagenes.length + videos.filter(v => v.video).length + imagenesProcesadas.length;

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
                                <span className="info-value">{camara.ip}</span>
                            </div>
                            <div className="info-item">
                                <span className="info-label">Puerto:</span>
                                <span className="info-value">{camara.puerto}</span>
                            </div>
                            <div className="info-item">
                                <span className="info-label">Resolución:</span>
                                <span className="info-value">{camara.resolucion}</span>
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

                {/* Sección de Imágenes */}
                <div className="media-section">
                    <div className="section-header">
                        <h3 className="section-title">📷 Imágenes Originales</h3>
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
                                    />
                                    <div className="media-overlay">
                                        <span className="media-info">Imagen #{img.id}</span>
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
                                        <video controls preload="metadata">
                                            <source
                                                src={`data:video/mp4;base64,${vid.video}`}
                                                type="video/mp4"
                                            />
                                            Tu navegador no soporta la reproducción de video.
                                        </video>
                                        <div className="media-overlay">
                                            <span className="media-info">Video #{vid.id}</span>
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

                {/* Sección de Imágenes Procesadas */}
                <div className="media-section">
                    <div className="section-header">
                        <h3 className="section-title">🔍 Imágenes Procesadas</h3>
                        <span className="section-count">{imagenesProcesadas.length} elementos</span>
                    </div>
                    {imagenesProcesadas.length > 0 ? (
                        <div className="media-grid">
                            {imagenesProcesadas.map((img, index) => (
                                <div key={img.id} className="media-item processed-item">
                                    <img
                                        src={`data:image/png;base64,${img.imagen}`}
                                        alt={`Procesada ${index + 1}`}
                                        loading="lazy"
                                    />
                                    <div className="media-overlay">
                                        <span className="media-info">Procesada #{img.id}</span>
                                    </div>
                                </div>
                            ))}
                        </div>
                    ) : (
                        <div className="empty-state">
                            <p>🔍 No hay imágenes procesadas disponibles para esta cámara.</p>
                        </div>
                    )}
                </div>

                {/* Resumen de la galería */}
                <div className="summary-section">
                    <h3 className="section-title">📊 Resumen de la Galería</h3>
                    <div className="summary-grid">
                        <div className="summary-item">
                            <span className="summary-number">{imagenes.length}</span>
                            <span className="summary-label">Imágenes Originales</span>
                        </div>
                        <div className="summary-item">
                            <span className="summary-number">{videos.filter(v => v.video).length}</span>
                            <span className="summary-label">Videos</span>
                        </div>
                        <div className="summary-item">
                            <span className="summary-number">{imagenesProcesadas.length}</span>
                            <span className="summary-label">Imágenes Procesadas</span>
                        </div>
                        <div className="summary-item total">
                            <span className="summary-number">{totalElementos}</span>
                            <span className="summary-label">Total de Elementos</span>
                        </div>
                    </div>
                </div>
            </div>
        </div>
    );
}

export default Galeria;