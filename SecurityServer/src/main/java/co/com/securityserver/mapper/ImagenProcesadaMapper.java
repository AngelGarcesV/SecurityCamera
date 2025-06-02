package co.com.securityserver.mapper;

import co.com.securityserver.dto.ImagenProcesadaDTO;
import co.com.securityserver.models.Imagen;
import co.com.securityserver.models.ImagenProcesada;

import java.util.Base64;

public class ImagenProcesadaMapper {
    public static ImagenProcesada toImagenProcesada(ImagenProcesadaDTO dto, Imagen imagen) {
        if (dto == null) return null;
        ImagenProcesada entidad = new ImagenProcesada();
        entidad.setId(dto.getId());
        entidad.setNombre(dto.getNombre());
        entidad.setResolucion(dto.getResolucion());
        entidad.setImagenEditada(dto.getImagenEditada());
        entidad.setFecha(dto.getFecha());
        entidad.setImagen(imagen);

        return entidad;
    }

    public static ImagenProcesadaDTO toImagenProcesadaDTO(ImagenProcesada entidad) {
        if (entidad == null) return null;

        String base64 = null;
        if (entidad.getImagenEditada() != null && entidad.getImagenEditada().length > 0) {
            base64 = Base64.getEncoder().encodeToString(entidad.getImagenEditada());
        }

        return new ImagenProcesadaDTO(
                entidad.getId(),
                entidad.getNombre(),
                entidad.getResolucion(),
                entidad.getImagenEditada(),
                base64,
                entidad.getFecha(),
                entidad.getImagen().getId()
        );
    }
}