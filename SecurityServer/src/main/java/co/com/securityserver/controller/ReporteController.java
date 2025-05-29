package co.com.securityserver.controller;

import co.com.securityserver.repository.CamaraRepository;
import co.com.securityserver.repository.ImagenRepository;
import co.com.securityserver.repository.VideoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/reportes")
public class ReporteController {

    @Autowired
    private CamaraRepository camaraRepository;

    @Autowired
    private ImagenRepository imagenRepository;

    @Autowired
    private VideoRepository videoRepository;

    @GetMapping
    public ResponseEntity<Map<String, Integer>> obtenerEstadisticas() {
        Map<String, Integer> response = new HashMap<>();
        response.put("camaras", (int) camaraRepository.count());
        response.put("imagenes", (int) imagenRepository.count());
        response.put("videos", (int) videoRepository.count());

        return ResponseEntity.ok(response);
    }
}
