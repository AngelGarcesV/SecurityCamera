package co.com.securityserver.models;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Entity
@Data
@AllArgsConstructor
public class Imagen {
    @Id
    @GeneratedValue(strategy = jakarta.persistence.GenerationType.IDENTITY)
    private Long id;
    private String nombre;
    @Lob
    @Column(columnDefinition = "LONGBLOB")
    private Byte[] imagen;
    private String resolucion;
    private Date fecha;
    @ManyToOne
    @JoinColumn(name = "camara_id", nullable = false)
    Camara camara;
    @ManyToOne
    @JoinColumn(name = "usuario_id", nullable = false)
    Usuario usuario;

    @OneToMany(mappedBy = "imagen", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ImagenProcesada> imagenesProcesadas = new ArrayList<>();


    public Imagen() {
    }

    public Imagen(Long id,String nombre,  Byte[] imagen, String resolucion, Date fecha, Camara camara,Usuario usuario) {
        this.id = id;
        this.nombre = nombre;
        this.imagen = imagen;
        this.resolucion = resolucion;
        this.fecha = fecha;
        this.camara = camara;
        this.usuario = usuario;
    }



    public void addImagenProcesada(ImagenProcesada imagenProcesada) {
        imagenesProcesadas.add(imagenProcesada);
        imagenProcesada.setImagen(this);
    }

    public void removeImagenProcesada(ImagenProcesada imagenProcesada) {
        imagenesProcesadas.remove(imagenProcesada);
        imagenProcesada.setImagen(null);
    }
}
