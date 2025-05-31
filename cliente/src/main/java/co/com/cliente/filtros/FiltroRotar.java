package co.com.cliente.filtros;

import org.imgscalr.Scalr;
import java.awt.image.BufferedImage;

public class FiltroRotar implements Filtro {
    private Scalr.Rotation rotacion;

    public FiltroRotar() {
        this.rotacion = Scalr.Rotation.CW_90; // Por defecto 90 grados
    }

    public FiltroRotar(Scalr.Rotation rotacion) {
        this.rotacion = rotacion;
    }

    @Override
    public BufferedImage aplicar(BufferedImage imagen) {
        return Scalr.rotate(imagen, rotacion, Scalr.OP_ANTIALIAS);
    }

    public void setRotacion(Scalr.Rotation rotacion) {
        this.rotacion = rotacion;
    }
}