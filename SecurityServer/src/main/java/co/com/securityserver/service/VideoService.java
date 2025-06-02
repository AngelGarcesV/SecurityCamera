package co.com.securityserver.service;

import co.com.securityserver.dto.VideoDTO;
import co.com.securityserver.mapper.VideoMapper;
import co.com.securityserver.models.Camara;
import co.com.securityserver.models.Usuario;
import co.com.securityserver.repository.CamaraRepository;
import co.com.securityserver.repository.UsuarioRepository;
import co.com.securityserver.repository.VideoRepository;
import co.com.securityserver.models.Video;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.List;

@Service
public class VideoService {
    @Autowired
    private VideoRepository videoRepository;
    @Autowired
    private CamaraRepository camaraRepository;
    @Autowired
    private UsuarioRepository usuarioRepository;

    public VideoService(VideoRepository videoRepository, CamaraRepository camaraRepository, UsuarioRepository usuarioRepository) {
        this.videoRepository = videoRepository;
        this.camaraRepository = camaraRepository;
        this.usuarioRepository = usuarioRepository;
    }

    @Transactional
    public Video saveVideo(VideoDTO dto) {
        Usuario infoUser = usuarioRepository.findById(dto.getUsuarioId()).orElse(null);
        Camara camaraInfo = camaraRepository.findById(dto.getCamaraId()).orElse(null);
        if(infoUser == null || camaraInfo == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "No se encontró el usuario o la cámara");
        }
        Video video = VideoMapper.toVideo(dto, infoUser, camaraInfo);
        return videoRepository.save(video);
    }

    @Transactional
    public Video saveVideoFile(VideoDTO dto, MultipartFile videoFile) throws IOException {
        Usuario infoUser = usuarioRepository.findById(dto.getUsuarioId()).orElse(null);
        Camara camaraInfo = camaraRepository.findById(dto.getCamaraId()).orElse(null);
        if(infoUser == null || camaraInfo == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "No se encontró el usuario o la cámara");
        }

        // Crear el objeto Video con los metadatos
        Video video = new Video();
        video.setNombre(dto.getNombre());
        video.setFecha(dto.getFecha());
        video.setDuracion(dto.getDuracion());
        video.setCamara(camaraInfo);
        video.setUsuario(infoUser);

        // Establecer el contenido del archivo
        video.setVideo(videoFile.getBytes());

        return videoRepository.save(video);
    }

    @Transactional
    public Video saveVideoFromFile(VideoDTO dto, File videoFile) throws IOException {
        Usuario infoUser = usuarioRepository.findById(dto.getUsuarioId()).orElse(null);
        Camara camaraInfo = camaraRepository.findById(dto.getCamaraId()).orElse(null);

        if(infoUser == null || camaraInfo == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "No se encontró el usuario o la cámara");
        }

        // Crear el objeto Video con los metadatos
        Video video = new Video();
        video.setNombre(dto.getNombre());
        video.setFecha(dto.getFecha());
        video.setDuracion(dto.getDuracion());
        video.setCamara(camaraInfo);
        video.setUsuario(infoUser);

        // Leer el archivo y establecer el contenido
        byte[] fileBytes = Files.readAllBytes(videoFile.toPath());
        video.setVideo(fileBytes);

        return videoRepository.save(video);
    }

    @Transactional
    public Video updateVideo(VideoDTO dto) {
        // Comprobar que el video existe
        Video existingVideo = videoRepository.findById(dto.getId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "No se encontró el video con id: " + dto.getId()));

        Usuario infoUser = usuarioRepository.findById(dto.getUsuarioId()).orElse(null);
        Camara infoCamara = camaraRepository.findById(dto.getCamaraId()).orElse(null);
        if(infoUser == null || infoCamara == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "No se encontró el usuario o la cámara");
        }

        // Conservar el contenido del video si no se proporciona uno nuevo
        byte[] videoContent = existingVideo.getVideo();
        if (dto.getVideo() != null && !dto.getVideo().isEmpty()) {
            // Si se proporciona contenido nuevo en base64, actualizarlo
            videoContent = VideoMapper.base64ToBytes(dto.getVideo());
        }

        // Actualizar los campos modificables
        existingVideo.setNombre(dto.getNombre());
        existingVideo.setFecha(dto.getFecha());
        existingVideo.setDuracion(dto.getDuracion());
        existingVideo.setCamara(infoCamara);
        existingVideo.setUsuario(infoUser);
        existingVideo.setVideo(videoContent);

        return videoRepository.save(existingVideo);
    }

    @Transactional
    public Video updateVideoFile(VideoDTO dto, MultipartFile videoFile) throws IOException {
        // Comprobar que el video existe
        Video existingVideo = videoRepository.findById(dto.getId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "No se encontró el video con id: " + dto.getId()));

        Usuario infoUser = usuarioRepository.findById(dto.getUsuarioId()).orElse(null);
        Camara infoCamara = camaraRepository.findById(dto.getCamaraId()).orElse(null);
        if(infoUser == null || infoCamara == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "No se encontró el usuario o la cámara");
        }

        // Actualizar los campos modificables
        existingVideo.setNombre(dto.getNombre());
        if (dto.getFecha() != null) {
            existingVideo.setFecha(dto.getFecha());
        }
        if (dto.getDuracion() != null) {
            existingVideo.setDuracion(dto.getDuracion());
        }
        existingVideo.setCamara(infoCamara);
        existingVideo.setUsuario(infoUser);

        // Actualizar el contenido del video con el archivo
        existingVideo.setVideo(videoFile.getBytes());

        return videoRepository.save(existingVideo);
    }

    @Transactional(readOnly = true)
    public List<Video> getAllVideos() {
        List<Video> videos = (List<Video>) videoRepository.findAll();
        if(videos.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "No se encontraron videos");
        }
        return videos;
    }

    @Transactional(readOnly = true)
    public Video GetVideoById(Long id) {
        return videoRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "No se encontró el video con el id: " + id));
    }

    @Transactional(readOnly = true)
    public List<Video> GetVideosByCamaraId(Long camaraId) {
        return videoRepository.findByCamaraId(camaraId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "No se encontraron videos relacionados con la cámara de id: " + camaraId));
    }

    @Transactional
    public Boolean deleteVideoById(Long id) {
        if(videoRepository.existsById(id)) {
            videoRepository.deleteById(id);
            return true;
        } else {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND,
                    "No se encontró un video con id: " + id);
        }
    }

    @Transactional(readOnly = true)
    public List<Video> GetVideosByUsuarioId(Long usuarioId) {
        return videoRepository.findByUsuarioId(usuarioId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "No se encontraron videos relacionados con el usuario de id: " + usuarioId));
    }
}