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
    private byte[] imagen;
    private String resolucion;
    private Date fecha;
    private Long camaraId;
    private Long usuarioId;

    public ImagenDTO() {
    }

    public ImagenDTO(Long id, String nombre, byte[] imagen, String resolucion, Date fecha, Long camaraId, Long usuarioId) {
        this.id = id;
        this.nombre = nombre;
        this.imagen = imagen;
        this.resolucion = resolucion;
        this.fecha = fecha;
        this.camaraId = camaraId;
        this.usuarioId = usuarioId;
    }
}
