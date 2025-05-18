package co.com.securityserver.controller;

import co.com.securityserver.dto.UsuarioDTO;
import co.com.securityserver.mapper.UsuarioMapper;
import co.com.securityserver.models.Usuario;
import co.com.securityserver.service.UsuarioService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/usuario")
public class UsuarioController {
    @Autowired
    private UsuarioService usuarioService;

    @GetMapping("/all")
    public List<UsuarioDTO> getAllUsuarios() {
        return usuarioService.getAllUsuarios().stream()
                .map(UsuarioMapper::toUsuarioDTO)
                .collect(Collectors.toList());
    }
    @PostMapping("/save")
    public UsuarioDTO saveUsuario(@RequestBody Usuario infoUsuario) {
        return UsuarioMapper.toUsuarioDTO(
                usuarioService.saveUsuario(infoUsuario));
    }
    @PutMapping("/{id}")
    public UsuarioDTO updateUsuario(@PathVariable Long id ,@RequestBody Usuario infoUsuario) {
        UsuarioDTO user = UsuarioMapper.toUsuarioDTO(usuarioService.getUsuarioById(id));
        if (user != null) {
            return UsuarioMapper.toUsuarioDTO(usuarioService.updateUsuario(infoUsuario));
        }else{
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuario no encontrado");
        }
    }

    @DeleteMapping("/{id}")
    public boolean deleteUsuario(@PathVariable Long id) {
        if (usuarioService.deleteUsuario(id)) {
            return true;
        } else {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuario no encontrado");
        }
    }

    @GetMapping("/{id}")
    public UsuarioDTO getUsuarioById(@PathVariable Long id) {
        UsuarioDTO user = UsuarioMapper.toUsuarioDTO(usuarioService.getUsuarioById(id));
        if (user != null) {
            return user;
        } else {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuario no encontrado");
        }
    }
}
