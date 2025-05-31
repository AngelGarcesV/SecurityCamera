package co.com.cliente.filtros.pools;

import co.com.cliente.filtros.FiltroBrillo;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.Queue;

public class PoolFiltroBrillo {
    private final Queue<FiltroBrillo> pool = new ConcurrentLinkedQueue<>();
    private static final int MAX_POOL_SIZE = 10;
    private static PoolFiltroBrillo instance;

    private PoolFiltroBrillo() {
        // Inicializar el pool con algunos filtros
        for (int i = 0; i < 5; i++) {
            pool.offer(new FiltroBrillo());
        }
    }

    public static synchronized PoolFiltroBrillo getInstance() {
        if (instance == null) {
            instance = new PoolFiltroBrillo();
        }
        return instance;
    }

    public FiltroBrillo obtener() {
        FiltroBrillo filtro = pool.poll();
        if (filtro == null) {
            filtro = new FiltroBrillo();
        }
        return filtro;
    }

    public void liberar(FiltroBrillo filtro) {
        if (filtro != null && pool.size() < MAX_POOL_SIZE) {
            pool.offer(filtro);
        }
    }

    public int getPoolSize() {
        return pool.size();
    }
}