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

import java.util.List;

@Service
public class ImagenProcesadaService {

    @Autowired
    private ImagenProcesadaRepository imagenProcesadaRepository;
    @Autowired
    private ImagenRepository imagenRepository;

    public ImagenProcesadaService(ImagenProcesadaRepository imagenProcesadaRepository, ImagenRepository imagenRepository) {
        this.imagenProcesadaRepository = imagenProcesadaRepository;
        this.imagenRepository = imagenRepository;
    }

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

    @Transactional(readOnly = true)
    public List<ImagenProcesada> getAllImagenProcesada(){
        List<ImagenProcesada> listImagenProcesada = (List<ImagenProcesada>) imagenProcesadaRepository.findAll();
        if(listImagenProcesada.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "No se encontraron imágenes procesadas");
        }
        return listImagenProcesada;
    }

    @Transactional
    public ImagenProcesada updateImagenProcesada(ImagenProcesadaDTO dto){
        ImagenProcesada infoImagenProcesada = imagenProcesadaRepository.findById(dto.getImagenId()).orElse(null);
        if (infoImagenProcesada == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Imagen no encontrada");
        }
        Imagen infoImage = imagenRepository.findById(dto.getImagenId()).orElse(null);
        if (infoImage == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Imagen sin editar no encontrada");
        }
        ImagenProcesada updatedImage = ImagenProcesadaMapper.toImagenProcesada(dto, infoImage);
        return imagenProcesadaRepository.save(updatedImage);
    }

    @Transactional(readOnly = true)
    public List<ImagenProcesada> getImagenProcesadaByImagenId(Long imagenId) {
       return  imagenProcesadaRepository.findByImagenId(imagenId).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "No se encontraron imágenes procesadas para la imagen con ID: " + imagenId));
    }

    @Transactional(readOnly = true)
    public List<ImagenProcesada> getImagenProcesadaByCamaraId(Long camaraId) {
        return imagenProcesadaRepository.findByCamaraId(camaraId).orElseThrow(() ->
                new ResponseStatusException(HttpStatus.NOT_FOUND, "No se encontraron imágenes procesadas para la cámara con ID: " + camaraId));
    }
}
