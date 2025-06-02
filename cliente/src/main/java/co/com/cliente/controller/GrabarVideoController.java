package co.com.cliente.controller;

import co.com.cliente.Main;
import co.com.cliente.dto.CamaraDTO;
import co.com.cliente.dto.VideoDTO;
import co.com.cliente.httpRequest.HttpService;
import co.com.cliente.websocket.VideoWebSocketClient;
import co.com.cliente.websocket.WebSocketVideoService;
import co.com.cliente.websocket.WebSocketVideoService.VideoUploadProgressCallback;
import co.com.cliente.ui.UploadProgressDialog;
import co.com.cliente.ui.WebSocketStatusIndicator;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.image.Image;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.VBox;
import javafx.scene.layout.HBox;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;

import org.json.JSONObject;
import org.opencv.core.Core;
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
import java.nio.file.Files;
import java.text.SimpleDateFormat;
import java.util.Base64;
import java.util.Date;
import java.util.ResourceBundle;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

public class GrabarVideoController implements Initializable {

    @FXML
    private AnchorPane cameraView;

    @FXML
    private Button recordBtn;

    @FXML
    private Button snapshotBtn;

    @FXML
    private Label timerLabel;

    @FXML
    private Label statusValue;

    @FXML
    private Label resolutionValue;

    @FXML
    private Label fpsValue;

    @FXML
    private VBox rotateAction;

    @FXML
    private VBox fullscreenAction;

    @FXML
    private VBox stopFeedAction;

    @FXML
    private VBox recordingsAction;

    @FXML
    private VBox activityList;

    // URL de la cámara IP - imagen individual
    private static final String IP_CAMERA_URL = "http://10.86.26.35:8080/shot.jpg";

    private boolean isRecording = false;
    private VideoWriter videoWriter;
    private Mat currentFrame; // Frame actual para grabación y snapshots
    private Image currentJavaFXImage; // Imagen para mostrar en la UI
    private Thread recordingThread;
    private AtomicBoolean stopRecording = new AtomicBoolean(false);
    private AtomicInteger recordingSeconds = new AtomicInteger(0);
    private Thread timerThread;
    private AtomicBoolean stopTimer = new AtomicBoolean(false);

    private ImageView cameraImageView;
    private Thread cameraThread;
    private boolean cameraActive = false;
    private File recordingsDir;

    // Variables para control de FPS
    private Timer frameTimer;
    private static final int TARGET_FPS = 10; // FPS objetivo para captura
    private final Object frameLock = new Object(); // Lock para sincronización de frames
    private final Object videoWriterLock = new Object(); // Lock para VideoWriter

    // Variables para medir duración real
    private long segmentStartTime = 0;
    private long recordingStartTime = 0;

    // Añadimos la referencia a la cámara seleccionada
    private CamaraDTO selectedCamara;
    private static final String API_SAVE_IMAGE_URL = "http://localhost:9000/api/imagenes/save";
    private static final String API_SAVE_VIDEO_URL = "http://localhost:9000/api/video/save";

    // Variables para el control de tiempo de grabación
    private String currentRecordingDuration = "00:00:00";
    private String currentVideoFileName = "";

    // Variables para grabación por segmentos
    private Timer segmentTimer;
    private int currentSegmentNumber = 0;
    private String currentSessionId;
    private boolean enableAutoUpload = true;

    // Variables para manejo de reconexión
    private int reconnectionAttempts = 0;
    private static final int MAX_RECONNECTION_ATTEMPTS = 5;
    private Timer reconnectionTimer;

    // Variables adicionales para WebSocket
    private WebSocketVideoService webSocketService;
    private boolean useWebSocket = true; // Flag para alternar entre HTTP y WebSocket
    private UploadProgressDialog currentUploadDialog;
    private WebSocketStatusIndicator webSocketStatusIndicator;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        try {
            nu.pattern.OpenCV.loadLocally();
            System.out.println("OpenCV cargado correctamente: " + Core.VERSION);
        } catch (UnsatisfiedLinkError e) {
            try {
                System.loadLibrary("opencv_java470");
                System.out.println("OpenCV 4.11.0 cargado correctamente.");
            } catch (UnsatisfiedLinkError e2) {
                System.err.println("Error al cargar OpenCV: " + e2.getMessage());
                showAlert(AlertType.ERROR, "Error OpenCV",
                        "No se pudo cargar la biblioteca OpenCV. Asegúrate de que opencv_java4110.dll esté en la ruta de bibliotecas del sistema.");
            }
        }

        // Inicializar servicio WebSocket
        webSocketService = new WebSocketVideoService();

        // Inicializar indicador de estado WebSocket
        webSocketStatusIndicator = new WebSocketStatusIndicator();

        // Conectar al servidor WebSocket
        connectToWebSocketServer();

        recordingsDir = new File(System.getProperty("user.home") + "/security_camera_recordings");
        if (!recordingsDir.exists()) {
            recordingsDir.mkdirs();
        }
    }

    private void connectToWebSocketServer() {
        if (webSocketStatusIndicator != null) {
            webSocketStatusIndicator.setConnecting();
        }

        Platform.runLater(() -> {
            addRecentActivity("🔌", "Conectando a servidor WebSocket...", "just now");
        });

        webSocketService.connectToServer().thenAccept(connected -> {
            Platform.runLater(() -> {
                if (connected) {
                    if (webSocketStatusIndicator != null) {
                        webSocketStatusIndicator.setConnected();
                    }
                    addRecentActivity("✅", "Conectado a servidor WebSocket", "just now");
                } else {
                    if (webSocketStatusIndicator != null) {
                        webSocketStatusIndicator.setDisconnected();
                    }
                    addRecentActivity("❌", "Error al conectar WebSocket - usando HTTP", "just now");
                    useWebSocket = false; // Fallback a HTTP
                }
            });
        });
    }

    public void setCamara(CamaraDTO camara) {
        this.selectedCamara = camara;
        Main.setActiveVideoController(this);

        Platform.runLater(() -> {
            if (statusValue != null) {
                statusValue.setText("Conectando a cámara IP...");
                statusValue.setStyle("-fx-text-fill: #ff9900;");
            }
            if (resolutionValue != null) {
                resolutionValue.setText(camara.getResolucion());
            }
        });

        initializeIPCamera();
    }

    private void initializeIPCamera() {
        new Thread(() -> {
            try {
                Platform.runLater(() -> {
                    cameraImageView = new ImageView();
                    cameraImageView.setFitWidth(650);
                    cameraImageView.setFitHeight(370);
                    cameraImageView.setPreserveRatio(true);

                    AnchorPane.setTopAnchor(cameraImageView, 10.0);
                    AnchorPane.setLeftAnchor(cameraImageView, 10.0);

                    cameraView.getChildren().clear();
                    cameraView.getChildren().add(cameraImageView);
                });

                // Probar conectividad inicial
                testConnection();

                cameraActive = true;
                reconnectionAttempts = 0;

                Platform.runLater(() -> {
                    statusValue.setText("Conectado a cámara IP");
                    statusValue.setStyle("-fx-text-fill: #009900;");
                    fpsValue.setText(String.valueOf(TARGET_FPS));
                    addRecentActivity("📹", "Conectado a cámara IP: " + IP_CAMERA_URL, "just now");
                });

                // Iniciar captura continua de frames
                startFrameCapture();

            } catch (Exception e) {
                e.printStackTrace();
                Platform.runLater(() -> {
                    statusValue.setText("Error de conexión");
                    statusValue.setStyle("-fx-text-fill: #ff0000;");
                    showAlert(AlertType.ERROR, "Error de Cámara IP",
                            "No se pudo conectar a la cámara IP: " + e.getMessage() +
                                    "\n\nVerifica:\n" +
                                    "1. Que la cámara esté encendida\n" +
                                    "2. Que la dirección IP sea correcta: " + IP_CAMERA_URL + "\n" +
                                    "3. Que no haya firewall bloqueando la conexión\n" +
                                    "4. Que estés en la misma red que la cámara");

                    addRecentActivity("❌", "Error al conectar cámara IP", "just now");
                });

                scheduleReconnection();
            }
        }).start();
    }

    private void testConnection() throws Exception {
        URL url = new URL(IP_CAMERA_URL);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setConnectTimeout(5000);
        connection.setReadTimeout(5000);
        connection.setRequestMethod("GET");

        int responseCode = connection.getResponseCode();
        if (responseCode != HttpURLConnection.HTTP_OK) {
            throw new Exception("HTTP Error: " + responseCode);
        }

        connection.disconnect();
        System.out.println("Conexión a cámara IP verificada exitosamente");
    }

    private void startFrameCapture() {
        frameTimer = new Timer(true);

        // Capturar cada 100ms para 10 FPS más estables
        frameTimer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                if (cameraActive) {
                    captureFrame();
                }
            }
        }, 0, 100); // 100ms = 10 FPS
    }

    private void captureFrame() {
        try {
            URL url = new URL(IP_CAMERA_URL);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setConnectTimeout(3000);
            connection.setReadTimeout(3000);

            try (InputStream inputStream = connection.getInputStream()) {
                byte[] imageData = inputStream.readAllBytes();

                // Crear imagen JavaFX para mostrar
                Image image = new Image(new ByteArrayInputStream(imageData));

                if (!image.isError()) {
                    // Convertir a Mat para OpenCV (grabación)
                    Mat newFrame = new Mat();
                    MatOfByte matOfByte = new MatOfByte(imageData);
                    newFrame = Imgcodecs.imdecode(matOfByte, Imgcodecs.IMREAD_COLOR);

                    if (!newFrame.empty()) {
                        synchronized (frameLock) {
                            // Liberar frame anterior si existe
                            if (currentFrame != null) {
                                currentFrame.release();
                            }
                            currentFrame = newFrame.clone(); // Hacer copia del frame
                            currentJavaFXImage = image;
                        }

                        // Actualizar UI
                        Platform.runLater(() -> {
                            if (cameraImageView != null) {
                                cameraImageView.setImage(image);
                            }
                        });

                        // Si estamos grabando, escribir frame al video
                        if (isRecording) {
                            synchronized (videoWriterLock) {
                                if (videoWriter != null && videoWriter.isOpened()) {
                                    videoWriter.write(newFrame);
                                }
                            }
                        }
                    }
                    newFrame.release(); // Liberar el frame temporal
                }
            }

            connection.disconnect();

        } catch (Exception e) {
            // Reducir logs de error para evitar spam - solo cada 10 errores
            if (System.currentTimeMillis() % 10000 < 1000) {
                System.err.println("Error al capturar frame: " + e.getMessage());
            }

            // Si hay muchos errores consecutivos, intentar reconectar
            if (cameraActive) {
                Platform.runLater(() -> {
                    if (reconnectionAttempts == 0) { // Solo mostrar una vez
                        statusValue.setText("Conexión inestable");
                        statusValue.setStyle("-fx-text-fill: #ff9900;");
                        addRecentActivity("⚠️", "Conexión inestable con cámara IP", "just now");
                    }
                });

                // Si estamos grabando y hay error crítico, detener grabación
                if (isRecording && e.getMessage().contains("timeout")) {
                    Platform.runLater(() -> {
                        stopRecording();
                        showAlert(AlertType.WARNING, "Grabación Interrumpida",
                                "La grabación se detuvo debido a problemas de conectividad con la cámara IP.");
                    });
                }
            }
        }
    }

    private void scheduleReconnection() {
        if (reconnectionAttempts < MAX_RECONNECTION_ATTEMPTS) {
            reconnectionAttempts++;

            Platform.runLater(() -> {
                statusValue.setText("Reintentando conexión... (" + reconnectionAttempts + "/" + MAX_RECONNECTION_ATTEMPTS + ")");
                statusValue.setStyle("-fx-text-fill: #ff9900;");
                addRecentActivity("🔄", "Reintentando conexión automática...", "just now");
            });

            reconnectionTimer = new Timer(true);
            reconnectionTimer.schedule(new TimerTask() {
                @Override
                public void run() {
                    stopFrameCapture();
                    initializeIPCamera();
                }
            }, 3000);
        } else {
            Platform.runLater(() -> {
                statusValue.setText("Sin conexión");
                statusValue.setStyle("-fx-text-fill: #ff0000;");
                addRecentActivity("❌", "Máximo de reintentos alcanzado", "just now");
            });
        }
    }

    private void stopFrameCapture() {
        if (frameTimer != null) {
            frameTimer.cancel();
            frameTimer = null;
        }
    }

    @FXML
    private void handleRecordButtonClick() {
        if (!isRecording) {
            startRecording();
        } else {
            stopRecording();
        }
    }

    @FXML
    private void handleSnapshotButtonClick() {
        takeSnapshot();
    }

    @FXML
    private void handleRotateAction() {
        showNotImplementedMessage("Rotar la vista de la cámara");
    }

    @FXML
    private void handleFullscreenAction() {
        showNotImplementedMessage("Pantalla completa");
    }

    @FXML
    private void handleStopFeedAction() {
        if (cameraActive) {
            cameraActive = false;
            stopFrameCapture();

            if (reconnectionTimer != null) {
                reconnectionTimer.cancel();
                reconnectionTimer = null;
            }

            statusValue.setText("Desconectado");
            statusValue.setStyle("-fx-text-fill: #ff0000;");

            addRecentActivity("🛑", "Cámara IP desconectada manualmente", "just now");
            stopFeedAction.getChildren().get(0).setStyle("-fx-text-fill: #ff4d4d;");
        } else {
            reconnectionAttempts = 0;
            initializeIPCamera();
            stopFeedAction.getChildren().get(0).setStyle("-fx-text-fill: #4285f4;");
        }
    }

    @FXML
    private void handleRecordingsAction() {
        try {
            java.awt.Desktop.getDesktop().open(recordingsDir);
        } catch (Exception e) {
            e.printStackTrace();
            showAlert(AlertType.ERROR, "Error", "No se pudo abrir el directorio de grabaciones: " + e.getMessage());
        }
    }

    private void showNotImplementedMessage(String feature) {
        showAlert(Alert.AlertType.INFORMATION, "Función no implementada",
                "La función '" + feature + "' no está implementada en esta demostración.");
    }

    private void takeSnapshot() {
        try {
            Mat frameToSave;
            synchronized (frameLock) {
                if (!cameraActive || currentFrame == null || currentFrame.empty()) {
                    throw new Exception("No hay imagen disponible para capturar o la cámara IP no está conectada.");
                }

                if (selectedCamara == null) {
                    throw new Exception("No hay cámara seleccionada.");
                }

                frameToSave = currentFrame.clone();
            }

            // Guardar la imagen localmente
            File snapshotsDir = new File(recordingsDir, "snapshots");
            if (!snapshotsDir.exists()) {
                snapshotsDir.mkdirs();
            }

            String timestamp = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss").format(new Date());
            String snapshotFileName = snapshotsDir.getAbsolutePath() + "/snapshot_IP_" + timestamp + ".jpg";

            boolean success = Imgcodecs.imwrite(snapshotFileName, frameToSave);

            if (!success) {
                frameToSave.release();
                throw new Exception("No se pudo guardar la imagen localmente.");
            }

            // Para snapshots, mantener HTTP por simplicidad
            MatOfByte buffer = new MatOfByte();
            Imgcodecs.imencode(".jpg", frameToSave, buffer);
            byte[] imageBytes = buffer.toArray();
            String base64Image = Base64.getEncoder().encodeToString(imageBytes);

            frameToSave.release();

            // Crear el objeto JSON para enviar al servidor
            JSONObject jsonRequest = new JSONObject();
            jsonRequest.put("nombre", "Snapshot_IP_" + timestamp);
            jsonRequest.put("imagen", base64Image);
            jsonRequest.put("resolucion", selectedCamara.getResolucion());

            SimpleDateFormat isoFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
            Date now = new Date();
            jsonRequest.put("fecha", isoFormat.format(now));

            jsonRequest.put("camaraId", selectedCamara.getId());
            String userId = HttpService.getInstance().getUserIdFromClaims();
            if(userId != null){
                jsonRequest.put("usuarioId", Integer.valueOf(userId));
            }

            // Enviar la imagen al servidor en un hilo separado
            new Thread(() -> {
                try {
                    HttpService.getInstance().sendPostRequest(API_SAVE_IMAGE_URL, jsonRequest.toString());

                    Platform.runLater(() -> {
                        addRecentActivity("📷", "Snapshot de cámara IP guardado en servidor", "just now");
                    });
                } catch (Exception e) {
                    e.printStackTrace();
                    Platform.runLater(() -> {
                        showAlert(AlertType.ERROR, "Error al guardar en servidor",
                                "No se pudo guardar la imagen en el servidor: " + e.getMessage());
                    });
                }
            }).start();

            addRecentActivity("📷", "Snapshot tomado de cámara IP", "just now");

            showAlert(Alert.AlertType.INFORMATION, "Captura Realizada",
                    "¡Captura de cámara IP realizada con éxito!\nGuardada localmente en: " + snapshotFileName + "\nY enviada al servidor.");

        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Error de Captura",
                    "No se pudo tomar la captura de la cámara IP: " + e.getMessage());
        }
    }

    private void startRecording() {
        try {
            synchronized (frameLock) {
                if (!cameraActive || currentFrame == null || currentFrame.empty()) {
                    throw new Exception("La cámara IP no está activa o no hay imagen disponible.");
                }
            }

            // Usar dimensiones del frame actual
            int width, height;
            synchronized (frameLock) {
                width = currentFrame.cols();
                height = currentFrame.rows();
            }

            double fps = TARGET_FPS * 0.75; // Usar FPS objetivo

            currentSessionId = UUID.randomUUID().toString();
            currentSegmentNumber = 0;

            // Registrar tiempo de inicio
            recordingStartTime = System.currentTimeMillis();
            segmentStartTime = recordingStartTime;

            createNewVideoSegment(width, height, fps);

            isRecording = true;
            stopRecording.set(false);
            recordingSeconds.set(0);
            currentRecordingDuration = "00:00:00";

            recordBtn.setText("DETENER GRABACIÓN");
            recordBtn.setStyle("-fx-background-color: #ea4335; -fx-text-fill: white;");
            statusValue.setText("Grabando desde cámara IP...");

            startRecordingTimer();

            if (enableAutoUpload) {
                startSegmentTimer();
            }

            addRecentActivity("🔴", "Grabación iniciada desde cámara IP", "just now");
            System.out.println("Grabación de cámara IP iniciada - FPS: " + fps + ", Resolución: " + width + "x" + height);

        } catch (Exception e) {
            e.printStackTrace();
            showAlert(AlertType.ERROR, "Error de Grabación", "No se pudo iniciar la grabación de la cámara IP: " + e.getMessage());
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
                currentVideoFileName = recordingsDir.getAbsolutePath() +
                        "/IP_segment_" + currentSessionId + "_" + String.format("%03d", currentSegmentNumber) +
                        "_" + timestamp + ".mp4";

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
                    currentVideoFileName = recordingsDir.getAbsolutePath() +
                            "/IP_segment_" + currentSessionId + "_" + String.format("%03d", currentSegmentNumber) +
                            "_" + timestamp + ".avi";
                    videoWriter = new VideoWriter(currentVideoFileName, VideoWriter.fourcc('D', 'I', 'V', 'X'),
                            fps, new Size(width, height), true);
                }

                if (!videoWriter.isOpened()) {
                    throw new Exception("No se pudo crear el archivo de video con ningún formato compatible.");
                }
            }

            System.out.println("Nuevo segmento de cámara IP creado: " + currentVideoFileName);

        } catch (Exception e) {
            e.printStackTrace();
            showAlert(AlertType.ERROR, "Error", "No se pudo crear nuevo segmento de video: " + e.getMessage());
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

            // Calcular duración real del segmento
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
                addRecentActivity("📤",
                        "Enviando segmento IP " + (currentSegmentNumber + 1) + " (" + realSegmentDuration + ") al servidor...",
                        "just now");

                sendSegmentToServer(completedSegmentFileName, currentSegmentNumber, realSegmentDuration);
            }

            // Preparar siguiente segmento
            currentSegmentNumber++;
            segmentStartTime = currentTime; // Nuevo tiempo de inicio para el siguiente segmento

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
            double fps = TARGET_FPS;

            createNewVideoSegment(width, height, fps);

        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("Error al procesar segmento de cámara IP: " + e.getMessage());

            Platform.runLater(() -> {
                addRecentActivity("❌",
                        "Error al procesar segmento IP " + (currentSegmentNumber + 1),
                        "just now");
            });
        }
    }

    // Método modificado para enviar segmentos - NÚCLEO DE LA FUNCIONALIDAD WEBSOCKET
    private void sendSegmentToServer(String segmentFileName, int segmentNumber, String realDuration) {
        if (useWebSocket && webSocketService.isConnected()) {
            sendSegmentViaWebSocket(segmentFileName, segmentNumber, realDuration);
        } else {
           // sendSegmentViaHTTP(segmentFileName, segmentNumber, realDuration);
            System.out.println("no se pudo");
        }
    }

    private void sendSegmentViaWebSocket(String segmentFileName, int segmentNumber, String realDuration) {
        new Thread(() -> {
            try {
                File segmentFile = new File(segmentFileName);
                if (!segmentFile.exists()) {
                    System.err.println("El archivo de segmento de cámara IP no existe: " + segmentFileName);
                    return;
                }

                // Comprimir video antes de enviar
                File compressedSegment = compressVideoWithOpenCV(segmentFile);

                String segmentName = "IP_Segmento_" + (segmentNumber + 1) + "_" +
                        new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss").format(new Date());

                // Mostrar dialog de progreso
                Platform.runLater(() -> {
                    if (currentUploadDialog != null) {
                        currentUploadDialog.hide();
                    }
                    try {
                        currentUploadDialog = new UploadProgressDialog(
                                (Stage) cameraView.getScene().getWindow(),
                                compressedSegment.getName()
                        );
                        currentUploadDialog.show();
                    } catch (Exception e) {
                        System.err.println("No se pudo mostrar dialog de progreso: " + e.getMessage());
                    }
                });

                // Callback para manejar el progreso del upload
                VideoUploadProgressCallback callback = new VideoUploadProgressCallback() {
                    @Override
                    public void onUploadStarted() {
                        Platform.runLater(() -> {
                            addRecentActivity("📤",
                                    "Iniciando envío WebSocket segmento IP " + (segmentNumber + 1) + " (" + realDuration + ")",
                                    "just now");
                        });
                    }

                    @Override
                    public void onProgress(double progress, long sentBytes, long totalBytes) {
                        if (currentUploadDialog != null && !currentUploadDialog.isCancelled()) {
                            currentUploadDialog.updateProgress(progress, sentBytes, totalBytes);
                        }
                    }

                    @Override
                    public void onUploadComplete(Long videoId) {
                        Platform.runLater(() -> {
                            if (currentUploadDialog != null) {
                                currentUploadDialog.showSuccess("Video ID: " + videoId);
                            }
                            addRecentActivity("✅",
                                    "Segmento IP " + (segmentNumber + 1) + " (" + realDuration + ") enviado exitosamente via WebSocket",
                                    "just now");
                        });

                        // Limpiar archivo temporal comprimido si es diferente del original
                        if (!compressedSegment.equals(segmentFile)) {
                            compressedSegment.delete();
                        }
                    }

                    @Override
                    public void onError(String error) {
                        Platform.runLater(() -> {
                            if (currentUploadDialog != null) {
                                currentUploadDialog.showError(error);
                            }
                            addRecentActivity("❌",
                                    "Error WebSocket segmento IP " + (segmentNumber + 1) + ": " + error,
                                    "just now");

                            // Fallback a HTTP en caso de error
                            addRecentActivity("🔄", "Reintentando con HTTP...", "just now");
                        });

                        // Reintentar con HTTP
                        sendSegmentViaHTTP(segmentFileName, segmentNumber, realDuration);
                    }
                };

                // Enviar el archivo via WebSocket
                webSocketService.uploadVideo(
                        compressedSegment,
                        segmentName,
                        realDuration,
                        selectedCamara.getId(),
                        selectedCamara.getUsuarioId(),
                        callback
                );

            } catch (Exception e) {
                e.printStackTrace();
                Platform.runLater(() -> {
                    if (currentUploadDialog != null) {
                        currentUploadDialog.showError(e.getMessage());
                    }
                    addRecentActivity("❌",
                            "Error al procesar segmento IP " + (segmentNumber + 1) + " para WebSocket: " + e.getMessage(),
                            "just now");
                });

                // Fallback a HTTP
                sendSegmentViaHTTP(segmentFileName, segmentNumber, realDuration);
            }
        }).start();
    }

    private void sendSegmentViaHTTP(String segmentFileName, int segmentNumber, String realDuration) {
        // Mantener el método HTTP original como fallback
        new Thread(() -> {
            try {
                File segmentFile = new File(segmentFileName);
                if (!segmentFile.exists()) {
                    System.err.println("El archivo de segmento de cámara IP no existe: " + segmentFileName);
                    return;
                }

                File compressedSegment = compressVideoWithOpenCV(segmentFile);

                String segmentName = "IP_Segmento_" + (segmentNumber + 1) + "_" +
                        new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss").format(new Date());

                SimpleDateFormat isoFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
                Date segmentDate = new Date();

                String response = HttpService.getInstance().uploadVideoFile(
                        "http://localhost:9000/api/video/upload",
                        compressedSegment,
                        segmentName,
                        segmentDate,
                        realDuration,
                        selectedCamara.getId(),
                        selectedCamara.getUsuarioId()
                );

                Platform.runLater(() -> {
                    addRecentActivity("✅",
                            "Segmento IP " + (segmentNumber + 1) + " (" + realDuration + ") enviado exitosamente via HTTP",
                            "just now");
                });

            } catch (Exception e) {
                e.printStackTrace();

                Platform.runLater(() -> {
                    addRecentActivity("❌",
                            "Error al enviar segmento IP " + (segmentNumber + 1) + " via HTTP: " + e.getMessage(),
                            "just now");
                });
            }
        }).start();
    }

    // Método para formatear duración de milisegundos a HH:MM:SS
    private String formatDuration(long durationMs) {
        long seconds = durationMs / 1000;
        long hours = seconds / 3600;
        long minutes = (seconds % 3600) / 60;
        seconds = seconds % 60;

        return String.format("%02d:%02d:%02d", hours, minutes, seconds);
    }

    private void startRecordingTimer() {
        final AtomicReference<Integer> seconds = new AtomicReference<>(0);
        final AtomicReference<Integer> minutes = new AtomicReference<>(0);
        final AtomicReference<Integer> hours = new AtomicReference<>(0);

        stopTimer.set(false);

        timerThread = new Thread(() -> {
            try {
                while (!stopTimer.get()) {
                    Thread.sleep(1000);

                    seconds.getAndUpdate(s -> (s + 1) % 60);
                    if (seconds.get() == 0) {
                        minutes.getAndUpdate(m -> (m + 1) % 60);

                        if (minutes.get() == 0) {
                            hours.getAndUpdate(h -> h + 1);
                        }
                    }

                    currentRecordingDuration = String.format("%02d:%02d:%02d", hours.get(), minutes.get(), seconds.get());

                    Platform.runLater(() -> {
                        timerLabel.setText(currentRecordingDuration);
                    });
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        });

        timerThread.setDaemon(true);
        timerThread.start();
    }

    private void stopRecording() {
        try {
            isRecording = false;
            stopRecording.set(true);

            if (segmentTimer != null) {
                segmentTimer.cancel();
                segmentTimer = null;
            }

            if (recordingThread != null) {
                recordingThread.join(2000);
            }

            stopTimer.set(true);
            if (timerThread != null) {
                timerThread.join(1000);
            }

            if (enableAutoUpload && currentVideoFileName != null && !currentVideoFileName.isEmpty()) {
                // Calcular duración del último segmento
                long currentTime = System.currentTimeMillis();
                long lastSegmentDurationMs = currentTime - segmentStartTime;
                String lastSegmentDuration = formatDuration(lastSegmentDurationMs);

                synchronized (videoWriterLock) {
                    if (videoWriter != null && videoWriter.isOpened()) {
                        videoWriter.release();
                    }
                }

                File lastSegmentFile = new File(currentVideoFileName);
                if (lastSegmentFile.exists() && lastSegmentFile.length() > 0) {
                    Platform.runLater(() -> {
                        addRecentActivity("📤",
                                "Enviando último segmento IP (" + lastSegmentDuration + ") al servidor...",
                                "just now");
                    });

                    sendSegmentToServer(currentVideoFileName, currentSegmentNumber, lastSegmentDuration);
                }
            }

            cleanupRecordingResources();

            addRecentActivity("🛑", "Grabación IP detenida - " + (currentSegmentNumber + 1) + " segmentos procesados", "just now");

            recordBtn.setText("Record");
            recordBtn.setStyle("-fx-background-color: #ff4d4d; -fx-text-fill: white; -fx-font-weight: bold;");

            timerLabel.setText("00:00:00");
            statusValue.setText("Conectado a cámara IP");
            statusValue.setStyle("-fx-text-fill: #009900;");

            showAlert(Alert.AlertType.INFORMATION, "Grabación Completada",
                    "Grabación de cámara IP finalizada con " + (currentSegmentNumber + 1) + " segmentos.\n" +
                            "Los segmentos han sido enviados automáticamente al servidor.");

        } catch (InterruptedException e) {
            e.printStackTrace();
            showAlert(AlertType.ERROR, "Error", "Error al detener la grabación: " + e.getMessage());
        }
    }

    public void stopAndSaveRecording() {
        if (isRecording) {
            Platform.runLater(() -> {
                statusValue.setText("Terminando grabación IP por cierre de aplicación...");
            });
            stopRecording();
        }
    }

    private File compressVideoWithOpenCV(File originalVideo) {
        try {
            File compressedDir = new File(recordingsDir, "compressed");
            if (!compressedDir.exists()) {
                compressedDir.mkdirs();
            }

            String timestamp = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss").format(new Date());
            File compressedVideo = new File(compressedDir, "compressed_IP_" + timestamp + ".mp4");

            VideoCapture capture = new VideoCapture(originalVideo.getAbsolutePath());

            int originalWidth = (int) capture.get(Videoio.CAP_PROP_FRAME_WIDTH);
            int originalHeight = (int) capture.get(Videoio.CAP_PROP_FRAME_HEIGHT);
            double originalFps = capture.get(Videoio.CAP_PROP_FPS);

            // Compresión más conservadora - mantener mejor calidad
            int newWidth = Math.max(640, originalWidth * 2 / 3);  // Reducir menos la resolución
            int newHeight = (int) (originalHeight * ((double) newWidth / originalWidth));
            double newFps = Math.max(8.0, Math.min(originalFps, 12.0));  // FPS entre 8-12

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

                // Redimensionar frame sin filtros agresivos
                Imgproc.resize(frame, resizedFrame, new Size(newWidth, newHeight));

                // Sin blur adicional para mantener calidad
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

                System.out.println("Archivo original IP: " + originalVideo.getName() +
                        " - Tamaño: " + originalSize + " KB");
                System.out.println("Archivo comprimido IP: " + compressedVideo.getName() +
                        " - Tamaño: " + compressedSize + " KB");
                System.out.println("Ratio de compresión IP: " + String.format("%.2f", compressionRatio) + "%");

                // Limitar compresión máxima al 50%
                if (compressionRatio > 50) {
                    System.out.println("Compresión excesiva (" + String.format("%.2f", compressionRatio) + "%), usando archivo original.");
                    compressedVideo.delete();
                    return originalVideo;
                }

                // Si la compresión es mínima (menos del 10%), usar original
                if (compressionRatio < 10) {
                    compressedVideo.delete();
                    System.out.println("Compresión mínima, usando archivo original IP.");
                    return originalVideo;
                }

                return compressedVideo;
            } else {
                System.err.println("Fallo en la compresión de video IP. Usando archivo original.");
                return originalVideo;
            }
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("Error al comprimir video IP: " + e.getMessage());
            return originalVideo;
        }
    }

    private void cleanupRecordingResources() {
        synchronized (videoWriterLock) {
            if (videoWriter != null) {
                videoWriter.release();
                videoWriter = null;
            }
        }
    }

    private void addRecentActivity(String icon, String activity, String time) {
        Platform.runLater(() -> {
            HBox activityItem = new HBox(10);

            Label iconLabel = new Label(icon);
            iconLabel.setStyle("-fx-font-size: 16px;");

            VBox details = new VBox(2);

            Label activityLabel = new Label(activity);
            activityLabel.setStyle("-fx-font-size: 12px;");

            Label timeLabel = new Label(time);
            timeLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: gray;");

            details.getChildren().addAll(activityLabel, timeLabel);
            activityItem.getChildren().addAll(iconLabel, details);

            if (activityList.getChildren().size() > 0) {
                activityList.getChildren().add(0, activityItem);
            } else {
                activityList.getChildren().add(activityItem);
            }

            if (activityList.getChildren().size() > 5) {
                activityList.getChildren().remove(5, activityList.getChildren().size());
            }
        });
    }

    private void showAlert(AlertType type, String title, String message) {
        Platform.runLater(() -> {
            Alert alert = new Alert(type);
            alert.setTitle(title);
            alert.setHeaderText(null);
            alert.setContentText(message);
            alert.showAndWait();
        });
    }

    // Método adicional para alternar entre WebSocket y HTTP
    public void toggleWebSocketMode(boolean enabled) {
        useWebSocket = enabled;
        if (enabled && !webSocketService.isConnected()) {
            connectToWebSocketServer();
        }

        Platform.runLater(() -> {
            addRecentActivity("🔧",
                    "Modo de envío cambiado a: " + (enabled ? "WebSocket" : "HTTP"),
                    "just now");
        });
    }

    // Método para verificar estado de conexión
    public boolean isWebSocketConnected() {
        return webSocketService.isConnected();
    }

    public void onClose() {
        if (isRecording) {
            stopRecording();
        }

        if (segmentTimer != null) {
            segmentTimer.cancel();
            segmentTimer = null;
        }

        if (reconnectionTimer != null) {
            reconnectionTimer.cancel();
            reconnectionTimer = null;
        }

        cameraActive = false;
        stopFrameCapture();

        // Desconectar WebSocket
        if (webSocketService != null) {
            webSocketService.disconnectFromServer();
        }

        // Liberar recursos de OpenCV
        synchronized (frameLock) {
            if (currentFrame != null) {
                currentFrame.release();
                currentFrame = null;
            }
        }

        Main.clearActiveVideoController();
    }

    public boolean isRecording() {
        return isRecording;
    }

    public void setAutoUploadEnabled(boolean enabled) {
        this.enableAutoUpload = enabled;
        if (!enabled && segmentTimer != null) {
            segmentTimer.cancel();
            segmentTimer = null;
        }
    }

    public void forceReconnection() {
        if (!cameraActive) {
            reconnectionAttempts = 0;
            initializeIPCamera();
        }
    }

    public boolean isConnected() {
        return cameraActive;
    }

    public String getCameraURL() {
        return IP_CAMERA_URL;
    }

    public WebSocketStatusIndicator getWebSocketStatusIndicator() {
        return webSocketStatusIndicator;
    }
}