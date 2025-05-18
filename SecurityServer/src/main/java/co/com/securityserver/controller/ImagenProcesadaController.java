package co.com.securityserver.controller;

import co.com.securityserver.dto.ImagenProcesadaDTO;
import co.com.securityserver.models.ImagenProcesada;
import co.com.securityserver.service.ImagenProcesadaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/imagenes-procesadas")
@CrossOrigin(origins = "*")
public class ImagenProcesadaController {

    @Autowired
    private ImagenProcesadaService imagenProcesadaService;

    @PostMapping
    public ResponseEntity<ImagenProcesada> createImagenProcesada(@RequestBody ImagenProcesadaDTO imagenProcesadaDTO) {
        ImagenProcesada savedImagenProcesada = imagenProcesadaService.saveImagenProcesada(imagenProcesadaDTO);
        return new ResponseEntity<>(savedImagenProcesada, HttpStatus.CREATED);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ImagenProcesada> getImagenProcesadaById(@PathVariable Long id) {
        ImagenProcesada imagenProcesada = imagenProcesadaService.getImagenProcesadaById(id);
        return new ResponseEntity<>(imagenProcesada, HttpStatus.OK);
    }

    @GetMapping
    public ResponseEntity<List<ImagenProcesada>> getAllImagenesProcesadas() {
        List<ImagenProcesada> imagenesProcesadas = imagenProcesadaService.getAllImagenProcesada();
        return new ResponseEntity<>(imagenesProcesadas, HttpStatus.OK);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ImagenProcesada> updateImagenProcesada(@PathVariable Long id, @RequestBody ImagenProcesadaDTO imagenProcesadaDTO) {
        imagenProcesadaDTO.setId(id);
        ImagenProcesada updatedImagenProcesada = imagenProcesadaService.updateImagenProcesada(imagenProcesadaDTO);
        return new ResponseEntity<>(updatedImagenProcesada, HttpStatus.OK);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteImagenProcesada(@PathVariable Long id) {
        boolean deleted = imagenProcesadaService.deleteImagenProcesada(id);
        if (deleted) {
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        }
        return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }

    @GetMapping("/imagen/{imagenId}")
    public ResponseEntity<List<ImagenProcesada>> getImagenProcesadaByImagenId(@PathVariable Long imagenId) {
        List<ImagenProcesada> imagenesProcesadas = imagenProcesadaService.getImagenProcesadaByImagenId(imagenId);
        return new ResponseEntity<>(imagenesProcesadas, HttpStatus.OK);
    }
}