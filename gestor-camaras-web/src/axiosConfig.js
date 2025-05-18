import axios from "axios";

// Crear una instancia de axios
const api = axios.create({
  baseURL: "http://localhost:9000/api", // Base común
});

// Agregar el token automáticamente si existe
api.interceptors.request.use((config) => {
  const token = localStorage.getItem("token");
  if (token) {
    config.headers.Authorization = `Bearer ${token}`;
  }
  return config;
});

export default api;
