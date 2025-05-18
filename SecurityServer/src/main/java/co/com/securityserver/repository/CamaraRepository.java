package co.com.securityserver.repository;

import co.com.securityserver.dto.CamaraDTO;
import co.com.securityserver.models.Camara;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import javax.swing.text.html.Option;
import java.util.List;
import java.util.Optional;

@Repository
public interface CamaraRepository extends CrudRepository<Camara, Long> {

    @Query("SELECT c FROM Camara c WHERE c.usuario.id = :usuarioId")
    Optional<List<Camara>>  findByUsuarioId(@Param("usuarioId") Long usuarioId);

}