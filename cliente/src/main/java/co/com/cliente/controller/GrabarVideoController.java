package co.com.cliente.controller;

import co.com.cliente.Main;
import co.com.cliente.dto.CamaraDTO;
import co.com.cliente.httpRequest.HttpService;
import com.auth0.jwt.interfaces.Claim;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ProgressBar;
import javafx.scene.image.Image;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.VBox;
import javafx.scene.layout.HBox;
import javafx.scene.image.ImageView;

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

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.text.SimpleDateFormat;
import java.util.Base64;
import java.util.Date;
import java.util.Map;
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

    private boolean isRecording = false;
    private VideoCapture capture;
    private VideoWriter videoWriter;
    private Mat frame;
    private Thread recordingThread;
    private AtomicBoolean stopRecording = new AtomicBoolean(false);
    private AtomicInteger recordingSeconds = new AtomicInteger(0);
    private Thread timerThread;
    private AtomicBoolean stopTimer = new AtomicBoolean(false);

    private ImageView cameraImageView;
    private Thread cameraThread;
    private boolean cameraActive = false;
    private File recordingsDir;

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
    private boolean enableAutoUpload = true; // Configuración para habilitar/deshabilitar subida automática

    // Tamaño máximo de fragmento para subir videos (500KB)
    private static final int MAX_CHUNK_SIZE = 500 * 1024;

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

        recordingsDir = new File(System.getProperty("user.home") + "/security_camera_recordings");
        if (!recordingsDir.exists()) {
            recordingsDir.mkdirs();
        }

        // No inicializamos la cámara aquí, se hará cuando se establezca la cámara seleccionada
    }

    // Método para establecer la cámara seleccionada
    public void setCamara(CamaraDTO camara) {
        this.selectedCamara = camara;

        // Registrar este controlador como activo en la aplicación principal
        Main.setActiveVideoController(this);

        // Mostrar información de la cámara seleccionada
        Platform.runLater(() -> {
            if (statusValue != null) {
                statusValue.setText("Seleccionada: " + camara.getDescripcion());
            }
            if (resolutionValue != null) {
                resolutionValue.setText(camara.getResolucion());
            }
        });

        // Inicializar la cámara
        initializeCamera();
    }

    private void initializeCamera() {
        try {
            cameraImageView = new ImageView();
            cameraImageView.setFitWidth(650);
            cameraImageView.setFitHeight(370);
            cameraImageView.setPreserveRatio(true);

            AnchorPane.setTopAnchor(cameraImageView, 10.0);
            AnchorPane.setLeftAnchor(cameraImageView, 10.0);

            cameraView.getChildren().add(cameraImageView);

            capture = new VideoCapture(0);

            if (!capture.isOpened()) {
                throw new Exception("No se pudo abrir la cámara. Verifica que esté conectada y disponible.");
            }

            capture.set(Videoio.CAP_PROP_FRAME_WIDTH, 1280);
            capture.set(Videoio.CAP_PROP_FRAME_HEIGHT, 720);

            double fps = capture.get(Videoio.CAP_PROP_FPS);
            if (fps <= 0) fps = 30.0;
            fpsValue.setText(String.format("%.0f", fps));

            resolutionValue.setText(selectedCamara.getResolucion());

            frame = new Mat();

            cameraActive = true;

            startLiveVideoThread();

        } catch (Exception e) {
            e.printStackTrace();
            statusValue.setText("Error");
            statusValue.setStyle("-fx-text-fill: #ff0000;");
            showAlert(AlertType.ERROR, "Error de Cámara", "No se pudo inicializar la cámara: " + e.getMessage());
        }
    }

    private Image matToImage(Mat frame) {
        try {
            MatOfByte buffer = new MatOfByte();

            Imgcodecs.imencode(".png", frame, buffer);

            byte[] imageData = buffer.toArray();

            return new Image(new ByteArrayInputStream(imageData));
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private void startLiveVideoThread() {
        cameraThread = new Thread(() -> {
            while (cameraActive) {
                try {
                    if (capture.read(frame)) {
                        Image currentFrame = matToJavaFXImage(frame);
                        Platform.runLater(() -> {
                            cameraImageView.setImage(currentFrame);
                        });

                        if (isRecording && videoWriter != null && videoWriter.isOpened()) {
                            videoWriter.write(frame);
                        }
                    } else {
                        System.err.println("Error al leer el frame de la cámara");
                        break;
                    }

                    Thread.sleep(1);
                } catch (InterruptedException e) {
                    break;
                } catch (Exception e) {
                    e.printStackTrace();
                    System.err.println("Error en el hilo de la cámara: " + e.getMessage());
                    break;
                }
            }
        });

        cameraThread.setDaemon(true);
        cameraThread.start();
    }

    private javafx.scene.image.Image matToJavaFXImage(Mat mat) {
        org.opencv.core.MatOfByte buffer = new org.opencv.core.MatOfByte();
        Imgcodecs.imencode(".png", mat, buffer);
        byte[] byteArray = buffer.toArray();
        return new javafx.scene.image.Image(new java.io.ByteArrayInputStream(byteArray));
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
            if (cameraThread != null) cameraThread.interrupt();

            statusValue.setText("Offline");
            statusValue.setStyle("-fx-text-fill: #ff0000;");

            if (capture != null && capture.isOpened()) {
                capture.release();
            }

            stopFeedAction.getChildren().get(0).setStyle("-fx-text-fill: #ff4d4d;");
        } else {
            initializeCamera();

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
            if (!cameraActive || frame == null || frame.empty()) {
                throw new Exception("No hay imagen disponible para capturar.");
            }

            if (selectedCamara == null) {
                throw new Exception("No hay cámara seleccionada.");
            }

            // Guardar la imagen localmente para tener un respaldo
            File snapshotsDir = new File(recordingsDir, "snapshots");
            if (!snapshotsDir.exists()) {
                snapshotsDir.mkdirs();
            }

            String timestamp = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss").format(new Date());
            String snapshotFileName = snapshotsDir.getAbsolutePath() + "/snapshot_" + timestamp + ".jpg";

            boolean success = Imgcodecs.imwrite(snapshotFileName, frame);

            if (!success) {
                throw new Exception("No se pudo guardar la imagen localmente.");
            }

            // Convertir la imagen a base64 para enviarla al servidor
            MatOfByte buffer = new MatOfByte();
            Imgcodecs.imencode(".jpg", frame, buffer);
            byte[] imageBytes = buffer.toArray();
            String base64Image = Base64.getEncoder().encodeToString(imageBytes);

            // Crear el objeto JSON para enviar al servidor
            JSONObject jsonRequest = new JSONObject();
            jsonRequest.put("nombre", "Snapshot_" + timestamp);
            jsonRequest.put("imagen", base64Image);
            jsonRequest.put("resolucion", selectedCamara.getResolucion());

            // Formatear la fecha como ISO 8601 para el API
            SimpleDateFormat isoFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
            Date now = new Date();
            jsonRequest.put("fecha", isoFormat.format(now));

            jsonRequest.put("camaraId", selectedCamara.getId());
            String userId = HttpService.getInstance().getUserIdFromClaims();
            if(userId!= null){
                jsonRequest.put("usuarioId",Integer.valueOf(userId));
            }

            // Enviar la imagen al servidor en un hilo separado
            new Thread(() -> {
                try {
                    HttpService.getInstance().sendPostRequest(API_SAVE_IMAGE_URL, jsonRequest.toString());

                    Platform.runLater(() -> {
                        addRecentActivity("📷", "Snapshot guardado en servidor", "just now");
                    });
                } catch (Exception e) {
                    e.printStackTrace();
                    Platform.runLater(() -> {
                        showAlert(AlertType.ERROR, "Error al guardar en servidor",
                                "No se pudo guardar la imagen en el servidor: " + e.getMessage());
                    });
                }
            }).start();

            addRecentActivity("📷", "Snapshot taken", "just now");

            showAlert(Alert.AlertType.INFORMATION, "Captura Realizada",
                    "¡Captura realizada con éxito!\nGuardada localmente en: " + snapshotFileName + "\nY enviada al servidor.");

            System.out.println("Snapshot guardado: " + snapshotFileName);

        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Error de Captura",
                    "No se pudo tomar la captura: " + e.getMessage());
        }
    }

    private void startRecording() {
        try {
            if (!cameraActive || capture == null || !capture.isOpened()) {
                throw new Exception("La cámara no está activa o disponible.");
            }

            int width = (int) capture.get(Videoio.CAP_PROP_FRAME_WIDTH);
            int height = (int) capture.get(Videoio.CAP_PROP_FRAME_HEIGHT);
            double fps = capture.get(Videoio.CAP_PROP_FPS);

            if (width <= 0 || height <= 0) {
                width = 1280;
                height = 720;
            }

            if (fps <= 0) fps = 30.0;

            // Generar un ID único para esta sesión de grabación
            currentSessionId = UUID.randomUUID().toString();
            currentSegmentNumber = 0;

            // Crear el primer segmento
            createNewVideoSegment(width, height, fps);

            isRecording = true;
            stopRecording.set(false);
            recordingSeconds.set(0);
            currentRecordingDuration = "00:00:00";

            recordBtn.setText("DETENER GRABACIÓN");
            recordBtn.setStyle("-fx-background-color: #ea4335; -fx-text-fill: white;");
            statusValue.setText("Grabando...");

            startRecordingTimer();

            // Iniciar el timer para crear segmentos cada minuto
            if (enableAutoUpload) {
                startSegmentTimer();
            }

            System.out.println("Grabación iniciada con segmentos automáticos cada minuto");

        } catch (Exception e) {
            e.printStackTrace();
            showAlert(AlertType.ERROR, "Error de Grabación", "No se pudo iniciar la grabación: " + e.getMessage());
            isRecording = false;
        }
    }

    // Nuevo método para crear segmentos de video
    private void createNewVideoSegment(int width, int height, double fps) {
        try {
            // Cerrar el segmento anterior si existe
            if (videoWriter != null && videoWriter.isOpened()) {
                videoWriter.release();
            }

            String timestamp = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss").format(new Date());
            currentVideoFileName = recordingsDir.getAbsolutePath() +
                    "/segment_" + currentSessionId + "_" + String.format("%03d", currentSegmentNumber) +
                    "_" + timestamp + ".mp4";

            videoWriter = new VideoWriter(
                    currentVideoFileName,
                    VideoWriter.fourcc('X', 'V', 'I', 'D'),
                    fps,
                    new org.opencv.core.Size(width, height),
                    true
            );

            if (!videoWriter.isOpened()) {
                videoWriter.release();
                videoWriter = new VideoWriter(currentVideoFileName, VideoWriter.fourcc('M', 'J', 'P', 'G'),
                        fps, new org.opencv.core.Size(width, height), true);
            }

            if (!videoWriter.isOpened()) {
                currentVideoFileName = recordingsDir.getAbsolutePath() +
                        "/segment_" + currentSessionId + "_" + String.format("%03d", currentSegmentNumber) +
                        "_" + timestamp + ".avi";
                videoWriter = new VideoWriter(currentVideoFileName, VideoWriter.fourcc('D', 'I', 'V', 'X'),
                        fps, new org.opencv.core.Size(width, height), true);
            }

            if (!videoWriter.isOpened()) {
                throw new Exception("No se pudo crear el archivo de video con ningún formato compatible.");
            }

            System.out.println("Nuevo segmento creado: " + currentVideoFileName);

        } catch (Exception e) {
            e.printStackTrace();
            showAlert(AlertType.ERROR, "Error", "No se pudo crear nuevo segmento de video: " + e.getMessage());
        }
    }

    // Nuevo método para iniciar el timer de segmentos
    private void startSegmentTimer() {
        segmentTimer = new Timer(true); // Timer daemon

        segmentTimer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                if (isRecording) {
                    Platform.runLater(() -> {
                        processCurrentSegment();
                    });
                }
            }
        }, 60000, 60000); // Primer ejecución después de 1 minuto, luego cada minuto
    }

    // Nuevo método para procesar el segmento actual
    private void processCurrentSegment() {
        if (!isRecording) return;

        try {
            // Guardar el nombre del archivo actual
            String completedSegmentFileName = currentVideoFileName;

            // Cerrar el segmento actual
            if (videoWriter != null && videoWriter.isOpened()) {
                videoWriter.release();
            }

            // Verificar que el archivo existe y tiene contenido
            File segmentFile = new File(completedSegmentFileName);
            if (segmentFile.exists() && segmentFile.length() > 0) {

                // Mostrar actividad en la UI
                addRecentActivity("📤",
                        "Enviando segmento " + (currentSegmentNumber + 1) + " al servidor...",
                        "just now");

                // Enviar el segmento completado al servidor en un hilo separado
                sendSegmentToServer(completedSegmentFileName, currentSegmentNumber);
            }

            // Crear el siguiente segmento
            currentSegmentNumber++;
            int width = (int) capture.get(Videoio.CAP_PROP_FRAME_WIDTH);
            int height = (int) capture.get(Videoio.CAP_PROP_FRAME_HEIGHT);
            double fps = capture.get(Videoio.CAP_PROP_FPS);

            if (width <= 0 || height <= 0) {
                width = 1280;
                height = 720;
            }
            if (fps <= 0) fps = 30.0;

            createNewVideoSegment(width, height, fps);

        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("Error al procesar segmento: " + e.getMessage());

            Platform.runLater(() -> {
                addRecentActivity("❌",
                        "Error al procesar segmento " + (currentSegmentNumber + 1),
                        "just now");
            });
        }
    }

    // Nuevo método para enviar segmento al servidor
    private void sendSegmentToServer(String segmentFileName, int segmentNumber) {
        new Thread(() -> {
            try {
                File segmentFile = new File(segmentFileName);
                if (!segmentFile.exists()) {
                    System.err.println("El archivo de segmento no existe: " + segmentFileName);
                    return;
                }

                // Comprimir el segmento
                File compressedSegment = compressVideoWithOpenCV(segmentFile);

                // Crear nombre descriptivo para el segmento
                String segmentName = "Segmento_" + (segmentNumber + 1) + "_" +
                        new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss").format(new Date());

                SimpleDateFormat isoFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
                Date segmentDate = new Date();

                // Calcular duración del segmento (aproximadamente 1 minuto)
                String segmentDuration = "00:01:00";

                // Subir usando HttpService
                String response = HttpService.getInstance().uploadVideoFile(
                        "http://localhost:9000/api/video/upload",
                        compressedSegment,
                        segmentName,
                        segmentDate,
                        segmentDuration,
                        selectedCamara.getId(),
                        selectedCamara.getUsuarioId()
                );

                Platform.runLater(() -> {
                    addRecentActivity("✅",
                            "Segmento " + (segmentNumber + 1) + " enviado exitosamente",
                            "just now");
                });

                // Opcional: Eliminar el archivo local del segmento después de subirlo exitosamente
                // segmentFile.delete();
                // if (!compressedSegment.equals(segmentFile)) {
                //     compressedSegment.delete();
                // }

            } catch (Exception e) {
                e.printStackTrace();

                Platform.runLater(() -> {
                    addRecentActivity("❌",
                            "Error al enviar segmento " + (segmentNumber + 1) + ": " + e.getMessage(),
                            "just now");

                    // Opcional: Mostrar alerta solo para errores críticos
                    // showAlert(AlertType.WARNING, "Error de Subida",
                    //     "No se pudo enviar el segmento al servidor: " + e.getMessage());
                });
            }
        }).start();
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

            // Detener el timer de segmentos
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

            // Procesar el último segmento antes de limpiar recursos
            if (enableAutoUpload && currentVideoFileName != null && !currentVideoFileName.isEmpty()) {
                // Cerrar el último segmento
                if (videoWriter != null && videoWriter.isOpened()) {
                    videoWriter.release();
                }

                // Verificar y enviar el último segmento
                File lastSegmentFile = new File(currentVideoFileName);
                if (lastSegmentFile.exists() && lastSegmentFile.length() > 0) {
                    Platform.runLater(() -> {
                        addRecentActivity("📤",
                                "Enviando último segmento al servidor...",
                                "just now");
                    });

                    sendSegmentToServer(currentVideoFileName, currentSegmentNumber);
                }
            }

            cleanupRecordingResources();

            addRecentActivity("🛑", "Grabación detenida - " + (currentSegmentNumber + 1) + " segmentos procesados", "just now");

            recordBtn.setText("Record");
            recordBtn.setStyle("-fx-background-color: #ff4d4d; -fx-text-fill: white; -fx-font-weight: bold;");

            timerLabel.setText("00:00:00");
            statusValue.setText("Online");

            // Mostrar resumen de la grabación
            showAlert(Alert.AlertType.INFORMATION, "Grabación Completada",
                    "Grabación finalizada con " + (currentSegmentNumber + 1) + " segmentos.\n" +
                            "Los segmentos han sido enviados automáticamente al servidor.");

        } catch (InterruptedException e) {
            e.printStackTrace();
            showAlert(AlertType.ERROR, "Error", "Error al detener la grabación: " + e.getMessage());
        }
    }

    // Método público para que Main.java pueda detener la grabación y guardarla
    public void stopAndSaveRecording() {
        if (isRecording) {
            Platform.runLater(() -> {
                statusValue.setText("Terminando grabación por cierre de aplicación...");
            });
            stopRecording();
        }
    }

    // Requiere dependencias de JavaCV
    private File compressVideoWithOpenCV(File originalVideo) {
        try {
            // Crear directorio temporal para videos comprimidos
            File compressedDir = new File(recordingsDir, "compressed");
            if (!compressedDir.exists()) {
                compressedDir.mkdirs();
            }

            // Nombre del archivo comprimido
            String timestamp = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss").format(new Date());
            File compressedVideo = new File(compressedDir, "compressed_" + timestamp + ".mp4");

            // Cargar el video original
            VideoCapture capture = new VideoCapture(originalVideo.getAbsolutePath());

            // Obtener propiedades del video original
            int originalWidth = (int) capture.get(Videoio.CAP_PROP_FRAME_WIDTH);
            int originalHeight = (int) capture.get(Videoio.CAP_PROP_FRAME_HEIGHT);
            double originalFps = capture.get(Videoio.CAP_PROP_FPS);

            // Configuraciones agresivas de compresión
            int newWidth = 480;  // Reducir resolución más significativamente
            int newHeight = (int) (originalHeight * ((double) newWidth / originalWidth));
            double newFps = Math.min(originalFps, 15.0);  // Reducir FPS

            // Configurar VideoWriter con configuraciones de compresión más agresivas
            VideoWriter writer = new VideoWriter(
                    compressedVideo.getAbsolutePath(),
                    VideoWriter.fourcc('X', '2', '6', '4'),  // H.264
                    newFps,
                    new Size(newWidth, newHeight),
                    true
            );

            // Leer y escribir frames con compresión
            Mat frame = new Mat();
            Mat resizedFrame = new Mat();
            Mat compressedFrame = new Mat();
            int frameCount = 0;
            int skipFrames = Math.max(1, (int)(originalFps / newFps));

            while (capture.read(frame)) {
                // Saltar frames para reducir FPS
                frameCount++;
                if (frameCount % skipFrames != 0) continue;

                // Redimensionar frame
                Imgproc.resize(frame, resizedFrame, new Size(newWidth, newHeight));

                // Aplicar compresión adicional
                Imgproc.GaussianBlur(resizedFrame, compressedFrame, new Size(3, 3), 0);

                writer.write(compressedFrame);
            }

            // Liberar recursos
            capture.release();
            writer.release();
            frame.release();
            resizedFrame.release();
            compressedFrame.release();

            // Verificar si se creó el video
            if (compressedVideo.exists()) {
                // Verificar información de compresión
                long originalSize = originalVideo.length() / 1024;
                long compressedSize = compressedVideo.length() / 1024;
                double compressionRatio = (1 - (double)compressedSize / originalSize) * 100;

                System.out.println("Archivo original: " + originalVideo.getName() +
                        " - Tamaño: " + originalSize + " KB");
                System.out.println("Archivo comprimido: " + compressedVideo.getName() +
                        " - Tamaño: " + compressedSize + " KB");
                System.out.println("Ratio de compresión: " + String.format("%.2f", compressionRatio) + "%");

                // Si la compresión no es significativa, eliminar el archivo comprimido
                if (compressionRatio < 5) {
                    compressedVideo.delete();
                    System.err.println("Compresión insignificante. Usando archivo original.");
                    return originalVideo;
                }

                return compressedVideo;
            } else {
                System.err.println("Fallo en la compresión de video. Usando archivo original.");
                return originalVideo;
            }
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("Error al comprimir video: " + e.getMessage());
            return originalVideo;
        }
    }

    // Método modificado para usar compresión antes de enviar
    private void saveVideoToServer(String duration) {
        if (currentVideoFileName.isEmpty() || !new File(currentVideoFileName).exists()) {
            showAlert(AlertType.ERROR, "Error", "No se encuentra el archivo de video para subir al servidor.");
            return;
        }

        new Thread(() -> {
            try {
                // Preparar el archivo de video
                File videoFile = new File(currentVideoFileName);

                // Comprimir video antes de enviar
                File compressedVideoFile = compressVideoWithOpenCV(videoFile);

                // Notificar al usuario que estamos procesando el video
                Platform.runLater(() -> {
                    statusValue.setText("Procesando video...");
                    addRecentActivity("🔄", "Preparando video para el servidor...", "just now");
                });

                // Crear el nombre del video basado en la fecha
                String videoName = "Video_" + new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss").format(new Date());
                SimpleDateFormat isoFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
                Date now = new Date();

                // Actualizar estado
                Platform.runLater(() -> {
                    statusValue.setText("Enviando video al servidor...");
                    addRecentActivity("🔄", "Enviando video al servidor...", "just now");
                });

                // Usar HttpService para subir el archivo comprimido
                String response = HttpService.getInstance().uploadVideoFile(
                        "http://localhost:9000/api/video/upload",  // URL de subida de video
                        compressedVideoFile,
                        videoName,
                        now,
                        duration,
                        selectedCamara.getId(),
                        selectedCamara.getUsuarioId()
                );

                Platform.runLater(() -> {
                    statusValue.setText("Online");
                    addRecentActivity("🎥", "Video guardado en servidor", "just now");

                    showAlert(Alert.AlertType.INFORMATION, "Grabación Completada",
                            "La grabación ha sido guardada localmente y enviada al servidor con éxito.\n" +
                                    "Archivo local: " + compressedVideoFile.getAbsolutePath());
                });

            } catch (Exception e) {
                e.printStackTrace();
                Platform.runLater(() -> {
                    statusValue.setText("Online");

                    // Mostrar alerta de error
                    showAlert(AlertType.ERROR, "Error al guardar en servidor",
                            "No se pudo guardar el video en el servidor: " + e.getMessage());
                });
            }
        }).start();
    }

    private void cleanupRecordingResources() {
        if (videoWriter != null) {
            videoWriter.release();
            videoWriter = null;
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

    public void onClose() {
        if (isRecording) {
            stopRecording();
        }

        // Detener el timer de segmentos si está activo
        if (segmentTimer != null) {
            segmentTimer.cancel();
            segmentTimer = null;
        }

        cameraActive = false;
        if (cameraThread != null) {
            cameraThread.interrupt();
        }

        if (capture != null && capture.isOpened()) {
            capture.release();
        }

        if (frame != null) {
            frame.release();
        }

        // Eliminar esta instancia como controlador activo
        Main.clearActiveVideoController();
    }

    // Método público para verificar si hay una grabación en curso
    public boolean isRecording() {
        return isRecording;
    }

    // Método opcional para configurar la subida automática
    public void setAutoUploadEnabled(boolean enabled) {
        this.enableAutoUpload = enabled;
        if (!enabled && segmentTimer != null) {
            segmentTimer.cancel();
            segmentTimer = null;
        }
    }
}