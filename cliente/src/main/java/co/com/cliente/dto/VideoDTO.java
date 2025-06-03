package co.com.cliente.dto;

import java.util.Date;

public class VideoDTO {
    private Long id;
    private String nombre;
    private Date fecha;
    private String video;
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

    // Getters y Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public Date getFecha() {
        return fecha;
    }

    public void setFecha(Date fecha) {
        this.fecha = fecha;
    }

    public String getVideo() {
        return video;
    }

    public void setVideo(String video) {
        this.video = video;
    }

    public String getDuracion() {
        return duracion;
    }

    public void setDuracion(String duracion) {
        this.duracion = duracion;
    }

    public Long getCamaraId() {
        return camaraId;
    }

    public void setCamaraId(Long camaraId) {
        this.camaraId = camaraId;
    }

    public Long getUsuarioId() {
        return usuarioId;
    }

    public void setUsuarioId(Long usuarioId) {
        this.usuarioId = usuarioId;
    }
}