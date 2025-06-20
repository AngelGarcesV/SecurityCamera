package co.com.securityserver.builder;

import co.com.securityserver.models.Camara;
import co.com.securityserver.models.Usuario;
import co.com.securityserver.service.UsuarioService;

public class CamaraSeguridadBuilder implements CamaraBuilder {
    private Long id;
    private String ip;
    private Integer puerto;
    private String descripcion;
    private Double coordenadax;
    private Double coordenaday;
    private String resolucion;
    private Usuario usuario;
    private final UsuarioService usuarioService;

    public CamaraSeguridadBuilder(UsuarioService usuarioService) {
        this.usuarioService = usuarioService;
    }

    @Override
    public CamaraSeguridadBuilder setId(Long id) {
        this.id = id;
        return this;
    }

    @Override
    public CamaraSeguridadBuilder setIp(String ip) {
        this.ip = ip;
        return this;
    }

    @Override
    public CamaraSeguridadBuilder setPuerto(Integer puerto) {
        this.puerto = puerto;
        return this;
    }

    @Override
    public CamaraSeguridadBuilder setDescripcion(String descripcion) {
        this.descripcion = descripcion;
        return this;
    }

    @Override
    public CamaraSeguridadBuilder setCoordenadax(Double coordenadax) {
        this.coordenadax = coordenadax;
        return this;
    }

    @Override
    public CamaraSeguridadBuilder setCoordenaday(Double coordenaday) {
        this.coordenaday = coordenaday;
        return this;
    }

    @Override
    public CamaraSeguridadBuilder setResolucion(String resolucion) {
        this.resolucion = resolucion;
        return this;
    }

    @Override
    public CamaraSeguridadBuilder setUsuario(Long usuarioId) {
        this.usuario = usuarioService.getUsuarioById(usuarioId);
        return this;
    }

    @Override
    public Camara build() {
        if (usuario == null) {
            throw new IllegalStateException("Usuario no puede ser nulo");
        }
        return new Camara(id, ip, puerto, usuario, descripcion, coordenadax, coordenaday, resolucion);
    }
}