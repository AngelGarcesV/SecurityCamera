package co.com.securityserver.models;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.Date;

@Entity
@Data
@AllArgsConstructor
public class Imagen {
    @Id
    @GeneratedValue(strategy = jakarta.persistence.GenerationType.IDENTITY)
    private Long id;
    private String nombre;
    private Byte[] imagen;
    private String resolucion;
    private Date fecha;
    @ManyToOne
    @JoinColumn(name = "camara_id")
    Camara camara;
    @ManyToOne
    @JoinColumn(name = "usuario_id")
    Usuario usuario;


    public Imagen() {
    }

    public Imagen(String nombre,  Byte[] imagen, String resolucion, Date fecha, Camara camara,Usuario usuario) {
        this.nombre = nombre;
        this.imagen = imagen;
        this.resolucion = resolucion;
        this.fecha = fecha;
        this.camara = camara;
        this.usuario = usuario;
    }
}
