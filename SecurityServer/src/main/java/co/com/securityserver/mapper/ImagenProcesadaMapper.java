package co.com.securityserver.mapper;

import co.com.securityserver.dto.ImagenProcesadaDTO;
import co.com.securityserver.models.Camara;
import co.com.securityserver.models.Imagen;
import co.com.securityserver.models.ImagenProcesada;
import co.com.securityserver.models.Usuario;

public class ImagenProcesadaMapper {
    public static ImagenProcesada toImagenProcesada(ImagenProcesadaDTO imagenProcesadaDTO, Imagen image) {
        if (imagenProcesadaDTO == null) {
            return null;
        }
        return new ImagenProcesada();
    }

    public static ImagenProcesadaDTO toImagenProcesadaDTO(ImagenProcesada imagenProcesada) {
        if (imagenProcesada == null) {
            return null;
        }
        return new ImagenProcesadaDTO(
                imagenProcesada.getId(),
                imagenProcesada.getNombre(),
                imagenProcesada.getResolucion(),
                imagenProcesada.getImagenEditada(),
                imagenProcesada.getFecha(),
                imagenProcesada.getImagen().getId()
        );
    }
}
