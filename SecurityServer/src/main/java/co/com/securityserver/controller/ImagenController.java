package co.com.securityserver.controller;

import co.com.securityserver.dto.ImagenDTO;
import co.com.securityserver.models.Imagen;
import co.com.securityserver.service.ImagenService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/imagenes")
@CrossOrigin(origins = "*")
public class ImagenController {

    @Autowired
    private ImagenService imagenService;

    @PostMapping
    public ResponseEntity<Imagen> createImagen(@RequestBody ImagenDTO imagenDTO) {
        Imagen savedImagen = imagenService.saveImagen(imagenDTO);
        return new ResponseEntity<>(savedImagen, HttpStatus.CREATED);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Imagen> getImagenById(@PathVariable Long id) {
        Imagen imagen = imagenService.getImagenById(id);
        return new ResponseEntity<>(imagen, HttpStatus.OK);
    }

    @GetMapping
    public ResponseEntity<List<Imagen>> getAllImagenes() {
        List<Imagen> imagenes = imagenService.getAllImanen();
        return new ResponseEntity<>(imagenes, HttpStatus.OK);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Imagen> updateImagen(@PathVariable Long id, @RequestBody ImagenDTO imagenDTO) {
        imagenDTO.setId(id);
        Imagen updatedImagen = imagenService.updateImagen(imagenDTO);
        return new ResponseEntity<>(updatedImagen, HttpStatus.OK);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteImagen(@PathVariable Long id) {
        boolean deleted = imagenService.deleteImagen(id);
        if (deleted) {
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        }
        return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }

    @GetMapping("/camara/{camaraId}")
    public ResponseEntity<List<Imagen>> getImagenesByCamaraId(@PathVariable Long camaraId) {
        List<Imagen> imagenes = imagenService.getImagenByCamaraId(camaraId);
        return new ResponseEntity<>(imagenes, HttpStatus.OK);
    }

    @GetMapping("/usuario/{usuarioId}")
    public ResponseEntity<List<Imagen>> getImagenesByUsuarioId(@PathVariable Long usuarioId) {
        List<Imagen> imagenes = imagenService.getImagenByUsuarioId(usuarioId);
        return new ResponseEntity<>(imagenes, HttpStatus.OK);
    }
}