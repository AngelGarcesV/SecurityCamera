import { BrowserRouter, Routes, Route } from "react-router-dom";
import Login from "./pages/Login";
import Reportes from "./pages/Reportes";
import Camaras from "./pages/Camaras";
import Mapa from "./pages/Mapa";
import Usuarios from "./pages/usuarios/Usuarios";
import CrearUsuario from "./pages/usuarios/CrearUsuario";
import EditarUsuario from "./pages/usuarios/EditarUsuario";
import EliminarUsuario from "./pages/usuarios/EliminarUsuario";
import VerUsuarios from "./pages/usuarios/VerUsuarios";
import Galeria from "./pages/Galeria"; // ✅ Agregado
import AppLayout from "./components/AppLayout";

function App() {
    return (
        <BrowserRouter>
            <Routes>
                {/* Ruta pública para login */}
                <Route path="/" element={<Login />} />
                {/* Rutas protegidas dentro del layout */}
                <Route path="/" element={<AppLayout />}>
                    <Route path="reportes" element={<Reportes />} />
                    <Route path="camaras" element={<Camaras />} />
                    <Route path="ubicacion" element={<Mapa />} />
                    <Route path="usuarios" element={<Usuarios />} />
                    <Route path="usuarios/crear" element={<CrearUsuario />} />
                    <Route path="usuarios/editar" element={<EditarUsuario />} />
                    <Route path="usuarios/eliminar" element={<EliminarUsuario />} />
                    <Route path="usuarios/ver" element={<VerUsuarios />} />
                    <Route path="camaras/:id/galeria" element={<Galeria />} /> {/* ✅ Nueva ruta */}
                </Route>
            </Routes>
        </BrowserRouter>
    );
}

export default App;



