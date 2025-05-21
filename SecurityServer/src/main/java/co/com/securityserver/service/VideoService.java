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
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

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
    public Video saveVideo (VideoDTO dto){
        Usuario infoUser = usuarioRepository.findById(dto.getUsuarioId()).orElse(null);
        Camara camaraInfo = camaraRepository.findById(dto.getCamaraId()).orElse(null);
        if(infoUser == null || camaraInfo == null){
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "No se encontro el usuario o la camara");
        }
        Video video = VideoMapper.toVideo(dto, infoUser, camaraInfo );
        return videoRepository.save(video);
    }

    @Transactional
    public Video updateVideo (VideoDTO dto){
        Usuario infoUser = usuarioRepository.findById(dto.getUsuarioId()).orElse(null);
        Camara infoCamara = camaraRepository.findById(dto.getCamaraId()).orElse(null);
        if(infoUser == null || infoCamara == null){
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "No se encontro el usuario o la camara");
        }
        Video video = VideoMapper.toVideo(dto, infoUser, infoCamara);
        return videoRepository.save(video);
    }

    @Transactional(readOnly = true)
    public List<Video> getAllVideos(){
        List<Video> videos = (List<Video>) videoRepository.findAll();
        if(videos.isEmpty()){
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "No se encontraron videos");
        }
        return videos;
    }

    @Transactional(readOnly = true)
    public Video GetVideoById(Long id){
        return videoRepository.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "No se encontro el video con el id" + id));
    }

    @Transactional(readOnly = true)
    public List<Video> GetVideosByCamaraId(Long camaraId){
        return videoRepository.findByCamaraId(camaraId).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "No se encontro videos relacionados con la camara de id: "+ camaraId));
    }

    @Transactional
    public Boolean deleteVideoById(Long id){
        if(videoRepository.existsById(id)){
            videoRepository.deleteById(id);
            return true;
        }else {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "No se encontro un video con id: "+ id);
        }
    }

    @Transactional(readOnly = true)
    public List<Video> GetVideosByUsuarioId(Long usuarioId){
        return videoRepository.findByUsuarioId(usuarioId).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "No se encontro videos relacionados con el usuario de id: "+ usuarioId));
    }


}
