package co.com.securityserver.controller;

import co.com.securityserver.dto.ImagenProcesadaDTO;
import co.com.securityserver.mapper.ImagenProcesadaMapper;
import co.com.securityserver.models.ImagenProcesada;
import co.com.securityserver.service.ImagenProcesadaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/imagenesProcesadas")
public class ImagenProcesadaController {

    @Autowired
    private ImagenProcesadaService imagenProcesadaService;

    @PostMapping
    public ResponseEntity<ImagenProcesadaDTO> createImagenProcesada(@RequestBody ImagenProcesadaDTO imagenProcesadaDTO) {
        ImagenProcesada savedImagenProcesada = imagenProcesadaService.saveImagenProcesada(imagenProcesadaDTO);
        return new ResponseEntity<>(ImagenProcesadaMapper.toImagenProcesadaDTO(savedImagenProcesada), HttpStatus.CREATED);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ImagenProcesadaDTO> getImagenProcesadaById(@PathVariable Long id) {
        ImagenProcesada imagenProcesada = imagenProcesadaService.getImagenProcesadaById(id);
        return new ResponseEntity<>(ImagenProcesadaMapper.toImagenProcesadaDTO(imagenProcesada), HttpStatus.OK);
    }

    @GetMapping
    public ResponseEntity<List<ImagenProcesadaDTO>> getAllImagenesProcesadas() {
        List<ImagenProcesada> imagenesProcesadas = imagenProcesadaService.getAllImagenProcesada();
        List<ImagenProcesadaDTO> imagenProcesadaDTOs = imagenesProcesadas.stream()
                .map(ImagenProcesadaMapper::toImagenProcesadaDTO)
                .collect(Collectors.toList());
        return new ResponseEntity<>(imagenProcesadaDTOs, HttpStatus.OK);
    }

    @PutMapping("/update")
    public ResponseEntity<ImagenProcesadaDTO> updateImagenProcesada(@RequestBody ImagenProcesadaDTO imagenProcesadaDTO) {
        ImagenProcesada updatedImagenProcesada = imagenProcesadaService.updateImagenProcesada(imagenProcesadaDTO);
        return new ResponseEntity<>(ImagenProcesadaMapper.toImagenProcesadaDTO(updatedImagenProcesada), HttpStatus.OK);
    }

    @DeleteMapping("/{id}")
    public boolean deleteImagenProcesada(@PathVariable Long id) {
        return imagenProcesadaService.deleteImagenProcesada(id);
    }

    @GetMapping("/imagen/{imagenId}")
    public ResponseEntity<List<ImagenProcesadaDTO>> getImagenProcesadaByImagenId(@PathVariable Long imagenId) {
        List<ImagenProcesada> imagenesProcesadas = imagenProcesadaService.getImagenProcesadaByImagenId(imagenId);
        List<ImagenProcesadaDTO> imagenProcesadaDTOs = imagenesProcesadas.stream()
                .map(ImagenProcesadaMapper::toImagenProcesadaDTO)
                .collect(Collectors.toList());
        return new ResponseEntity<>(imagenProcesadaDTOs, HttpStatus.OK);
    }


}