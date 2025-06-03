package co.com.cliente.dto;

public class CamaraDTO {
    private Long id;
    private String ip;
    private Integer puerto;
    private String descripcion;
    private Double coordenadax;
    private Double coordenaday;
    private String resolucion;
    private Long usuarioId;

    public CamaraDTO() {}

    public CamaraDTO(Long id, String ip, Integer puerto, String descripcion,
                     Double coordenadax, Double coordenaday, String resolucion, Long usuarioId) {
        this.id = id;
        this.ip = ip;
        this.puerto = puerto;
        this.descripcion = descripcion;
        this.coordenadax = coordenadax;
        this.coordenaday = coordenaday;
        this.resolucion = resolucion;
        this.usuarioId = usuarioId;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public Integer getPuerto() {
        return puerto;
    }

    public void setPuerto(Integer puerto) {
        this.puerto = puerto;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public Double getCoordenadax() {
        return coordenadax;
    }

    public void setCoordenadax(Double coordenadax) {
        this.coordenadax = coordenadax;
    }

    public Double getCoordenaday() {
        return coordenaday;
    }

    public void setCoordenaday(Double coordenaday) {
        this.coordenaday = coordenaday;
    }

    public String getResolucion() {
        return resolucion;
    }

    public void setResolucion(String resolucion) {
        this.resolucion = resolucion;
    }

    public Long getUsuarioId() {
        return usuarioId;
    }

    public void setUsuarioId(Long usuarioId) {
        this.usuarioId = usuarioId;
    }

    // Método para obtener la URL completa de la cámara
    public String getCameraUrl() {
        return "http://" + ip + ":" + puerto + "/shot.jpg";
    }

    @Override
    public String toString() {
        return "CamaraDTO{" +
                "id=" + id +
                ", ip='" + ip + '\'' +
                ", puerto=" + puerto +
                ", descripcion='" + descripcion + '\'' +
                ", coordenadax=" + coordenadax +
                ", coordenaday=" + coordenaday +
                ", resolucion='" + resolucion + '\'' +
                ", usuarioId=" + usuarioId +
                '}';
    }
}