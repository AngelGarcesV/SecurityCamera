package co.com.securityserver.repository;

import co.com.securityserver.models.Usuario;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UsuarioRepository extends CrudRepository<Usuario, Long> {

    @Query("SELECT u FROM Usuario u WHERE u.correo = :correo")
    Optional<Usuario>  findByCorreo(@Param("correo") String correo);

    @Query("SELECT COUNT(u) > 0 FROM Usuario u WHERE u.correo = :correo")
    boolean existsByCorreo(@Param("correo") String correo);

}
