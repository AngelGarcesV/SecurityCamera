package co.com.securityserver.controller;

import co.com.securityserver.dto.CamaraDTO;
import co.com.securityserver.models.Camara;
import co.com.securityserver.service.CamaraService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/camara")
public class CamaraController {

    @Autowired
    private CamaraService camaraService;



    @GetMapping("/all")
    public List<Camara> getAllCamaras() {
        return camaraService.getAllCamaras();
    }

    @PostMapping("/save")
    public Camara saveCamara(@RequestBody CamaraDTO camaraDTO) {
        return camaraService.saveCamara(camaraDTO);
    }

    @PutMapping("/{id}")
    public Camara updateCamara(@PathVariable Long id, @RequestBody CamaraDTO camaraDTO) {
        return camaraService.updateCamara(camaraDTO);
    }

    @DeleteMapping("/{id}")
    public boolean deleteCamara(@PathVariable Long id) {
        if (camaraService.deleteCamara(id)) {
            return true;
        } else {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Camara no encontrada");
        }
    }

    @GetMapping("/{id}")
    public Camara getCamaraById(@PathVariable Long id) {
        return camaraService.getCamaraById(id);
    }

    @GetMapping("/usuario/{usuarioId}")
    public List<Camara> getCamarasByUsuarioId(@PathVariable Long usuarioId) {
        return camaraService.getCamarasByUsuarioId(usuarioId);
    }
}