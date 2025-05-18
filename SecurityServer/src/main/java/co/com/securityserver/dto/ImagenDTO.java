package co.com.securityserver.dto;

import co.com.securityserver.models.Camara;
import co.com.securityserver.models.Usuario;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.Data;

import java.util.Date;

@Data
public class ImagenDTO {
    private Long id;
    private String nombre;
    private Byte[] imagen;
    private String resolucion;
    private Date fecha;
    private Long camara_id;
    private Long usuario_id;

    public ImagenDTO() {
    }

    public ImagenDTO(Long id, String nombre, Byte[] imagen, String resolucion, Date fecha, Long camara_id, Long usuario_id) {
        this.id = id;
        this.nombre = nombre;
        this.imagen = imagen;
        this.resolucion = resolucion;
        this.fecha = fecha;
        this.camara_id = camara_id;
        this.usuario_id = usuario_id;
    }
}
