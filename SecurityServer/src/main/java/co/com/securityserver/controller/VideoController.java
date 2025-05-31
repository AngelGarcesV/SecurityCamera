package co.com.securityserver.controller;

import co.com.securityserver.dto.VideoDTO;
import co.com.securityserver.mapper.VideoMapper;
import co.com.securityserver.models.Video;
import co.com.securityserver.service.VideoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/video")
@CrossOrigin(origins = "*")
public class VideoController {

    @Autowired
    private VideoService videoService;

    // Método existente para compatibilidad con clientes anteriores
    @PostMapping("/save")
    public ResponseEntity<VideoDTO> createVideo(@RequestBody VideoDTO videoDTO) {
        Video savedVideo = videoService.saveVideo(videoDTO);
        return new ResponseEntity<>(VideoMapper.toVideoDTO(savedVideo), HttpStatus.CREATED);
    }

    // Nuevo método que usa MultipartFile para la subida de videos
    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<VideoDTO> uploadVideo(
            @RequestParam("video") MultipartFile videoFile,
            @RequestParam("nombre") String nombre,
            @RequestParam("fecha") String fechaStr,
            @RequestParam("duracion") String duracion,
            @RequestParam("camaraId") Long camaraId,
            @RequestParam("usuarioId") Long usuarioId) throws IOException, ParseException {

        // Convertir fecha de String a Date
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
        Date fecha = dateFormat.parse(fechaStr);

        // Crear DTO con los datos recibidos
        VideoDTO videoDTO = new VideoDTO();
        videoDTO.setNombre(nombre);
        videoDTO.setFecha(fecha);
        videoDTO.setDuracion(duracion);
        videoDTO.setCamaraId(camaraId);
        videoDTO.setUsuarioId(usuarioId);

        // Guardar el video usando el servicio
        Video savedVideo = videoService.saveVideoFile(videoDTO, videoFile);

        return new ResponseEntity<>(VideoMapper.toVideoDTO(savedVideo), HttpStatus.CREATED);
    }

    @GetMapping("/{id}")
    public ResponseEntity<VideoDTO> getVideoById(@PathVariable Long id) {
        Video video = videoService.GetVideoById(id);
        return new ResponseEntity<>(VideoMapper.toVideoDTO(video), HttpStatus.OK);
    }

    // Método para obtener solo los metadatos del video (sin el contenido binario)
    @GetMapping("/{id}/metadata")
    public ResponseEntity<VideoDTO> getVideoMetadataById(@PathVariable Long id) {
        Video video = videoService.GetVideoById(id);
        VideoDTO videoDTO = VideoMapper.toVideoDTOWithoutContent(video);
        return new ResponseEntity<>(videoDTO, HttpStatus.OK);
    }

    @GetMapping
    public ResponseEntity<List<VideoDTO>> getAllVideos() {
        List<VideoDTO> videoDTOs = videoService.getAllVideos().stream()
                .map(VideoMapper::toVideoDTOWithoutContent) // Solo metadatos
                .collect(Collectors.toList());
        return new ResponseEntity<>(videoDTOs, HttpStatus.OK);
    }

    @PutMapping("/update")
    public ResponseEntity<VideoDTO> updateVideo(@RequestBody VideoDTO videoDTO) {
        Video updatedVideo = videoService.updateVideo(videoDTO);
        return new ResponseEntity<>(VideoMapper.toVideoDTOWithoutContent(updatedVideo), HttpStatus.OK);
    }

    // Actualizar video con nuevo archivo
    @PutMapping(value = "/update/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<VideoDTO> updateVideoWithFile(
            @PathVariable Long id,
            @RequestParam(value = "video", required = false) MultipartFile videoFile,
            @RequestParam("nombre") String nombre,
            @RequestParam(value = "fecha", required = false) String fechaStr,
            @RequestParam(value = "duracion", required = false) String duracion) throws IOException, ParseException {

        // Obtener el video existente
        Video existingVideo = videoService.GetVideoById(id);
        VideoDTO videoDTO = VideoMapper.toVideoDTOWithoutContent(existingVideo);

        // Actualizar los campos proporcionados
        videoDTO.setNombre(nombre);

        if (fechaStr != null && !fechaStr.isEmpty()) {
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
            Date fecha = dateFormat.parse(fechaStr);
            videoDTO.setFecha(fecha);
        }

        if (duracion != null && !duracion.isEmpty()) {
            videoDTO.setDuracion(duracion);
        }

        // Actualizar con o sin archivo
        Video updatedVideo;
        if (videoFile != null && !videoFile.isEmpty()) {
            updatedVideo = videoService.updateVideoFile(videoDTO, videoFile);
        } else {
            updatedVideo = videoService.updateVideo(videoDTO);
        }

        return new ResponseEntity<>(VideoMapper.toVideoDTOWithoutContent(updatedVideo), HttpStatus.OK);
    }

    @DeleteMapping("/{id}")
    public boolean deleteVideo(@PathVariable Long id) {
        return videoService.deleteVideoById(id);
    }

    @GetMapping("/camara/{camaraId}")
    public ResponseEntity<List<VideoDTO>> getVideosByCamaraId(@PathVariable Long camaraId) {
        List<VideoDTO> videoDTOs = videoService.GetVideosByCamaraId(camaraId).stream()
                .map(VideoMapper::toVideoDTOWithoutContent) // Solo metadatos
                .collect(Collectors.toList());
        return new ResponseEntity<>(videoDTOs, HttpStatus.OK);
    }

    @GetMapping("/usuario/{usuarioId}")
    public ResponseEntity<List<VideoDTO>> getVideosByUsuarioId(@PathVariable Long usuarioId) {
        List<VideoDTO> videoDTOs = videoService.GetVideosByUsuarioId(usuarioId).stream()
                .map(VideoMapper::toVideoDTOWithoutContent) // Solo metadatos
                .collect(Collectors.toList());
        return new ResponseEntity<>(videoDTOs, HttpStatus.OK);
    }

    // Obtener solo el contenido binario del video (útil para streaming)
    @GetMapping(value = "/{id}/content", produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
    public ResponseEntity<byte[]> getVideoContent(@PathVariable Long id) {
        Video video = videoService.GetVideoById(id);
        return ResponseEntity
                .ok()
                .header("Content-Disposition", "attachment; filename=\"" + video.getNombre() + ".mp4\"")
                .body(video.getVideo());
    }
}