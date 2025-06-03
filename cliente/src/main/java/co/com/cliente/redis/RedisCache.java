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
    private static final int CONNECTION_TIMEOUT = 2000;

    private static RedisCache instance;
    private Jedis jedis;
    private ObjectMapper objectMapper;
    private AtomicBoolean redisDisponible = new AtomicBoolean(false);
    private AtomicBoolean intentadoConectar = new AtomicBoolean(false);
    private static final Logger LOGGER = Logger.getLogger(RedisCache.class.getName());

    private RedisCache() {
        objectMapper = new ObjectMapper();
    }

    public static synchronized RedisCache getInstance() {
        if (instance == null) {
            instance = new RedisCache();
        }
        return instance;
    }

    private synchronized void inicializarConexion() {
        if (intentadoConectar.get()) {
            return;
        }
        intentadoConectar.set(true);
        try {
            LOGGER.info("Intentando conectar a Redis...");
            jedis = new Jedis(HOST, PORT, CONNECTION_TIMEOUT);

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

    public boolean isRedisDisponible() {
        if (!intentadoConectar.get()) {
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
            redisDisponible.set(false);
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
            redisDisponible.set(false);
        }
        return null;
    }

    public void guardarVideosDeUsuario(Long usuarioId, List<VideoDTO> videos) {
        if (!isRedisDisponible()) return;

        try {
            List<VideoDTO> videosAGuardar = videos;
            if (videos.size() > MAX_CACHED_VIDEOS) {
                videosAGuardar = videos.subList(0, MAX_CACHED_VIDEOS);
            }

            String clave = VIDEO_CACHE_KEY_PREFIX + usuarioId;
            guardarListaEnCache(clave, videosAGuardar);
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Error al guardar videos en caché: " + e.getMessage(), e);
            redisDisponible.set(false);
        }
    }

    public List<VideoDTO> obtenerVideosDeUsuario(Long usuarioId) {
        if (!isRedisDisponible()) return new ArrayList<>();

        String clave = VIDEO_CACHE_KEY_PREFIX + usuarioId;
        List<VideoDTO> videos = obtenerListaDeCache(clave);
        return videos != null ? videos : new ArrayList<>();
    }

    public boolean hayVideosEnCache(Long usuarioId) {
        if (!isRedisDisponible()) return false;

        try {
            String clave = VIDEO_CACHE_KEY_PREFIX + usuarioId;
            return jedis.exists(clave);
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Error al verificar caché: " + e.getMessage(), e);
            redisDisponible.set(false);
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