package co.com.cliente.filtros.pools;

import co.com.cliente.filtros.FiltroEscalaGrises;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.Queue;

public class PoolFiltroEscalaGrises {
    private final Queue<FiltroEscalaGrises> pool = new ConcurrentLinkedQueue<>();
    private static final int MAX_POOL_SIZE = 10;
    private static PoolFiltroEscalaGrises instance;

    private PoolFiltroEscalaGrises() {
        // Inicializar el pool con algunos filtros
        for (int i = 0; i < 5; i++) {
            pool.offer(new FiltroEscalaGrises());
        }
    }

    public static synchronized PoolFiltroEscalaGrises getInstance() {
        if (instance == null) {
            instance = new PoolFiltroEscalaGrises();
        }
        return instance;
    }

    public FiltroEscalaGrises obtener() {
        FiltroEscalaGrises filtro = pool.poll();
        if (filtro == null) {
            filtro = new FiltroEscalaGrises();
        }
        return filtro;
    }

    public void liberar(FiltroEscalaGrises filtro) {
        if (filtro != null && pool.size() < MAX_POOL_SIZE) {
            pool.offer(filtro);
        }
    }

    public int getPoolSize() {
        return pool.size();
    }
}
