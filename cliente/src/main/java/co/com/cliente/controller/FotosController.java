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
<<<<<<< HEAD
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
=======
        initializeExecutor();
        configureJWT();
        configurePhotoGrid();
        loadImages();
    }

    private void initializeExecutor() {
        executorService = Executors.newFixedThreadPool(4);
    }

    private void configureJWT() {
        if (isJWTTokenMissing()) {
            // Configurar JWT si es necesario
>>>>>>> 4a75fa5c835fed9bb0e511a9929deb9ee421307f
        }
    }

    private boolean isJWTTokenMissing() {
        return HttpService.getInstance().getJwtToken() == null ||
                HttpService.getInstance().getJwtToken().isEmpty();
    }

    private void configurePhotoGrid() {
        photoGrid.setHgap(20);
        photoGrid.setVgap(20);
        photoGrid.setPadding(new Insets(20, 20, 20, 20));
    }

    private void loadImages() {
        String userId = getUserId();
        if (userId == null) {
            showUserIdError();
            return;
        }

        loadImagesFromServer(userId);
    }

    private String getUserId() {
        return HttpService.getInstance().getUserIdFromClaims();
    }

    private void showUserIdError() {
        showAlert(Alert.AlertType.ERROR, "Error", "No se pudo obtener el ID del usuario.");
    }

    private void loadImagesFromServer(String userId) {
        try {
            String jsonResponse = sendGetRequest(userId);
            List<ImagenDTO> images = parseImageResponse(jsonResponse);
            updateCurrentImages(images);
            displayImages();
        } catch (Exception e) {
<<<<<<< HEAD
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
=======
            handleImageLoadError(e);
>>>>>>> 4a75fa5c835fed9bb0e511a9929deb9ee421307f
        }
    }

    private String sendGetRequest(String userId) throws Exception {
        return HttpService.getInstance().sendGetRequest(API_BASE_URL + "/usuario/" + userId);
    }

    private List<ImagenDTO> parseImageResponse(String jsonResponse) throws Exception {
        return Arrays.asList(JsonResponseHandler.parseResponse(jsonResponse, ImagenDTO[].class));
    }

    private void updateCurrentImages(List<ImagenDTO> images) {
        currentImages = images;
    }

    private void handleImageLoadError(Exception e) {
        e.printStackTrace();
        Platform.runLater(() -> {
            clearPhotoGrid();
            if (isNotFoundError(e)) {
                showNoImagesMessage();
            } else {
                showErrorMessage(e.getMessage());
            }
        });
    }

<<<<<<< HEAD
    private void addPhotoToGrid(ImagenDTO imagen) {

        VBox photoContainer = new VBox();
        photoContainer.setAlignment(Pos.CENTER);
        photoContainer.setSpacing(5);
        photoContainer.setPrefWidth(THUMBNAIL_SIZE);


=======
    private boolean isNotFoundError(Exception e) {
        return e.getMessage() != null && e.getMessage().contains("404");
    }

    private void displayImages() {
        Platform.runLater(() -> {
            clearPhotoGrid();

            if (hasNoImages()) {
                showNoImagesMessage();
                return;
            }

            addImagesToGrid();
        });
    }

    private void clearPhotoGrid() {
        photoGrid.getChildren().clear();
    }

    private boolean hasNoImages() {
        return currentImages.isEmpty();
    }

    private void showNoImagesMessage() {
        Label noImagesLabel = createNoImagesLabel();
        photoGrid.getChildren().add(noImagesLabel);
    }

    private Label createNoImagesLabel() {
        Label label = new Label("No se han tomado imágenes");
        label.setFont(Font.font("System", FontWeight.NORMAL, 14));
        label.setStyle("-fx-text-fill: #888888;");
        return label;
    }

    private void showErrorMessage(String message) {
        Label errorLabel = createErrorLabel(message);
        photoGrid.getChildren().add(errorLabel);
    }

    private Label createErrorLabel(String message) {
        Label errorLabel = new Label("Error al cargar las imágenes: " + message);
        errorLabel.setFont(Font.font("System", FontWeight.NORMAL, 14));
        errorLabel.setStyle("-fx-text-fill: #888888;");
        return errorLabel;
    }

    private void addImagesToGrid() {
        for (ImagenDTO imagen : currentImages) {
            addSingleImageToGrid(imagen);
        }
    }

    private void addSingleImageToGrid(ImagenDTO imagen) {
        VBox photoContainer = createPhotoContainer(imagen);
        photoGrid.getChildren().add(photoContainer);
        loadImageAsync(photoContainer, imagen);
    }

    private VBox createPhotoContainer(ImagenDTO imagen) {
        VBox photoContainer = createBaseContainer();

        StackPane photoItem = createPhotoPlaceholder();
        Label nameLabel = createNameLabel(imagen);
        HBox buttonBox = createActionButtons(imagen);

        photoContainer.getChildren().addAll(photoItem, nameLabel, buttonBox);
        return photoContainer;
    }

    private VBox createBaseContainer() {
        VBox container = new VBox();
        container.setAlignment(Pos.CENTER);
        container.setSpacing(5);
        container.setPrefWidth(THUMBNAIL_SIZE);
        return container;
    }

    private StackPane createPhotoPlaceholder() {
>>>>>>> 4a75fa5c835fed9bb0e511a9929deb9ee421307f
        StackPane photoItem = new StackPane();
        photoItem.setPrefSize(THUMBNAIL_SIZE, THUMBNAIL_SIZE);
        photoItem.setStyle("-fx-background-color: #e0e0e0;");
        return photoItem;
    }

<<<<<<< HEAD

=======
    private Label createNameLabel(ImagenDTO imagen) {
>>>>>>> 4a75fa5c835fed9bb0e511a9929deb9ee421307f
        Label nameLabel = new Label(imagen.getNombre());
        nameLabel.setMaxWidth(THUMBNAIL_SIZE);
        nameLabel.setWrapText(true);
        nameLabel.setTextAlignment(TextAlignment.CENTER);
        nameLabel.setAlignment(Pos.CENTER);
        return nameLabel;
    }

<<<<<<< HEAD

=======
    private HBox createActionButtons(ImagenDTO imagen) {
        HBox buttonBox = createButtonContainer();

        Button editButton = createEditButton(imagen);
        Button updateButton = createUpdateButton(imagen);
        Button deleteButton = createDeleteButton(imagen);

        buttonBox.getChildren().addAll(editButton, updateButton, deleteButton);
        return buttonBox;
    }

    private HBox createButtonContainer() {
>>>>>>> 4a75fa5c835fed9bb0e511a9929deb9ee421307f
        HBox buttonBox = new HBox();
        buttonBox.setSpacing(3);
        buttonBox.setAlignment(Pos.CENTER);
        return buttonBox;
    }

<<<<<<< HEAD

=======
    private Button createEditButton(ImagenDTO imagen) {
>>>>>>> 4a75fa5c835fed9bb0e511a9929deb9ee421307f
        Button editButton = new Button("Editar");
        editButton.setStyle("-fx-background-color: #28a745; -fx-text-fill: white; -fx-font-size: 10px;");
        editButton.setPrefWidth(60);
        editButton.setOnAction(e -> handleEditImage(imagen));
        return editButton;
    }

<<<<<<< HEAD

=======
    private Button createUpdateButton(ImagenDTO imagen) {
>>>>>>> 4a75fa5c835fed9bb0e511a9929deb9ee421307f
        Button updateButton = new Button("Actualizar");
        updateButton.setStyle("-fx-background-color: #007bff; -fx-text-fill: white; -fx-font-size: 10px;");
        updateButton.setPrefWidth(60);
        updateButton.setOnAction(e -> handleUpdateImage(imagen));
        return updateButton;
    }

<<<<<<< HEAD

        Button deleteButton = new Button("Eliminar");
        deleteButton.setStyle("-fx-background-color: #dc3545; -fx-text-fill: white; -fx-font-size: 10px;");
        deleteButton.setPrefWidth(60);
        deleteButton.setOnAction(e -> confirmAndDelete(imagen));


        buttonBox.getChildren().addAll(editButton, updateButton, deleteButton);


        photoContainer.getChildren().addAll(photoItem, nameLabel, buttonBox);


        photoGrid.getChildren().add(photoContainer);
=======
    private Button createDeleteButton(ImagenDTO imagen) {
        Button deleteButton = new Button("Eliminar");
        deleteButton.setStyle("-fx-background-color: #dc3545; -fx-text-fill: white; -fx-font-size: 10px;");
        deleteButton.setPrefWidth(60);
        deleteButton.setOnAction(e -> handleDeleteImage(imagen));
        return deleteButton;
    }
>>>>>>> 4a75fa5c835fed9bb0e511a9929deb9ee421307f

    private void loadImageAsync(VBox container, ImagenDTO imagen) {
        executorService.submit(() -> {
            try {
                Image image = createThumbnailImage(imagen);
                Platform.runLater(() -> updateContainerWithImage(container, image, imagen));
            } catch (Exception e) {
                Platform.runLater(() -> handleImageLoadFailure(container));
            }
        });
    }

    private Image createThumbnailImage(ImagenDTO imagen) {
        byte[] imageBytes = imagen.getImagen();
        ByteArrayInputStream bis = new ByteArrayInputStream(imageBytes);
        return new Image(bis, THUMBNAIL_SIZE, THUMBNAIL_SIZE, true, true);
    }

    private void updateContainerWithImage(VBox container, Image image, ImagenDTO imagen) {
        try {
<<<<<<< HEAD

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
=======
            StackPane photoItem = getPhotoItemFromContainer(container);
            ImageView imageView = createImageView(image);

            updatePhotoItem(photoItem, imageView);
            addImageTooltip(photoItem, imagen);
            addImageClickHandler(photoItem, imagen);

        } catch (Exception e) {
            handleImageLoadFailure(container);
>>>>>>> 4a75fa5c835fed9bb0e511a9929deb9ee421307f
        }
    }

    private StackPane getPhotoItemFromContainer(VBox container) {
        return (StackPane) container.getChildren().get(0);
    }

    private ImageView createImageView(Image image) {
        ImageView imageView = new ImageView(image);
        imageView.setFitWidth(THUMBNAIL_SIZE);
        imageView.setFitHeight(THUMBNAIL_SIZE);
        imageView.setPreserveRatio(true);
        return imageView;
    }

    private void updatePhotoItem(StackPane photoItem, ImageView imageView) {
        photoItem.getChildren().clear();
        photoItem.getChildren().add(imageView);
    }

    private void addImageTooltip(StackPane photoItem, ImagenDTO imagen) {
        String date = formatImageDate(imagen);
        Tooltip tooltip = new Tooltip(imagen.getNombre() + "\n" + date);
        Tooltip.install(photoItem, tooltip);
    }

    private String formatImageDate(ImagenDTO imagen) {
        return new SimpleDateFormat("dd/MM/yyyy HH:mm").format(imagen.getFecha());
    }

    private void addImageClickHandler(StackPane photoItem, ImagenDTO imagen) {
        photoItem.setOnMouseClicked(event -> handleImageClick(imagen));
    }

    private void handleImageLoadFailure(VBox container) {
        StackPane photoItem = getPhotoItemFromContainer(container);
        photoItem.setStyle("-fx-background-color: #cccccc;");
    }

    private void handleEditImage(ImagenDTO imagen) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/co/com/cliente/views/editar-fotos-view.fxml"));
            Parent editView = loader.load();

            Object controller = loader.getController();
            if (controller != null) {
                setImageToEdit(controller, imagen);
            }

            navigateToEditView(editView);
            updateApplicationTitle("EDITAR FOTOS");
        } catch (IOException e) {
            showEditLoadError(e);
        }
    }

    private void setImageToEdit(Object controller, ImagenDTO imagen) {
        try {
            controller.getClass().getMethod("setImagenToEdit", ImagenDTO.class).invoke(controller, imagen);
        } catch (Exception e) {
            System.out.println("Error al configurar imagen para editar: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void navigateToEditView(Parent editView) {
        StackPane contentArea = findContentArea();
        if (contentArea != null) {
            replaceContent(contentArea, editView);
        }
    }

    private void updateApplicationTitle(String title) {
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

    private void showEditLoadError(IOException e) {
        e.printStackTrace();
        showAlert(Alert.AlertType.ERROR, "Error", "No se pudo abrir la vista de edición: " + e.getMessage());
    }

    private void handleUpdateImage(ImagenDTO imagen) {
        Dialog<Pair<String, String>> dialog = createUpdateDialog(imagen);
        Optional<Pair<String, String>> result = dialog.showAndWait();

        result.ifPresent(nameResolution ->
                updateImageOnServer(imagen, nameResolution.getKey(), nameResolution.getValue()));
    }

    private Dialog<Pair<String, String>> createUpdateDialog(ImagenDTO imagen) {
        Dialog<Pair<String, String>> dialog = new Dialog<>();
        dialog.setTitle("Actualizar Imagen");
        dialog.setHeaderText("Modificar nombre y resolución");

        ButtonType updateButtonType = new ButtonType("Actualizar", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(updateButtonType, ButtonType.CANCEL);

        GridPane grid = createUpdateDialogGrid(imagen);
        dialog.getDialogPane().setContent(grid);

        configureDialogResultConverter(dialog, updateButtonType, grid);
        return dialog;
    }

    private GridPane createUpdateDialogGrid(ImagenDTO imagen) {
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

        return grid;
    }

    private void configureDialogResultConverter(Dialog<Pair<String, String>> dialog,
                                                ButtonType updateButtonType, GridPane grid) {
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == updateButtonType) {
                TextField nombreField = (TextField) grid.getChildren().get(1);
                TextField resolucionField = (TextField) grid.getChildren().get(3);
                return new Pair<>(nombreField.getText(), resolucionField.getText());
            }
            return null;
        });
    }

    private void updateImageOnServer(ImagenDTO imagen, String newName, String newResolution) {
        try {
            JSONObject jsonRequest = buildUpdateRequest(imagen, newName, newResolution);
            sendUpdateRequest(jsonRequest);
            handleUpdateSuccess();
        } catch (Exception e) {
            handleUpdateError(e);
        }
    }

    private JSONObject buildUpdateRequest(ImagenDTO imagen, String newName, String newResolution) {
        JSONObject jsonRequest = new JSONObject();
        jsonRequest.put("id", imagen.getId());
        jsonRequest.put("imagen", imagen.getImagen());
        jsonRequest.put("nombre", newName);
        jsonRequest.put("resolucion", newResolution);
        jsonRequest.put("fecha", imagen.getFecha().getTime());
        jsonRequest.put("camaraId", imagen.getCamaraId());
        jsonRequest.put("usuarioId", imagen.getUsuarioId());
        return jsonRequest;
    }

    private void sendUpdateRequest(JSONObject jsonRequest) throws Exception {
        HttpService.getInstance().sendPutRequest(API_BASE_URL + "/update", jsonRequest.toString());
    }

    private void handleUpdateSuccess() {
        loadImages();
        showAlert(Alert.AlertType.INFORMATION, "Éxito", "Imagen actualizada correctamente");
    }

    private void handleUpdateError(Exception e) {
        e.printStackTrace();
        showAlert(Alert.AlertType.ERROR, "Error", "No se pudo actualizar la imagen: " + e.getMessage());
    }

    private void handleDeleteImage(ImagenDTO imagen) {
        boolean confirmed = showDeleteConfirmation();
        if (confirmed) {
            deleteImageFromServer(imagen);
        }
    }

    private boolean showDeleteConfirmation() {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirmar eliminación");
        alert.setHeaderText("¿Está seguro de eliminar esta imagen?");
        alert.setContentText("Esta acción no se puede deshacer.");

        Optional<ButtonType> result = alert.showAndWait();
        return result.isPresent() && result.get() == ButtonType.OK;
    }

    private void deleteImageFromServer(ImagenDTO imagen) {
        try {
            sendDeleteRequest(imagen.getId());
            removeImageFromList(imagen);
            refreshImageDisplay();
            showDeleteSuccess();
        } catch (Exception e) {
            handleDeleteError(e);
        }
    }

    private void sendDeleteRequest(Long imageId) throws Exception {
        HttpService.getInstance().sendDeleteRequest(API_BASE_URL + "/" + imageId);
    }

    private void removeImageFromList(ImagenDTO imagen) {
        List<ImagenDTO> updatedImages = new ArrayList<>();
        for (ImagenDTO img : currentImages) {
            if (!img.getId().equals(imagen.getId())) {
                updatedImages.add(img);
            }
        }
        currentImages = updatedImages;
    }

    private void refreshImageDisplay() {
        displayImages();
    }

    private void showDeleteSuccess() {
        showAlert(Alert.AlertType.INFORMATION, "Éxito", "Imagen eliminada correctamente");
    }

    private void handleDeleteError(Exception e) {
        e.printStackTrace();
        showAlert(Alert.AlertType.ERROR, "Error", "No se pudo eliminar la imagen: " + e.getMessage());
    }

    private void handleImageClick(ImagenDTO imagen) {
        try {
            File tempFile = createTempImageFile();
            writeImageToFile(tempFile, imagen);
            openImageFile(tempFile);
        } catch (Exception e) {
            handleImageOpenError(e);
        }
    }

    private File createTempImageFile() throws Exception {
        File tempFile = File.createTempFile("image_", ".png");
        tempFile.deleteOnExit();
        return tempFile;
    }

    private void writeImageToFile(File tempFile, ImagenDTO imagen) throws Exception {
        try (FileOutputStream fos = new FileOutputStream(tempFile)) {
            fos.write(imagen.getImagen());
        }
    }

    private void openImageFile(File tempFile) throws Exception {
        java.awt.Desktop.getDesktop().open(tempFile);
    }

    private void handleImageOpenError(Exception e) {
        e.printStackTrace();
        showAlert(Alert.AlertType.ERROR, "Error", "No se pudo abrir la imagen: " + e.getMessage());
    }

    private StackPane findContentArea() {
        try {
<<<<<<< HEAD

=======
>>>>>>> 4a75fa5c835fed9bb0e511a9929deb9ee421307f
            Parent root = photoGrid.getScene().getRoot();
            return findStackPane(root);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private StackPane findStackPane(Parent parent) {
        if (parent instanceof StackPane &&
                ((StackPane) parent).getId() != null &&
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

    private void replaceContent(StackPane contentArea, Parent newView) {
        contentArea.getChildren().clear();
        contentArea.getChildren().add(newView);
    }

    private Label findTitleLabel(Parent parent) {
        if (parent instanceof Label &&
                ((Label) parent).getId() != null &&
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

<<<<<<< HEAD
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

=======
>>>>>>> 4a75fa5c835fed9bb0e511a9929deb9ee421307f
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