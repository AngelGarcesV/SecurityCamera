import { useState } from "react";
import api from "../axiosConfig";
import { useNavigate } from "react-router-dom";
import "@/styles/layout.css";
import "@/styles/login.css";

function Login() {
  const [usuario, setUsuario] = useState("");
  const [password, setPassword] = useState("");
  const [recordarme, setRecordarme] = useState(false);
  const navigate = useNavigate();
  
  const handleLogin = async () => {
    try {
      const res = await api.post("/auth/login", {
        correo: usuario,
        password,
      });

      localStorage.setItem("token", res.data.token);
      localStorage.setItem("rol", res.data.rol);

      navigate("/reportes");
    } catch (error) {
      alert(error);
    }
  };

  return (
    <div className="login-container">
      <div className="login-card">
        {/* Logo */}
        <div className="logo-container">
          <div className="logo">
            <svg xmlns="http://www.w3.org/2000/svg" width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="white" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
              <path d="M12 22s8-4 8-10V5l-8-3-8 3v7c0 6 8 10 8 10z"></path>
            </svg>
          </div>
        </div>
        
        {/* Título */}
        <h2 className="login-title">Sistema de Gestión de Cámaras</h2>
        <p className="login-subtitle">Accede a tu panel de control</p>
        
        {/* Formulario */}
        <div>
          {/* Campo de usuario */}
          <div className="form-group">
            <label className="form-label">Usuario</label>
            <div className="input-container">
              <div className="input-icon">
                <svg xmlns="http://www.w3.org/2000/svg" width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
                  <path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2"></path>
                  <circle cx="12" cy="7" r="4"></circle>
                </svg>
              </div>
              <input
                type="text"
                placeholder="Ingrese su usuario"
                value={usuario}
                onChange={(e) => setUsuario(e.target.value)}
                className="login-input"
              />
            </div>
          </div>
          
          {/* Campo de contraseña */}
          <div className="form-group">
            <label className="form-label">Contraseña</label>
            <div className="input-container">
              <div className="input-icon">
                <svg xmlns="http://www.w3.org/2000/svg" width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
                  <rect x="3" y="11" width="18" height="11" rx="2" ry="2"></rect>
                  <path d="M7 11V7a5 5 0 0 1 10 0v4"></path>
                </svg>
              </div>
              <input
                type="password"
                placeholder="Ingrese su contraseña"
                value={password}
                onChange={(e) => setPassword(e.target.value)}
                className="login-input"
              />
            </div>
          </div>
          
          {/* Recordarme y Olvidó contraseña */}
          <div className="checkbox-row">
            <div className="checkbox-container">
              <input
                type="checkbox"
                id="recordarme"
                checked={recordarme}
                onChange={(e) => setRecordarme(e.target.checked)}
                className="checkbox"
              />
              <label htmlFor="recordarme" className="checkbox-label">
                Recordarme
              </label>
            </div>
            <a href="#" className="forgot-link">
              ¿Olvidó su contraseña?
            </a>
          </div>
          
          {/* Botón de inicio de sesión */}
          <button
            onClick={handleLogin}
            className="login-button"
          >
            Iniciar Sesión
          </button>
          
          {/* Enlace para registrarse */}
          <div className="register-container">
            <span className="register-text">¿No tiene una cuenta? </span>
            <a href="#" className="register-link">
              Registrarse
            </a>
          </div>
        </div>
      </div>
    </div>
  );
}

export default Login;