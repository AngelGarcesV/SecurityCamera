package co.com.securityserver.mapper;

import co.com.securityserver.dto.CamaraDTO;
import co.com.securityserver.models.Camara;
import co.com.securityserver.models.Usuario;

public class CamaraMapper {
    public static CamaraDTO toCamaraDTO(Camara camara) {
        if (camara == null) {
            return null;
        }
        return new CamaraDTO(
                camara.getId(),
                camara.getIp(),
                camara.getPuerto(),
                camara.getDescripcion(),
                camara.getCoordenadax(),
                camara.getCoordenaday(),
                camara.getResolucion(),
                camara.getUsuario().getId()
        );
    }

    public static Camara toCamara(CamaraDTO camaraDTO, Usuario usuario) {
        if (camaraDTO == null) {
            return null;
        }
        return new Camara(
                camaraDTO.getId(),
                camaraDTO.getIp(),
                camaraDTO.getPuerto(),
                usuario,
                camaraDTO.getDescripcion(),
                camaraDTO.getCoordenadax(),
                camaraDTO.getCoordenaday(),
                camaraDTO.getResolucion()
        );
    }
}