package co.com.cliente.controller;

import co.com.cliente.Main;
import co.com.cliente.dto.CamaraDTO;
import co.com.cliente.httpRequest.HttpService;
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
import org.opencv.imgcodecs.Imgcodecs;
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
import java.util.ResourceBundle;
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

    // Tamaño máximo de fragmento para subir videos (500KB)
    private static final int MAX_CHUNK_SIZE = 500 * 1024;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        try {
            System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
            System.out.println("OpenCV cargado correctamente: " + Core.VERSION);
        } catch (UnsatisfiedLinkError e) {
            try {
                System.loadLibrary("opencv_java4110");
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
            jsonRequest.put("usuarioId", selectedCamara.getUsuarioId());

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

            String timestamp = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss").format(new Date());
            currentVideoFileName = recordingsDir.getAbsolutePath() + "/security_recording_" + timestamp + ".mp4";

            try {
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
                    currentVideoFileName = recordingsDir.getAbsolutePath() + "/security_recording_" + timestamp + ".avi";
                    videoWriter = new VideoWriter(currentVideoFileName, VideoWriter.fourcc('D', 'I', 'V', 'X'),
                            fps, new org.opencv.core.Size(width, height), true);
                }

                if (!videoWriter.isOpened()) {
                    throw new Exception("No se pudo crear el archivo de video con ningún formato compatible.");
                }

                isRecording = true;
                stopRecording.set(false);
                recordingSeconds.set(0);
                currentRecordingDuration = "00:00:00";

                recordBtn.setText("DETENER GRABACIÓN");
                recordBtn.setStyle("-fx-background-color: #ea4335; -fx-text-fill: white;");
                statusValue.setText("Grabando...");

                startRecordingTimer();

                System.out.println("Grabación iniciada: " + currentVideoFileName);

            } catch (Exception e) {
                showAlert(AlertType.ERROR, "Error de Grabación", "No se pudo iniciar la grabación: " + e.getMessage());
                isRecording = false;
            }
        } catch (Exception e) {
            e.printStackTrace();
            showAlert(AlertType.ERROR, "Error de Grabación", "No se pudo iniciar la grabación: " + e.getMessage());
            isRecording = false;
        }
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

            if (recordingThread != null) {
                recordingThread.join(2000);
            }

            stopTimer.set(true);
            if (timerThread != null) {
                timerThread.join(1000);
            }

            cleanupRecordingResources();

            addRecentActivity("🛑", "Recording stopped", "just now");

            recordBtn.setText("Record");
            recordBtn.setStyle("-fx-background-color: #ff4d4d; -fx-text-fill: white; -fx-font-weight: bold;");

            // Guardar la duración final antes de resetear el timer
            final String finalDuration = currentRecordingDuration;

            timerLabel.setText("00:00:00");
            statusValue.setText("Online");

            // Verificar si el video existe y no está vacío
            File videoFile = new File(currentVideoFileName);
            if (videoFile.exists() && videoFile.length() > 0) {
                // Mostrar mensaje de procesamiento
                Platform.runLater(() -> {
                    statusValue.setText("Procesando video...");
                });

                // Guardar localmente y enviar video optimizado al servidor
                saveVideoToServer(finalDuration);
            } else {
                showAlert(AlertType.ERROR, "Error", "No se pudo guardar el video correctamente.");
            }

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

    private void saveVideoToServer(String duration) {
        if (currentVideoFileName.isEmpty() || !new File(currentVideoFileName).exists()) {
            showAlert(AlertType.ERROR, "Error", "No se encuentra el archivo de video para subir al servidor.");
            return;
        }

        new Thread(() -> {
            try {
                // Crear el objeto JSON con metadatos
                String videoName = "Video_" + new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss").format(new Date());
                SimpleDateFormat isoFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
                Date now = new Date();

                // Obtener el archivo de video
                File videoFile = new File(currentVideoFileName);

                // Notificar al usuario que estamos procesando el video
                Platform.runLater(() -> {
                    statusValue.setText("Procesando video...");
                    addRecentActivity("🔄", "Preparando video para el servidor...", "just now");
                });

                // Leer el archivo de video y convertirlo a base64
                byte[] videoBytes = Files.readAllBytes(videoFile.toPath());
                String videoBase64 = Base64.getEncoder().encodeToString(videoBytes);

                // Actualizar estado
                Platform.runLater(() -> {
                    statusValue.setText("Enviando video al servidor...");
                    addRecentActivity("🔄", "Enviando video al servidor...", "just now");
                });

                // Crear el objeto JSON para enviar al servidor
                JSONObject jsonRequest = new JSONObject();
                jsonRequest.put("nombre", videoName);
                jsonRequest.put("video", videoBase64); // Enviamos el video completo en base64
                jsonRequest.put("duracion", duration);
                jsonRequest.put("fecha", isoFormat.format(now));
                jsonRequest.put("camaraId", selectedCamara.getId());
                jsonRequest.put("usuarioId", selectedCamara.getUsuarioId());

                // Enviar al servidor
                String response = HttpService.getInstance().sendPostRequest(API_SAVE_VIDEO_URL, jsonRequest.toString());

                Platform.runLater(() -> {
                    statusValue.setText("Online");
                    addRecentActivity("🎥", "Video guardado en servidor", "just now");

                    showAlert(Alert.AlertType.INFORMATION, "Grabación Completada",
                            "La grabación ha sido guardada localmente y enviada al servidor con éxito.\n" +
                                    "Archivo local: " + currentVideoFileName);
                });

            } catch (OutOfMemoryError e) {
                // Error por tamaño de archivo demasiado grande
                e.printStackTrace();
                Platform.runLater(() -> {
                    statusValue.setText("Online");
                    showAlert(AlertType.ERROR, "Error de memoria",
                            "El archivo de video es demasiado grande para enviarlo directamente.\n" +
                                    "Puedes intentar con una grabación más corta o de menor resolución.\n\n" +
                                    "El video ha sido guardado localmente en: " + currentVideoFileName);
                });
            } catch (Exception e) {
                e.printStackTrace();
                Platform.runLater(() -> {
                    statusValue.setText("Online");
                    showAlert(AlertType.ERROR, "Error al guardar en servidor",
                            "No se pudo guardar el video en el servidor: " + e.getMessage() +
                                    "\n\nEl video ha sido guardado localmente en: " + currentVideoFileName);
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
}