package co.com.cliente.websocket;

import co.com.cliente.redis.RedisCache;
import co.com.cliente.websocket.VideoWebSocketClient;
import javafx.application.Platform;

import java.io.File;
import java.util.concurrent.CompletableFuture;

public class WebSocketVideoService {

    private VideoWebSocketClient webSocketClient;
    private static final String WEBSOCKET_URL = "ws://192.168.1.18:9000/ws/video";
    public WebSocketVideoService() {
        webSocketClient = new VideoWebSocketClient(WEBSOCKET_URL);
    }

    public CompletableFuture<Boolean> connectToServer() {
        return webSocketClient.connect();
    }

    public void disconnectFromServer() {
        webSocketClient.disconnect();
    }

    public CompletableFuture<Boolean> uploadVideo(File videoFile, String videoName, String duration,
                                                  Long camaraId, Long usuarioId,
                                                  VideoUploadProgressCallback progressCallback) {

        VideoWebSocketClient.VideoUploadCallback callback = new VideoWebSocketClient.VideoUploadCallback() {
            @Override
            public void onUploadStarted() {
                if (progressCallback != null) {
                    progressCallback.onUploadStarted();
                }
            }

            @Override
            public void onProgress(double progress, long sentBytes, long totalBytes) {
                if (progressCallback != null) {
                    progressCallback.onProgress(progress, sentBytes, totalBytes);
                }
            }

            @Override
            public void onUploadComplete(Long videoId) {
                if (progressCallback != null) {
                    progressCallback.onUploadComplete(videoId);
                }
            }

            @Override
            public void onError(String error) {
                if (progressCallback != null) {
                    progressCallback.onError(error);
                }
            }
        };

        return webSocketClient.uploadVideo(videoFile, videoName, duration, camaraId, usuarioId, callback);
    }

    public boolean isConnected() {
        return webSocketClient.isConnected();
    }

    public interface VideoUploadProgressCallback {
        void onUploadStarted();
        void onProgress(double progress, long sentBytes, long totalBytes);
        void onUploadComplete(Long videoId);
        void onError(String error);
    }
}