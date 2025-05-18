package co.com.securityserver.dto;

import lombok.Data;

@Data
public class UsuarioDTO {
    private Long id;
    private String nombre;
    private String correo;
    private String rol;

    public UsuarioDTO() {
    }

    public UsuarioDTO(Long id, String nombre, String correo, String rol) {
        this.id = id;
        this.nombre = nombre;
        this.correo = correo;
        this.rol = rol;
    }

}
