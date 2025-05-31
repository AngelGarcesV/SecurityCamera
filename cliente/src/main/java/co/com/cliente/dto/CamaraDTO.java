package co.com.cliente.dto;

import java.util.Date;

public class CamaraDTO {
    private Long id;
    private String descripcion;
    private Double coordenadax;
    private Double coordenaday;
    private String resolucion;
    private Long usuarioId;

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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
}