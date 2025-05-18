package co.com.securityserver.service;

import co.com.securityserver.dto.ImagenProcesadaDTO;
import co.com.securityserver.mapper.ImagenProcesadaMapper;
import co.com.securityserver.models.Imagen;
import co.com.securityserver.models.ImagenProcesada;
import co.com.securityserver.models.Usuario;
import co.com.securityserver.repository.ImagenProcesadaRepository;
import co.com.securityserver.repository.ImagenRepository;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class ImagenProcesadaService {

    @Autowired
    private ImagenProcesadaRepository imagenProcesadaRepository;
    @Autowired
    private ImagenRepository imagenRepository;

    @Transactional(readOnly = true)
    public ImagenProcesada saveImagenProcesada(ImagenProcesadaDTO imagenProcesadaDTO) {
        Imagen infoImage = imagenRepository.findById(imagenProcesadaDTO.getImagenId()).orElse(null);
        if (infoImage == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Imagen no encontrada");
        }
        ImagenProcesada imagenProcesada = ImagenProcesadaMapper.toImagenProcesada(imagenProcesadaDTO, infoImage);
        return imagenProcesadaRepository.save(imagenProcesada);
    }
    @Transactional(readOnly = true)
    public ImagenProcesada getImagenProcesadaById(Long id){
        return imagenProcesadaRepository.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Imagen procesada no encontrada"));
    }
    @Transactional
    public boolean deleteImagenProcesada(Long id) {
        if (imagenProcesadaRepository.existsById(id)) {
            imagenProcesadaRepository.deleteById(id);
            return true;
        }
        return false;
    }
}
