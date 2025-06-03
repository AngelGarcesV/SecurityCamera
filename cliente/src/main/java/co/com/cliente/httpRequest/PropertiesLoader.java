package co.com.cliente.httpRequest;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class PropertiesLoader {

    private static final String CONFIG_FILE = "/application.properties";
    private static Properties properties;

    static {
        loadProperties();
    }

    private static void loadProperties() {
        properties = new Properties();
        try (InputStream input = PropertiesLoader.class.getResourceAsStream(CONFIG_FILE)) {
            if (input != null) {
                properties.load(input);
            } else {
                throw new RuntimeException("No se pudo encontrar el archivo " + CONFIG_FILE);
            }
        } catch (IOException e) {
            throw new RuntimeException("Error al cargar la configuración", e);
        }
    }
    public static String getBaseUrl() {
        return properties.getProperty("api.base.url", "http://localhost:9000");
    }
}
