package co.com.securityserver.dto;

import co.com.securityserver.models.Camara;
import lombok.Data;

import java.util.Date;

@Data
public class VideoDTO {
    private Long id;
    private String nombre;
    private Date fecha;
    private String video; // Usamos String para la transferencia (base64)
    private String duracion;
    private Long camaraId;
    private Long usuarioId;

    // Constructor vacío
    public VideoDTO() {
    }

    // Constructor completo
    public VideoDTO(Long id, String nombre, Date fecha, String video, String duracion, Long camaraId, Long usuarioId) {
        this.id = id;
        this.nombre = nombre;
        this.fecha = fecha;
        this.video = video;
        this.duracion = duracion;
        this.camaraId = camaraId;
        this.usuarioId = usuarioId;
    }

}
