package co.com.cliente.filtros.pools;

import co.com.cliente.filtros.FiltroRotar;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.Queue;

public class PoolFiltroRotar {
    private final Queue<FiltroRotar> pool = new ConcurrentLinkedQueue<>();
    private static final int MAX_POOL_SIZE = 10;
    private static PoolFiltroRotar instance;

    private PoolFiltroRotar() {
        // Inicializar el pool con algunos filtros
        for (int i = 0; i < 5; i++) {
            pool.offer(new FiltroRotar());
        }
    }

    public static synchronized PoolFiltroRotar getInstance() {
        if (instance == null) {
            instance = new PoolFiltroRotar();
        }
        return instance;
    }

    public FiltroRotar obtener() {
        FiltroRotar filtro = pool.poll();
        if (filtro == null) {
            filtro = new FiltroRotar();
        }
        return filtro;
    }

    public void liberar(FiltroRotar filtro) {
        if (filtro != null && pool.size() < MAX_POOL_SIZE) {
            pool.offer(filtro);
        }
    }

    public int getPoolSize() {
        return pool.size();
    }
}