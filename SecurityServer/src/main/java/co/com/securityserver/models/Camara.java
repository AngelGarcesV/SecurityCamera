package co.com.securityserver.models;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Entity
@Data
@AllArgsConstructor
public class Camara {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;

    private String ip;

    private Integer puerto;

    private String descripcion;

    private Double coordenadax;

    private Double coordenaday;

    private String resolucion;

    @OneToMany(mappedBy = "camara", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Imagen> imagenes = new ArrayList<>();

    @OneToMany(mappedBy = "camara", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Video> videos = new ArrayList<>();

    public Camara() {}

    public Camara(Long id, String ip, Integer puerto, Usuario usuario, String descripcion, Double coordenadax, Double coordenaday, String resolucion) {
        this.id = id;
        this.ip = ip;
        this.puerto = puerto;
        this.usuario = usuario;
        this.descripcion = descripcion;
        this.coordenadax = coordenadax;
        this.coordenaday = coordenaday;
        this.resolucion = resolucion;
    }

    public void addImagen(Imagen imagen) {
        imagenes.add(imagen);
        imagen.setCamara(this);
    }

    public void removeImagen(Imagen imagen) {
        imagenes.remove(imagen);
        imagen.setCamara(null);
    }

    public void addVideo(Video video) {
        videos.add(video);
        video.setCamara(this);
    }

    public void removeVideo(Video video) {
        videos.remove(video);
        video.setCamara(null);
    }
}
