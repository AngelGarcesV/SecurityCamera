package co.com.securityserver.dto;

import co.com.securityserver.models.Camara;
import lombok.Data;

import java.util.Date;

@Data
public class VideoDTO {
    private Long id;
    private String nombre;
    private Date fecha;
    private Byte[] video;
    private String duracion;
    private Camara camara;
    private Long usuarioId;

    public VideoDTO() {
    }

    public VideoDTO(Long id, String nombre, Date fecha, Byte[] video, String duracion, Camara camara, Long usuarioId) {
        this.id = id;
        this.nombre = nombre;
        this.fecha = fecha;
        this.video = video;
        this.duracion = duracion;
        this.camara = camara;
        this.usuarioId = usuarioId;
    }
}
