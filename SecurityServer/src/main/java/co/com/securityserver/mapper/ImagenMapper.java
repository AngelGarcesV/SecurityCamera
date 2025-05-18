package co.com.securityserver.mapper;

import co.com.securityserver.dto.ImagenDTO;
import co.com.securityserver.models.Imagen;
import co.com.securityserver.models.Usuario;
import co.com.securityserver.models.Camara;

public class ImagenMapper {
    public static ImagenDTO toImagenDTO(Imagen imagen) {
        if (imagen == null) {
            return null;
        }
        return new ImagenDTO(imagen.getId(), imagen.getNombre(), imagen.getImagen(), imagen.getResolucion(), imagen.getFecha(), imagen.getCamara().getId(), imagen.getUsuario().getId());
    }

    public static Imagen toImagen(ImagenDTO imagenDTO, Camara camara, Usuario usuario) {
        if (imagenDTO == null) {
            return null;
        }
        return new Imagen(
                imagenDTO.getId(),
                imagenDTO.getNombre(),
                imagenDTO.getImagen(),
                imagenDTO.getResolucion(),
                imagenDTO.getFecha(),
                camara,
                usuario
        );
    }
}
