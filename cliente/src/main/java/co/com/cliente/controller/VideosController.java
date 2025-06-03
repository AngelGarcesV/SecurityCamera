package co.com.cliente.controller;

import co.com.cliente.dto.VideoDTO;
import co.com.cliente.httpRequest.HttpService;
import co.com.cliente.httpRequest.PropertiesLoader;
import co.com.cliente.redis.RedisCache;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.TextInputDialog;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;

public class VideosController implements Initializable {

    @FXML
    private GridPane videoGrid;

    @FXML
    private Label statusLabel;

    @FXML
    private Button verMasButton;

    private File tempDir;
    private Long usuarioId = 2L;
    private static final String API_GET_VIDEOS_URL = PropertiesLoader.getBaseUrl()+"/api/video/usuario/";
    private static final String API_GET_VIDEO_URL = PropertiesLoader.getBaseUrl()+"/api/video/";
    private static final String API_UPDATE_VIDEO_URL = PropertiesLoader.getBaseUrl()+"/api/video/update";
    private static final String API_DELETE_VIDEO_URL = PropertiesLoader.getBaseUrl()+"/api/video/";

    private boolean mostrandoCache = true;
    private List<VideoDTO> videos = new ArrayList<>();

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {

        if (statusLabel != null) {
            statusLabel.setText("Iniciando...");
        }

        if (verMasButton != null) {
            verMasButton.setVisible(false);
        }


        tempDir = new File(System.getProperty("java.io.tmpdir") + "/security_camera_temp");
        if (!tempDir.exists()) {
            tempDir.mkdirs();
        }


        Platform.runLater(this::iniciarCargaDeVideos);
    }


    private void iniciarCargaDeVideos() {
        try {

            RedisCache cache = RedisCache.getInstance();
            if (cache.isRedisDisponible()) {

                cargarVideosDesdeCache();
            } else {

                mostrandoCache = false;
                if (statusLabel != null) {
                    statusLabel.setText("Cargando videos desde el servidor...");
                }
                if (verMasButton != null) {
                    verMasButton.setVisible(false);
                }
                loadVideosFromServer();
            }
        } catch (Exception e) {

            e.printStackTrace();
            if (statusLabel != null) {
                statusLabel.setText("Error al inicializar. Cargando desde el servidor...");
            }
            mostrandoCache = false;
            loadVideosFromServer();
        }
    }


    private void cargarVideosDesdeCache() {
        try {
            RedisCache cache = RedisCache.getInstance();


            if (!cache.isRedisDisponible()) {
                mostrandoCache = false;
                if (statusLabel != null) {
                    statusLabel.setText("Caché no disponible. Cargando desde el servidor...");
                }
                if (verMasButton != null) {
                    verMasButton.setVisible(false);
                }
                loadVideosFromServer();
                return;
            }

            if (cache.hayVideosEnCache(usuarioId)) {

                videos = cache.obtenerVideosDeUsuario(usuarioId);
                mostrandoCache = true;
                if (statusLabel != null) {
                    statusLabel.setText("Mostrando los últimos 5 videos vistos (desde caché)");
                }
                if (verMasButton != null) {
                    verMasButton.setVisible(true);
                }

                displayVideos();
            } else {

                mostrandoCache = false;
                if (statusLabel != null) {
                    statusLabel.setText("Cargando videos desde el servidor...");
                }
                if (verMasButton != null) {
                    verMasButton.setVisible(false);
                }

                loadVideosFromServer();
            }
        } catch (Exception e) {

            e.printStackTrace();
            mostrandoCache = false;
            if (statusLabel != null) {
                statusLabel.setText("Error al cargar caché. Cargando desde el servidor...");
            }
            loadVideosFromServer();
        }
    }


    @FXML
    public void cargarTodosVideos() {

        if (!mostrandoCache) {
            return;
        }


        mostrandoCache = false;
        statusLabel.setText("Cargando todos los videos...");
        verMasButton.setVisible(false);

        loadVideosFromServer();
    }

    private void loadVideosFromServer() {
        videoGrid.getChildren().clear();


        ProgressIndicator loadingIndicator = new ProgressIndicator();
        loadingIndicator.setMaxSize(50, 50);

        Label loadingLabel = new Label("Cargando videos...");
        loadingLabel.setFont(Font.font("System", 14));

        VBox loadingBox = new VBox(10, loadingIndicator, loadingLabel);
        loadingBox.setAlignment(javafx.geometry.Pos.CENTER);

        videoGrid.add(loadingBox, 0, 0, 3, 1);


        new Thread(() -> {
            try {
                String response = HttpService.getInstance().sendGetRequest(API_GET_VIDEOS_URL + usuarioId);


                JSONArray jsonArray = new JSONArray(response);
                videos.clear();

                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONObject videoJson = jsonArray.getJSONObject(i);

                    VideoDTO video = new VideoDTO();
                    video.setId(videoJson.getLong("id"));
                    video.setNombre(videoJson.getString("nombre"));


                    String fechaStr = videoJson.getString("fecha");
                    SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSXXX");
                    Date fecha = format.parse(fechaStr);
                    video.setFecha(fecha);


                    video.setVideo(null);

                    video.setDuracion(videoJson.getString("duracion"));
                    video.setCamaraId(videoJson.getLong("camaraId"));
                    video.setUsuarioId(videoJson.getLong("usuarioId"));

                    videos.add(video);
                }


                videos.sort(Comparator.comparing(VideoDTO::getFecha).reversed());


                RedisCache cache = RedisCache.getInstance();
                if (cache.isRedisDisponible()) {
                    cache.guardarVideosDeUsuario(usuarioId, videos);
                }


                Platform.runLater(() -> {
                    mostrandoCache = false;
                    statusLabel.setText("Mostrando todos los videos (" + videos.size() + ")");
                    verMasButton.setVisible(false);
                    displayVideos();
                });

            } catch (Exception e) {
                e.printStackTrace();
                Platform.runLater(() -> {
                    videoGrid.getChildren().clear();
                    showAlert(Alert.AlertType.ERROR, "Error de conexión",
                            "No se pudieron cargar los videos del servidor: " + e.getMessage());

                    Label errorLabel = new Label("Error al cargar videos del servidor.\nHaga clic para reintentar.");
                    errorLabel.setFont(Font.font("System", FontWeight.BOLD, 14));
                    errorLabel.setTextFill(Color.RED);
                    errorLabel.setWrapText(true);
                    errorLabel.setOnMouseClicked(event -> loadVideosFromServer());

                    videoGrid.add(errorLabel, 0, 0);

                    statusLabel.setText("Error al cargar videos");
                    verMasButton.setVisible(false);
                });
            }
        }).start();
    }

    private void displayVideos() {
        videoGrid.getChildren().clear();


        videoGrid.setHgap(15);
        videoGrid.setVgap(20);
        videoGrid.setPadding(new Insets(10));


        videoGrid.setPrefWidth(880);


        videoGrid.getColumnConstraints().clear();
        for (int i = 0; i < 3; i++) {
            javafx.scene.layout.ColumnConstraints column = new javafx.scene.layout.ColumnConstraints();
            column.setPercentWidth(33.33);
            column.setHgrow(Priority.SOMETIMES);
            videoGrid.getColumnConstraints().add(column);
        }

        if (videos.isEmpty()) {
            Label noVideosLabel = new Label("No hay videos disponibles.");
            noVideosLabel.setFont(Font.font("System", FontWeight.BOLD, 16));
            videoGrid.add(noVideosLabel, 0, 0, 3, 1);


            if (mostrandoCache) {
                statusLabel.setText("No hay videos en caché");
                verMasButton.setText("Cargar Videos");
            } else {
                statusLabel.setText("No hay videos disponibles");
                verMasButton.setVisible(false);
            }

            return;
        }

        int row = 0;
        int col = 0;

        for (VideoDTO video : videos) {
            VBox videoItem = createVideoCard(video);


            videoItem.setMinWidth(270);
            videoItem.setPrefWidth(280);


            videoGrid.add(videoItem, col, row);
            GridPane.setMargin(videoItem, new Insets(5));

            col++;
            if (col >= 3) {
                col = 0;
                row++;
            }
        }
    }

    private VBox createVideoCard(VideoDTO video) {
        VBox item = new VBox(5);
        item.setPrefSize(280, 240);
        item.setStyle("-fx-background-color: white; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.2), 5, 0, 0, 2); -fx-background-radius: 5;");


        StackPane previewContainer = new StackPane();
        previewContainer.setMinHeight(150);
        previewContainer.setStyle("-fx-background-color: #333333; -fx-background-radius: 5 5 0 0;");


        Label videoIcon = new Label("🎥");
        videoIcon.setFont(Font.font("System", 48));
        videoIcon.setTextFill(Color.WHITE);

        previewContainer.getChildren().add(videoIcon);


        VBox infoBox = new VBox(2);
        infoBox.setPadding(new Insets(5, 10, 5, 10));


        String displayName = video.getNombre();
        if (displayName.length() > 25) {
            displayName = displayName.substring(0, 22) + "...";
        }

        Label titleLabel = new Label(displayName);
        titleLabel.setFont(Font.font("System", FontWeight.BOLD, 12));



        HBox metadataBox = new HBox(10);

        Label durationLabel = new Label("⏱ " + video.getDuracion());
        durationLabel.setFont(Font.font("System", 11));


        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm");
        Label dateLabel = new Label("📅 " + dateFormat.format(video.getFecha()));
        dateLabel.setFont(Font.font("System", 11));
        titleLabel.setStyle("-fx-text-fill: black;");
        durationLabel.setStyle("-fx-text-fill: black;");
        dateLabel.setStyle("-fx-text-fill: black;");


        metadataBox.getChildren().addAll(durationLabel, spacer, dateLabel);


        HBox actionBox1 = new HBox(5);
        actionBox1.setPadding(new Insets(5, 0, 0, 0));
        actionBox1.setAlignment(Pos.CENTER);

        Button playButton = new Button("Reproducir");
        playButton.setStyle("-fx-background-color: #4285f4; -fx-text-fill: white;");
        playButton.setPrefWidth(130);

        Button downloadButton = new Button("Descargar");
        downloadButton.setStyle("-fx-background-color: #34a853; -fx-text-fill: white;");
        downloadButton.setPrefWidth(130);

        actionBox1.getChildren().addAll(playButton, downloadButton);


        HBox actionBox2 = new HBox(5);
        actionBox2.setPadding(new Insets(5, 0, 5, 0));
        actionBox2.setAlignment(Pos.CENTER);

        Button updateButton = new Button("Actualizar");
        updateButton.setStyle("-fx-background-color: #fbbc05; -fx-text-fill: white;");
        updateButton.setPrefWidth(130);

        Button deleteButton = new Button("Eliminar");
        deleteButton.setStyle("-fx-background-color: #ea4335; -fx-text-fill: white;");
        deleteButton.setPrefWidth(130);

        actionBox2.getChildren().addAll(updateButton, deleteButton);


        infoBox.getChildren().addAll(titleLabel, metadataBox, actionBox1, actionBox2);
        item.getChildren().addAll(previewContainer, infoBox);


        playButton.setOnAction(event -> playVideo(video));
        downloadButton.setOnAction(event -> downloadVideo(video));
        updateButton.setOnAction(event -> updateVideo(video));
        deleteButton.setOnAction(event -> deleteVideo(video));


        previewContainer.setOnMouseClicked(event -> playVideo(video));

        return item;
    }

    private void playVideo(VideoDTO video) {

        showAlert(Alert.AlertType.INFORMATION, "Descargando video",
                "Descargando video para reproducción. Por favor espere...");


        new Thread(() -> {
            try {

                String response = HttpService.getInstance().sendGetRequest(API_GET_VIDEO_URL + video.getId());
                JSONObject videoJson = new JSONObject(response);


                String videoBase64 = videoJson.getString("video");


                byte[] videoBytes = Base64.getDecoder().decode(videoBase64);

                String fileExtension = determineFileExtension(videoBytes);
                File tempFile = new File(tempDir, "temp_video_" + System.currentTimeMillis() + fileExtension);

                try (FileOutputStream fos = new FileOutputStream(tempFile)) {
                    fos.write(videoBytes);
                }


                Platform.runLater(() -> {
                    try {
                        java.awt.Desktop.getDesktop().open(tempFile);
                    } catch (Exception e) {
                        e.printStackTrace();
                        showAlert(Alert.AlertType.ERROR, "Error de reproducción",
                                "No se pudo reproducir el video: " + e.getMessage());
                    }
                });

            } catch (Exception e) {
                e.printStackTrace();
                Platform.runLater(() -> {
                    showAlert(Alert.AlertType.ERROR, "Error de descarga",
                            "No se pudo descargar el video: " + e.getMessage());
                });
            }
        }).start();
    }

    private void downloadVideo(VideoDTO video) {

        showAlert(Alert.AlertType.INFORMATION, "Descargando video",
                "Descargando video. Por favor espere...");


        new Thread(() -> {
            try {

                String response = HttpService.getInstance().sendGetRequest(API_GET_VIDEO_URL + video.getId());
                JSONObject videoJson = new JSONObject(response);


                String videoBase64 = videoJson.getString("video");


                byte[] videoBytes = Base64.getDecoder().decode(videoBase64);

                String fileExtension = determineFileExtension(videoBytes);


                File downloadsDir = new File(System.getProperty("user.home") + "/Downloads");
                if (!downloadsDir.exists()) {
                    downloadsDir.mkdirs();
                }


                String fileName = video.getNombre().replaceAll("[^a-zA-Z0-9.-]", "_") + fileExtension;
                File outputFile = new File(downloadsDir, fileName);

                try (FileOutputStream fos = new FileOutputStream(outputFile)) {
                    fos.write(videoBytes);
                }

                Platform.runLater(() -> {
                    showAlert(Alert.AlertType.INFORMATION, "Descarga completada",
                            "Video descargado correctamente en:\n" + outputFile.getAbsolutePath());
                });

            } catch (Exception e) {
                e.printStackTrace();
                Platform.runLater(() -> {
                    showAlert(Alert.AlertType.ERROR, "Error de descarga",
                            "No se pudo descargar el video: " + e.getMessage());
                });
            }
        }).start();
    }

    private void updateVideo(VideoDTO video) {

        TextInputDialog dialog = new TextInputDialog(video.getNombre());
        dialog.setTitle("Actualizar Video");
        dialog.setHeaderText("Ingrese el nuevo nombre para el video");
        dialog.setContentText("Nombre:");

        Optional<String> result = dialog.showAndWait();
        if (result.isPresent() && !result.get().trim().isEmpty()) {
            String newName = result.get().trim();


            showAlert(Alert.AlertType.INFORMATION, "Actualizando video",
                    "Actualizando información del video. Por favor espere...");


            new Thread(() -> {
                try {

                    String response = HttpService.getInstance().sendGetRequest(API_GET_VIDEO_URL + video.getId());
                    JSONObject videoJson = new JSONObject(response);


                    JSONObject updateJson = new JSONObject();
                    updateJson.put("id", video.getId());
                    updateJson.put("nombre", newName);
                    updateJson.put("video", videoJson.getString("video"));
                    updateJson.put("duracion", video.getDuracion());
                    updateJson.put("fecha", new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'").format(video.getFecha()));
                    updateJson.put("camaraId", video.getCamaraId());
                    updateJson.put("usuarioId", video.getUsuarioId());


                    String updateResponse = HttpService.getInstance().sendPutRequest(API_UPDATE_VIDEO_URL, updateJson.toString());

                    Platform.runLater(() -> {
                        showAlert(Alert.AlertType.INFORMATION, "Video actualizado",
                                "El nombre del video ha sido actualizado correctamente.");


                        for (VideoDTO v : videos) {
                            if (v.getId() == video.getId()) {
                                v.setNombre(newName);
                                break;
                            }
                        }


                        RedisCache cache = RedisCache.getInstance();
                        if (!mostrandoCache && cache.isRedisDisponible()) {
                            cache.guardarVideosDeUsuario(usuarioId, videos);
                        }


                        displayVideos();
                    });

                } catch (Exception e) {
                    e.printStackTrace();
                    Platform.runLater(() -> {
                        showAlert(Alert.AlertType.ERROR, "Error de actualización",
                                "No se pudo actualizar el video: " + e.getMessage());
                    });
                }
            }).start();
        }
    }

    private void deleteVideo(VideoDTO video) {

        Alert confirmDialog = new Alert(Alert.AlertType.CONFIRMATION);
        confirmDialog.setTitle("Confirmar eliminación");
        confirmDialog.setHeaderText("¿Está seguro que desea eliminar este video?");
        confirmDialog.setContentText("Esta acción no se puede deshacer.");

        Optional<ButtonType> result = confirmDialog.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {

            showAlert(Alert.AlertType.INFORMATION, "Eliminando video",
                    "Eliminando video. Por favor espere...");


            new Thread(() -> {
                try {

                    String deleteUrl = API_DELETE_VIDEO_URL + video.getId();
                    System.out.println("Eliminando video en: " + deleteUrl);
                    HttpService.getInstance().sendDeleteRequest(deleteUrl);


                    final VideoDTO videoToRemove = video;

                    Platform.runLater(() -> {
                        try {
                            showAlert(Alert.AlertType.INFORMATION, "Video eliminado",
                                    "El video ha sido eliminado correctamente.");


                            videos.removeIf(v -> v.getId() == videoToRemove.getId());


                            RedisCache cache = RedisCache.getInstance();
                            if (!mostrandoCache && cache.isRedisDisponible()) {
                                cache.guardarVideosDeUsuario(usuarioId, videos);
                            }


                            displayVideos();
                        } catch (Exception e) {
                            e.printStackTrace();
                            showAlert(Alert.AlertType.ERROR, "Error al actualizar la interfaz",
                                    "Se eliminó el video pero hubo un error al actualizar la interfaz: " + e.getMessage());
                        }
                    });

                } catch (Exception e) {
                    e.printStackTrace();
                    Platform.runLater(() -> {
                        showAlert(Alert.AlertType.ERROR, "Error de eliminación",
                                "No se pudo eliminar el video: " + e.getMessage());
                    });
                }
            }).start();
        }
    }


    private String determineFileExtension(byte[] data) {
        if (data.length < 12) {
            return ".mp4";
        }


        if (data[0] == (byte)0x00 && data[1] == (byte)0x00 &&
                data[2] == (byte)0x00 && data[3] == (byte)0x1C &&
                data[4] == (byte)0x66 && data[5] == (byte)0x74 &&
                data[6] == (byte)0x79 && data[7] == (byte)0x70) {
            return ".mp4";
        }

        if (data[0] == (byte)0x52 && data[1] == (byte)0x49 &&
                data[2] == (byte)0x46 && data[3] == (byte)0x46) {
            return ".avi";
        }


        return ".mp4";
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

    public void refreshVideos() {

        if (mostrandoCache) {
            cargarVideosDesdeCache();
        } else {
            loadVideosFromServer();
        }
    }
}