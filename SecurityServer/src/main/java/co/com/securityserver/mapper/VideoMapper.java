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
            videoBytes = base64ToBytes(videoDTO.getVideo());
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

    /**
     * Convierte un Video a VideoDTO sin incluir el contenido binario del video
     * Útil para listar videos sin sobrecargar la respuesta
     */
    public static VideoDTO toVideoDTOWithoutContent(Video video) {
        if (video == null) {
            return null;
        }

        return new VideoDTO(
                video.getId(),
                video.getNombre(),
                video.getFecha(),
                null, // No incluimos el contenido del video
                video.getDuracion(),
                video.getCamara().getId(),
                video.getUsuario().getId()
        );
    }

    /**
     * Convierte una cadena Base64 a un array de bytes
     */
    public static byte[] base64ToBytes(String base64) {
        try {
            return Base64.getDecoder().decode(base64);
        } catch (IllegalArgumentException e) {
            throw new RuntimeException("Error al decodificar el video Base64", e);
        }
    }
}