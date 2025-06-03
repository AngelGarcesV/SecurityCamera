package co.com.cliente.controller;

import co.com.cliente.dto.CamaraDTO;
import co.com.cliente.httpRequest.HttpService;
import co.com.cliente.httpRequest.JsonResponseHandler;
import co.com.cliente.httpRequest.PropertiesLoader;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.util.Callback;

import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Arrays;
import java.util.List;
import java.util.ResourceBundle;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

public class SelectCamaraController implements Initializable {

    @FXML
    private ListView<CamaraDTO> camaraListView;


    @FXML
    private Label errorLabel;

    @FXML
    private ProgressIndicator loadingIndicator;

    @FXML
    private Label statusLabel;

    private static final String API_BASE_URL = PropertiesLoader.getBaseUrl()+"/api/camara/usuario/";
    private ObservableList<CamaraDTO> camarasDisponibles = FXCollections.observableArrayList();
    private ExecutorService executorService = Executors.newFixedThreadPool(10);

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {

        camaraListView.setCellFactory(new Callback<ListView<CamaraDTO>, ListCell<CamaraDTO>>() {
            @Override
            public ListCell<CamaraDTO> call(ListView<CamaraDTO> param) {
                return new ListCell<CamaraDTO>() {
                    @Override
                    protected void updateItem(CamaraDTO camara, boolean empty) {
                        super.updateItem(camara, empty);
                        if (empty || camara == null) {
                            setGraphic(null);
                            setText(null);
                        } else {

                            VBox container = new VBox(5);
                            container.setStyle("-fx-padding: 10; -fx-background-color: white; -fx-border-color: #e0e0e0; -fx-border-radius: 5;");

                            HBox headerBox = new HBox(10);


                            Label statusIndicator = new Label("●");
                            statusIndicator.setStyle("-fx-text-fill: #4CAF50; -fx-font-size: 16px;");


                            VBox infoBox = new VBox(2);
                            Label nameLabel = new Label(camara.getDescripcion());
                            nameLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;");

                            Label detailsLabel = new Label(String.format("%s:%d - %s",
                                    camara.getIp(), camara.getPuerto(), camara.getResolucion()));
                            detailsLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #666;");

                            infoBox.getChildren().addAll(nameLabel, detailsLabel);

                            headerBox.getChildren().addAll(statusIndicator, infoBox);
                            container.getChildren().add(headerBox);

                            setGraphic(container);
                        }
                    }
                };
            }
        });

        camaraListView.setItems(camarasDisponibles);


        if (loadingIndicator == null) {
            loadingIndicator = new ProgressIndicator();
            loadingIndicator.setVisible(false);
        }

        if (statusLabel == null) {
            statusLabel = new Label();
        }


        loadAndTestCamaras();
    }

    private void loadAndTestCamaras() {
        Platform.runLater(() -> {
            errorLabel.setVisible(false);
            if (loadingIndicator != null) {
                loadingIndicator.setVisible(true);
            }
            if (statusLabel != null) {
                statusLabel.setText("Cargando cámaras y probando conexiones...");
                statusLabel.setVisible(true);
            }
        });

        CompletableFuture.supplyAsync(() -> {
            try {
                String userId = HttpService.getInstance().getUserIdFromClaims();
                if (userId == null || userId.isEmpty()) {
                    throw new Exception("No se pudo obtener el ID del usuario.");
                }

                String jsonResponse = HttpService.getInstance().sendGetRequest(API_BASE_URL + userId);
                List<CamaraDTO> camerasList = Arrays.asList(JsonResponseHandler.parseResponse(jsonResponse, CamaraDTO[].class));

                return camerasList;
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }).thenCompose(camerasList -> {
            if (camerasList.isEmpty()) {
                return CompletableFuture.completedFuture(new java.util.ArrayList<CamaraDTO>());
            }

            Platform.runLater(() -> {
                if (statusLabel != null) {
                    statusLabel.setText("Probando conexión a " + camerasList.size() + " cámaras...");
                }
            });


            CompletableFuture<?>[] futures = camerasList.stream()
                    .map(this::testCameraConnection)
                    .toArray(CompletableFuture[]::new);

            return CompletableFuture.allOf(futures)
                    .thenApply(v -> camarasDisponibles.stream().collect(java.util.stream.Collectors.toList()));

        }).whenComplete((result, throwable) -> {
            Platform.runLater(() -> {
                if (loadingIndicator != null) {
                    loadingIndicator.setVisible(false);
                }

                if (throwable != null) {
                    showError("Error al cargar las cámaras: " + throwable.getMessage());
                    if (statusLabel != null) {
                        statusLabel.setText("Error al cargar cámaras");
                    }
                    return;
                }

                if (camarasDisponibles.isEmpty()) {
                    showError("No hay cámaras disponibles o ninguna responde.");
                    if (statusLabel != null) {
                        statusLabel.setText("No hay cámaras disponibles");
                    }
                } else {
                    errorLabel.setVisible(false);


                    camaraListView.getSelectionModel().select(0);

                    if (statusLabel != null) {
                        statusLabel.setText(camarasDisponibles.size() + " cámara(s) disponible(s)");
                        statusLabel.setStyle("-fx-text-fill: #4CAF50;");
                    }
                }
            });
        });
    }

    private CompletableFuture<Void> testCameraConnection(CamaraDTO camara) {
        return CompletableFuture.runAsync(() -> {
            String cameraUrl = "http://" + camara.getIp() + ":" + camara.getPuerto() + "/shot.jpg";
            System.out.println("Probando conexión a: " + cameraUrl);

            boolean connected = false;
            int attempts = 0;
            final int maxAttempts = 3;

            while (attempts < maxAttempts && !connected) {
                attempts++;

                int finalAttempts = attempts;
                Platform.runLater(() -> {
                    if (statusLabel != null) {
                        statusLabel.setText("Probando " + camara.getDescripcion() + " (intento " + finalAttempts + "/" + maxAttempts + ")");
                    }
                });

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
                            connected = true;
                            System.out.println("✅ Conexión exitosa a " + camara.getDescripcion() + " en intento " + attempts);
                        }
                    }

                    connection.disconnect();

                } catch (Exception e) {
                    System.out.println("❌ Intento " + attempts + " fallido para " + camara.getDescripcion() + ": " + e.getMessage());

                    if (attempts < maxAttempts) {
                        try {
                            Thread.sleep(1000);
                        } catch (InterruptedException ie) {
                            Thread.currentThread().interrupt();
                            break;
                        }
                    }
                }
            }

            if (connected) {
                Platform.runLater(() -> {
                    camarasDisponibles.add(camara);
                    System.out.println("Cámara agregada a la lista: " + camara.getDescripcion());
                });
            } else {
                System.out.println("❌ No se pudo conectar a " + camara.getDescripcion() + " después de " + maxAttempts + " intentos");
            }

        }, executorService);
    }

    @FXML
    private void handleMultiViewButtonClick() {
        try {

            FXMLLoader loader = new FXMLLoader(getClass().getResource("/co/com/cliente/views/multi-camera-view.fxml"));
            Parent view = loader.load();


            StackPane contentArea = (StackPane) camaraListView.getScene().lookup("#contentArea");
            if (contentArea != null) {
                contentArea.getChildren().clear();
                contentArea.getChildren().add(view);
            }
        } catch (Exception e) {
            e.printStackTrace();
            showError("Error al cargar la vista múltiple: " + e.getMessage());
        }
    }

    @FXML
    private void handleRefreshButtonClick() {
        camarasDisponibles.clear();
        loadAndTestCamaras();
    }

    private void showError(String message) {
        errorLabel.setText(message);
        errorLabel.setVisible(true);
        errorLabel.setStyle("-fx-text-fill: #f44336;");
    }


    public void cleanup() {
        if (executorService != null && !executorService.isShutdown()) {
            executorService.shutdown();
        }
    }
}