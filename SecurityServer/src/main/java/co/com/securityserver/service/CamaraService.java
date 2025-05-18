package co.com.securityserver.service;

import co.com.securityserver.builder.CamaraSeguridadBuilder;
import co.com.securityserver.builder.DirectorCamaraSeguridad;
import co.com.securityserver.dto.CamaraDTO;
import co.com.securityserver.models.Camara;
import co.com.securityserver.models.Usuario;
import co.com.securityserver.repository.CamaraRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
public class CamaraService {

    @Autowired
    private CamaraRepository camaraRepo;

    @Autowired
    private UsuarioService usuarioService;

    public CamaraService(CamaraRepository camaraRepo, UsuarioService usuarioService) {
        this.camaraRepo = camaraRepo;
        this.usuarioService = usuarioService;
    }

    @Transactional
    public Camara saveCamara(CamaraDTO dto) {
        Usuario infoUser = usuarioService.getUsuarioById(dto.getId());
        if (infoUser == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuario no encontrado");
        }
        CamaraSeguridadBuilder builder = new CamaraSeguridadBuilder(usuarioService);
        DirectorCamaraSeguridad director = new DirectorCamaraSeguridad(builder);
        Camara camera = director.ConstruirCamara(dto);
        return camaraRepo.save(camera);
    }

    @Transactional(readOnly = true)
    public Camara getCamaraById(Long id) {
        return camaraRepo.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Camara no encontrada"));
    }

    @Transactional
    public boolean deleteCamara(Long id) {
        if (camaraRepo.existsById(id)) {
            camaraRepo.deleteById(id);
            return true;
        }
        throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Camara no encontrada");
    }

    @Transactional(readOnly = true)
    public List<Camara> getAllCamaras() {
        List<Camara> cameraList = (List<Camara>) camaraRepo.findAll();
        if(cameraList.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "No se encontraron cámaras");
        }
        return cameraList;
    }

    @Transactional
    public Camara updateCamara(CamaraDTO camaraDTO) {
        if (camaraRepo.existsById(camaraDTO.getId()) && camaraRepo.existsById(camaraDTO.getId())) {
            CamaraSeguridadBuilder builder = new CamaraSeguridadBuilder(usuarioService);
            DirectorCamaraSeguridad director = new DirectorCamaraSeguridad(builder);
            Camara camara = director.ConstruirCamara(camaraDTO);
            return camaraRepo.save(camara);
        }else{
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Camara o usuario no encontrados");
        }

    }

    @Transactional(readOnly = true)
    public List<Camara> getCamarasByUsuarioId(Long usuarioId) {
        return camaraRepo.findByUsuarioId(usuarioId)
                .filter(list -> !list.isEmpty())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "No se encontraron cámaras para el usuario"));
    }
}