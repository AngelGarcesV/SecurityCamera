import { useState, useEffect } from "react";
import api from "../axiosConfig";
import { useNavigate } from "react-router-dom";
import "@/styles/layout.css";
import "@/styles/login.css";

function decodeJWT(token) {
  try {
    if (!token || typeof token !== 'string') return null;
    const parts = token.split('.');
    if (parts.length !== 3) return null;
    const base64 = parts[1].replace(/-/g, '+').replace(/_/g, '/');
    const jsonPayload = decodeURIComponent(atob(base64).split('').map(c => '%' + ('00' + c.charCodeAt(0).toString(16)).slice(-2)).join(''));
    return JSON.parse(jsonPayload);
  } catch {
    return null;
  }
}

function Login() {
  const [usuario, setUsuario] = useState("");
  const [password, setPassword] = useState("");
  const [recordarme, setRecordarme] = useState(false);
  const [isLoggingIn, setIsLoggingIn] = useState(false);
  const [error, setError] = useState("");
  const navigate = useNavigate();

  useEffect(() => {
    // Cargar si había recordado sesión
    const savedUser = localStorage.getItem("rememberedUser");
    if (savedUser) {
      setUsuario(savedUser);
      setRecordarme(true);
    }

    // Limpiar la sesión anterior (pero no borrar rememberedUser)
    localStorage.removeItem("token");
    localStorage.removeItem("rol");
    localStorage.removeItem("usuario");
    localStorage.removeItem("nombre");
  }, []);

  const handleLogin = async () => {
    if (isLoggingIn) return;
    setError("");
    try {
      setIsLoggingIn(true);
      const res = await api.post("/auth/login", { correo: usuario, password });

      let token, rol, nombre;

      if (typeof res.data === 'object') {
        token = res.data.token;
        rol = res.data.rol;
        nombre = res.data.nombre;
      } else {
        token = res.data;
        const decoded = decodeJWT(token);
        rol = decoded?.rol;
      }

      if (!token) throw new Error("Token no recibido");
      if (!rol) rol = "user";

      localStorage.clear();
      localStorage.setItem("token", token);
      localStorage.setItem("rol", rol);
      localStorage.setItem("usuario", usuario);
      if (nombre) localStorage.setItem("nombre", nombre);

      if (recordarme) {
        localStorage.setItem("rememberedUser", usuario);
      } else {
        localStorage.removeItem("rememberedUser");
      }

      navigate("/reportes", { replace: true });
    } catch (error) {
      const msg = error?.response?.data?.mensaje || error?.message || "Error de autenticación";
      setError(msg);
    } finally {
      setIsLoggingIn(false);
    }
  };

  return (
      <div className="login-container">
        <div className="login-card">
          <h2 className="login-title">Sistema de Gestión de Cámaras</h2>
          <p className="login-subtitle">Accede a tu panel de control</p>
          {error && <div className="error-message" style={{ color: "red" }}>{error}</div>}
          <div>
            <div className="form-group">
              <label className="form-label">Usuario</label>
              <input type="text" placeholder="Ingrese su usuario" value={usuario} onChange={(e) => setUsuario(e.target.value)} className="login-input" />
            </div>
            <div className="form-group">
              <label className="form-label">Contraseña</label>
              <input type="password" placeholder="Ingrese su contraseña" value={password} onChange={(e) => setPassword(e.target.value)} className="login-input" />
            </div>

            <div className="checkbox-row">
              <label>
                <input
                    type="checkbox"
                    checked={recordarme}
                    onChange={(e) => setRecordarme(e.target.checked)}
                />
                Recordarme
              </label>
            </div>

            <button onClick={handleLogin} className="login-button" disabled={isLoggingIn}>
              {isLoggingIn ? "Iniciando sesión..." : "Iniciar Sesión"}
            </button>

            <div className="register-container">
              ¿No tiene una cuenta? <a href="/registro" className="register-link">Registrarse</a>
            </div>
          </div>
        </div>
      </div>
  );
}

export default Login;
