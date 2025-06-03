package co.com.cliente.ui;

import co.com.cliente.dto.CamaraDTO;
import co.com.cliente.httpRequest.HttpService;
import co.com.cliente.httpRequest.PropertiesLoader;
import co.com.cliente.websocket.WebSocketVideoService;
import co.com.cliente.websocket.WebSocketVideoService.VideoUploadProgressCallback;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

import org.json.JSONObject;
import org.opencv.core.Mat;
import org.opencv.core.MatOfByte;
import org.opencv.core.Size;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;
import org.opencv.videoio.VideoCapture;
import org.opencv.videoio.VideoWriter;
import org.opencv.videoio.Videoio;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Base64;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;

public class CameraPanel {
    private final CamaraDTO camara;
    private final VBox panel;
    private final ImageView imageView;
    private final Label statusLabel;
    private Label nameLabel = new Label();
    private Button recordButton = new Button();
    private Button snapshotButton  = new Button();
    private boolean isRunning = false;
    private Timer imageTimer;


    private boolean isRecording = false;
    private VideoWriter videoWriter;
    private Mat currentFrame;
    private String currentVideoFileName;
    private final Object frameLock = new Object();
    private final Object videoWriterLock = new Object();
    private long recordingStartTime;
    private long segmentStartTime;


    private Timer segmentTimer;
    private int currentSegmentNumber = 0;
    private String currentSessionId;
    private boolean enableAutoUpload = true;


    private final ExecutorService executorService;
    private final WebSocketVideoService webSocketService;
    private final File recordingsDir;


    private static final String API_SAVE_IMAGE_URL = PropertiesLoader.getBaseUrl() + "/api/imagenes/save";

    public CameraPanel(CamaraDTO camara, ExecutorService executorService,
                       WebSocketVideoService webSocketService, File recordingsDir) {
        this.camara = camara;
        this.executorService = executorService;
        this.webSocketService = webSocketService;
        this.recordingsDir = recordingsDir;


        panel = new VBox(5);
        panel.setAlignment(Pos.CENTER);
        panel.setStyle("-fx-background-color: white; -fx-border-color: #e0e0e0; -fx-border-width: 1; -fx-border-radius: 5; -fx-background-radius: 5;");
        panel.setPadding(new Insets(10));
        panel.setPrefSize(350, 300);
        panel.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);


        HBox header = createHeader();


        imageView = new ImageView();
        imageView.setFitWidth(330);
        imageView.setFitHeight(200);
        imageView.setPreserveRatio(true);
        imageView.setStyle("-fx-background-color: black;");


        HBox controls = createControls();


        statusLabel = new Label("Conectando...");
        statusLabel.setTextFill(Color.ORANGE);
        statusLabel.setFont(Font.font(10));

        panel.getChildren().addAll(header, imageView, controls, statusLabel);


        VBox.setVgrow(imageView, Priority.ALWAYS);
    }

    private HBox createHeader() {
        HBox header = new HBox(10);
        header.setAlignment(Pos.CENTER_LEFT);

        Circle statusIndicator = new Circle(6);
        statusIndicator.setFill(Color.GREEN);

        nameLabel = new Label(camara.getDescripcion());
        nameLabel.setFont(Font.font(null, FontWeight.BOLD, 12));

        Label ipLabel = new Label(camara.getIp() + ":" + camara.getPuerto());
        ipLabel.setTextFill(Color.GRAY);
        ipLabel.setFont(Font.font(9));

        VBox headerInfo = new VBox(2);
        headerInfo.getChildren().addAll(nameLabel, ipLabel);

        header.getChildren().addAll(statusIndicator, headerInfo);
        return header;
    }

    private HBox createControls() {
        HBox controls = new HBox(5);
        controls.setAlignment(Pos.CENTER);

        recordButton = new Button("🔴");
        recordButton.setPrefSize(30, 25);
        recordButton.setStyle("-fx-background-color: #ff4d4d; -fx-text-fill: white; -fx-font-size: 10px;");
        recordButton.setOnAction(e -> toggleRecording());

        snapshotButton = new Button("📸");
        snapshotButton.setPrefSize(30, 25);
        snapshotButton.setStyle("-fx-background-color: #4285f4; -fx-text-fill: white; -fx-font-size: 10px;");
        snapshotButton.setOnAction(e -> takeSnapshot("manual"));

        controls.getChildren().addAll(recordButton, snapshotButton);
        return controls;
    }

    public void start() {
        isRunning = true;
        imageTimer = new Timer(true);
        imageTimer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                if (isRunning) {
                    refreshImage();
                }
            }
        }, 0, 100);
    }
/*
    public void refreshImage() {
        if (!isRunning) return;

        CompletableFuture.runAsync(() -> {
            try {
                String cameraUrl = "http://" + camara.getIp() + ":" + camara.getPuerto() + "/shot.jpg";
                URL url = new URL(cameraUrl);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setConnectTimeout(5000);
                connection.setReadTimeout(5000);

                try (InputStream inputStream = connection.getInputStream()) {
                    byte[] imageData = inputStream.readAllBytes();
                    Image image = new Image(new ByteArrayInputStream(imageData));

                    if (!image.isError()) {

                        Mat newFrame = new Mat();
                        MatOfByte matOfByte = new MatOfByte(imageData);
                        newFrame = Imgcodecs.imdecode(matOfByte, Imgcodecs.IMREAD_COLOR);

                        if (!newFrame.empty()) {
                            synchronized (frameLock) {

                                if (currentFrame != null) {
                                    currentFrame.release();
                                }
                                currentFrame = newFrame.clone();
                            }


                            if (isRecording) {
                                synchronized (videoWriterLock) {
                                    if (videoWriter != null && videoWriter.isOpened()) {
                                        videoWriter.write(newFrame);
                                    }
                                }
                            }
                        }
                        newFrame.release();

                        Platform.runLater(() -> {
                            imageView.setImage(image);
                            statusLabel.setText("Conectado - " + camara.getResolucion() +
                                    (isRecording ? " 🔴 GRABANDO" : ""));
                            statusLabel.setTextFill(isRecording ? Color.RED : Color.GREEN);
                        });
                    } else {
                        Platform.runLater(() -> {
                            statusLabel.setText("Error en la imagen");
                            statusLabel.setTextFill(Color.RED);
                        });
                    }
                }
                connection.disconnect();

            } catch (Exception e) {
                Platform.runLater(() -> {
                    statusLabel.setText("Sin conexión");
                    statusLabel.setTextFill(Color.RED);
                });
            }
        }, executorService);
    }
*/
public void refreshImage() {
    if (!isRunning) return;

    CompletableFuture.runAsync(() -> {
        try {
            byte[] imageData = getImagefromCamera();
            Image image = createFXImage(imageData);

            if (!image.isError()) {
                Mat newFrame = convertToMat(imageData);
                if (!newFrame.empty()) {
                    updateCurrentFrame(newFrame);
                    writeFrameIfRecording(newFrame);
                    releaseFrame(newFrame);
                    updateUIWithImage(image);
                } else {
                    showImageError();
                }
            } else {
                showImageError();
            }
        } catch (Exception e) {
            showConnectionError();
        }
    }, executorService);
}

    private byte[] getImagefromCamera() throws IOException {
        String cameraUrl = "http://" + camara.getIp() + ":" + camara.getPuerto() + "/shot.jpg";
        URL url = new URL(cameraUrl);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setConnectTimeout(5000);
        connection.setReadTimeout(5000);

        try (InputStream inputStream = connection.getInputStream()) {
            return inputStream.readAllBytes();
        } finally {
            connection.disconnect();
        }
    }

    private Image createFXImage(byte[] imageData) {
        return new Image(new ByteArrayInputStream(imageData));
    }

    private Mat convertToMat(byte[] imageData) {
        MatOfByte matOfByte = new MatOfByte(imageData);
        return Imgcodecs.imdecode(matOfByte, Imgcodecs.IMREAD_COLOR);
    }

    private void updateCurrentFrame(Mat newFrame) {
        synchronized (frameLock) {
            if (currentFrame != null) {
                currentFrame.release();
            }
            currentFrame = newFrame.clone();
        }
    }
    private void writeFrameIfRecording(Mat frame) {
        if (isRecording) {
            synchronized (videoWriterLock) {
                if (videoWriter != null && videoWriter.isOpened()) {
                    videoWriter.write(frame);
                }
            }
        }
    }

    private void releaseFrame(Mat frame) {
        frame.release();
    }

    private void updateUIWithImage(Image image) {
        Platform.runLater(() -> {
            imageView.setImage(image);
            statusLabel.setText("Conectado - " + camara.getResolucion() +
                    (isRecording ? " 🔴 GRABANDO" : ""));
            statusLabel.setTextFill(isRecording ? Color.RED : Color.GREEN);
        });
    }

    private void showImageError() {
        Platform.runLater(() -> {
            statusLabel.setText("Error en la imagen");
            statusLabel.setTextFill(Color.RED);
        });
    }

    private void showConnectionError() {
        Platform.runLater(() -> {
            statusLabel.setText("Sin conexión");
            statusLabel.setTextFill(Color.RED);
        });
    }

    private void toggleRecording() {
        if (!isRecording) {
            startRecording(UUID.randomUUID().toString());
        } else {
            stopRecording();
        }
    }

    public void startRecording(String sessionId) {
        if (isRecording) return;

        try {

            synchronized (frameLock) {
                if (currentFrame == null || currentFrame.empty()) {
                    throw new Exception("No hay imagen disponible para grabar de " + camara.getDescripcion());
                }
            }

            this.currentSessionId = sessionId;
            this.currentSegmentNumber = 0;
            recordingStartTime = System.currentTimeMillis();
            segmentStartTime = recordingStartTime;


            int width, height;
            synchronized (frameLock) {
                width = currentFrame.cols();
                height = currentFrame.rows();
            }

            double fps = 10;

            createNewVideoSegment(width, height, fps);

            isRecording = true;

            Platform.runLater(() -> {
                recordButton.setText("⏹");
                recordButton.setStyle("-fx-background-color: #ea4335; -fx-text-fill: white; -fx-font-size: 10px;");
                snapshotButton.setDisable(false);
            });


            if (enableAutoUpload) {
                startSegmentTimer();
            }

            System.out.println("🔴 Iniciada grabación de " + camara.getDescripcion() +
                    " - FPS: " + fps + ", Resolución: " + width + "x" + height);
            System.out.println("Nuevo segmento de " + camara.getDescripcion() + " creado: " + currentVideoFileName);

        } catch (Exception e) {
            e.printStackTrace();
            Platform.runLater(() -> {
                statusLabel.setText("Error al iniciar grabación: " + e.getMessage());
                statusLabel.setTextFill(Color.RED);
            });
            isRecording = false;
        }
    }

    private void createNewVideoSegment(int width, int height, double fps) {
        try {
            synchronized (videoWriterLock) {
                if (videoWriter != null && videoWriter.isOpened()) {
                    videoWriter.release();
                }

                String timestamp = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss").format(new Date());
                String cameraNameSafe = camara.getDescripcion().replaceAll("[^a-zA-Z0-9]", "_");
                currentVideoFileName = recordingsDir.getAbsolutePath() + "/" + cameraNameSafe + "_segment_" +
                        currentSessionId + "_" + String.format("%03d", currentSegmentNumber) + "_" + timestamp + ".mp4";

                videoWriter = new VideoWriter(
                        currentVideoFileName,
                        VideoWriter.fourcc('X', 'V', 'I', 'D'),
                        fps,
                        new Size(width, height),
                        true
                );

                if (!videoWriter.isOpened()) {
                    videoWriter.release();
                    videoWriter = new VideoWriter(currentVideoFileName, VideoWriter.fourcc('M', 'J', 'P', 'G'),
                            fps, new Size(width, height), true);
                }

                if (!videoWriter.isOpened()) {
                    currentVideoFileName = recordingsDir.getAbsolutePath() + "/" + cameraNameSafe + "_segment_" +
                            currentSessionId + "_" + String.format("%03d", currentSegmentNumber) + "_" + timestamp + ".avi";
                    videoWriter = new VideoWriter(currentVideoFileName, VideoWriter.fourcc('D', 'I', 'V', 'X'),
                            fps, new Size(width, height), true);
                }

                if (!videoWriter.isOpened()) {
                    throw new Exception("No se pudo crear el archivo de video con ningún formato compatible para " + camara.getDescripcion());
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Error al crear segmento de video: " + e.getMessage());
        }
    }

    private void startSegmentTimer() {
        segmentTimer = new Timer(true);
        segmentTimer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                if (isRecording) {
                    Platform.runLater(() -> {
                        processCurrentSegment();
                    });
                }
            }
        }, 60000, 60000);
    }

    private void processCurrentSegment() {
        if (!isRecording) return;

        try {
            String completedSegmentFileName = currentVideoFileName;


            long currentTime = System.currentTimeMillis();
            long segmentDurationMs = currentTime - segmentStartTime;
            String realSegmentDuration = formatDuration(segmentDurationMs);

            synchronized (videoWriterLock) {
                if (videoWriter != null && videoWriter.isOpened()) {
                    videoWriter.release();
                }
            }

            File segmentFile = new File(completedSegmentFileName);
            if (segmentFile.exists() && segmentFile.length() > 0) {
                System.out.println("📤 Enviando segmento " + (currentSegmentNumber + 1) +
                        " de " + camara.getDescripcion() + " (" + realSegmentDuration + ") al servidor...");

                sendSegmentToServer(completedSegmentFileName, currentSegmentNumber, realSegmentDuration);
            }


            currentSegmentNumber++;
            segmentStartTime = currentTime;


            int width, height;
            synchronized (frameLock) {
                if (currentFrame != null && !currentFrame.empty()) {
                    width = currentFrame.cols();
                    height = currentFrame.rows();
                } else {
                    width = 640;
                    height = 480;
                }
            }
            double fps = 7.5;

            createNewVideoSegment(width, height, fps);
            System.out.println("Nuevo segmento de " + camara.getDescripcion() + " creado: " + currentVideoFileName);

        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("Error al procesar segmento de " + camara.getDescripcion() + ": " + e.getMessage());

            Platform.runLater(() -> {
                statusLabel.setText("Error al procesar segmento " + (currentSegmentNumber + 1));
                statusLabel.setTextFill(Color.RED);
            });
        }
    }

    private void sendSegmentToServer(String segmentFileName, int segmentNumber, String realDuration) {
        CompletableFuture.runAsync(() -> {
            try {
                File segmentFile = new File(segmentFileName);
                if (!segmentFile.exists()) {
                    System.err.println("El archivo de segmento de " + camara.getDescripcion() + " no existe: " + segmentFileName);
                    return;
                }


                File compressedSegment = compressVideo(segmentFile);

                String segmentName = camara.getDescripcion().replaceAll("[^a-zA-Z0-9]", "_") + "_Segmento_" + (segmentNumber + 1) + "_" +
                        new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss").format(new Date());

                Platform.runLater(() -> {
                    statusLabel.setText("Enviando segmento " + (segmentNumber + 1) + "...");
                    statusLabel.setTextFill(Color.ORANGE);
                });

                if (webSocketService.isConnected()) {

                    webSocketService.uploadVideo(
                            compressedSegment,
                            segmentName,
                            realDuration,
                            camara.getId(),
                            camara.getUsuarioId(),
                            new VideoUploadProgressCallback() {
                                @Override
                                public void onUploadStarted() {
                                    Platform.runLater(() -> {
                                        statusLabel.setText("Upload iniciado seg." + (segmentNumber + 1));
                                        statusLabel.setTextFill(Color.ORANGE);
                                    });
                                }

                                @Override
                                public void onProgress(double progress, long sentBytes, long totalBytes) {
                                    Platform.runLater(() -> {
                                        statusLabel.setText(String.format("Seg.%d %.0f%%", (segmentNumber + 1), progress * 100));
                                    });
                                }

                                @Override
                                public void onUploadComplete(Long videoId) {
                                    Platform.runLater(() -> {
                                        statusLabel.setText("Seg." + (segmentNumber + 1) + " enviado ✅");
                                        statusLabel.setTextFill(Color.GREEN);
                                    });

                                    System.out.println("✅ Segmento " + (segmentNumber + 1) + " de " + camara.getDescripcion() +
                                            " (" + realDuration + ") enviado exitosamente via WebSocket");


                                    if (!compressedSegment.equals(segmentFile)) {
                                        compressedSegment.delete();
                                    }
                                }

                                @Override
                                public void onError(String error) {
                                    Platform.runLater(() -> {
                                        statusLabel.setText("Error seg." + (segmentNumber + 1));
                                        statusLabel.setTextFill(Color.RED);
                                    });

                                    System.err.println("❌ Error WebSocket segmento " + (segmentNumber + 1) +
                                            " de " + camara.getDescripcion() + ": " + error);


                                    sendVideoViaHTTP(compressedSegment, segmentName, realDuration);
                                }
                            }
                    );
                } else {

                    sendVideoViaHTTP(compressedSegment, segmentName, realDuration);
                }

            } catch (Exception e) {
                e.printStackTrace();
                Platform.runLater(() -> {
                    statusLabel.setText("Error al procesar segmento");
                    statusLabel.setTextFill(Color.RED);
                });
            }
        }, executorService);
    }

    public void stopRecording() {
        if (!isRecording) return;

        isRecording = false;


        if (segmentTimer != null) {
            segmentTimer.cancel();
            segmentTimer = null;
        }


        if (enableAutoUpload && currentVideoFileName != null && !currentVideoFileName.isEmpty()) {

            long currentTime = System.currentTimeMillis();
            long lastSegmentDurationMs = currentTime - segmentStartTime;
            String lastSegmentDuration = formatDuration(lastSegmentDurationMs);

            synchronized (videoWriterLock) {
                if (videoWriter != null && videoWriter.isOpened()) {
                    videoWriter.release();
                    videoWriter = null;
                }
            }

            File lastSegmentFile = new File(currentVideoFileName);
            if (lastSegmentFile.exists() && lastSegmentFile.length() > 0) {
                System.out.println("📤 Enviando último segmento de " + camara.getDescripcion() +
                        " (" + lastSegmentDuration + ") al servidor...");
                sendSegmentToServer(currentVideoFileName, currentSegmentNumber, lastSegmentDuration);
            }
        } else {

            synchronized (videoWriterLock) {
                if (videoWriter != null && videoWriter.isOpened()) {
                    videoWriter.release();
                    videoWriter = null;
                }
            }
        }

        synchronized (frameLock) {
            if (currentFrame != null) {
                currentFrame.release();
                currentFrame = null;
            }
        }

        Platform.runLater(() -> {
            recordButton.setText("🔴");
            recordButton.setStyle("-fx-background-color: #ff4d4d; -fx-text-fill: white; -fx-font-size: 10px;");
            snapshotButton.setDisable(false);
        });

        System.out.println("🛑 Detenida grabación de " + camara.getDescripcion() +
                " - " + (currentSegmentNumber + 1) + " segmentos procesados");
    }

    private void sendVideoViaHTTP(File videoFile, String videoName, String duration) {
        try {
            SimpleDateFormat isoFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
            Date videoDate = new Date();

            String response = HttpService.getInstance().uploadVideoFile(
                    PropertiesLoader.getBaseUrl() + "/api/video/upload",
                    videoFile,
                    videoName,
                    videoDate,
                    duration,
                    camara.getId(),
                    camara.getUsuarioId()
            );

            Platform.runLater(() -> {
                statusLabel.setText("Video enviado ✅ (HTTP)");
                statusLabel.setTextFill(Color.GREEN);
            });

        } catch (Exception e) {
            e.printStackTrace();
            Platform.runLater(() -> {
                statusLabel.setText("Error HTTP");
                statusLabel.setTextFill(Color.RED);
            });
        }
    }

    public boolean takeSnapshot(String batchId) {
        try {

            String cameraUrl = "http://" + camara.getIp() + ":" + camara.getPuerto() + "/shot.jpg";
            URL url = new URL(cameraUrl);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setConnectTimeout(5000);
            connection.setReadTimeout(5000);

            byte[] imageData;
            try (InputStream inputStream = connection.getInputStream()) {
                imageData = inputStream.readAllBytes();
            }
            connection.disconnect();

            if (imageData.length == 0) {
                throw new Exception("No se pudo capturar imagen");
            }


            File snapshotsDir = new File(recordingsDir, "snapshots");
            if (!snapshotsDir.exists()) {
                snapshotsDir.mkdirs();
            }

            String timestamp = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss").format(new Date());
            String snapshotFileName = snapshotsDir.getAbsolutePath() + "/snapshot_" +
                    camara.getDescripcion().replaceAll("[^a-zA-Z0-9]", "_") + "_" + batchId + "_" + timestamp + ".jpg";

            try (FileOutputStream fos = new FileOutputStream(snapshotFileName)) {
                fos.write(imageData);
            }


            sendSnapshotToServer(imageData, timestamp);

            System.out.println("📸 Snapshot de " + camara.getDescripcion() + " guardado: " + snapshotFileName);
            return true;

        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("Error al tomar snapshot de " + camara.getDescripcion() + ": " + e.getMessage());
            return false;
        }
    }

    private void sendSnapshotToServer(byte[] imageData, String timestamp) {
        CompletableFuture.runAsync(() -> {
            try {
                String base64Image = Base64.getEncoder().encodeToString(imageData);

                JSONObject jsonRequest = new JSONObject();
                jsonRequest.put("nombre", "Snapshot_" + camara.getDescripcion() + "_" + timestamp);
                jsonRequest.put("imagen", base64Image);
                jsonRequest.put("resolucion", camara.getResolucion());

                SimpleDateFormat isoFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
                Date now = new Date();
                jsonRequest.put("fecha", isoFormat.format(now));

                jsonRequest.put("camaraId", camara.getId());
                String userId = HttpService.getInstance().getUserIdFromClaims();
                if (userId != null) {
                    jsonRequest.put("usuarioId", Integer.valueOf(userId));
                }

                HttpService.getInstance().sendPostRequest(API_SAVE_IMAGE_URL, jsonRequest.toString());

                Platform.runLater(() -> {

                    statusLabel.setText("Snapshot enviado ✅");
                    Timer resetTimer = new Timer(true);
                    resetTimer.schedule(new TimerTask() {
                        @Override
                        public void run() {
                            Platform.runLater(() -> {
                                if (!isRecording) {
                                    statusLabel.setText("Conectado - " + camara.getResolucion());
                                    statusLabel.setTextFill(Color.GREEN);
                                }
                            });
                        }
                    }, 2000);
                });

            } catch (Exception e) {
                e.printStackTrace();
                Platform.runLater(() -> {
                    statusLabel.setText("Error al enviar snapshot");
                    statusLabel.setTextFill(Color.RED);
                });
            }
        }, executorService);
    }

    private File compressVideo(File originalVideo) {
        try {
            File compressedDir = new File(recordingsDir, "compressed");
            if (!compressedDir.exists()) {
                compressedDir.mkdirs();
            }

            String timestamp = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss").format(new Date());
            String cameraNameSafe = camara.getDescripcion().replaceAll("[^a-zA-Z0-9]", "_");
            File compressedVideo = new File(compressedDir, "compressed_" + cameraNameSafe + "_" + timestamp + ".mp4");

            VideoCapture capture = new VideoCapture(originalVideo.getAbsolutePath());

            int originalWidth = (int) capture.get(Videoio.CAP_PROP_FRAME_WIDTH);
            int originalHeight = (int) capture.get(Videoio.CAP_PROP_FRAME_HEIGHT);
            double originalFps = capture.get(Videoio.CAP_PROP_FPS);


            int newWidth = Math.max(480, originalWidth * 2 / 3);
            int newHeight = (int) (originalHeight * ((double) newWidth / originalWidth));
            double newFps = Math.max(6.0, Math.min(originalFps, 10.0));

            VideoWriter writer = new VideoWriter(
                    compressedVideo.getAbsolutePath(),
                    VideoWriter.fourcc('X', '2', '6', '4'),
                    newFps,
                    new Size(newWidth, newHeight),
                    true
            );

            Mat frame = new Mat();
            Mat resizedFrame = new Mat();
            int frameCount = 0;
            int skipFrames = Math.max(1, (int)(originalFps / newFps));

            while (capture.read(frame)) {
                frameCount++;
                if (frameCount % skipFrames != 0) continue;

                Imgproc.resize(frame, resizedFrame, new Size(newWidth, newHeight));
                writer.write(resizedFrame);
            }

            capture.release();
            writer.release();
            frame.release();
            resizedFrame.release();

            if (compressedVideo.exists()) {
                long originalSize = originalVideo.length() / 1024;
                long compressedSize = compressedVideo.length() / 1024;
                double compressionRatio = (1 - (double)compressedSize / originalSize) * 100;


                if (compressionRatio < 10) {
                    compressedVideo.delete();
                    return originalVideo;
                }

                return compressedVideo;
            } else {
                return originalVideo;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return originalVideo;
        }
    }

    private String formatDuration(long durationMs) {
        long seconds = durationMs / 1000;
        long hours = seconds / 3600;
        long minutes = (seconds % 3600) / 60;
        seconds = seconds % 60;
        return String.format("%02d:%02d:%02d", hours, minutes, seconds);
    }

    public void stop() {
        isRunning = false;

        if (isRecording) {
            stopRecording();
        }

        if (segmentTimer != null) {
            segmentTimer.cancel();
            segmentTimer = null;
        }

        if (imageTimer != null) {
            imageTimer.cancel();
        }

        synchronized (frameLock) {
            if (currentFrame != null) {
                currentFrame.release();
                currentFrame = null;
            }
        }
    }


    public VBox getPanel() {
        return panel;
    }

    public CamaraDTO getCamara() {
        return camara;
    }

    public boolean isRecording() {
        return isRecording;
    }
}