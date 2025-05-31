package co.com.cliente.dto;

import java.util.Date;

public class ImagenProcesadaDTO {
    private Long id;
    private String nombre;
    private String resolucion;
    private byte[] imagenEditada;
    private Date fecha;
    private Long imagenId;

    // Getters and Setters
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

    public String getResolucion() {
        return resolucion;
    }

    public void setResolucion(String resolucion) {
        this.resolucion = resolucion;
    }

    public byte[] getImagenEditada() {
        return imagenEditada;
    }

    public void setImagenEditada(byte[] imagenEditada) {
        this.imagenEditada = imagenEditada;
    }

    public Date getFecha() {
        return fecha;
    }

    public void setFecha(Date fecha) {
        this.fecha = fecha;
    }

    public Long getImagenId() {
        return imagenId;
    }

    public void setImagenId(Long imagenId) {
        this.imagenId = imagenId;
    }
}