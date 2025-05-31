// FiltroBrillo corregido
package co.com.cliente.filtros;

import java.awt.image.BufferedImage;
import java.awt.image.RescaleOp;

public class FiltroBrillo implements Filtro {
    private float factorBrillo;
    private float offset;

    public FiltroBrillo() {
        this.factorBrillo = 1.0f; // Sin cambio por defecto
        this.offset = 0.0f;
    }

    public FiltroBrillo(float factorBrillo) {
        this.factorBrillo = Math.max(0.1f, Math.min(3.0f, factorBrillo)); // Limitar entre 0.1 y 3.0
        this.offset = 0.0f;
    }

    public FiltroBrillo(float factorBrillo, float offset) {
        this.factorBrillo = Math.max(0.1f, Math.min(3.0f, factorBrillo));
        this.offset = Math.max(-100.0f, Math.min(100.0f, offset)); // Limitar offset
    }

    @Override
    public BufferedImage aplicar(BufferedImage imagen) {
        if (imagen == null) {
            return null;
        }

        try {
            // Crear una copia de la imagen original para preservar el tipo
            BufferedImage resultado = new BufferedImage(
                    imagen.getWidth(),
                    imagen.getHeight(),
                    imagen.getType()
            );

            // Aplicar el filtro de brillo usando RescaleOp
            RescaleOp rescaleOp = new RescaleOp(factorBrillo, offset, null);
            rescaleOp.filter(imagen, resultado);

            return resultado;

        } catch (Exception e) {
            System.err.println("Error al aplicar filtro de brillo: " + e.getMessage());
            // En caso de error, devolver la imagen original
            return imagen;
        }
    }

    // Métodos para configurar el brillo de forma más intuitiva
    public void aumentarBrillo(float incremento) {
        this.factorBrillo = Math.min(3.0f, this.factorBrillo + incremento);
    }

    public void disminuirBrillo(float decremento) {
        this.factorBrillo = Math.max(0.1f, this.factorBrillo - decremento);
    }

    public void setBrilloRelativo(int porcentaje) {
        // Convertir porcentaje a factor (100% = 1.0f, 150% = 1.5f, 50% = 0.5f)
        this.factorBrillo = Math.max(0.1f, Math.min(3.0f, porcentaje / 100.0f));
    }

    public void setBrilloConOffset(float factor, float offsetValue) {
        this.factorBrillo = Math.max(0.1f, Math.min(3.0f, factor));
        this.offset = Math.max(-100.0f, Math.min(100.0f, offsetValue));
    }

    // Getters
    public float getFactorBrillo() {
        return factorBrillo;
    }

    public float getOffset() {
        return offset;
    }

    public int getBrilloPorcentaje() {
        return Math.round(factorBrillo * 100);
    }

    // Setters con validación
    public void setFactorBrillo(float factorBrillo) {
        this.factorBrillo = Math.max(0.1f, Math.min(3.0f, factorBrillo));
    }

    public void setOffset(float offset) {
        this.offset = Math.max(-100.0f, Math.min(100.0f, offset));
    }

    // Método para resetear a valores por defecto
    public void reset() {
        this.factorBrillo = 1.0f;
        this.offset = 0.0f;
    }

    @Override
    public String toString() {
        return String.format("FiltroBrillo[factor=%.2f, offset=%.2f, porcentaje=%d%%]",
                factorBrillo, offset, getBrilloPorcentaje());
    }
}