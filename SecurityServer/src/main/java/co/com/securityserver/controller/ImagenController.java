package co.com.securityserver.controller;

import co.com.securityserver.dto.ImagenDTO;
import co.com.securityserver.mapper.ImagenMapper;
import co.com.securityserver.models.Imagen;
import co.com.securityserver.service.ImagenService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/imagenes")
public class ImagenController {

    @Autowired
    private ImagenService imagenService;

    @PostMapping("/save")
    public ResponseEntity<ImagenDTO> createImagen(@RequestBody ImagenDTO imagenDTO) {
        Imagen savedImagen = imagenService.saveImagen(imagenDTO);
        return new ResponseEntity<>(ImagenMapper.toImagenDTO(savedImagen), HttpStatus.CREATED);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ImagenDTO> getImagenById(@PathVariable Long id) {
        Imagen imagen = imagenService.getImagenById(id);
        return new ResponseEntity<>(ImagenMapper.toImagenDTO(imagen), HttpStatus.OK);
    }

    @GetMapping("/all")
    public ResponseEntity<List<ImagenDTO>> getAllImagenes() {
        List<Imagen> imagenes = imagenService.getAllImanen();
        List<ImagenDTO> imagenDTOs = imagenes.stream()
                .map(ImagenMapper::toImagenDTO)
                .collect(Collectors.toList());
        return new ResponseEntity<>(imagenDTOs, HttpStatus.OK);
    }

    @PutMapping("/update")
    public ResponseEntity<ImagenDTO> updateImagen(@RequestBody ImagenDTO imagenDTO) {
        Imagen updatedImagen = imagenService.updateImagen(imagenDTO);
        return new ResponseEntity<>(ImagenMapper.toImagenDTO(updatedImagen), HttpStatus.OK);
    }

    @DeleteMapping("/{id}")
    public boolean deleteImagen(@PathVariable Long id) {
        return imagenService.deleteImagen(id);
    }

    @GetMapping("/camara/{camaraId}")
    public ResponseEntity<List<ImagenDTO>> getImagenesByCamaraId(@PathVariable Long camaraId) {
        List<ImagenDTO> imagenDTOs = imagenService.getImagenByCamaraId(camaraId).stream()
                .map(ImagenMapper::toImagenDTO)
                .collect(Collectors.toList());
        return new ResponseEntity<>(imagenDTOs, HttpStatus.OK);
    }

    @GetMapping("/usuario/{usuarioId}")
    public ResponseEntity<List<ImagenDTO>> getImagenesByUsuarioId(@PathVariable Long usuarioId) {
        List<ImagenDTO> imagenDTOs = imagenService.getImagenByUsuarioId(usuarioId).stream()
                .map(ImagenMapper::toImagenDTO)
                .collect(Collectors.toList());
        return new ResponseEntity<>(imagenDTOs, HttpStatus.OK);
    }
}