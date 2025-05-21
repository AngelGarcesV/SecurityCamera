package co.com.securityserver.mapper;

import co.com.securityserver.dto.VideoDTO;
import co.com.securityserver.models.Camara;
import co.com.securityserver.models.Usuario;
import co.com.securityserver.models.Video;

import java.util.Base64;

public class VideoMapper {

    public static Video toVideo(VideoDTO videoDTO, Usuario usuario, Camara camara) {
        if (videoDTO == null) {
            return null;
        }

        byte[] videoBytes = null;
        if (videoDTO.getVideo() != null && !videoDTO.getVideo().isEmpty()) {
            try {
                videoBytes = Base64.getDecoder().decode(videoDTO.getVideo());
            } catch (IllegalArgumentException e) {
                throw new RuntimeException("Error al decodificar el video Base64", e);
            }
        }

        return new Video(
                videoDTO.getNombre(),
                videoDTO.getFecha(),
                videoBytes,
                videoDTO.getDuracion(),
                camara,
                usuario
        );
    }

    public static VideoDTO toVideoDTO(Video video) {
        if (video == null) {
            return null;
        }

        String videoBase64 = null;
        if (video.getVideo() != null && video.getVideo().length > 0) {
            videoBase64 = Base64.getEncoder().encodeToString(video.getVideo());
        }

        return new VideoDTO(
                video.getId(),
                video.getNombre(),
                video.getFecha(),
                videoBase64,
                video.getDuracion(),
                video.getCamara().getId(),
                video.getUsuario().getId()
        );
    }
}
