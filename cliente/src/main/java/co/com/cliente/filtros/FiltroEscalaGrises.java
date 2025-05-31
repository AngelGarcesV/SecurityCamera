package co.com.cliente.filtros;

import org.imgscalr.Scalr;
import java.awt.image.BufferedImage;

public class FiltroEscalaGrises implements Filtro {

    @Override
    public BufferedImage aplicar(BufferedImage imagen) {
        return Scalr.apply(imagen, Scalr.OP_GRAYSCALE);
    }
}