package co.com.securityserver.models;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.Date;

@Entity
@Data
@AllArgsConstructor
public class Video {
    @Id
    @GeneratedValue(strategy = jakarta.persistence.GenerationType.IDENTITY)
    private Long id;
    private String nombre;
    private Date fecha;
    @Lob
    @Column(columnDefinition = "LONGBLOB")
    private Byte[] video;
    private String duracion;
    @ManyToOne
    @JoinColumn(name = "camara_id")
    private Camara camara;
    @ManyToOne
    @JoinColumn(name = "usuario_id")
    private Usuario usuario;

    public Video() {
    }
    public Video(String nombre, Date fecha, Byte[] video, String duracion, Camara camara, Usuario usuario) {
        this.nombre = nombre;
        this.fecha = fecha;
        this.video = video;
        this.duracion = duracion;
        this.camara = camara;
        this.usuario = usuario;
    }
}
