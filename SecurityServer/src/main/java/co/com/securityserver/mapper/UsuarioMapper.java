package co.com.securityserver.mapper;

import co.com.securityserver.dto.UsuarioDTO;
import co.com.securityserver.models.Usuario;

public class UsuarioMapper {

    public static UsuarioDTO toUsuarioDTO(Usuario usuario) {
        if (usuario == null) {
            return null;
        }
        return new UsuarioDTO(usuario.getId(), usuario.getNombre(), usuario.getCorreo(), usuario.getRol());
    }

    public static Usuario toUsuario(UsuarioDTO usuarioDTO) {
        if (usuarioDTO == null) {
            return null;
        }
        return new Usuario(
                usuarioDTO.getId(),
                usuarioDTO.getNombre(),
                usuarioDTO.getCorreo(),
                null,
                usuarioDTO.getRol()
        );
    }
}
