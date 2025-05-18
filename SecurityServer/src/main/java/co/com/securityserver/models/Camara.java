package co.com.securityserver.models;

import jakarta.persistence.*;

import co.com.securityserver.models.Usuario;
import lombok.AllArgsConstructor;
import lombok.Data;

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
    private String descripcion;
    private Double coordenadax;
    private Double coordenaday;
    private String resolucion;

    public Camara() {
    }
    public Camara(Usuario usuario, String descripcion, Double coordenadax, Double coordenaday, String resolucion) {
        this.usuario = usuario;
        this.descripcion = descripcion;
        this.coordenadax = coordenadax;
        this.coordenaday = coordenaday;
        this.resolucion = resolucion;
    }
}
