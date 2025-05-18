package co.com.securityserver.repository;

import co.com.securityserver.models.Imagen;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

public interface ImagenRepository extends CrudRepository<Imagen, Long> {

    @Query(value = "SELECT I FROM Imagen I WHERE I.camara.id = :camaraId")
    Imagen findByCamaraId(@Param("camaraId") Long camaraId);

    @Query(value = "SELECT I FROM Imagen I WHERE I.usuario.id = :usuarioId")
    Imagen findByUsuarioId(@Param("usuarioId") Long usuarioId);

}
