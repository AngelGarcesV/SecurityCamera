import { useState, useEffect } from "react";
import api from "../axiosConfig";
import { useNavigate } from "react-router-dom";
import "@/styles/layout.css";
import "@/styles/login.css";

// Función mejorada para decodificar JWT
function decodeJWT(token) {
  try {
    // Verificar si el token es válido
    if (!token || typeof token !== 'string' || token.trim() === '') {
      console.error("Token inválido:", token);
      return null;
    }

    // Obtener las partes del token
    const parts = token.split('.');
    if (parts.length !== 3) {
      console.error("Formato de token inválido. Debe tener 3 partes separadas por puntos.");
      return null;
    }

    // Decodificar la parte de payload
    const base64Url = parts[1];
    const base64 = base64Url.replace(/-/g, '+').replace(/_/g, '/');
    const jsonPayload = decodeURIComponent(
        atob(base64)
            .split('')
            .map(c => '%' + ('00' + c.charCodeAt(0).toString(16)).slice(-2))
            .join('')
    );

    return JSON.parse(jsonPayload);
  } catch (e) {
    console.error("Error decodificando JWT:", e);
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

  // Verificar si ya está autenticado al cargar el componente
  useEffect(() => {
    const token = localStorage.getItem("token");
    if (token) {
      console.log("Login - Ya hay un token, redirigiendo a /reportes");
      navigate("/reportes", { replace: true });
    }
  }, [navigate]);

  const handleLogin = async () => {
    // Evitar múltiples intentos simultáneos
    if (isLoggingIn) return;
    setError("");

    try {
      setIsLoggingIn(true);
      console.log("Intentando iniciar sesión con:", { correo: usuario });

      const res = await api.post("/auth/login", {
        correo: usuario,
        password,
      });

      // Verificar la respuesta
      console.log("Respuesta completa del login:", res.data);

      // Manejar tanto si la respuesta es un objeto con token y rol o si es un string de token directo
      let token, rol, nombre;

      if (typeof res.data === 'object' && res.data !== null) {
        // Si es un objeto (respuesta formateada correctamente del backend)
        token = res.data.token;
        rol = res.data.rol;
        nombre = res.data.nombre;
        console.log("Formato de respuesta: objeto con propiedades");
      } else if (typeof res.data === 'string') {
        // Si es un string (token directo)
        token = res.data;
        console.log("Formato de respuesta: string de token directo");

        // Intentar obtener el rol del token
        const decodedToken = decodeJWT(token);
        if (decodedToken && decodedToken.rol) {
          rol = decodedToken.rol;
          console.log("Rol extraído del token JWT:", rol);
        }
      } else {
        throw new Error("Formato de respuesta no reconocido");
      }

      // Verificar si tenemos un token válido
      if (!token) {
        throw new Error("No se recibió un token válido");
      }

      console.log("Token recibido:", token ? "Existe" : "No existe");
      console.log("Rol:", rol);

      // Asegurar que tengamos un rol
      if (!rol) {
        rol = "user"; // Valor por defecto
        console.log("Usando rol por defecto:", rol);
      }

      // Limpiar localStorage antes de guardar nuevos datos
      localStorage.clear();

      // Guardar datos en localStorage
      localStorage.setItem("token", token);
      localStorage.setItem("rol", rol);
      localStorage.setItem("usuario", usuario);
      if (nombre) {
        localStorage.setItem("nombre", nombre);
      }

      // Verificar lo que se guardó
      console.log("Verificando localStorage después de guardar:");
      console.log("Token:", localStorage.getItem("token") ? "Existe" : "No existe");
      console.log("Rol guardado:", localStorage.getItem("rol"));

      // Disparar evento personalizado para notificar el login
      window.dispatchEvent(new Event('storage'));

      // Redirigir usando navigate con replace
      navigate("/reportes", { replace: true });

    } catch (error) {
      console.error("Error en login:", error);

      if (error.response?.data) {
        if (typeof error.response.data === 'string') {
          setError(error.response.data);
        } else if (error.response.data.mensaje) {
          setError(error.response.data.mensaje);
        } else {
          setError("Error de autenticación. Verifique sus credenciales.");
        }
      } else {
        setError(error.message || "No se pudo conectar con el servidor");
      }
    } finally {
      setIsLoggingIn(false);
    }
  };

  // Handler para enviar el formulario con Enter
  const handleKeyDown = (e) => {
    if (e.key === 'Enter') {
      handleLogin();
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

          {/* Mensaje de error */}
          {error && (
              <div className="error-message" style={{ color: 'red', marginBottom: '15px', textAlign: 'center' }}>
                {error}
              </div>
          )}

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
                    onKeyDown={handleKeyDown}
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
                    onKeyDown={handleKeyDown}
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
                disabled={isLoggingIn}
            >
              {isLoggingIn ? "Iniciando sesión..." : "Iniciar Sesión"}
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