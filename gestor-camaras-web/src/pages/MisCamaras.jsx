import { useEffect, useState } from "react";
import api from "../axiosConfig";
import { useNavigate } from "react-router-dom";

function MisCamaras() {
  const [camaras, setCamaras] = useState([]);

  useEffect(() => {
    const token = localStorage.getItem("token");

    api.get("/camaras/mis-camaras")
      .then((res) => setCamaras(res.data))
      .catch(() => alert("Error al cargar tus cámaras"));
  }, []);

  return (
    <div className="p-10">
      <h2 className="text-2xl mb-4">Mis Cámaras</h2>
      <ul>
        {camaras.map((cam) => (
          <li key={cam.id}>
            <strong>{cam.nombre}</strong> – {cam.direccion}
          </li>
        ))}
      </ul>
    </div>
  );
}

export default MisCamaras;
