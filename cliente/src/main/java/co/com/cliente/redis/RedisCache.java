package co.com.cliente.redis;

import co.com.cliente.dto.VideoDTO;
import redis.clients.jedis.Jedis;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;

public class RedisCache {

    private static final String HOST = "localhost";
    private static final int PORT = 6379;

    private Jedis jedis;
    private ObjectMapper objectMapper;

    public RedisCache() {
        jedis = new Jedis(HOST, PORT);
        objectMapper = new ObjectMapper();
    }

    public void guardarListaEnCache(String clave, List<VideoDTO> lista) {
        try {
            String json = objectMapper.writeValueAsString(lista);
            jedis.set(clave, json);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public List<VideoDTO> obtenerListaDeCache(String clave) {
        try {
            String json = jedis.get(clave);
            if (json != null) {
                return objectMapper.readValue(json, new TypeReference<List<VideoDTO>>() {});
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public void cerrarConexion() {
        jedis.close();
    }
}