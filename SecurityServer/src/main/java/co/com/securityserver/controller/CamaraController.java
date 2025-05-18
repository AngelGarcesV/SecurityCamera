package co.com.securityserver.controller;

import co.com.securityserver.dto.CamaraDTO;
import co.com.securityserver.mapper.CamaraMapper;
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
    public List<CamaraDTO> getAllCamaras() {
        return camaraService.getAllCamaras().stream()
                .map(CamaraMapper::toCamaraDTO)
                .collect(Collectors.toList());
    }

    @PostMapping("/save")
    public CamaraDTO saveCamara(@RequestBody CamaraDTO camaraDTO) {
        return CamaraMapper.toCamaraDTO(camaraService.saveCamara(camaraDTO));
    }

    @PutMapping("/update")
    public CamaraDTO updateCamara( @RequestBody CamaraDTO camaraDTO) {
        return CamaraMapper.toCamaraDTO(camaraService.updateCamara(camaraDTO));
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
    public CamaraDTO getCamaraById(@PathVariable Long id) {
        return CamaraMapper.toCamaraDTO(camaraService.getCamaraById(id));
    }

    @GetMapping("/usuario/{usuarioId}")
    public List<CamaraDTO> getCamarasByUsuarioId(@PathVariable Long usuarioId) {
        return camaraService.getCamarasByUsuarioId(usuarioId).stream()
                .map(CamaraMapper::toCamaraDTO)
                .collect(Collectors.toList());
    }
}