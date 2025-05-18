package co.com.cliente.controller;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.StackPane;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

import java.io.File;
import java.io.FileInputStream;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Date;
import java.util.ResourceBundle;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class FotosController implements Initializable {

    @FXML
    private FlowPane photoGrid;

    private File snapshotsDir;
    private ExecutorService executorService;
    private final int THUMBNAIL_SIZE = 200; // Tamaño de las miniaturas

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        // Crear servicio para carga de imágenes en segundo plano
        executorService = Executors.newFixedThreadPool(4);

        // Configurar la cuadrícula
        photoGrid.setHgap(20);
        photoGrid.setVgap(20);
        photoGrid.setPadding(new Insets(0, 0, 0, 0));

        // Crear o obtener el directorio de capturas
        File recordingsDir = new File(System.getProperty("user.home") + "/security_camera_recordings");
        if (!recordingsDir.exists()) {
            recordingsDir.mkdirs();
        }

        snapshotsDir = new File(recordingsDir, "snapshots");
        if (!snapshotsDir.exists()) {
            snapshotsDir.mkdirs();
        }

        // Cargar las fotos capturadas
        loadSnapshots();
    }

    private void loadSnapshots() {
        // Limpiar la cuadrícula
        photoGrid.getChildren().clear();

        // Cargar imágenes en segundo plano
        executorService.submit(() -> {
            try {
                // Obtener archivos de imagen
                File[] imageFiles = snapshotsDir.listFiles((dir, name) ->
                        name.toLowerCase().endsWith(".jpg") ||
                                name.toLowerCase().endsWith(".jpeg") ||
                                name.toLowerCase().endsWith(".png"));

                // Actualizar UI en el hilo de JavaFX
                Platform.runLater(() -> {
                    photoGrid.getChildren().clear();

                    if (imageFiles == null || imageFiles.length == 0) {
                        // No hay imágenes disponibles, no mostrar nada o mostrar mensaje discreto
                        Label noImagesLabel = new Label("No hay capturas disponibles");
                        noImagesLabel.setFont(Font.font("System", FontWeight.NORMAL, 14));
                        noImagesLabel.setStyle("-fx-text-fill: #888888;");
                        photoGrid.getChildren().add(noImagesLabel);
                        return;
                    }

                    // Ordenar por fecha de modificación (más reciente primero)
                    Arrays.sort(imageFiles, Comparator.comparing(File::lastModified).reversed());

                    // Añadir imágenes a la cuadrícula
                    for (File imageFile : imageFiles) {
                        addPhotoToGrid(imageFile);
                    }
                });
            } catch (Exception e) {
                e.printStackTrace();
                Platform.runLater(() -> {
                    photoGrid.getChildren().clear();
                    Label errorLabel = new Label("Error al cargar las imágenes");
                    errorLabel.setStyle("-fx-text-fill: #888888;");
                    photoGrid.getChildren().add(errorLabel);
                });
            }
        });
    }

    private void addPhotoToGrid(File imageFile) {
        // Crear contenedor para la imagen
        StackPane photoItem = new StackPane();
        photoItem.setPrefSize(THUMBNAIL_SIZE, THUMBNAIL_SIZE);
        photoItem.setStyle("-fx-background-color: #e0e0e0;");

        // Añadir el contenedor a la cuadrícula inmediatamente
        photoGrid.getChildren().add(photoItem);

        // Cargar la imagen en segundo plano
        executorService.submit(() -> {
            try {
                // Cargar imagen
                Image image = new Image(new FileInputStream(imageFile),
                        THUMBNAIL_SIZE, THUMBNAIL_SIZE, true, true);

                // Si la imagen se cargó correctamente, actualizar la UI
                Platform.runLater(() -> {
                    try {
                        // Crear ImageView
                        ImageView imageView = new ImageView(image);
                        imageView.setFitWidth(THUMBNAIL_SIZE);
                        imageView.setFitHeight(THUMBNAIL_SIZE);
                        imageView.setPreserveRatio(true);

                        // Limpiar contenedor y añadir imagen
                        photoItem.getChildren().clear();
                        photoItem.getChildren().add(imageView);

                        // Agregar tooltip con información de la imagen
                        String date = new SimpleDateFormat("dd/MM/yyyy HH:mm").format(new Date(imageFile.lastModified()));
                        javafx.scene.control.Tooltip tooltip = new javafx.scene.control.Tooltip(
                                imageFile.getName() + "\n" + date);
                        javafx.scene.control.Tooltip.install(photoItem, tooltip);

                        // Manejar evento de clic para ver la imagen completa
                        photoItem.setOnMouseClicked(event -> viewFullImage(imageFile));

                    } catch (Exception e) {
                        // Si algo falla en la UI, mostrar placeholder
                        photoItem.setStyle("-fx-background-color: #cccccc;");
                    }
                });
            } catch (Exception e) {
                // Si falla la carga de la imagen, mostrar placeholder
                Platform.runLater(() -> {
                    photoItem.setStyle("-fx-background-color: #cccccc;");
                });
            }
        });
    }

    private void viewFullImage(File imageFile) {
        try {
            // Abrir la imagen con la aplicación predeterminada del sistema
            java.awt.Desktop.getDesktop().open(imageFile);
        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Error",
                    "No se pudo abrir la imagen: " + e.getMessage());
        }
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

    // Método para actualizar la vista de fotos (puede ser llamado desde fuera)
    public void refreshPhotos() {
        loadSnapshots();
    }

    // Método para limpiar recursos cuando se cierra la vista
    public void onClose() {
        if (executorService != null && !executorService.isShutdown()) {
            executorService.shutdown();
        }
    }
}