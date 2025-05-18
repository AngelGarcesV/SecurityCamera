import { BrowserRouter, Routes, Route, Navigate } from "react-router-dom";
import AppLayout from "./components/AppLayout";
import Camaras from "./pages/Camaras";
import Reportes from "./pages/Reportes";
import Mapa from "./pages/Mapa";
import Usuarios from "./pages/usuarios/Usuarios";
import Login from "./pages/Login";
import '@/styles/layout.css';

function App() {
  const isAuthenticated = !!localStorage.getItem("token");

  return (
    <BrowserRouter>
      <Routes>
        {/* Página de login, sin layout */}
        <Route path="/" element={<Login />} />

        {/* Rutas protegidas con layout */}
        {isAuthenticated ? (
          <Route element={<AppLayout />}>
            <Route path="/reportes" element={<Reportes />} />
            <Route path="/camaras" element={<Camaras />} />
            <Route path="/ubicacion" element={<Mapa />} />
            <Route path="/usuarios/*" element={<Usuarios />} />
          </Route>
        ) : (
          // Si no está autenticado, redirigir cualquier otra ruta a login
          <Route path="*" element={<Navigate to="/" />} />
        )}
      </Routes>
    </BrowserRouter>
  );
}

export default App;

