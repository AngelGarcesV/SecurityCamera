package co.com.cliente.filtros;

import java.awt.image.BufferedImage;

public interface Filtro {
    BufferedImage aplicar(BufferedImage imagen);
}