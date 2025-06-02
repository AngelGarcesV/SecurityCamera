package co.com.securityserver.builder;

import co.com.securityserver.models.Camara;

public interface CamaraBuilder {
    CamaraBuilder setId(Long id);
    CamaraBuilder setDescripcion(String descripcion);
    CamaraBuilder setCoordenadax(Double coordenadax);
    CamaraBuilder setCoordenaday(Double coordenaday);
    CamaraBuilder setResolucion(String resolucion);
    CamaraBuilder setUsuario(Long usuarioId);
    CamaraBuilder setIp(String ip);
    CamaraBuilder setPuerto(Integer puerto);
    Camara build();
}
