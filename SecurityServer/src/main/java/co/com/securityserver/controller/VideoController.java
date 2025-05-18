package co.com.securityserver.controller;

import co.com.securityserver.dto.VideoDTO;
import co.com.securityserver.models.Video;
import co.com.securityserver.service.VideoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/videos")
@CrossOrigin(origins = "*")
public class VideoController {

    @Autowired
    private VideoService videoService;

    @PostMapping
    public ResponseEntity<Video> createVideo(@RequestBody VideoDTO videoDTO) {
        Video savedVideo = videoService.saveVideo(videoDTO);
        return new ResponseEntity<>(savedVideo, HttpStatus.CREATED);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Video> getVideoById(@PathVariable Long id) {
        Video video = videoService.GetVideoById(id);
        return new ResponseEntity<>(video, HttpStatus.OK);
    }

    @GetMapping
    public ResponseEntity<List<Video>> getAllVideos() {
        List<Video> videos = videoService.getAllVideos();
        return new ResponseEntity<>(videos, HttpStatus.OK);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Video> updateVideo(@PathVariable Long id, @RequestBody VideoDTO videoDTO) {
        videoDTO.setId(id);
        Video updatedVideo = videoService.updateVideo(videoDTO);
        return new ResponseEntity<>(updatedVideo, HttpStatus.OK);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteVideo(@PathVariable Long id) {
        videoService.deleteVideoById(id);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @GetMapping("/camara/{camaraId}")
    public ResponseEntity<List<Video>> getVideosByCamaraId(@PathVariable Long camaraId) {
        List<Video> videos = videoService.GetVideosByCamaraId(camaraId);
        return new ResponseEntity<>(videos, HttpStatus.OK);
    }
}