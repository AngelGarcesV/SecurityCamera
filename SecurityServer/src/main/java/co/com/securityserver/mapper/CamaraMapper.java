package co.com.securityserver.mapper;


import co.com.securityserver.dto.CamaraDTO;
import co.com.securityserver.models.Camara;
import co.com.securityserver.models.Usuario;

public class CamaraMapper {
    private Long id;
    private Long usuarioId;
    private String descripcion;
    private Double coordenadax;
    private Double coordenaday;
    private String resolucion;

    public static CamaraDTO toCamaraDTO(Camara camara) {
        if (camara == null) {
            return null;
        }
        return new CamaraDTO(camara.getId(), camara.getDescripcion(), camara.getCoordenadax(), camara.getCoordenaday(), camara.getResolucion(), camara.getUsuario().getId());
    }

    public static Camara toCamara(CamaraDTO camaraDTO, Usuario usuario) {
        if (camaraDTO == null) {
            return null;
        }
        return new Camara(camaraDTO.getId(), usuario, camaraDTO.getDescripcion(), camaraDTO.getCoordenadax(), camaraDTO.getCoordenaday(), camaraDTO.getResolucion());
    }


}
