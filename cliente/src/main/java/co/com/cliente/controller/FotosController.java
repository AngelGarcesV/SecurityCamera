package co.com.cliente.controller;

import co.com.cliente.dto.ImagenDTO;
import co.com.cliente.httpRequest.HttpService;
import co.com.cliente.httpRequest.JsonResponseHandler;
import co.com.cliente.httpRequest.PropertiesLoader;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.TextAlignment;
import javafx.util.Pair;
import org.json.JSONObject;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.Arrays;
import java.util.concurrent.Executors;

public class FotosController implements Initializable {

    @FXML
    private FlowPane photoGrid;

    private ExecutorService executorService;
    private final int THUMBNAIL_SIZE = 200;
    private final String API_BASE_URL = PropertiesLoader.getBaseUrl() + "/api/imagenes";
    private List<ImagenDTO> currentImages = new ArrayList<>();

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        executorService = Executors.newFixedThreadPool(4);


        configurarJWT();

        photoGrid.setHgap(20);
        photoGrid.setVgap(20);
        photoGrid.setPadding(new Insets(20, 20, 20, 20));

        loadImages();
    }

    private void configurarJWT() {

        if (HttpService.getInstance().getJwtToken() == null ||
                HttpService.getInstance().getJwtToken().isEmpty()) {
        }
    }

    private void loadImages() {
        try {
            String userId = HttpService.getInstance().getUserIdFromClaims();
            if (userId == null || userId.isEmpty()) {
                showAlert(Alert.AlertType.ERROR, "Error", "No se pudo obtener el ID del usuario.");
                return;
            }
            String jsonResponse = HttpService.getInstance().sendGetRequest(API_BASE_URL + "/usuario/" + userId);
            currentImages = Arrays.asList(JsonResponseHandler.parseResponse(jsonResponse, ImagenDTO[].class));
            displayImages();
        } catch (Exception e) {
            e.printStackTrace();
            Platform.runLater(() -> {
                photoGrid.getChildren().clear();
                Label errorLabel;


                if (e.getMessage() != null && e.getMessage().contains("404")) {
                    errorLabel = new Label("No se han tomado imágenes");
                } else {
                    errorLabel = new Label("Error al cargar las imágenes: " + e.getMessage());
                }

                errorLabel.setFont(Font.font("System", FontWeight.NORMAL, 14));
                errorLabel.setStyle("-fx-text-fill: #888888;");
                photoGrid.getChildren().add(errorLabel);
            });
        }
    }

    private void displayImages() {
        Platform.runLater(() -> {
            photoGrid.getChildren().clear();

            if (currentImages.isEmpty()) {
                Label noImagesLabel = new Label("No se han tomado imágenes");
                noImagesLabel.setFont(Font.font("System", FontWeight.NORMAL, 14));
                noImagesLabel.setStyle("-fx-text-fill: #888888;");
                photoGrid.getChildren().add(noImagesLabel);
                return;
            }

            for (ImagenDTO imagen : currentImages) {
                addPhotoToGrid(imagen);
            }
        });
    }

    private void addPhotoToGrid(ImagenDTO imagen) {

        VBox photoContainer = new VBox();
        photoContainer.setAlignment(Pos.CENTER);
        photoContainer.setSpacing(5);
        photoContainer.setPrefWidth(THUMBNAIL_SIZE);


        StackPane photoItem = new StackPane();
        photoItem.setPrefSize(THUMBNAIL_SIZE, THUMBNAIL_SIZE);
        photoItem.setStyle("-fx-background-color: #e0e0e0;");


        Label nameLabel = new Label(imagen.getNombre());
        nameLabel.setMaxWidth(THUMBNAIL_SIZE);
        nameLabel.setWrapText(true);
        nameLabel.setTextAlignment(TextAlignment.CENTER);
        nameLabel.setAlignment(Pos.CENTER);


        HBox buttonBox = new HBox();
        buttonBox.setSpacing(3);
        buttonBox.setAlignment(Pos.CENTER);


        Button editButton = new Button("Editar");
        editButton.setStyle("-fx-background-color: #28a745; -fx-text-fill: white; -fx-font-size: 10px;");
        editButton.setPrefWidth(60);
        editButton.setOnAction(e -> openEditarFotosView(imagen));


        Button updateButton = new Button("Actualizar");
        updateButton.setStyle("-fx-background-color: #007bff; -fx-text-fill: white; -fx-font-size: 10px;");
        updateButton.setPrefWidth(60);
        updateButton.setOnAction(e -> showUpdateDialog(imagen));


        Button deleteButton = new Button("Eliminar");
        deleteButton.setStyle("-fx-background-color: #dc3545; -fx-text-fill: white; -fx-font-size: 10px;");
        deleteButton.setPrefWidth(60);
        deleteButton.setOnAction(e -> confirmAndDelete(imagen));


        buttonBox.getChildren().addAll(editButton, updateButton, deleteButton);


        photoContainer.getChildren().addAll(photoItem, nameLabel, buttonBox);


        photoGrid.getChildren().add(photoContainer);

        executorService.submit(() -> {
            try {
                byte[] imageBytes = imagen.getImagen();
                Image image = new Image(new ByteArrayInputStream(imageBytes),
                        THUMBNAIL_SIZE, THUMBNAIL_SIZE, true, true);

                Platform.runLater(() -> {
                    try {
                        ImageView imageView = new ImageView(image);
                        imageView.setFitWidth(THUMBNAIL_SIZE);
                        imageView.setFitHeight(THUMBNAIL_SIZE);
                        imageView.setPreserveRatio(true);

                        photoItem.getChildren().clear();
                        photoItem.getChildren().add(imageView);

                        String date = new SimpleDateFormat("dd/MM/yyyy HH:mm").format(imagen.getFecha());
                        Tooltip tooltip = new Tooltip(
                                imagen.getNombre() + "\n" + date);
                        Tooltip.install(photoItem, tooltip);

                        photoItem.setOnMouseClicked(event -> viewFullImage(imagen));

                    } catch (Exception e) {
                        photoItem.setStyle("-fx-background-color: #cccccc;");
                    }
                });
            } catch (Exception e) {
                Platform.runLater(() -> {
                    photoItem.setStyle("-fx-background-color: #cccccc;");
                });
            }
        });
    }

    private void openEditarFotosView(ImagenDTO imagen) {
        try {

            FXMLLoader loader = new FXMLLoader(getClass().getResource("/co/com/cliente/views/editar-fotos-view.fxml"));
            Parent editView = loader.load();


            Object controller = loader.getController();


            if (controller != null) {
                try {

                    controller.getClass().getMethod("setImagenToEdit", ImagenDTO.class).invoke(controller, imagen);
                } catch (Exception e) {
                    System.out.println("El controlador no tiene método setImagenToEdit: " + e.getMessage());
                }
            }


            StackPane contentArea = getContentArea();
            if (contentArea != null) {
                contentArea.getChildren().clear();
                contentArea.getChildren().add(editView);


                updateTitle("EDITAR FOTOS");
            }

        } catch (IOException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Error", "No se pudo abrir la vista de edición: " + e.getMessage());
        }
    }

    private StackPane getContentArea() {
        try {

            Parent root = photoGrid.getScene().getRoot();
            return findStackPane(root);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private StackPane findStackPane(Parent parent) {
        if (parent instanceof StackPane && ((StackPane) parent).getId() != null &&
                ((StackPane) parent).getId().equals("contentArea")) {
            return (StackPane) parent;
        }

        for (var node : parent.getChildrenUnmodifiable()) {
            if (node instanceof Parent) {
                StackPane result = findStackPane((Parent) node);
                if (result != null) {
                    return result;
                }
            }
        }
        return null;
    }

    private void updateTitle(String title) {
        try {
            Parent root = photoGrid.getScene().getRoot();
            Label titleLabel = findTitleLabel(root);
            if (titleLabel != null) {
                titleLabel.setText(title);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private Label findTitleLabel(Parent parent) {
        if (parent instanceof Label && ((Label) parent).getId() != null &&
                ((Label) parent).getId().equals("titleLabel")) {
            return (Label) parent;
        }

        for (var node : parent.getChildrenUnmodifiable()) {
            if (node instanceof Parent) {
                Label result = findTitleLabel((Parent) node);
                if (result != null) {
                    return result;
                }
            }
        }
        return null;
    }

    private void showUpdateDialog(ImagenDTO imagen) {

        Dialog<Pair<String, String>> dialog = new Dialog<>();
        dialog.setTitle("Actualizar Imagen");
        dialog.setHeaderText("Modificar nombre y resolución");


        ButtonType updateButtonType = new ButtonType("Actualizar", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(updateButtonType, ButtonType.CANCEL);


        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));

        TextField nombreField = new TextField(imagen.getNombre());
        TextField resolucionField = new TextField(imagen.getResolucion());

        grid.add(new Label("Nombre:"), 0, 0);
        grid.add(nombreField, 1, 0);
        grid.add(new Label("Resolución:"), 0, 1);
        grid.add(resolucionField, 1, 1);

        dialog.getDialogPane().setContent(grid);


        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == updateButtonType) {
                return new Pair<>(nombreField.getText(), resolucionField.getText());
            }
            return null;
        });


        Optional<Pair<String, String>> result = dialog.showAndWait();
        result.ifPresent(nombreResolucion -> {
            updateImage(imagen, nombreResolucion.getKey(), nombreResolucion.getValue());
        });
    }

    private void updateImage(ImagenDTO imagen, String nuevoNombre, String nuevaResolucion) {
        try {
            JSONObject jsonRequest = new JSONObject();
            jsonRequest.put("id", imagen.getId());
            jsonRequest.put("imagen", imagen.getImagen());
            jsonRequest.put("nombre", nuevoNombre);
            jsonRequest.put("resolucion", nuevaResolucion);
            jsonRequest.put("fecha", imagen.getFecha().getTime());
            jsonRequest.put("camaraId", imagen.getCamaraId());
            jsonRequest.put("usuarioId", imagen.getUsuarioId());




            HttpService.getInstance().sendPutRequest(
                    API_BASE_URL + "/update",
                    jsonRequest.toString()
            );


            loadImages();


            showAlert(Alert.AlertType.INFORMATION, "Éxito", "Imagen actualizada correctamente");

        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Error", "No se pudo actualizar la imagen: " + e.getMessage());
        }
    }

    private void confirmAndDelete(ImagenDTO imagen) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirmar eliminación");
        alert.setHeaderText("¿Está seguro de eliminar esta imagen?");
        alert.setContentText("Esta acción no se puede deshacer.");

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            deleteImage(imagen);
        }
    }

    private void deleteImage(ImagenDTO imagen) {
        try {

            HttpService.getInstance().sendDeleteRequest(API_BASE_URL + "/" + imagen.getId());

            List<ImagenDTO> updatedImages = new ArrayList<>();
            for (ImagenDTO img : currentImages) {
                if (!img.getId().equals(imagen.getId())) {
                    updatedImages.add(img);
                }
            }


            currentImages = updatedImages;
            displayImages();


            showAlert(Alert.AlertType.INFORMATION, "Éxito", "Imagen eliminada correctamente");

        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Error", "No se pudo eliminar la imagen: " + e.getMessage());
        }
    }

    private void viewFullImage(ImagenDTO imagen) {
        try {
            File tempFile = File.createTempFile("image_", ".png");
            tempFile.deleteOnExit();

            try (FileOutputStream fos = new FileOutputStream(tempFile)) {
                fos.write(imagen.getImagen());
            }

            java.awt.Desktop.getDesktop().open(tempFile);
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

    public void onClose() {
        if (executorService != null && !executorService.isShutdown()) {
            executorService.shutdown();
        }
    }
}