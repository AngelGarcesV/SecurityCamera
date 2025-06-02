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
    useEffect(() => {
        const fetchData = async () => {
            try {
                const resCam = await api.get(`/camara/${id}`);
                const resImg = await api.get(`/imagenes/camara/${id}`);
                const resVid = await api.get(`/video/camara/${id}`);
                const resProc = await api.get(`/imagenesProcesadas/camara/${id}`);

                setCamara(resCam.data);
                setImagenes(resImg.data);
                setVideos(resVid.data);
                setImagenesProcesadas(resProc.data);
            } catch (error) {
                console.error("Error al cargar la galería:", error);
                navigate("/camaras");
            }
        };

        void fetchData();
    }, [id, navigate]);

    if (!camara) return <div className="loading">Cargando galería...</div>;

    return (
        <div className="main-content">
            <h2 className="page-title">Galería - {camara.nombre}</h2>
            <p className="page-subtitle">Multimedia registrada de la cámara</p>

            <div className="galeria-seccion">
                <h3>Imágenes</h3>
                {imagenes.length > 0 ? (
                    <div className="galeria-grid">
                        {imagenes.map((img) => (
                            <img
                                key={img.id}
                                src={`data:image/jpeg;base64,${img.imagen}`}
                                alt={`Imagen ${img.id}`}
                            />
                        ))}
                    </div>
                ) : (
                    <p>No hay imágenes disponibles.</p>
                )}
            </div>

            <div className="galeria-seccion">
                <h3>Videos</h3>
                {videos.length > 0 && videos.some(v => v.video) ? (
                    <div className="galeria-grid">
                        {videos.map((vid) =>
                            vid.video ? (
                                <video key={vid.id} controls>
                                    <source
                                        src={`data:video/mp4;base64,${vid.video}`}
                                        type="video/mp4"
                                    />
                                    Tu navegador no soporta el video.
                                </video>
                            ) : null
                        )}
                    </div>
                ) : (
                    <p>No hay videos disponibles.</p>
                )}
            </div>

            <div className="galeria-seccion">
                <h3>Imágenes Procesadas</h3>
                {imagenesProcesadas.length > 0 ? (
                    <div className="galeria-grid">
                        {imagenesProcesadas.map((img) => (
                            <img
                                key={img.id}
                                src={`data:image/png;base64,${img.imagen}`}
                                alt={`Procesada ${img.id}`}
                            />
                        ))}
                    </div>
                ) : (
                    <p>No hay imágenes procesadas disponibles.</p>
                )}
            </div>
        </div>
    );
}

export default Galeria;