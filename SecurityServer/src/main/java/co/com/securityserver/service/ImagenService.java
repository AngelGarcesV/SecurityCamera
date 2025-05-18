package co.com.securityserver.service;

import co.com.securityserver.dto.ImagenDTO;
import co.com.securityserver.models.Camara;
import co.com.securityserver.models.Imagen;
import co.com.securityserver.models.Usuario;
import co.com.securityserver.repository.CamaraRepository;
import co.com.securityserver.repository.ImagenRepository;
import co.com.securityserver.repository.UsuarioRepository;
import co.com.securityserver.mapper.ImagenMapper;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;


@Service
public class ImagenService {
    @Autowired
    private ImagenRepository imagenRepository;
    @Autowired
    private UsuarioRepository usuarioRepository;
    @Autowired
    private CamaraRepository camaraRepository;

    public ImagenService(ImagenRepository imagenRepository, UsuarioRepository usuarioRepository, CamaraRepository camaraRepository) {
        this.imagenRepository = imagenRepository;
        this.usuarioRepository = usuarioRepository;
        this.camaraRepository = camaraRepository;
    }

    @Transactional
    public Imagen saveImagen(ImagenDTO imagenDTO) {
        Usuario infoUser= usuarioRepository.findById(imagenDTO.getUsuario_id()).orElse(null);
        Camara infoCamera = camaraRepository.findById(imagenDTO.getCamara_id()).orElse(null);
        if(infoUser != null && infoCamera != null) {
            Imagen image = ImagenMapper.toImagen(imagenDTO, infoCamera, infoUser);
            return imagenRepository.save(image);
        }
        throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuario o Camara no encontrados");
    }

    @Transactional(readOnly = true)
    public Imagen getImagenById(Long id) {
        return imagenRepository.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Imagen no encontrada"));
    }

    @Transactional(readOnly = true)
    public List<Imagen> getAllImanen(){
        List<Imagen> imageList = (List<Imagen>) imagenRepository.findAll();
        if(imageList.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "No se encontraron imágenes");
        }
        return imageList;
    }

    @Transactional
    public boolean deleteImagen(Long id) {
        if (imagenRepository.existsById(id)) {
            imagenRepository.deleteById(id);
            return true;
        }
        throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Imagen no encontrada");
    }



}
