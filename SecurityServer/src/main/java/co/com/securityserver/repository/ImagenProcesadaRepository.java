package co.com.securityserver.repository;

import co.com.securityserver.models.ImagenProcesada;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ImagenProcesadaRepository extends CrudRepository<ImagenProcesada, Long> {

    @Query(value = "SELECT IP FROM ImagenProcesada IP WHERE IP.imagen.id = :imagenId")
    Optional<List<ImagenProcesada>> findByImagenId(@Param("imagenId") Long usuarioId);
}
