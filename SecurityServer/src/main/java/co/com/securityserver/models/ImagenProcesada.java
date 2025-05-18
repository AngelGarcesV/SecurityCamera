package co.com.securityserver.models;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.Date;

@Entity
@Data
@AllArgsConstructor
public class ImagenProcesada {
    @Id
    @GeneratedValue(strategy = jakarta.persistence.GenerationType.IDENTITY)
    private Long id;
    private String nombre;
    private String resolucion;
    private Byte[] imagenEditada;
    private Date fecha;
    @ManyToOne
    @JoinColumn(name = "imagen_id")
    private Imagen imagen;

    public ImagenProcesada() {
    }

    public ImagenProcesada(String nombre, String resolucion, Byte[] imagenEditada, Date fecha, Imagen imagen) {
        this.nombre = nombre;
        this.resolucion = resolucion;
        this.imagenEditada = imagenEditada;
        this.fecha = fecha;
        this.imagen = imagen;
    }

}
