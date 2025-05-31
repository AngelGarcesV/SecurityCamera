package co.com.cliente.redis;

import co.com.cliente.dto.VideoDTO;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPoolConfig;
import redis.clients.jedis.exceptions.JedisConnectionException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;
import java.util.logging.Logger;

public class RedisCache {

    private static final String HOST = "localhost";
    private static final int PORT = 6379;
    private static final String VIDEO_CACHE_KEY_PREFIX = "videos_user_";
    private static final int MAX_CACHED_VIDEOS = 5;
    private static final int CONNECTION_TIMEOUT = 2000; // 2 segundos de timeout

    private static RedisCache instance;
    private Jedis jedis;
    private ObjectMapper objectMapper;
    private AtomicBoolean redisDisponible = new AtomicBoolean(false);
    private AtomicBoolean intentadoConectar = new AtomicBoolean(false);
    private static final Logger LOGGER = Logger.getLogger(RedisCache.class.getName());

    // Private constructor to prevent instantiation
    private RedisCache() {
        objectMapper = new ObjectMapper();
        // No intentar conectar a Redis inmediatamente - lo haremos cuando sea necesario
    }

    // Public method to get the single instance
    public static synchronized RedisCache getInstance() {
        if (instance == null) {
            instance = new RedisCache();
        }
        return instance;
    }

    // Inicializar conexión en un método separado - conexión diferida
    private synchronized void inicializarConexion() {
        // Si ya intentamos conectar, no lo intentamos de nuevo
        if (intentadoConectar.get()) {
            return;
        }

        intentadoConectar.set(true);

        try {
            LOGGER.info("Intentando conectar a Redis...");
            // Configurar un timeout corto para evitar bloqueos largos
            jedis = new Jedis(HOST, PORT, CONNECTION_TIMEOUT);

            // Probar la conexión con un ping
            String pong = jedis.ping();
            if ("PONG".equals(pong)) {
                redisDisponible.set(true);
                LOGGER.info("Conexión a Redis establecida correctamente");
            } else {
                LOGGER.warning("Redis no respondió correctamente al ping");
            }
        } catch (JedisConnectionException e) {
            LOGGER.log(Level.WARNING, "No se pudo conectar a Redis: " + e.getMessage());
            redisDisponible.set(false);
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error al inicializar Redis: " + e.getMessage(), e);
            redisDisponible.set(false);
        }
    }

    // Método para comprobar si Redis está disponible
    public boolean isRedisDisponible() {
        if (!intentadoConectar.get()) {
            // Inicializamos la conexión solo cuando se necesita por primera vez
            inicializarConexion();
        }
        return redisDisponible.get();
    }

    public void guardarListaEnCache(String clave, List<VideoDTO> lista) {
        if (!isRedisDisponible()) return;

        try {
            String json = objectMapper.writeValueAsString(lista);
            jedis.set(clave, json);
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Error al guardar en caché: " + e.getMessage(), e);
            redisDisponible.set(false); // Marcar como no disponible si falla
        }
    }

    public List<VideoDTO> obtenerListaDeCache(String clave) {
        if (!isRedisDisponible()) return null;

        try {
            String json = jedis.get(clave);
            if (json != null) {
                return objectMapper.readValue(json, new TypeReference<List<VideoDTO>>() {});
            }
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Error al obtener de caché: " + e.getMessage(), e);
            redisDisponible.set(false); // Marcar como no disponible si falla
        }
        return null;
    }

    // Método específico para guardar los últimos N videos de un usuario
    public void guardarVideosDeUsuario(Long usuarioId, List<VideoDTO> videos) {
        if (!isRedisDisponible()) return;

        try {
            // Si hay más de MAX_CACHED_VIDEOS, solo guardamos los últimos N
            List<VideoDTO> videosAGuardar = videos;
            if (videos.size() > MAX_CACHED_VIDEOS) {
                videosAGuardar = videos.subList(0, MAX_CACHED_VIDEOS);
            }

            String clave = VIDEO_CACHE_KEY_PREFIX + usuarioId;
            guardarListaEnCache(clave, videosAGuardar);
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Error al guardar videos en caché: " + e.getMessage(), e);
            redisDisponible.set(false); // Marcar como no disponible si falla
        }
    }

    // Método específico para obtener los videos en caché de un usuario
    public List<VideoDTO> obtenerVideosDeUsuario(Long usuarioId) {
        if (!isRedisDisponible()) return new ArrayList<>();

        String clave = VIDEO_CACHE_KEY_PREFIX + usuarioId;
        List<VideoDTO> videos = obtenerListaDeCache(clave);
        return videos != null ? videos : new ArrayList<>();
    }

    // Verificar si hay videos en caché para un usuario
    public boolean hayVideosEnCache(Long usuarioId) {
        if (!isRedisDisponible()) return false;

        try {
            String clave = VIDEO_CACHE_KEY_PREFIX + usuarioId;
            return jedis.exists(clave);
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Error al verificar caché: " + e.getMessage(), e);
            redisDisponible.set(false); // Marcar como no disponible si falla
            return false;
        }
    }

    public void cerrarConexion() {
        if (redisDisponible.get() && jedis != null) {
            try {
                jedis.close();
            } catch (Exception e) {
                LOGGER.log(Level.WARNING, "Error al cerrar conexión con Redis: " + e.getMessage(), e);
            }
        }
    }
}