package co.com.securityserver.mapper;

import co.com.securityserver.dto.VideoDTO;
import co.com.securityserver.models.Camara;
import co.com.securityserver.models.Usuario;
import co.com.securityserver.models.Video;

public class VideoMapper {

    public static Video toVideo(VideoDTO videoDTO, Usuario usuario, Camara camara) {
        if (videoDTO == null) {
            return null;
        }
        return new Video(
                videoDTO.getNombre(),
                videoDTO.getFecha(),
                videoDTO.getVideo(),
                videoDTO.getDuracion(),
                camara,
                usuario
        );
    }


    public static VideoDTO toVideoDTO(Video video) {
        if (video == null) {
            return null;
        }
        return new VideoDTO(
                video.getId(),
                video.getNombre(),
                video.getFecha(),
                video.getVideo(),
                video.getDuracion(),
                video.getCamara().getId(),
                video.getUsuario().getId()
        );
    }
}
