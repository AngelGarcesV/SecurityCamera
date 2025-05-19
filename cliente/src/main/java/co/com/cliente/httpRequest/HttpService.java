package co.com.cliente.httpRequest;

            import java.io.OutputStream;
            import java.net.HttpURLConnection;
            import java.net.URL;
            import java.nio.charset.StandardCharsets;
            import java.util.Scanner;

            public class HttpService {

                private String jwtToken;
                private static HttpService instance;

                private HttpService() {
                    this.jwtToken = null;
                }
                public static HttpService getInstance() {
                    if (instance == null) {
                        instance = new HttpService();
                    }
                    return instance;
                }

                public String getJwtToken() {
                    return jwtToken;
                }

                public void setJwtToken(String jwtToken) {
                    this.jwtToken = jwtToken;
                }

                public String sendGetRequest(String urlString) throws Exception {
                    URL url = new URL(urlString);
                    HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                    connection.setRequestMethod("GET");

                    if (jwtToken != null && !jwtToken.isEmpty()) {
                        connection.setRequestProperty("Authorization", "Bearer " + jwtToken);
                    }

                    int responseCode = connection.getResponseCode();
                    if (responseCode == HttpURLConnection.HTTP_OK) {
                        try (Scanner scanner = new Scanner(connection.getInputStream(), StandardCharsets.UTF_8)) {
                            return scanner.useDelimiter("\\A").next();
                        }
                    } else {
                        throw new Exception("HTTP GET error: " + responseCode);
                    }
                }

                public String sendPostRequest(String urlString, String jsonBody) throws Exception {
                    return sendRequestWithBody("POST", urlString, jsonBody);
                }

                public String sendPutRequest(String urlString, String jsonBody) throws Exception {
                    return sendRequestWithBody("PUT", urlString, jsonBody);
                }

                public void sendDeleteRequest(String urlString) throws Exception {
                    URL url = new URL(urlString);
                    HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                    connection.setRequestMethod("DELETE");

                    if (jwtToken != null && !jwtToken.isEmpty()) {
                        connection.setRequestProperty("Authorization", "Bearer " + jwtToken);
                    }

                    int responseCode = connection.getResponseCode();
                    if (responseCode != HttpURLConnection.HTTP_OK && responseCode != HttpURLConnection.HTTP_NO_CONTENT) {
                        throw new Exception("HTTP DELETE error: " + responseCode);
                    }
                }

                private String sendRequestWithBody(String method, String urlString, String jsonBody) throws Exception {
                    URL url = new URL(urlString);
                    HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                    connection.setRequestMethod(method);
                    connection.setRequestProperty("Content-Type", "application/json");

                    if (jwtToken != null && !jwtToken.isEmpty()) {
                        connection.setRequestProperty("Authorization", "Bearer " + jwtToken);
                    }

                    connection.setDoOutput(true);

                    try (OutputStream os = connection.getOutputStream()) {
                        byte[] input = jsonBody.getBytes(StandardCharsets.UTF_8);
                        os.write(input, 0, input.length);
                    }

                    int responseCode = connection.getResponseCode();
                    if (responseCode == HttpURLConnection.HTTP_OK || responseCode == HttpURLConnection.HTTP_CREATED) {
                        try (Scanner scanner = new Scanner(connection.getInputStream(), StandardCharsets.UTF_8)) {
                            return scanner.useDelimiter("\\A").next();
                        }
                    } else {
                        throw new Exception("HTTP " + method + " error: " + responseCode);
                    }
                }
            }