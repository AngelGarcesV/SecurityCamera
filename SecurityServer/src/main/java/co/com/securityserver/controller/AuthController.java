package co.com.securityserver.controller;

import co.com.securityserver.models.Usuario;
import co.com.securityserver.service.UsuarioService;
import co.com.securityserver.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

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
            String token = jwtUtil.generateToken(user.getNombre(),user.getCorreo(), user.getRol(),user.getId());
            return ResponseEntity.ok().body(token);
        }
        return ResponseEntity.status(401).body("Invalid credentials");
    }
}