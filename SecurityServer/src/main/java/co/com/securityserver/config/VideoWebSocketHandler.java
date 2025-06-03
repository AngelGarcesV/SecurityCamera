package co.com.securityserver.config;

import co.com.securityserver.dto.VideoDTO;
import co.com.securityserver.models.Video;
import co.com.securityserver.service.VideoService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.*;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class VideoWebSocketHandler implements WebSocketHandler {

    @Autowired
    private VideoService videoService;

    private final ObjectMapper objectMapper = new ObjectMapper();

    // Mapa para almacenar sesiones de carga de video en progreso
    private final Map<String, VideoUploadSession> uploadSessions = new ConcurrentHashMap<>();

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        System.out.println("WebSocket connection established: " + session.getId());

        // Enviar mensaje de confirmación de conexión
        TextMessage confirmationMessage = new TextMessage("{\"type\":\"connection_established\",\"sessionId\":\"" + session.getId() + "\"}");
        session.sendMessage(confirmationMessage);
    }

    @Override
    public void handleMessage(WebSocketSession session, WebSocketMessage<?> message) throws Exception {
        if (message instanceof TextMessage) {
            handleTextMessage(session, (TextMessage) message);
        } else if (message instanceof BinaryMessage) {
            handleBinaryMessage(session, (BinaryMessage) message);
        }
    }

    private void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        try {
            Map<String, Object> messageData = objectMapper.readValue(message.getPayload(), Map.class);
            String messageType = (String) messageData.get("type");

            switch (messageType) {
                case "video_upload_start":
                    handleVideoUploadStart(session, messageData);
                    break;
                case "video_upload_complete":
                    handleVideoUploadComplete(session, messageData);
                    break;
                default:
                    System.out.println("Unknown message type: " + messageType);
            }
        } catch (Exception e) {
            e.printStackTrace();
            sendErrorMessage(session, "Error processing message: " + e.getMessage());
        }
    }

    private void handleBinaryMessage(WebSocketSession session, BinaryMessage message) throws Exception {
        String sessionId = session.getId();
        VideoUploadSession uploadSession = uploadSessions.get(sessionId);

        if (uploadSession == null) {
            sendErrorMessage(session, "No upload session found. Start upload first.");
            return;
        }

        try {
            // Escribir chunk de datos al archivo temporal
            ByteBuffer payload = message.getPayload();
            byte[] data = new byte[payload.remaining()];
            payload.get(data);
            System.out.println(data);
            uploadSession.writeChunk(data);
            uploadSession.incrementReceivedBytes(data.length);

            // Enviar progreso
            sendProgressMessage(session, uploadSession);

        } catch (Exception e) {
            e.printStackTrace();
            sendErrorMessage(session, "Error writing video data: " + e.getMessage());
            cleanupUploadSession(sessionId);
        }
    }

    private void handleVideoUploadStart(WebSocketSession session, Map<String, Object> messageData) throws Exception {
        String sessionId = session.getId();

        // Crear nueva sesión de carga
        VideoUploadSession uploadSession = new VideoUploadSession();
        uploadSession.setSessionId(sessionId);
        uploadSession.setFileName((String) messageData.get("fileName"));
        uploadSession.setVideoName((String) messageData.get("videoName"));
        uploadSession.setExpectedSize(((Number) messageData.get("fileSize")).longValue());
        uploadSession.setDuration((String) messageData.get("duration"));
        uploadSession.setCamaraId(((Number) messageData.get("camaraId")).longValue());
        uploadSession.setUsuarioId(((Number) messageData.get("usuarioId")).longValue());

        // Crear archivo temporal
        File tempDir = new File(System.getProperty("java.io.tmpdir"), "websocket_uploads");
        if (!tempDir.exists()) {
            tempDir.mkdirs();
        }

        File tempFile = new File(tempDir, "upload_" + sessionId + "_" + System.currentTimeMillis() + ".tmp");
        uploadSession.setTempFile(tempFile);
        uploadSession.setFileOutputStream(new FileOutputStream(tempFile));

        uploadSessions.put(sessionId, uploadSession);

        // Confirmar inicio de carga
        TextMessage response = new TextMessage("{\"type\":\"upload_started\",\"sessionId\":\"" + sessionId + "\"}");
        session.sendMessage(response);
    }

    private void handleVideoUploadComplete(WebSocketSession session, Map<String, Object> messageData) throws Exception {
        String sessionId = session.getId();
        VideoUploadSession uploadSession = uploadSessions.get(sessionId);

        if (uploadSession == null) {
            sendErrorMessage(session, "No upload session found");
            return;
        }

        try {
            // Cerrar archivo
            uploadSession.getFileOutputStream().close();

            // Verificar integridad del archivo
            if (uploadSession.getTempFile().length() != uploadSession.getExpectedSize()) {
                throw new Exception("File size mismatch. Expected: " + uploadSession.getExpectedSize() +
                        ", Actual: " + uploadSession.getTempFile().length());
            }

            // Crear VideoDTO y guardar en base de datos
            VideoDTO videoDTO = new VideoDTO();
            videoDTO.setNombre(uploadSession.getVideoName());
            videoDTO.setFecha(new Date());
            videoDTO.setDuracion(uploadSession.getDuration());
            videoDTO.setCamaraId(uploadSession.getCamaraId());
            videoDTO.setUsuarioId(uploadSession.getUsuarioId());

            // Usar el servicio para guardar el video
            VideoUploadResult result = saveVideoFromFile(videoDTO, uploadSession.getTempFile());

            // Limpiar archivo temporal
            uploadSession.getTempFile().delete();
            cleanupUploadSession(sessionId);

            // Enviar confirmación de éxito
            String successMessage = String.format(
                    "{\"type\":\"upload_success\",\"videoId\":%d,\"message\":\"Video uploaded successfully\"}",
                    result.getVideoId()
            );
            session.sendMessage(new TextMessage(successMessage));

        } catch (Exception e) {
            e.printStackTrace();
            cleanupUploadSession(sessionId);
            sendErrorMessage(session, "Error saving video: " + e.getMessage());
        }
    }

    private VideoUploadResult saveVideoFromFile(VideoDTO videoDTO, File videoFile) throws Exception {
        // Llamar al método modificado del VideoService
        try {
            // Necesitarás implementar este método en VideoService
             Video savedVideo = videoService.saveVideoFromFile(videoDTO, videoFile);

            VideoUploadResult result = new VideoUploadResult();
            result.setVideoId(savedVideo.getId()); // Temporal - usar ID real del video guardado
            result.setSuccess(true);
            return result;
        } catch (Exception e) {
            throw new Exception("Error saving video to database: " + e.getMessage());
        }
    }

    private void sendProgressMessage(WebSocketSession session, VideoUploadSession uploadSession) throws IOException {
        double progress = (double) uploadSession.getReceivedBytes() / uploadSession.getExpectedSize() * 100;

        // Alternativa más robusta usando ObjectMapper
        Map<String, Object> progressData = new HashMap<>();
        progressData.put("type", "upload_progress");
        progressData.put("progress", Math.round(progress * 100.0) / 100.0);
        progressData.put("receivedBytes", uploadSession.getReceivedBytes());
        progressData.put("totalBytes", uploadSession.getExpectedSize());

        String progressMessage = objectMapper.writeValueAsString(progressData);
        session.sendMessage(new TextMessage(progressMessage));
    }

    private void sendErrorMessage(WebSocketSession session, String error) throws IOException {
        String errorMessage = String.format("{\"type\":\"error\",\"message\":\"%s\"}", error);
        session.sendMessage(new TextMessage(errorMessage));
    }

    private void cleanupUploadSession(String sessionId) {
        VideoUploadSession session = uploadSessions.remove(sessionId);
        if (session != null) {
            try {
                if (session.getFileOutputStream() != null) {
                    session.getFileOutputStream().close();
                }
                if (session.getTempFile() != null && session.getTempFile().exists()) {
                    session.getTempFile().delete();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) throws Exception {
        System.err.println("WebSocket transport error: " + exception.getMessage());
        cleanupUploadSession(session.getId());
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus closeStatus) throws Exception {
        System.out.println("WebSocket connection closed: " + session.getId());
        cleanupUploadSession(session.getId());
    }

    @Override
    public boolean supportsPartialMessages() {
        return true;
    }

    // Clases auxiliares
    private static class VideoUploadSession {
        private String sessionId;
        private String fileName;
        private String videoName;
        private long expectedSize;
        private long receivedBytes = 0;
        private String duration;
        private Long camaraId;
        private Long usuarioId;
        private File tempFile;
        private FileOutputStream fileOutputStream;

        // Getters y setters
        public String getSessionId() { return sessionId; }
        public void setSessionId(String sessionId) { this.sessionId = sessionId; }

        public String getFileName() { return fileName; }
        public void setFileName(String fileName) { this.fileName = fileName; }

        public String getVideoName() { return videoName; }
        public void setVideoName(String videoName) { this.videoName = videoName; }

        public long getExpectedSize() { return expectedSize; }
        public void setExpectedSize(long expectedSize) { this.expectedSize = expectedSize; }

        public long getReceivedBytes() { return receivedBytes; }
        public void incrementReceivedBytes(long bytes) { this.receivedBytes += bytes; }

        public String getDuration() { return duration; }
        public void setDuration(String duration) { this.duration = duration; }

        public Long getCamaraId() { return camaraId; }
        public void setCamaraId(Long camaraId) { this.camaraId = camaraId; }

        public Long getUsuarioId() { return usuarioId; }
        public void setUsuarioId(Long usuarioId) { this.usuarioId = usuarioId; }

        public File getTempFile() { return tempFile; }
        public void setTempFile(File tempFile) { this.tempFile = tempFile; }

        public FileOutputStream getFileOutputStream() { return fileOutputStream; }
        public void setFileOutputStream(FileOutputStream fileOutputStream) { this.fileOutputStream = fileOutputStream; }

        public void writeChunk(byte[] data) throws IOException {
            fileOutputStream.write(data);
            fileOutputStream.flush();
        }
    }

    private static class VideoUploadResult {
        private Long videoId;
        private boolean success;

        public Long getVideoId() { return videoId; }
        public void setVideoId(Long videoId) { this.videoId = videoId; }

        public boolean isSuccess() { return success; }
        public void setSuccess(boolean success) { this.success = success; }
    }
}