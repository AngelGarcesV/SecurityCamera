package co.com.securityserver.builder;

import co.com.securityserver.models.Camara;

public interface CamaraBuilder {
    CamaraBuilder setDescripcion(String descripcion);
    CamaraBuilder setCoordenadax(Double coordenadax);
    CamaraBuilder setCoordenaday(Double coordenaday);
    CamaraBuilder setResolucion(String resolucion);
    CamaraBuilder setUsuario(Long usuarioId);
    Camara build();
}
