package co.com.cliente.controller;

import co.com.cliente.dto.CamaraDTO;
import co.com.cliente.httpRequest.HttpService;
import co.com.cliente.httpRequest.JsonResponseHandler;
import co.com.cliente.httpRequest.PropertiesLoader;
import co.com.cliente.websocket.WebSocketVideoService;
import co.com.cliente.ui.CameraPanel;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;

import org.opencv.core.Core;

import java.io.File;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

public class MultiCameraController implements Initializable {

    @FXML
    private ScrollPane scrollPane;

    @FXML
    private GridPane cameraGrid;

    @FXML
    private Label statusLabel;

    @FXML
    private Button refreshButton;

    @FXML
    private Button recordAllButton;

    @FXML
    private Button snapshotAllButton;

    @FXML
    private ComboBox<String> layoutComboBox;

    @FXML
    private ProgressIndicator loadingIndicator;

    @FXML
    private Label timerLabel;

    @FXML
    private Label recordingStatusLabel;

    private static final String API_BASE_URL = PropertiesLoader.getBaseUrl() + "/api/camara/usuario/";

    private List<CamaraDTO> activeCameras = new ArrayList<>();
    private Map<Long, CameraPanel> cameraPanels = new HashMap<>();
    private ExecutorService executorService = Executors.newFixedThreadPool(50); // Más hilos para grabación
    private Timer refreshTimer;
    private boolean isActive = true;

    // Variables para grabación múltiple
    private boolean isRecordingAll = false;
    private AtomicInteger recordingSeconds = new AtomicInteger(0);
    private Timer recordingTimer;
    private AtomicBoolean stopTimer = new AtomicBoolean(false);
    private Thread timerThread;
    private String currentSessionId;
    private File recordingsDir;
    private WebSocketVideoService webSocketService;

    // Configuraciones de layout
    private static final Map<String, int[]> LAYOUT_CONFIGS = Map.of(
            "1 Columna", new int[]{1, 0},
            "2 Columnas", new int[]{2, 0},
            "3 Columnas", new int[]{3, 0},
            "4 Columnas", new int[]{4, 0},
            "2x2 Grid", new int[]{2, 2},
            "3x3 Grid", new int[]{3, 3},
            "Auto", new int[]{0, 0}
    );

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        initializeOpenCV();
        initializeWebSocket();
        setupUI();
        setupRecordingDirectory();
        loadActiveCameras();
        startRefreshTimer();
    }

    private void initializeOpenCV() {
        try {
            nu.pattern.OpenCV.loadLocally();
            System.out.println("OpenCV cargado correctamente para vista múltiple: " + Core.VERSION);
        } catch (UnsatisfiedLinkError e) {
            try {
                System.loadLibrary("opencv_java470");
                System.out.println("OpenCV 4.70 cargado correctamente para vista múltiple.");
            } catch (UnsatisfiedLinkError e2) {
                System.err.println("Error al cargar OpenCV en vista múltiple: " + e2.getMessage());
                showAlert(Alert.AlertType.ERROR, "Error OpenCV",
                        "No se pudo cargar OpenCV. Las funciones de grabación no estarán disponibles.");
            }
        }
    }

    private void initializeWebSocket() {
        webSocketService = new WebSocketVideoService();
        webSocketService.connectToServer();
    }

    private void setupRecordingDirectory() {
        recordingsDir = new File(System.getProperty("user.home") + "/multi_camera_recordings");
        if (!recordingsDir.exists()) {
            recordingsDir.mkdirs();
        }
    }

    private void setupUI() {
        // Configurar ComboBox de layouts
        layoutComboBox.getItems().addAll(LAYOUT_CONFIGS.keySet());
        layoutComboBox.setValue("Auto");
        layoutComboBox.setOnAction(e -> updateLayout());

        // Configurar ScrollPane
        scrollPane.setFitToWidth(true);
        scrollPane.setFitToHeight(true);
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);

        // Configurar GridPane
        cameraGrid.setHgap(10);
        cameraGrid.setVgap(10);
        cameraGrid.setPadding(new Insets(10));
        cameraGrid.setAlignment(Pos.CENTER);

        // Configurar botones de grabación
        recordAllButton.setStyle("-fx-background-color: #ff4d4d; -fx-text-fill: white; -fx-font-weight: bold;");
        snapshotAllButton.setStyle("-fx-background-color: #4285f4; -fx-text-fill: white; -fx-font-weight: bold;");

        statusLabel.setText("Cargando cámaras...");
        timerLabel.setText("00:00:00");
        recordingStatusLabel.setText("Listo para grabar");
    }

    private void loadActiveCameras() {
        Platform.runLater(() -> {
            loadingIndicator.setVisible(true);
            statusLabel.setText("Probando conexiones a cámaras...");
            refreshButton.setDisable(true);
        });

        CompletableFuture.supplyAsync(() -> {
            try {
                String userId = HttpService.getInstance().getUserIdFromClaims();
                if (userId == null || userId.isEmpty()) {
                    throw new Exception("No se pudo obtener el ID del usuario.");
                }

                String jsonResponse = HttpService.getInstance().sendGetRequest(API_BASE_URL + userId);
                List<CamaraDTO> allCameras = Arrays.asList(JsonResponseHandler.parseResponse(jsonResponse, CamaraDTO[].class));

                return allCameras;
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }).thenCompose(allCameras -> {
            if (allCameras.isEmpty()) {
                return CompletableFuture.completedFuture(new ArrayList<CamaraDTO>());
            }

            // Probar conexión a cada cámara en paralelo
            List<CompletableFuture<CamaraDTO>> futures = allCameras.stream()
                    .map(this::testAndReturnCamera)
                    .toList();

            return CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]))
                    .thenApply(v -> futures.stream()
                            .map(CompletableFuture::join)
                            .filter(Objects::nonNull)
                            .toList());

        }).whenComplete((activeCamerasList, throwable) -> {
            Platform.runLater(() -> {
                loadingIndicator.setVisible(false);
                refreshButton.setDisable(false);

                if (throwable != null) {
                    statusLabel.setText("Error al cargar cámaras: " + throwable.getMessage());
                    statusLabel.setTextFill(Color.RED);
                    return;
                }

                activeCameras = new ArrayList<>(activeCamerasList);

                if (activeCameras.isEmpty()) {
                    statusLabel.setText("No hay cámaras activas disponibles");
                    statusLabel.setTextFill(Color.ORANGE);
                    cameraGrid.getChildren().clear();
                    recordAllButton.setDisable(true);
                    snapshotAllButton.setDisable(true);
                } else {
                    statusLabel.setText(activeCameras.size() + " cámara(s) activa(s)");
                    statusLabel.setTextFill(Color.GREEN);
                    recordAllButton.setDisable(false);
                    snapshotAllButton.setDisable(false);
                    setupCameraPanels();
                    updateLayout();
                }
            });
        });
    }

    private CompletableFuture<CamaraDTO> testAndReturnCamera(CamaraDTO camara) {
        return CompletableFuture.supplyAsync(() -> {
            String cameraUrl = "http://" + camara.getIp() + ":" + camara.getPuerto() + "/shot.jpg";

            try {
                URL url = new URL(cameraUrl);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setConnectTimeout(3000);
                connection.setReadTimeout(3000);
                connection.setRequestMethod("GET");

                int responseCode = connection.getResponseCode();
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    String contentType = connection.getContentType();
                    if (contentType != null && contentType.startsWith("image/")) {
                        connection.disconnect();
                        System.out.println("✅ " + camara.getDescripcion() + " está activa");
                        return camara;
                    }
                }
                connection.disconnect();

            } catch (Exception e) {
                System.out.println("❌ " + camara.getDescripcion() + " no disponible: " + e.getMessage());
            }

            return null; // Cámara no disponible
        }, executorService);
    }

    private void setupCameraPanels() {
        // Limpiar paneles existentes
        cameraPanels.values().forEach(CameraPanel::stop);
        cameraPanels.clear();
        cameraGrid.getChildren().clear();

        // Crear nuevos paneles para cámaras activas usando la clase externa
        for (CamaraDTO camara : activeCameras) {
            CameraPanel panel = new CameraPanel(camara, executorService, webSocketService, recordingsDir);
            cameraPanels.put(camara.getId(), panel);
            panel.start();
        }
    }

    private void updateLayout() {
        if (activeCameras.isEmpty()) return;

        Platform.runLater(() -> {
            cameraGrid.getChildren().clear();
            cameraGrid.getColumnConstraints().clear();
            cameraGrid.getRowConstraints().clear();

            String selectedLayout = layoutComboBox.getValue();
            int[] config = LAYOUT_CONFIGS.get(selectedLayout);
            int columns = config[0];
            int rows = config[1];

            // Calcular layout automático si es necesario
            if (columns == 0) {
                int cameraCount = activeCameras.size();
                columns = (int) Math.ceil(Math.sqrt(cameraCount));
                rows = (int) Math.ceil((double) cameraCount / columns);
            }

            // Configurar constraints de columnas y filas
            for (int i = 0; i < columns; i++) {
                ColumnConstraints colConstraints = new ColumnConstraints();
                colConstraints.setPercentWidth(100.0 / columns);
                colConstraints.setHgrow(Priority.ALWAYS);
                cameraGrid.getColumnConstraints().add(colConstraints);
            }

            if (rows > 0) {
                for (int i = 0; i < rows; i++) {
                    RowConstraints rowConstraints = new RowConstraints();
                    rowConstraints.setPercentHeight(100.0 / rows);
                    rowConstraints.setVgrow(Priority.ALWAYS);
                    cameraGrid.getRowConstraints().add(rowConstraints);
                }
            }

            // Colocar paneles de cámaras en el grid
            int currentRow = 0;
            int currentCol = 0;

            for (CamaraDTO camara : activeCameras) {
                CameraPanel panel = cameraPanels.get(camara.getId());
                if (panel != null) {
                    cameraGrid.add(panel.getPanel(), currentCol, currentRow);

                    currentCol++;
                    if (currentCol >= columns) {
                        currentCol = 0;
                        currentRow++;
                    }
                }
            }
        });
    }

    @FXML
    private void handleRefreshButtonClick() {
        loadActiveCameras();
    }

    @FXML
    private void handleRecordAllButtonClick() {
        if (!isRecordingAll) {
            startRecordingAll();
        } else {
            stopRecordingAll();
        }
    }

    @FXML
    private void handleSnapshotAllButtonClick() {
        takeSnapshotAll();
    }

    private void startRecordingAll() {
        if (activeCameras.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Sin cámaras", "No hay cámaras activas para grabar.");
            return;
        }

        currentSessionId = UUID.randomUUID().toString();
        isRecordingAll = true;

        // Iniciar grabación en todas las cámaras
        for (CameraPanel panel : cameraPanels.values()) {
            panel.startRecording(currentSessionId);
        }

        // Actualizar UI
        Platform.runLater(() -> {
            recordAllButton.setText("🛑 DETENER GRABACIÓN");
            recordAllButton.setStyle("-fx-background-color: #ea4335; -fx-text-fill: white; -fx-font-weight: bold;");
            recordingStatusLabel.setText("Grabando " + activeCameras.size() + " cámaras...");
            recordingStatusLabel.setTextFill(Color.RED);
            snapshotAllButton.setDisable(false);
        });

        startRecordingTimer();
        System.out.println("🔴 Iniciada grabación simultánea de " + activeCameras.size() + " cámaras");
    }

    private void stopRecordingAll() {
        isRecordingAll = false;
        stopTimer.set(true);

        // Detener grabación en todas las cámaras
        for (CameraPanel panel : cameraPanels.values()) {
            panel.stopRecording();
        }

        // Actualizar UI
        Platform.runLater(() -> {
            recordAllButton.setText("🔴 GRABAR TODAS");
            recordAllButton.setStyle("-fx-background-color: #ff4d4d; -fx-text-fill: white; -fx-font-weight: bold;");
            recordingStatusLabel.setText("Grabación completada");
            recordingStatusLabel.setTextFill(Color.GREEN);
            timerLabel.setText("00:00:00");
            snapshotAllButton.setDisable(false);
        });

        if (timerThread != null) {
            try {
                timerThread.join(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        System.out.println("🛑 Detenida grabación simultánea de " + activeCameras.size() + " cámaras");

        showAlert(Alert.AlertType.INFORMATION, "Grabación Completada",
                "Se ha completado la grabación de " + activeCameras.size() + " cámaras.\n" +
                        "Los videos han sido guardados y enviados al servidor.");
    }

    private void takeSnapshotAll() {
        if (activeCameras.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Sin cámaras", "No hay cámaras activas para capturar.");
            return;
        }

        String timestamp = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss").format(new Date());
        AtomicInteger completedSnapshots = new AtomicInteger(0);
        int totalCameras = activeCameras.size();

        Platform.runLater(() -> {
            snapshotAllButton.setDisable(true);
            snapshotAllButton.setText("📸 Capturando...");
        });

        for (CameraPanel panel : cameraPanels.values()) {
            CompletableFuture.runAsync(() -> {
                boolean success = panel.takeSnapshot(timestamp);
                int completed = completedSnapshots.incrementAndGet();

                Platform.runLater(() -> {
                    if (completed == totalCameras) {
                        snapshotAllButton.setDisable(false);
                        snapshotAllButton.setText("📸 SNAPSHOT TODAS");
                        showAlert(Alert.AlertType.INFORMATION, "Capturas Completadas",
                                "Se han capturado snapshots de " + totalCameras + " cámaras.\n" +
                                        "Las imágenes han sido guardadas y enviadas al servidor.");
                    }
                });
            }, executorService);
        }

        System.out.println("📸 Iniciada captura simultánea de " + totalCameras + " cámaras");
    }

    private void startRecordingTimer() {
        recordingSeconds.set(0);
        stopTimer.set(false);

        timerThread = new Thread(() -> {
            try {
                while (!stopTimer.get()) {
                    Thread.sleep(1000);
                    int seconds = recordingSeconds.incrementAndGet();

                    int hours = seconds / 3600;
                    int minutes = (seconds % 3600) / 60;
                    int secs = seconds % 60;

                    String timeString = String.format("%02d:%02d:%02d", hours, minutes, secs);

                    Platform.runLater(() -> {
                        timerLabel.setText(timeString);
                    });
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        });

        timerThread.setDaemon(true);
        timerThread.start();
    }

    private void startRefreshTimer() {
        refreshTimer = new Timer(true);
        refreshTimer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                if (isActive && !activeCameras.isEmpty()) {
                    // Refrescar imágenes de todas las cámaras
                    cameraPanels.values().forEach(CameraPanel::refreshImage);
                }
            }
        }, 1000, 2000); // Refrescar cada 2 segundos
    }

    private void showAlert(Alert.AlertType type, String title, String message) {
        Platform.runLater(() -> {
            Alert alert = new Alert(type);
            alert.setTitle(title);
            alert.setHeaderText(null);
            alert.setContentText(message);
            alert.showAndWait();
        });
    }

    public void onClose() {
        isActive = false;

        if (isRecordingAll) {
            stopRecordingAll();
        }

        if (refreshTimer != null) {
            refreshTimer.cancel();
        }

        cameraPanels.values().forEach(CameraPanel::stop);

        if (webSocketService != null) {
            webSocketService.disconnectFromServer();
        }

        if (executorService != null && !executorService.isShutdown()) {
            executorService.shutdown();
        }
    }
}