package co.com.cliente.httpRequest;

            import com.auth0.jwt.JWT;
            import com.auth0.jwt.interfaces.Claim;
            import com.auth0.jwt.interfaces.DecodedJWT;

            import java.io.*;
            import java.net.HttpURLConnection;
            import java.net.URL;
            import java.nio.charset.StandardCharsets;
            import java.nio.file.Files;
            import java.text.SimpleDateFormat;
            import java.util.Date;
            import java.util.Map;
            import java.util.Scanner;
            import java.util.UUID;

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

    public Map<String, Claim> getJwtClaims() throws Exception {
        if (jwtToken == null || jwtToken.isEmpty()) {
            throw new Exception("JWT token is not set");
        }

        DecodedJWT decodedJWT = JWT.decode(jwtToken);
        return decodedJWT.getClaims();
    }

    public String getUserIdFromClaims(){
        try {
            Map<String, Claim> claims = this.getJwtClaims();
            Claim claim = claims.get("userId");;
            String userId = claim.asInt().toString();
            return userId;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
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

    public String uploadVideoFile(String urlString, File videoFile, String nombre, Date fecha, String duracion, Long camaraId, Long usuarioId) throws IOException {

        String boundary = "------------------------" + UUID.randomUUID().toString();

        URL url = new URL(urlString);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("POST");
        connection.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + boundary);
        connection.setDoOutput(true);

        try (OutputStream os = connection.getOutputStream()) {

            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
            String fechaStr = sdf.format(fecha);


            writeFormField(os, "nombre", nombre, boundary);
            writeFormField(os, "fecha", fechaStr, boundary);
            writeFormField(os, "duracion", duracion, boundary);
            writeFormField(os, "camaraId", camaraId.toString(), boundary);
            writeFormField(os, "usuarioId", usuarioId.toString(), boundary);


            writeBinaryFile(os, "video", videoFile, boundary);


            String endBoundary = "\r\n--" + boundary + "--\r\n";
            os.write(endBoundary.getBytes(StandardCharsets.UTF_8));
        }

        int responseCode = connection.getResponseCode();
        if (responseCode == HttpURLConnection.HTTP_OK || responseCode == HttpURLConnection.HTTP_CREATED) {
            BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            StringBuilder response = new StringBuilder();
            String inputLine;

            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }
            in.close();
            return response.toString();
        } else {
            BufferedReader errorReader = new BufferedReader(new InputStreamReader(connection.getErrorStream()));
            StringBuilder errorResponse = new StringBuilder();
            String line;

            while ((line = errorReader.readLine()) != null) {
                errorResponse.append(line);
            }
            errorReader.close();

            throw new IOException("HTTP error code: " + responseCode + " - " + errorResponse.toString());
        }
    }

    public String updateVideoFile(String urlString, Long videoId, File videoFile, String nombre, Date fecha, String duracion) throws IOException {

        String boundary = "------------------------" + UUID.randomUUID().toString();

        URL url = new URL(urlString + "/" + videoId);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("PUT");
        connection.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + boundary);
        connection.setDoOutput(true);

        try (OutputStream os = connection.getOutputStream()) {

            writeFormField(os, "nombre", nombre, boundary);

            if (fecha != null) {
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
                String fechaStr = sdf.format(fecha);
                writeFormField(os, "fecha", fechaStr, boundary);
            }

            if (duracion != null) {
                writeFormField(os, "duracion", duracion, boundary);
            }


            if (videoFile != null) {
                writeBinaryFile(os, "video", videoFile, boundary);
            }


            String endBoundary = "\r\n--" + boundary + "--\r\n";
            os.write(endBoundary.getBytes(StandardCharsets.UTF_8));
        }

        int responseCode = connection.getResponseCode();
        if (responseCode == HttpURLConnection.HTTP_OK) {
            BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            StringBuilder response = new StringBuilder();
            String inputLine;

            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }
            in.close();
            return response.toString();
        } else {
            throw new IOException("HTTP error code: " + responseCode);
        }
    }


    private void writeFormField(OutputStream os, String fieldName, String fieldValue, String boundary) throws IOException {
        String fieldPart = "--" + boundary + "\r\n" +
                "Content-Disposition: form-data; name=\"" + fieldName + "\"\r\n\r\n" +
                fieldValue + "\r\n";
        os.write(fieldPart.getBytes(StandardCharsets.UTF_8));
    }


    private void writeBinaryFile(OutputStream os, String fieldName, File file, String boundary) throws IOException {
        String fileName = file.getName();
        String contentType = determineContentType(fileName);

        String fileHeader = "--" + boundary + "\r\n" +
                "Content-Disposition: form-data; name=\"" + fieldName + "\"; filename=\"" + fileName + "\"\r\n" +
                "Content-Type: " + contentType + "\r\n\r\n";

        os.write(fileHeader.getBytes(StandardCharsets.UTF_8));


        Files.copy(file.toPath(), os);
    }


    private String determineContentType(String fileName) {
        if (fileName.toLowerCase().endsWith(".mp4")) {
            return "video/mp4";
        } else if (fileName.toLowerCase().endsWith(".avi")) {
            return "video/x-msvideo";
        } else if (fileName.toLowerCase().endsWith(".mov")) {
            return "video/quicktime";
        } else if (fileName.toLowerCase().endsWith(".wmv")) {
            return "video/x-ms-wmv";
        } else {
            return "application/octet-stream";
        }
    }
            }