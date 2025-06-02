import { useState } from "react";
import api from "../axiosConfig";
import { useNavigate } from "react-router-dom";
import "@/styles/layout.css"
import "@/styles/login.css";

function Registro() {
    const [nombre, setNombre] = useState("");
    const [correo, setCorreo] = useState("");
    const [password, setPassword] = useState("");
    const [error, setError] = useState("");
    const [success, setSuccess] = useState("");
    const navigate = useNavigate();

    const handleSubmit = async (e) => {
        e.preventDefault();
        setError("");
        setSuccess("");

        try {
            await api.post("/usuario/save", {
                nombre,
                correo,
                password,
                rol: "user",
            });

            setSuccess("Cuenta creada correctamente. Ahora puedes iniciar sesión.");
            setTimeout(() => navigate("/"), 2000);
        } catch {
            setError("Error al registrar el usuario. Verifica los datos.");
        }
    };

    return (
        <div className="login-container">
            <div className="login-card">
                <h2 className="login-title">Registro de Usuario</h2>
                {error && <div style={{ color: "red" }}>{error}</div>}
                {success && <div style={{ color: "green" }}>{success}</div>}
                <form onSubmit={handleSubmit}>
                    <div className="form-group">
                        <label>Nombre</label>
                        <input value={nombre} onChange={(e) => setNombre(e.target.value)} required />
                    </div>
                    <div className="form-group">
                        <label>Correo</label>
                        <input type="email" value={correo} onChange={(e) => setCorreo(e.target.value)} required />
                    </div>
                    <div className="form-group">
                        <label>Contraseña</label>
                        <input type="password" value={password} onChange={(e) => setPassword(e.target.value)} required />
                    </div>
                    <button type="submit" className="login-button">Registrarse</button>
                </form>
            </div>
        </div>
    );
}

export default Registro;
