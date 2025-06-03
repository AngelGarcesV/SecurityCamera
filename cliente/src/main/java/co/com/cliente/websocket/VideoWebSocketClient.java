package co.com.cliente.websocket;

import com.fasterxml.jackson.databind.ObjectMapper;
import javafx.application.Platform;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URI;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;

public class VideoWebSocketClient {

    private WebSocketClient client;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private boolean isConnected = false;
    private VideoUploadCallback currentCallback;
    private CountDownLatch connectionLatch;

    private static final int CHUNK_SIZE = 64 * 1024;

    public interface VideoUploadCallback {
        void onUploadStarted();
        void onProgress(double progress, long sentBytes, long totalBytes);
        void onUploadComplete(Long videoId);
        void onError(String error);
    }

    public VideoWebSocketClient(String serverUrl) {
        setupWebSocketClient(serverUrl);
    }

    private void setupWebSocketClient(String serverUrl) {
        try {
            URI serverUri = URI.create(serverUrl);

            client = new WebSocketClient(serverUri) {
                @Override
                public void onOpen(ServerHandshake handshake) {
                    System.out.println("WebSocket Connected to: " + serverUri);
                    isConnected = true;
                    if (connectionLatch != null) {
                        connectionLatch.countDown();
                    }
                }

                @Override
                public void onMessage(String message) {
                    handleTextMessage(message);
                }

                @Override
                public void onClose(int code, String reason, boolean remote) {
                    System.out.println("WebSocket Closed: " + reason);
                    isConnected = false;
                }

                @Override
                public void onError(Exception ex) {
                    System.err.println("WebSocket Error: " + ex.getMessage());
                    ex.printStackTrace();
                    isConnected = false;
                    if (currentCallback != null) {
                        Platform.runLater(() -> currentCallback.onError("WebSocket error: " + ex.getMessage()));
                    }
                }
            };

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public CompletableFuture<Boolean> connect() {
        CompletableFuture<Boolean> future = new CompletableFuture<>();
        if (isConnected) {
            future.complete(true);
            return future;
        }
        connectionLatch = new CountDownLatch(1);
        try {
            client.connect();
            new Thread(() -> {
                try {
                    boolean connected = connectionLatch.await(10, TimeUnit.SECONDS);
                    future.complete(connected && isConnected);
                } catch (InterruptedException e) {
                    future.complete(false);
                }
            }).start();
        } catch (Exception e) {
            e.printStackTrace();
            future.complete(false);
        }
        return future;
    }

    public void disconnect() {
        if (client != null && isConnected) {
            client.close();
        }
        isConnected = false;
    }

    public CompletableFuture<Boolean> uploadVideo(File videoFile, String videoName, String duration,
                                                  Long camaraId, Long usuarioId, VideoUploadCallback callback) {
        CompletableFuture<Boolean> future = new CompletableFuture<>();
        this.currentCallback = callback;
        if (!isConnected) {
            callback.onError("WebSocket not connected");
            future.complete(false);
            return future;
        }
        if (!videoFile.exists()) {
            callback.onError("Video file does not exist");
            future.complete(false);
            return future;
        }
        new Thread(() -> {
            try {
                boolean success = performVideoUpload(videoFile, videoName, duration, camaraId, usuarioId);
                future.complete(success);
            } catch (Exception e) {
                e.printStackTrace();
                Platform.runLater(() -> callback.onError("Upload failed: " + e.getMessage()));
                future.complete(false);
            }
        }).start();

        return future;
    }
/*
    private boolean performVideoUpload(File videoFile, String videoName, String duration,
                                       Long camaraId, Long usuarioId) throws Exception {

        Map<String, Object> startMessage = new HashMap<>();
        startMessage.put("type", "video_upload_start");
        startMessage.put("fileName", videoFile.getName());
        startMessage.put("videoName", videoName);
        startMessage.put("fileSize", videoFile.length());
        startMessage.put("duration", duration);
        startMessage.put("camaraId", camaraId);
        startMessage.put("usuarioId", usuarioId);

        String startMessageJson = objectMapper.writeValueAsString(startMessage);
        client.send(startMessageJson);

        Platform.runLater(() -> currentCallback.onUploadStarted());

        Thread.sleep(500);

        try (FileInputStream fis = new FileInputStream(videoFile)) {
            byte[] buffer = new byte[CHUNK_SIZE];
            long totalBytes = videoFile.length();
            long sentBytes = 0;
            int bytesRead;

            while ((bytesRead = fis.read(buffer)) != -1) {
                byte[] chunk = new byte[bytesRead];
                System.arraycopy(buffer, 0, chunk, 0, bytesRead);

                ByteBuffer byteBuffer = ByteBuffer.wrap(chunk);
                client.send(byteBuffer);

                sentBytes += bytesRead;

                final long finalSentBytes = sentBytes;
                final double progress = (double) sentBytes / totalBytes * 100;

                Platform.runLater(() ->
                        currentCallback.onProgress(progress, finalSentBytes, totalBytes)
                );

                Thread.sleep(10);
            }
        }

        Map<String, Object> completeMessage = new HashMap<>();
        completeMessage.put("type", "video_upload_complete");

        String completeMessageJson = objectMapper.writeValueAsString(completeMessage);
        client.send(completeMessageJson);

        return true;
    }
*/
    private boolean performVideoUpload(File videoFile, String videoName, String duration,
                                       Long camaraId, Long usuarioId) throws Exception {

        sendUploadStartMessage(videoFile, videoName, duration, camaraId, usuarioId);
        Platform.runLater(() -> currentCallback.onUploadStarted());
        Thread.sleep(500);
        uploadVideoChunks(videoFile);
        sendUploadCompleteMessage();
        return true;
    }
    private void sendUploadStartMessage(File videoFile, String videoName, String duration,
                                        Long camaraId, Long usuarioId) throws IOException {
        Map<String, Object> startMessage = new HashMap<>();
        startMessage.put("type", "video_upload_start");
        startMessage.put("fileName", videoFile.getName());
        startMessage.put("videoName", videoName);
        startMessage.put("fileSize", videoFile.length());
        startMessage.put("duration", duration);
        startMessage.put("camaraId", camaraId);
        startMessage.put("usuarioId", usuarioId);

        String startMessageJson = objectMapper.writeValueAsString(startMessage);
        client.send(startMessageJson);
    }

    private void uploadVideoChunks(File videoFile) throws IOException, InterruptedException {
        try (FileInputStream fis = new FileInputStream(videoFile)) {
            byte[] buffer = new byte[CHUNK_SIZE];
            long totalBytes = videoFile.length();
            long sentBytes = 0;
            int bytesRead;

            while ((bytesRead = fis.read(buffer)) != -1) {
                byte[] chunk = Arrays.copyOf(buffer, bytesRead);
                client.send(ByteBuffer.wrap(chunk));

                sentBytes += bytesRead;
                double progress = (double) sentBytes / totalBytes * 100;
                long finalSentBytes = sentBytes;

                Platform.runLater(() ->
                        currentCallback.onProgress(progress, finalSentBytes, totalBytes)
                );

                Thread.sleep(10);
            }
        }
    }

    private void sendUploadCompleteMessage() throws IOException {
        Map<String, Object> completeMessage = new HashMap<>();
        completeMessage.put("type", "video_upload_complete");

        String completeMessageJson = objectMapper.writeValueAsString(completeMessage);
        client.send(completeMessageJson);
    }

    private void handleTextMessage(String message) {
        try {
            Map<String, Object> messageData = objectMapper.readValue(message, Map.class);
            String type = (String) messageData.get("type");

            switch (type) {
                case "connection_established":
                    System.out.println("Connection established with session: " + messageData.get("sessionId"));
                    break;

                case "upload_started":
                    System.out.println("Upload started for session: " + messageData.get("sessionId"));
                    break;

                case "upload_progress":
                    break;

                case "upload_success":
                    Long videoId = ((Number) messageData.get("videoId")).longValue();
                    Platform.runLater(() -> {
                        if (currentCallback != null) {
                            currentCallback.onUploadComplete(videoId);
                        }
                    });
                    break;

                case "error":
                    String error = (String) messageData.get("message");
                    Platform.runLater(() -> {
                        if (currentCallback != null) {
                            currentCallback.onError(error);
                        }
                    });
                    break;

                default:
                    System.out.println("Unknown message type: " + type);
            }

        } catch (Exception e) {
            e.printStackTrace();
            Platform.runLater(() -> {
                if (currentCallback != null) {
                    currentCallback.onError("Error processing server response: " + e.getMessage());
                }
            });
        }
    }

    public boolean isConnected() {
        return isConnected;
    }
}