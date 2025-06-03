import axios from "axios";

// Crear una instancia de axios
const api = axios.create({
    baseURL: "http://192.168.11.103:9000/api", // Base común
    headers: {
        'Content-Type': 'application/json'
    }
});

// Agregar el token automáticamente si existe
api.interceptors.request.use(
    (config) => {
        const token = localStorage.getItem("token");
        if (token) {
            console.log(`Agregando token a solicitud: ${config.url}`);
            config.headers.Authorization = `Bearer ${token}`;
        }
        return config;
    },
    (error) => {
        console.error("Error en interceptor de solicitud:", error);
        return Promise.reject(error);
    }
);

// Interceptor para manejar respuestas
api.interceptors.response.use(
    (response) => {
        // Para respuestas de login, verificar y guardar el rol
        if (response.config.url.includes('/auth/login')) {
            console.log("Interceptando respuesta de login:", response.data);

            if (response.data.rol) {
                console.log("Rol en respuesta:", response.data.rol);
            }
        }
        return response;
    },
    (error) => {
        console.error("Error en respuesta:", error.response?.status, error.config?.url);

        if (error.response && error.response.status === 401) {
            console.log("Error 401 detectado, cerrando sesión");
            localStorage.clear();

            if (window.location.pathname !== "/") {
                window.location.href = "/";
            }
        }

        return Promise.reject(error);
    }
);

export default api;