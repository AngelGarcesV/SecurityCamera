package co.com.cliente.httpRequest;

import com.fasterxml.jackson.databind.ObjectMapper;

public class JsonResponseHandler {
    private static final ObjectMapper objectMapper = new ObjectMapper();

    public static <T> T parseResponse(String jsonResponse, Class<T> clazz) throws Exception {
        return objectMapper.readValue(jsonResponse, clazz);
    }
}