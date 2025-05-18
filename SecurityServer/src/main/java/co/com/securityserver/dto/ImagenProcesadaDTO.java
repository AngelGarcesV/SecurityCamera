package co.com.securityserver.dto;

import co.com.securityserver.models.Imagen;
import lombok.Data;

import java.util.Date;

@Data
public class ImagenProcesadaDTO {
    private Long id;
    private String nombre;
    private String resolucion;
    private Byte[] imagenEditada;
    private Date fecha;
    private Long imagenId;

    public ImagenProcesadaDTO() {
    }

    public ImagenProcesadaDTO(Long id, String nombre, String resolucion, Byte[] imagenEditada, Date fecha, Long imagenId) {
        this.id = id;
        this.nombre = nombre;
        this.resolucion = resolucion;
        this.imagenEditada = imagenEditada;
        this.fecha = fecha;
        this.imagenId = imagenId;
    }
}
