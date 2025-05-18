package co.com.cliente.controller;

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

import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.MatOfByte;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.videoio.VideoCapture;
import org.opencv.videoio.VideoWriter;
import org.opencv.videoio.Videoio;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.net.URL;
import java.text.SimpleDateFormat;
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

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        try {
            // Cargar biblioteca OpenCV
            System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
            System.out.println("OpenCV cargado correctamente: " + Core.VERSION);
        } catch (UnsatisfiedLinkError e) {
            try {
                // Si falla, intenta cargar específicamente opencv_java4110.dll
                System.loadLibrary("opencv_java4110");
                System.out.println("OpenCV 4.11.0 cargado correctamente.");
            } catch (UnsatisfiedLinkError e2) {
                System.err.println("Error al cargar OpenCV: " + e2.getMessage());
                showAlert(AlertType.ERROR, "Error OpenCV",
                        "No se pudo cargar la biblioteca OpenCV. Asegúrate de que opencv_java4110.dll esté en la ruta de bibliotecas del sistema.");
            }
        }

        // Crear directorio para grabaciones
        recordingsDir = new File(System.getProperty("user.home") + "/security_camera_recordings");
        if (!recordingsDir.exists()) {
            recordingsDir.mkdirs();
        }

        // Inicializar la cámara
        initializeCamera();

        // Añadir algunas actividades de ejemplo
        addRecentActivity("🎥", "Recording started", "15 minutes ago");
        addRecentActivity("📷", "Snapshot taken", "2 minutes ago");
    }

    private void initializeCamera() {
        try {
            // Crear el ImageView para mostrar la vista de cámara
            cameraImageView = new ImageView();
            cameraImageView.setFitWidth(650);
            cameraImageView.setFitHeight(370); // Reducimos altura para dejar espacio a botones
            cameraImageView.setPreserveRatio(true);

            // Posicionar el ImageView
            AnchorPane.setTopAnchor(cameraImageView, 10.0);
            AnchorPane.setLeftAnchor(cameraImageView, 10.0);

            cameraView.getChildren().add(cameraImageView);

            // Inicializar la captura de video
            capture = new VideoCapture(0);

            if (!capture.isOpened()) {
                throw new Exception("No se pudo abrir la cámara. Verifica que esté conectada y disponible.");
            }

            // Configurar resolución de la cámara
            capture.set(Videoio.CAP_PROP_FRAME_WIDTH, 1280);
            capture.set(Videoio.CAP_PROP_FRAME_HEIGHT, 720);

            // Obtener y mostrar FPS
            double fps = capture.get(Videoio.CAP_PROP_FPS);
            if (fps <= 0) fps = 30.0;
            fpsValue.setText(String.format("%.0f", fps));

            // Actualizar resolución en la UI
            resolutionValue.setText("1280x720");

            // Inicializar el frame para capturar imágenes
            frame = new Mat();

            // Marcar la cámara como activa
            cameraActive = true;

            // Iniciar hilo para mostrar video en vivo
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
            // Crear un buffer para almacenar la imagen codificada
            MatOfByte buffer = new MatOfByte();

            // Codificar el frame en formato PNG
            Imgcodecs.imencode(".png", frame, buffer);

            // Convertir el buffer a un array de bytes
            byte[] imageData = buffer.toArray();

            // Crear una Image de JavaFX a partir del array de bytes
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
                    // Captura un frame de la cámara
                    if (capture.read(frame)) {
                        // Convertir Mat a Image y mostrar en la UI
                        Image currentFrame = matToJavaFXImage(frame);
                        Platform.runLater(() -> {
                            cameraImageView.setImage(currentFrame);
                        });

                        // Si está grabando, escribe el frame
                        if (isRecording && videoWriter != null && videoWriter.isOpened()) {
                            videoWriter.write(frame);
                        }
                    } else {
                        System.err.println("Error al leer el frame de la cámara");
                        break;  // Si no se puede leer, salimos del bucle
                    }

                    Thread.sleep(1); // Pausa para mantener los FPS constantes (~30 FPS)
                } catch (InterruptedException e) {
                    break;  // Si el hilo es interrumpido, salimos del bucle
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
        // Crear buffer para la imagen
        org.opencv.core.MatOfByte buffer = new org.opencv.core.MatOfByte();
        // Codificar la imagen en formato PNG
        Imgcodecs.imencode(".png", mat, buffer);
        // Convertir a array de bytes
        byte[] byteArray = buffer.toArray();
        // Crear imagen JavaFX desde bytes
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
            // Detener la cámara
            cameraActive = false;
            if (cameraThread != null) cameraThread.interrupt();

            // Actualizar UI
            statusValue.setText("Offline");
            statusValue.setStyle("-fx-text-fill: #ff0000;");

            // Liberar recursos
            if (capture != null && capture.isOpened()) {
                capture.release();
            }

            // Actualizar botón
            stopFeedAction.getChildren().get(0).setStyle("-fx-text-fill: #ff4d4d;");
        } else {
            // Reiniciar la cámara
            initializeCamera();

            // Actualizar botón
            stopFeedAction.getChildren().get(0).setStyle("-fx-text-fill: #4285f4;");
        }
    }

    @FXML
    private void handleRecordingsAction() {
        try {
            // Abrir el directorio de grabaciones con el explorador de archivos
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
            // Verificar si la cámara está activa
            if (!cameraActive || frame == null || frame.empty()) {
                throw new Exception("No hay imagen disponible para capturar.");
            }

            // Crear directorio para capturas si no existe
            File snapshotsDir = new File(recordingsDir, "snapshots");
            if (!snapshotsDir.exists()) {
                snapshotsDir.mkdirs();
            }

            // Crear nombre de archivo con timestamp
            String timestamp = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss").format(new Date());
            String snapshotFileName = snapshotsDir.getAbsolutePath() + "/snapshot_" + timestamp + ".jpg";

            // Guardar la imagen
            boolean success = Imgcodecs.imwrite(snapshotFileName, frame);

            if (!success) {
                throw new Exception("No se pudo guardar la imagen.");
            }

            // Añadir a actividad reciente
            addRecentActivity("📷", "Snapshot taken", "just now");

            // Notificar al usuario
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Captura Realizada");
            alert.setHeaderText(null);
            alert.setContentText("¡Captura realizada con éxito!\n" +
                    "Guardada en: " + snapshotFileName);
            alert.showAndWait();

            // Registrar en la consola
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

            // Obtener resolución y fps
            int width = (int) capture.get(Videoio.CAP_PROP_FRAME_WIDTH);
            int height = (int) capture.get(Videoio.CAP_PROP_FRAME_HEIGHT);
            double fps = capture.get(Videoio.CAP_PROP_FPS);

            if (width <= 0 || height <= 0) {
                width = 1280;  // Si no es válida, usar valores predeterminados
                height = 720;
            }

            if (fps <= 0) fps = 30.0;  // Asignar un valor seguro para FPS

            String timestamp = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss").format(new Date());
            String videoFileName = recordingsDir.getAbsolutePath() + "/security_recording_" + timestamp + ".mp4";

            // Inicializar el VideoWriter con el codec y FPS correctos
            try {
                videoWriter = new VideoWriter(
                        videoFileName,
                        VideoWriter.fourcc('X', 'V', 'I', 'D'),
                        fps,  // Utilizar el FPS correcto
                        new org.opencv.core.Size(width, height),
                        true
                );

                // Si no se abre correctamente, intentar con otro codec
                if (!videoWriter.isOpened()) {
                    videoWriter.release();
                    videoWriter = new VideoWriter(videoFileName, VideoWriter.fourcc('M', 'J', 'P', 'G'),
                            fps, new org.opencv.core.Size(width, height), true);
                }

                // Si aún no se abre, probar con otro formato
                if (!videoWriter.isOpened()) {
                    videoFileName = recordingsDir.getAbsolutePath() + "/security_recording_" + timestamp + ".avi";
                    videoWriter = new VideoWriter(videoFileName, VideoWriter.fourcc('D', 'I', 'V', 'X'),
                            fps, new org.opencv.core.Size(width, height), true);
                }

                if (!videoWriter.isOpened()) {
                    throw new Exception("No se pudo crear el archivo de video con ningún formato compatible.");
                }

                // Configuración de la grabación
                isRecording = true;
                stopRecording.set(false);
                recordingSeconds.set(0);

                // Actualizar la UI
                recordBtn.setText("DETENER GRABACIÓN");
                recordBtn.setStyle("-fx-background-color: #ea4335; -fx-text-fill: white;");
                statusValue.setText("Grabando...");

                // Iniciar el hilo de grabación
                startRecordingTimer();

                System.out.println("Grabación iniciada: " + videoFileName);

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
        // Inicializa las referencias atómicas para almacenar las horas, minutos y segundos
        final AtomicReference<Integer> seconds = new AtomicReference<>(0);
        final AtomicReference<Integer> minutes = new AtomicReference<>(0);
        final AtomicReference<Integer> hours = new AtomicReference<>(0);

        stopTimer.set(false);

        // Crear un hilo para actualizar el temporizador cada segundo
        timerThread = new Thread(() -> {
            try {
                while (!stopTimer.get()) {
                    // Esperar un segundo
                    Thread.sleep(1000); // Actualiza cada segundo

                    // Incrementar los segundos
                    seconds.getAndUpdate(s -> (s + 1) % 60);
                    if (seconds.get() == 0) {
                        // Si los segundos llegan a 60, incrementamos los minutos
                        minutes.getAndUpdate(m -> (m + 1) % 60);

                        if (minutes.get() == 0) {
                            // Si los minutos llegan a 60, incrementamos las horas
                            hours.getAndUpdate(h -> h + 1);
                        }
                    }

                    // Actualizar la interfaz de usuario (JavaFX) con el tiempo transcurrido
                    Platform.runLater(() -> {
                        // Mostrar el tiempo en el formato "hh:mm:ss"
                        timerLabel.setText(String.format("%02d:%02d:%02d", hours.get(), minutes.get(), seconds.get()));
                    });
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        });

        // Hacer que el hilo sea un daemon, para que se cierre automáticamente cuando la aplicación termine
        timerThread.setDaemon(true);
        timerThread.start();
    }






    private void stopRecording() {
        try {
            // Detener grabación
            isRecording = false;
            stopRecording.set(true);

            // Esperar a que el hilo de grabación termine
            if (recordingThread != null) {
                recordingThread.join(2000); // Esperar hasta 2 segundos
            }

            // Detener el temporizador
            stopTimer.set(true); // Marcar que el temporizador debe detenerse
            if (timerThread != null) {
                timerThread.join();  // Esperar hasta que el hilo del temporizador termine
            }

            // Liberar recursos de grabación
            cleanupRecordingResources();

            // Añadir a actividad reciente
            addRecentActivity("🛑", "Recording stopped", "just now");

            // Restablecer estado de la UI
            recordBtn.setText("Record");
            recordBtn.setStyle("-fx-background-color: #ff4d4d; -fx-text-fill: white; -fx-font-weight: bold;");
            timerLabel.setText("00:00:00");

            // Mostrar mensaje de éxito
            showAlert(AlertType.INFORMATION, "Grabación Completada",
                    "La grabación ha sido guardada en el directorio:\n" + recordingsDir.getAbsolutePath());

        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }


    private void cleanupRecordingResources() {
        // Liberar recursos de video
        if (videoWriter != null) {
            videoWriter.release();
            videoWriter = null;
        }
    }

    private void addRecentActivity(String icon, String activity, String time) {
        Platform.runLater(() -> {
            // Crear elementos de UI para la actividad
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

            // Añadir al principio para que los más recientes estén arriba
            if (activityList.getChildren().size() > 0) {
                activityList.getChildren().add(0, activityItem);
            } else {
                activityList.getChildren().add(activityItem);
            }

            // Limitar a 5 actividades
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

    // Método llamado cuando se cierra la vista
    public void onClose() {
        // Detener grabación si está activa
        if (isRecording) {
            stopRecording();
        }

        // Detener cámara
        cameraActive = false;
        if (cameraThread != null) {
            cameraThread.interrupt();
        }

        // Liberar recursos
        if (capture != null && capture.isOpened()) {
            capture.release();
        }

        if (frame != null) {
            frame.release();
        }
    }
}