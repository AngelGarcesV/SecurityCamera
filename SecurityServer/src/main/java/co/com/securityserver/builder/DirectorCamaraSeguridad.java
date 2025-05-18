package co.com.securityserver.builder;

import co.com.securityserver.dto.CamaraDTO;
import co.com.securityserver.models.Camara;

public class DirectorCamaraSeguridad{
    private CamaraBuilder builder;

    public DirectorCamaraSeguridad(CamaraBuilder builder) {
        this.builder = builder;
    }
    public Camara ConstruirCamara(CamaraDTO infoCamara){
        builder.setDescripcion(infoCamara.getDescripcion());
        builder.setCoordenadax(infoCamara.getCoordenadax());
        builder.setCoordenaday(infoCamara.getCoordenaday());
        builder.setResolucion(infoCamara.getResolucion());
        builder.setUsuario(infoCamara.getUsuarioId());
        return builder.build();
    }
}
