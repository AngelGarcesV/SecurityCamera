package co.com.securityserver.controller;

import co.com.securityserver.dto.VideoDTO;
import co.com.securityserver.mapper.VideoMapper;
import co.com.securityserver.models.Video;
import co.com.securityserver.service.VideoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/video")
@CrossOrigin(origins = "*")
public class VideoController {

    @Autowired
    private VideoService videoService;

@PostMapping("/save")
public ResponseEntity<VideoDTO> createVideo(@RequestBody VideoDTO videoDTO) {
    Video savedVideo = videoService.saveVideo(videoDTO);
    return new ResponseEntity<>(VideoMapper.toVideoDTO(savedVideo), HttpStatus.CREATED);
}

@GetMapping("/{id}")
public ResponseEntity<VideoDTO> getVideoById(@PathVariable Long id) {
    Video video = videoService.GetVideoById(id);
    return new ResponseEntity<>(VideoMapper.toVideoDTO(video), HttpStatus.OK);
}

@GetMapping
public ResponseEntity<List<VideoDTO>> getAllVideos() {
    List<VideoDTO> videoDTOs = videoService.getAllVideos().stream()
            .map(VideoMapper::toVideoDTO)
            .collect(Collectors.toList());
    return new ResponseEntity<>(videoDTOs, HttpStatus.OK);
}

@PutMapping("/update")
public ResponseEntity<VideoDTO> updateVideo( @RequestBody VideoDTO videoDTO) {
    Video updatedVideo = videoService.updateVideo(videoDTO);
    return new ResponseEntity<>(VideoMapper.toVideoDTO(updatedVideo), HttpStatus.OK);
}

@DeleteMapping("/{id}")
public ResponseEntity<Void> deleteVideo(@PathVariable Long id) {
    videoService.deleteVideoById(id);
    return new ResponseEntity<>(HttpStatus.NO_CONTENT);
}

@GetMapping("/camara/{camaraId}")
public ResponseEntity<List<VideoDTO>> getVideosByCamaraId(@PathVariable Long camaraId) {
    List<VideoDTO> videoDTOs = videoService.GetVideosByCamaraId(camaraId).stream()
            .map(VideoMapper::toVideoDTO)
            .collect(Collectors.toList());
    return new ResponseEntity<>(videoDTOs, HttpStatus.OK);
}
}