import { useParams } from "react-router-dom";
import { useEffect, useState } from "react";
import "@/styles/galeria.css";

function Galeria() {
    const { id } = useParams();
    const [camara, setCamara] = useState(null);
    const [imagenes, setImagenes] = useState([]);
    const [videos, setVideos] = useState([]);
    const [imagenesProcesadas, setImagenesProcesadas] = useState([]);

    useEffect(() => {
// 🔧 Datos de prueba simulados
        const camaraDummy = {
            id,
            nombre: "Cámara Principal de Entrada",
        };
        const imagenesDummy = [
            { id: 1, url: "https://via.placeholder.com/300x200?text=Imagen+1" },
            { id: 2, url: "https://via.placeholder.com/300x200?text=Imagen+2" },
        ];

        const videosDummy = [
            { id: 1, url: "https://www.w3schools.com/html/mov_bbb.mp4" },
            { id: 2, url: "https://www.w3schools.com/html/movie.mp4" },
        ];

        const imagenesProcesadasDummy = [
            { id: 1, url: "https://via.placeholder.com/300x200?text=Procesada+1" },
            { id: 2, url: "https://via.placeholder.com/300x200?text=Procesada+2" },
        ];

        setTimeout(() => {
            setCamara(camaraDummy);
            setImagenes(imagenesDummy);
            setVideos(videosDummy);
            setImagenesProcesadas(imagenesProcesadasDummy);
        }, 300);
    }, [id]);

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
                            <img key={img.id} src={img.url} alt={`Imagen ${img.id}`} />
                        ))}
                    </div>
                ) : (
                    <p>No hay imágenes disponibles.</p>
                )}
            </div>

            <div className="galeria-seccion">
                <h3>Videos</h3>
                {videos.length > 0 ? (
                    <div className="galeria-grid">
                        {videos.map((vid) => (
                            <video key={vid.id} controls src={vid.url}></video>
                        ))}
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
                            <img key={img.id} src={img.url} alt={`Procesada ${img.id}`} />
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



