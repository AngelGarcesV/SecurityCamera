package co.com.securityserver.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
public class CamaraDTO {
    private Long id;
    private String ip;
    private Integer puerto;
    private String descripcion;
    private Double coordenadax;
    private Double coordenaday;
    private String resolucion;
    private Long usuarioId;
    public CamaraDTO() {
    }

    public CamaraDTO(Long id, String ip, Integer puerto, String descripcion, Double coordenadax, Double coordenaday, String resolucion, Long usuarioId) {
        this.id = id;
        this.ip = ip;
        this.puerto = puerto;
        this.descripcion = descripcion;
        this.coordenadax = coordenadax;
        this.coordenaday = coordenaday;
        this.resolucion = resolucion;
        this.usuarioId = usuarioId;
    }
}