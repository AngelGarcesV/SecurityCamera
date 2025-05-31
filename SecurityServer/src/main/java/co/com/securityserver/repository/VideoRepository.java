package co.com.securityserver.repository;

import co.com.securityserver.models.Video;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface VideoRepository extends CrudRepository<Video, Long>{
    @Query(value = "SELECT V FROM Video V WHERE V.camara.id = :camaraId")
    Optional<List<Video>>  findByCamaraId(@Param("camaraId") Long camaraId);

    @Query(value = "SELECT V FROM Video V WHERE V.usuario.id = :id")
    Optional<List<Video>>  findByUsuarioId(@Param("id") Long id);
}
