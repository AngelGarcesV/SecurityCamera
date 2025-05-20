package co.com.securityserver.service;

import co.com.securityserver.models.Usuario;
import co.com.securityserver.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.Objects;

@Service
public class UsuarioService {

    @Autowired
    private UsuarioRepository userRepo;

    public UsuarioService(UsuarioRepository userRepo) {
        this.userRepo = userRepo;
    }

    @Transactional
    public Usuario saveUsuario(Usuario usuario) {
        if (userRepo.existsByCorreo(usuario.getCorreo())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Ya existe un usuario con el correo: " + usuario.getCorreo());
        }else{
            usuario.setPassword(this.hashPassword(usuario.getPassword()));
            return userRepo.save(usuario);
        }
    }

    @Transactional(readOnly = true)
    public Usuario getUsuarioById(Long id) {
        return userRepo.findById(id).orElse(null);
    }

    @Transactional
    public boolean deleteUsuario(Long id) {
        if (userRepo.existsById(id)) {
            userRepo.deleteById(id);
            return true;
        }
        throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuario no encontrado");
    }

    @Transactional(readOnly = true)
    public List<Usuario> getAllUsuarios() {
        return (List<Usuario>) userRepo.findAll();
    }

    @Transactional
    public Usuario updateUsuario(Usuario infoUsuario) {
        Usuario userSearch = userRepo.findByCorreo(infoUsuario.getCorreo()).orElse(null);
        if (userRepo.existsById(infoUsuario.getId())  && (userSearch == null || (Objects.equals(userSearch.getId(), infoUsuario.getId())))){
            if (infoUsuario.getPassword() != null && !infoUsuario.getPassword().isEmpty()) {
                infoUsuario.setPassword(this.hashPassword(infoUsuario.getPassword()));
            } else {
                // Conservar la contraseña actual si no se envía una nueva
                Usuario actual = userRepo.findById(infoUsuario.getId()).orElseThrow(() ->
                        new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuario no encontrado"));
                infoUsuario.setPassword(actual.getPassword());
            }
            return userRepo.save(infoUsuario);
        }
        throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuario no encontrado, o el correo ya esta registrado");
    }

    @Transactional(readOnly = true)
    public Usuario getUsuarioByCorreo(String correo) {
        return userRepo.findByCorreo(correo).orElse(null);
    }


    public String hashPassword(String password) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] encodedHash = digest.digest(password.getBytes(StandardCharsets.UTF_8));
            StringBuilder hexString = new StringBuilder();
            for (byte b : encodedHash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) {
                    hexString.append('0');
                }
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Error hasheando el password", e);
        }
    }
}