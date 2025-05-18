package co.com.securityserver.models;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Entity
@Data
public class Usuario {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String nombre;
    private String correo;
    private String password;
    private String rol;

    @OneToMany(mappedBy = "usuario", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Camara> camaras = new ArrayList<>();

    @OneToMany(mappedBy = "usuario", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Imagen> imagenes = new ArrayList<>();

    @OneToMany(mappedBy = "usuario", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Video> videos = new ArrayList<>();


    public Usuario() {
    }

    public Usuario(Long id, String nombre, String correo, String password, String rol) {
        this.id = id;
        this.nombre = nombre;
        this.correo = correo;
        this.password = password;
        this.rol = rol;
    }

    public void addCamara(Camara camara) {
        camaras.add(camara);
        camara.setUsuario(this);
    }

    public void removeCamara(Camara camara) {
        camaras.remove(camara);
        camara.setUsuario(null);
    }

    public void addImagen(Imagen imagen) {
        imagenes.add(imagen);
        imagen.setUsuario(this);
    }

    public void removeImagen(Imagen imagen) {
        imagenes.remove(imagen);
        imagen.setUsuario(null);
    }

    public void addVideo(Video video) {
        videos.add(video);
        video.setUsuario(this);
    }

    public void removeVideo(Video video) {
        videos.remove(video);
        video.setUsuario(null);
    }


}
