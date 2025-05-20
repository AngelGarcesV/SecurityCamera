package co.com.securityserver.controller;

import co.com.securityserver.models.Usuario;
import co.com.securityserver.service.UsuarioService;
import co.com.securityserver.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private UsuarioService usuarioService;

    @Autowired
    private JwtUtil jwtUtil;


    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Usuario loginRequest) {
        Usuario user = usuarioService.getUsuarioByCorreo(loginRequest.getCorreo());
        if (user != null && user.getPassword().equals(usuarioService.hashPassword(loginRequest.getPassword()))) {
            String token = jwtUtil.generateToken(user.getNombre(), user.getCorreo(), user.getRol(), user.getId());

            // Crear un mapa con los datos de respuesta
            Map<String, Object> response = new HashMap<>();
            response.put("token", token);
            response.put("rol", user.getRol());
            response.put("nombre", user.getNombre());
            response.put("correo", user.getCorreo());

            return ResponseEntity.ok(response);
        }
        return ResponseEntity.status(401).body("Invalid credentials");
    }
}