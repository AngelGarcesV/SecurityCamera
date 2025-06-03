package co.com.cliente.controller;

import co.com.cliente.dto.ImagenDTO;
import co.com.cliente.filtros.*;
import co.com.cliente.filtros.pools.*;
import co.com.cliente.httpRequest.HttpService;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.StackPane;
import javafx.stage.FileChooser;
import org.json.JSONObject;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Base64;
import java.util.Date;
import java.util.ResourceBundle;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import co.com.cliente.httpRequest.PropertiesLoader;

public class EditarFotosController implements Initializable {

    @FXML private ImageView imageView;
    @FXML private Label placeholderLabel;
    @FXML private AnchorPane imageContainer;
    @FXML private Button selectImageBtn;
    @FXML private Button rotateBtn;
    @FXML private Button grayscaleBtn;
    @FXML private Button saveImageBtn;
    @FXML private Button backButton;
    @FXML private Button resetBtn;
    @FXML private Label imageInfoLabel;
    @FXML private Button increaseBrightnessBtn;
    @FXML private Button decreaseBrightnessBtn;
    @FXML private Label brightnessLevelLabel;
    @FXML private ProgressBar brightnessProgressBar;
    @FXML private Slider brightnessSlider;
    @FXML private Button darkBtn;
    @FXML private Button normalBtn;
    @FXML private Button brightBtn;

    private ImagenDTO imagenOriginal;
    private BufferedImage currentBufferedImage;
    private BufferedImage originalBufferedImage;
    private boolean hasUnsavedChanges = false;
    private ExecutorService executorService;

    private PoolFiltroBrillo poolBrillo;
    private PoolFiltroRotar poolRotar;
    private PoolFiltroEscalaGrises poolEscalaGrises;

    private float currentBrightnessLevel = 100; // Porcentaje
    private static final float BRIGHTNESS_STEP = 20; // Incremento/decremento por clic
    private static final float MIN_BRIGHTNESS = 10;  // Mínimo 10%
    private static final float MAX_BRIGHTNESS = 300; // Máximo 300%

    private ChangeListener<Number> brightnessSliderListener;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        initializeExecutorService();
        initializeFilterPools();
        initializeUIState();
        initializeBrightnessControls();
        configureButtonStyles();
        configureTooltips();
    }

    private void initializeExecutorService() {
        executorService = Executors.newFixedThreadPool(3);
    }

    private void initializeFilterPools() {
        poolBrillo = PoolFiltroBrillo.getInstance();
        poolRotar = PoolFiltroRotar.getInstance();
        poolEscalaGrises = PoolFiltroEscalaGrises.getInstance();
    }

    private void initializeUIState() {
        updateUIState(false);
        resetBrightnessLevel();
    }

    private void initializeBrightnessControls() {
        setupBrightnessSlider();
        configureButtonHoverEffects();
    }

    private void setupBrightnessSlider() {
        if (brightnessSlider != null) {
            createBrightnessSliderListener();
            configureBrightnessSliderEvents();
        }
    }

    private void createBrightnessSliderListener() {
        brightnessSliderListener = (obs, oldVal, newVal) -> {
            if (!brightnessSlider.isValueChanging()) {
                float newBrightness = newVal.floatValue();
                if (shouldUpdateBrightness(newBrightness)) {
                    applyBrightnessFilter(newBrightness, "Slider");
                }
            }
        };
        brightnessSlider.valueProperty().addListener(brightnessSliderListener);
    }

    private boolean shouldUpdateBrightness(float newBrightness) {
        return Math.abs(newBrightness - currentBrightnessLevel) > 5;
    }

    private void configureBrightnessSliderEvents() {
        brightnessSlider.setOnMouseDragged(e -> {
            if (currentBufferedImage != null) {
                float newBrightness = (float) brightnessSlider.getValue();
                currentBrightnessLevel = newBrightness;
                updateBrightnessIndicators();
            }
        });
    }

    private void configureButtonStyles() {
        selectImageBtn.setStyle("-fx-background-color: #4285f4; -fx-text-fill: white; -fx-font-weight: bold;");
        saveImageBtn.setStyle("-fx-background-color: #28a745; -fx-text-fill: white; -fx-font-weight: bold;");
        backButton.setStyle("-fx-background-color: #6c757d; -fx-text-fill: white; -fx-font-weight: bold;");
        if (resetBtn != null) {
            resetBtn.setStyle("-fx-background-color: #ffc107; -fx-text-fill: black; -fx-font-weight: bold;");
        }
    }

    private void configureTooltips() {
        addTooltipIfExists(increaseBrightnessBtn, "Aumenta el brillo en " + (int)BRIGHTNESS_STEP + "%");
        addTooltipIfExists(decreaseBrightnessBtn, "Reduce el brillo en " + (int)BRIGHTNESS_STEP + "%");
        addTooltipIfExists(brightnessSlider, "Arrastra para ajustar el brillo de " + (int)MIN_BRIGHTNESS + "% a " + (int)MAX_BRIGHTNESS + "%");
        addTooltipIfExists(resetBtn, "Restablecer imagen a su estado original");
        addTooltipIfExists(rotateBtn, "Rotar imagen 90 grados en sentido horario");
        addTooltipIfExists(grayscaleBtn, "Convertir imagen a escala de grises");
        addTooltipIfExists(darkBtn, "Aplicar preset oscuro (50%)");
        addTooltipIfExists(normalBtn, "Aplicar preset normal (100%)");
        addTooltipIfExists(brightBtn, "Aplicar preset brillante (200%)");
    }

    private void addTooltipIfExists(Control control, String text) {
        if (control != null) {
            Tooltip.install(control, new Tooltip(text));
        }
    }

    public void setImagenToEdit(ImagenDTO imagen) {
        if (imagen != null) {
            this.imagenOriginal = imagen;
            loadImageFromDTO(imagen);
        }
    }

    private void loadImageFromDTO(ImagenDTO imagen) {
        Task<Void> loadTask = createImageLoadTask(imagen);
        executorService.submit(loadTask);
    }

    private Task<Void> createImageLoadTask(ImagenDTO imagen) {
        return new Task<Void>() {
            @Override
            protected Void call() throws Exception {
                try {
                    BufferedImage loadedImage = convertDTOToBufferedImage(imagen);
                    Platform.runLater(() -> handleImageLoadSuccess(loadedImage, imagen.getNombre()));
                } catch (Exception e) {
                    Platform.runLater(() -> handleImageLoadError(e));
                }
                return null;
            }
        };
    }

    private BufferedImage convertDTOToBufferedImage(ImagenDTO imagen) throws IOException {
        byte[] imageData = imagen.getImagen();
        ByteArrayInputStream bis = new ByteArrayInputStream(imageData);
        BufferedImage loadedImage = ImageIO.read(bis);
        return loadedImage;
    }

    private void handleImageLoadSuccess(BufferedImage image, String imageName) {
        setImageState(image);
        displayImage(image);
        updateImageInfo(imageName, image);
        configureUIForLoadedImage();
    }

    private void setImageState(BufferedImage image) {
        originalBufferedImage = deepCopy(image);
        currentBufferedImage = deepCopy(image);
    }

    private void configureUIForLoadedImage() {
        updateUIState(true);
        placeholderLabel.setVisible(false);
        resetBrightnessLevel();
        hasUnsavedChanges = false;
        updateSaveButtonState();
        updateResetButtonState();
    }

    private void handleImageLoadError(Exception e) {
        showAlert(Alert.AlertType.ERROR, "Error", "No se pudo cargar la imagen: " + e.getMessage());
    }

    @FXML
    private void handleSelectImage(ActionEvent event) {
        File selectedFile = showFileChooser();
        if (selectedFile != null) {
            loadImageFromFile(selectedFile);
        }
    }

    private File showFileChooser() {
        FileChooser fileChooser = createImageFileChooser();
        return fileChooser.showOpenDialog(selectImageBtn.getScene().getWindow());
    }

    private FileChooser createImageFileChooser() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Seleccionar Imagen");
        addImageFileFilters(fileChooser);
        return fileChooser;
    }

    private void addImageFileFilters(FileChooser fileChooser) {
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Imágenes", "*.png", "*.jpg", "*.jpeg", "*.gif", "*.bmp"),
                new FileChooser.ExtensionFilter("PNG", "*.png"),
                new FileChooser.ExtensionFilter("JPEG", "*.jpg", "*.jpeg"),
                new FileChooser.ExtensionFilter("Todos los archivos", "*.*")
        );
    }

    private void loadImageFromFile(File file) {
        Task<Void> loadTask = createFileLoadTask(file);
        executorService.submit(loadTask);
    }

    private Task<Void> createFileLoadTask(File file) {
        return new Task<Void>() {
            @Override
            protected Void call() throws Exception {
                try {
                    BufferedImage loadedImage = ImageIO.read(file);
                    Platform.runLater(() -> handleImageLoadSuccess(loadedImage, file.getName()));
                } catch (Exception e) {
                    Platform.runLater(() -> handleImageLoadError(e));
                }
                return null;
            }
        };
    }

    @FXML
    private void handleIncreaseBrightness(ActionEvent event) {
        if (canModifyImage()) {
            float newBrightness = calculateNewBrightness(BRIGHTNESS_STEP);
            applyBrightnessFilter(newBrightness, "Aumentar");
        }
    }

    @FXML
    private void handleDecreaseBrightness(ActionEvent event) {
        if (canModifyImage()) {
            float newBrightness = calculateNewBrightness(-BRIGHTNESS_STEP);
            applyBrightnessFilter(newBrightness, "Disminuir");
        }
    }

    private boolean canModifyImage() {
        return currentBufferedImage != null;
    }

    private float calculateNewBrightness(float adjustment) {
        if (adjustment > 0) {
            return Math.min(MAX_BRIGHTNESS, currentBrightnessLevel + adjustment);
        } else {
            return Math.max(MIN_BRIGHTNESS, currentBrightnessLevel + adjustment);
        }
    }

    @FXML
    private void handleBrightnessSlider() {
        if (canModifyImage() && !brightnessSlider.isValueChanging()) {
            float newBrightness = (float) brightnessSlider.getValue();
            if (shouldUpdateBrightness(newBrightness)) {
                applyBrightnessFilter(newBrightness, "Slider");
            }
        }
    }

    @FXML
    private void handleDarkPreset(ActionEvent event) {
        if (canModifyImage()) {
            applyBrightnessFilter(50, "Preset oscuro");
        }
    }

    @FXML
    private void handleNormalPreset(ActionEvent event) {
        if (canModifyImage()) {
            applyBrightnessFilter(100, "Preset normal");
        }
    }

    @FXML
    private void handleBrightPreset(ActionEvent event) {
        if (canModifyImage()) {
            applyBrightnessFilter(200, "Preset brillante");
        }
    }

    @FXML
    private void handleRotate(ActionEvent event) {
        if (canModifyImage()) {
            applyRotateFilter();
        }
    }

    @FXML
    private void handleGrayscale(ActionEvent event) {
        if (canModifyImage()) {
            applyGrayscaleFilter();
        }
    }

    @FXML
    private void handleResetImage(ActionEvent event) {
        if (canResetImage()) {
            resetToOriginalImage();
        }
    }

    private boolean canResetImage() {
        return originalBufferedImage != null;
    }

    private void resetToOriginalImage() {
        currentBufferedImage = deepCopy(originalBufferedImage);
        displayImage(currentBufferedImage);
        resetBrightnessLevel();
        markImageAsUnmodified();
        updateImageInfo("Imagen restablecida a original", currentBufferedImage);
    }

    private void markImageAsUnmodified() {
        hasUnsavedChanges = false;
        updateSaveButtonState();
        updateResetButtonState();
    }

    @FXML
    private void handleSaveImage(ActionEvent event) {
        if (canSaveImage()) {
            saveEditedImage();
        }
    }

    private boolean canSaveImage() {
        return currentBufferedImage != null && hasUnsavedChanges;
    }

    @FXML
    private void handleBackButton(ActionEvent event) {
        if (hasUnsavedChanges) {
            handleUnsavedChangesOnExit();
        } else {
            navigateBackToPhotos();
        }
    }

    private void handleUnsavedChangesOnExit() {
        Alert alert = createUnsavedChangesAlert();
        alert.showAndWait().ifPresent(response -> {
            if (response.getButtonData().isDefaultButton()) {
                saveEditedImage();
            }
            navigateBackToPhotos();
        });
    }

    private Alert createUnsavedChangesAlert() {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Cambios sin guardar");
        alert.setHeaderText("Tienes cambios sin guardar");
        alert.setContentText("¿Deseas guardar los cambios antes de volver a la galería?");
        return alert;
    }

    private void applyBrightnessFilter(float targetBrightness, String action) {
        Task<Void> filterTask = createBrightnessFilterTask(targetBrightness, action);
        executorService.submit(filterTask);
    }

    private Task<Void> createBrightnessFilterTask(float targetBrightness, String action) {
        return new Task<Void>() {
            @Override
            protected Void call() throws Exception {
                showBrightnessProcessingIndicator();

                FiltroBrillo filtro = poolBrillo.obtener();
                try {
                    long startTime = System.currentTimeMillis();
                    BufferedImage result = applyBrightnessToImage(filtro, targetBrightness);
                    long processingTime = System.currentTimeMillis() - startTime;

                    Platform.runLater(() -> handleBrightnessFilterSuccess(result, targetBrightness, action, processingTime));
                } catch (Exception e) {
                    Platform.runLater(() -> handleBrightnessFilterError(e));
                } finally {
                    poolBrillo.liberar(filtro);
                }
                return null;
            }
        };
    }

    private void showBrightnessProcessingIndicator() {
        Platform.runLater(() -> {
            if (brightnessLevelLabel != null) {
                brightnessLevelLabel.setText("Procesando...");
            }
            if (brightnessProgressBar != null) {
                brightnessProgressBar.setProgress(-1); // Indeterminado
            }
        });
    }

    private BufferedImage applyBrightnessToImage(FiltroBrillo filtro, float targetBrightness) {
        filtro.setBrilloRelativo((int) targetBrightness);
        BufferedImage sourceImage = getSourceImageForBrightness();
        return filtro.aplicar(sourceImage);
    }

    private BufferedImage getSourceImageForBrightness() {
        return originalBufferedImage != null ? originalBufferedImage : currentBufferedImage;
    }

    private long measureProcessingTime(Runnable operation) {
        long startTime = System.currentTimeMillis();
        operation.run();
        return System.currentTimeMillis() - startTime;
    }

    private void handleBrightnessFilterSuccess(BufferedImage result, float targetBrightness, String action, long processingTime) {
        if (result != null) {
            updateImageAfterBrightnessFilter(result, targetBrightness);
            updateBrightnessUI(targetBrightness, action, processingTime);
            logBrightnessOperation(targetBrightness);
        } else {
            handleBrightnessFilterError(new Exception("Resultado nulo del filtro"));
        }
    }

    private void updateImageAfterBrightnessFilter(BufferedImage result, float targetBrightness) {
        currentBufferedImage = result;
        currentBrightnessLevel = targetBrightness;
        displayImage(currentBufferedImage);
        markImageAsModified();
    }

    private void updateBrightnessUI(float targetBrightness, String action, long processingTime) {
        updateBrightnessButtonsState();
        String message = String.format("%s brillo: %.0f%% (Tiempo: %dms)", action, targetBrightness, processingTime);
        updateImageInfo(message, currentBufferedImage);
    }

    private void logBrightnessOperation(float targetBrightness) {
        System.out.println(String.format("Brillo aplicado: %.0f%%", targetBrightness));
    }

    private void handleBrightnessFilterError(Exception e) {
        showAlert(Alert.AlertType.ERROR, "Error", "Error al procesar brillo: " + e.getMessage());
        updateBrightnessIndicators(); // Restaurar indicadores
        e.printStackTrace();
    }

    private void applyRotateFilter() {
        Task<Void> filterTask = createRotateFilterTask();
        executorService.submit(filterTask);
    }

    private Task<Void> createRotateFilterTask() {
        return new Task<Void>() {
            @Override
            protected Void call() throws Exception {
                FiltroRotar filtro = poolRotar.obtener();
                try {
                    long startTime = System.currentTimeMillis();
                    BufferedImage result = applyRotationToImage(filtro);
                    long endTime = System.currentTimeMillis();

                    Platform.runLater(() -> handleRotateFilterSuccess(result, endTime - startTime));
                } finally {
                    poolRotar.liberar(filtro);
                }
                return null;
            }
        };
    }

    private BufferedImage applyRotationToImage(FiltroRotar filtro) {
        filtro.setRotacion(org.imgscalr.Scalr.Rotation.CW_90);
        return filtro.aplicar(currentBufferedImage);
    }

    private void handleRotateFilterSuccess(BufferedImage result, long processingTime) {
        if (result != null) {
            updateImageAfterRotation(result);
            updateImageInfo("Imagen rotada 90° (Tiempo: " + processingTime + "ms)", currentBufferedImage);
        }
    }

    private void updateImageAfterRotation(BufferedImage result) {
        currentBufferedImage = result;
        originalBufferedImage = deepCopy(result); // Actualizar original para rotación
        displayImage(currentBufferedImage);
        markImageAsModified();
    }

    private void applyGrayscaleFilter() {
        Task<Void> filterTask = createGrayscaleFilterTask();
        executorService.submit(filterTask);
    }

    private Task<Void> createGrayscaleFilterTask() {
        return new Task<Void>() {
            @Override
            protected Void call() throws Exception {
                FiltroEscalaGrises filtro = poolEscalaGrises.obtener();
                try {
                    long startTime = System.currentTimeMillis();
                    BufferedImage result = filtro.aplicar(currentBufferedImage);
                    long endTime = System.currentTimeMillis();

                    Platform.runLater(() -> handleGrayscaleFilterSuccess(result, endTime - startTime));
                } finally {
                    poolEscalaGrises.liberar(filtro);
                }
                return null;
            }
        };
    }

    private void handleGrayscaleFilterSuccess(BufferedImage result, long processingTime) {
        if (result != null) {
            updateImageAfterGrayscale(result);
            updateImageInfo("Filtro escala de grises aplicado (Tiempo: " + processingTime + "ms)", currentBufferedImage);
        }
    }

    private void updateImageAfterGrayscale(BufferedImage result) {
        currentBufferedImage = result;
        displayImage(currentBufferedImage);
        markImageAsModified();
    }

    private void markImageAsModified() {
        hasUnsavedChanges = true;
        updateSaveButtonState();
        updateResetButtonState();
    }

    private void saveEditedImage() {
        if (currentBufferedImage == null) return;

        Task<Void> saveTask = createSaveTask();
        executorService.submit(saveTask);
    }

    private Task<Void> createSaveTask() {
        return new Task<Void>() {
            @Override
            protected Void call() throws Exception {
                try {
                    String base64Image = convertImageToBase64();
                    JSONObject jsonRequest = buildSaveRequest(base64Image);
                    sendSaveRequest(jsonRequest);

                    Platform.runLater(() -> handleSaveSuccess());
                } catch (Exception e) {
                    Platform.runLater(() -> handleSaveError(e));
                }
                return null;
            }
        };
    }

    private String convertImageToBase64() throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageIO.write(currentBufferedImage, "PNG", baos);
        byte[] imageBytes = baos.toByteArray();
        return Base64.getEncoder().encodeToString(imageBytes);
    }

    private JSONObject buildSaveRequest(String base64Image) {
        JSONObject jsonRequest = new JSONObject();
        String timestamp = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss").format(new Date());

        setBasicImageInfo(jsonRequest, timestamp);
        setImageData(jsonRequest, base64Image);
        setUserAndCameraInfo(jsonRequest);

        return jsonRequest;
    }

    private void setBasicImageInfo(JSONObject jsonRequest, String timestamp) {
        String editedImageName = getEditedImageName(timestamp);
        jsonRequest.put("nombre", editedImageName);
        jsonRequest.put("resolucion", currentBufferedImage.getWidth() + "x" + currentBufferedImage.getHeight());

        SimpleDateFormat isoFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
        jsonRequest.put("fecha", isoFormat.format(new Date()));
    }

    private String getEditedImageName(String timestamp) {
        if (imagenOriginal != null) {
            return imagenOriginal.getNombre() + "_editada_" + timestamp;
        } else {
            return "Editada_" + timestamp;
        }
    }

    private void setImageData(JSONObject jsonRequest, String base64Image) {
        jsonRequest.put("imagen", base64Image);
    }

    private void setUserAndCameraInfo(JSONObject jsonRequest) {
        if (imagenOriginal != null) {
            jsonRequest.put("camaraId", imagenOriginal.getCamaraId());
            jsonRequest.put("usuarioId", imagenOriginal.getUsuarioId());
        } else {
            String userId = HttpService.getInstance().getUserIdFromClaims();
            if (userId != null) {
                jsonRequest.put("usuarioId", Integer.valueOf(userId));
            }
            jsonRequest.put("camaraId", 1); // Valor por defecto
        }
    }

    private void sendSaveRequest(JSONObject jsonRequest) throws Exception {
        HttpService.getInstance().sendPostRequest(
                PropertiesLoader.getBaseUrl() + "/api/imagenes/save",
                jsonRequest.toString()
        );
    }

    private void handleSaveSuccess() {
        markImageAsUnmodified();
        showAlert(Alert.AlertType.INFORMATION, "Éxito", "Imagen guardada correctamente en el servidor");
        updateImageInfo("Imagen guardada exitosamente", currentBufferedImage);
    }

    private void handleSaveError(Exception e) {
        showAlert(Alert.AlertType.ERROR, "Error", "No se pudo guardar la imagen: " + e.getMessage());
    }

    private void displayImage(BufferedImage bufferedImage) {
        if (bufferedImage != null) {
            try {
                Image fxImage = convertBufferedImageToFXImage(bufferedImage);
                updateImageView(fxImage);
            } catch (IOException e) {
                showAlert(Alert.AlertType.ERROR, "Error", "No se pudo mostrar la imagen: " + e.getMessage());
            }
        }
    }

    private Image convertBufferedImageToFXImage(BufferedImage bufferedImage) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageIO.write(bufferedImage, "PNG", baos);
        ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
        return new Image(bais);
    }

    private void updateImageView(Image fxImage) {
        imageView.setImage(fxImage);
        imageView.setVisible(true);
        configureImageViewSize();
    }

    private void configureImageViewSize() {
        imageView.setFitWidth(580);
        imageView.setFitHeight(400);
        imageView.setPreserveRatio(true);
    }

    private void updateImageInfo(String action, BufferedImage image) {
        if (image != null) {
            String info = buildImageInfoText(action, image);
            imageInfoLabel.setText(info);
        } else {
            imageInfoLabel.setText("Ninguna imagen seleccionada");
        }
    }

    private String buildImageInfoText(String action, BufferedImage image) {
        return String.format("Acción: %s\nDimensiones: %dx%d\nTipo: %s\nBrillo: %.0f%%",
                action,
                image.getWidth(),
                image.getHeight(),
                getImageTypeString(image.getType()),
                currentBrightnessLevel
        );
    }

    private String getImageTypeString(int type) {
        switch (type) {
            case BufferedImage.TYPE_INT_RGB: return "RGB";
            case BufferedImage.TYPE_INT_ARGB: return "ARGB";
            case BufferedImage.TYPE_BYTE_GRAY: return "Escala de Grises";
            case BufferedImage.TYPE_3BYTE_BGR: return "BGR";
            default: return "Tipo " + type;
        }
    }

    private void resetBrightnessLevel() {
        currentBrightnessLevel = 100; // Brillo normal
        updateBrightnessButtonsState();
        updateBrightnessIndicators();
    }

    private void updateBrightnessButtonsState() {
        updateBrightnessButtonAvailability();
        updateBrightnessButtonLabels();
        updatePresetButtonsStyle();
        updateBrightnessIndicators();
    }

    private void updateBrightnessButtonAvailability() {
        boolean canIncrease = currentBrightnessLevel < MAX_BRIGHTNESS;
        boolean canDecrease = currentBrightnessLevel > MIN_BRIGHTNESS;

        increaseBrightnessBtn.setDisable(!canIncrease);
        decreaseBrightnessBtn.setDisable(!canDecrease);
    }

    private void updateBrightnessButtonLabels() {
        boolean canIncrease = currentBrightnessLevel < MAX_BRIGHTNESS;
        boolean canDecrease = currentBrightnessLevel > MIN_BRIGHTNESS;

        increaseBrightnessBtn.setText(canIncrease ? "Aumentar (+)" : "Máximo");
        decreaseBrightnessBtn.setText(canDecrease ? "Disminuir (-)" : "Mínimo");
    }

    private void updateBrightnessIndicators() {
        updateBrightnessLevelLabel();
        updateBrightnessProgressBar();
        updateBrightnessSliderValue();
    }

    private void updateBrightnessLevelLabel() {
        if (brightnessLevelLabel != null) {
            brightnessLevelLabel.setText(String.format("%.0f%%", currentBrightnessLevel));
        }
    }

    private void updateBrightnessProgressBar() {
        if (brightnessProgressBar != null) {
            double progress = calculateBrightnessProgress();
            brightnessProgressBar.setProgress(Math.max(0, Math.min(1, progress)));
            setBrightnessProgressBarColor();
        }
    }

    private double calculateBrightnessProgress() {
        return (currentBrightnessLevel - MIN_BRIGHTNESS) / (MAX_BRIGHTNESS - MIN_BRIGHTNESS);
    }

    private void setBrightnessProgressBarColor() {
        if (currentBrightnessLevel < 70) {
            brightnessProgressBar.setStyle("-fx-accent: #dc3545;"); // Rojo para bajo
        } else if (currentBrightnessLevel > 150) {
            brightnessProgressBar.setStyle("-fx-accent: #ffc107;"); // Amarillo para alto
        } else {
            brightnessProgressBar.setStyle("-fx-accent: #28a745;"); // Verde para normal
        }
    }

    private void updateBrightnessSliderValue() {
        if (brightnessSlider != null && brightnessSliderListener != null) {
            // Actualizar slider sin disparar el evento
            brightnessSlider.valueProperty().removeListener(brightnessSliderListener);
            brightnessSlider.setValue(currentBrightnessLevel);
            brightnessSlider.valueProperty().addListener(brightnessSliderListener);
        }
    }

    private void updatePresetButtonsStyle() {
        String normalStyle = "-fx-background-color: #6c757d; -fx-text-fill: white; -fx-font-size: 10px;";
        String activeStyle = "-fx-background-color: #007bff; -fx-text-fill: white; -fx-font-size: 10px; -fx-font-weight: bold;";

        resetPresetButtonStyles(normalStyle);
        highlightActivePreset(activeStyle);
    }

    private void resetPresetButtonStyles(String normalStyle) {
        if (darkBtn != null) darkBtn.setStyle(normalStyle);
        if (normalBtn != null) normalBtn.setStyle(normalStyle);
        if (brightBtn != null) brightBtn.setStyle(normalStyle);
    }

    private void highlightActivePreset(String activeStyle) {
        if (isCurrentlyDark() && darkBtn != null) {
            darkBtn.setStyle(activeStyle);
        } else if (isCurrentlyNormal() && normalBtn != null) {
            normalBtn.setStyle(activeStyle);
        } else if (isCurrentlyBright() && brightBtn != null) {
            brightBtn.setStyle(activeStyle);
        }
    }

    private boolean isCurrentlyDark() {
        return currentBrightnessLevel <= 60;
    }

    private boolean isCurrentlyNormal() {
        return currentBrightnessLevel >= 90 && currentBrightnessLevel <= 110;
    }

    private boolean isCurrentlyBright() {
        return currentBrightnessLevel >= 180;
    }

    private void updateUIState(boolean imageLoaded) {
        updateBasicControlsState(imageLoaded);
        updateAdvancedControlsState(imageLoaded);
        updateSaveButtonState();
        updateResetButtonState();

        if (imageLoaded) {
            updateBrightnessButtonsState();
        } else {
            resetBrightnessDisplayWhenNoImage();
        }
    }

    private void updateBasicControlsState(boolean imageLoaded) {
        increaseBrightnessBtn.setDisable(!imageLoaded);
        decreaseBrightnessBtn.setDisable(!imageLoaded);
        rotateBtn.setDisable(!imageLoaded);
        grayscaleBtn.setDisable(!imageLoaded);
    }

    private void updateAdvancedControlsState(boolean imageLoaded) {
        if (brightnessSlider != null) {
            brightnessSlider.setDisable(!imageLoaded);
        }
        if (darkBtn != null) darkBtn.setDisable(!imageLoaded);
        if (normalBtn != null) normalBtn.setDisable(!imageLoaded);
        if (brightBtn != null) brightBtn.setDisable(!imageLoaded);
    }

    private void resetBrightnessDisplayWhenNoImage() {
        if (brightnessLevelLabel != null) {
            brightnessLevelLabel.setText("--");
        }
        if (brightnessProgressBar != null) {
            brightnessProgressBar.setProgress(0);
        }
    }

    private void updateSaveButtonState() {
        saveImageBtn.setDisable(!hasUnsavedChanges || currentBufferedImage == null);
    }

    private void updateResetButtonState() {
        if (resetBtn != null) {
            resetBtn.setDisable(!hasUnsavedChanges || originalBufferedImage == null);
        }
    }

    private void configureButtonHoverEffects() {
        configureSelectImageButtonHover();
        configureBrightnessButtonsHover();
        configureFilterButtonsHover();
        configureActionButtonsHover();
    }

    private void configureSelectImageButtonHover() {
        selectImageBtn.setOnMouseEntered(e ->
                selectImageBtn.setStyle("-fx-background-color: #3367d6; -fx-text-fill: white; -fx-font-weight: bold;"));
        selectImageBtn.setOnMouseExited(e ->
                selectImageBtn.setStyle("-fx-background-color: #4285f4; -fx-text-fill: white; -fx-font-weight: bold;"));
    }

    private void configureBrightnessButtonsHover() {
        increaseBrightnessBtn.setOnMouseEntered(e ->
                increaseBrightnessBtn.setStyle("-fx-background-color: #4285f4; -fx-text-fill: white; -fx-font-weight: bold;"));
        increaseBrightnessBtn.setOnMouseExited(e ->
                increaseBrightnessBtn.setStyle("-fx-background-color: #5ba3f5; -fx-text-fill: white; -fx-font-weight: bold;"));

        decreaseBrightnessBtn.setOnMouseEntered(e ->
                decreaseBrightnessBtn.setStyle("-fx-background-color: #218838; -fx-text-fill: white; -fx-font-weight: bold;"));
        decreaseBrightnessBtn.setOnMouseExited(e ->
                decreaseBrightnessBtn.setStyle("-fx-background-color: #28a745; -fx-text-fill: white; -fx-font-weight: bold;"));
    }

    private void configureFilterButtonsHover() {
        rotateBtn.setOnMouseEntered(e ->
                rotateBtn.setStyle("-fx-background-color: #e0a800; -fx-text-fill: black; -fx-font-weight: bold;"));
        rotateBtn.setOnMouseExited(e ->
                rotateBtn.setStyle("-fx-background-color: #ffc107; -fx-text-fill: black; -fx-font-weight: bold;"));

        grayscaleBtn.setOnMouseEntered(e ->
                grayscaleBtn.setStyle("-fx-background-color: #c82333; -fx-text-fill: white; -fx-font-weight: bold;"));
        grayscaleBtn.setOnMouseExited(e ->
                grayscaleBtn.setStyle("-fx-background-color: #dc3545; -fx-text-fill: white; -fx-font-weight: bold;"));
    }

    private void configureActionButtonsHover() {
        configureSaveButtonHover();
        configureResetButtonHover();
        configureBackButtonHover();
    }

    private void configureSaveButtonHover() {
        saveImageBtn.setOnMouseEntered(e -> {
            if (!saveImageBtn.isDisabled()) {
                saveImageBtn.setStyle("-fx-background-color: #218838; -fx-text-fill: white; -fx-font-weight: bold;");
            }
        });
        saveImageBtn.setOnMouseExited(e -> {
            if (!saveImageBtn.isDisabled()) {
                saveImageBtn.setStyle("-fx-background-color: #28a745; -fx-text-fill: white; -fx-font-weight: bold;");
            }
        });
    }

    private void configureResetButtonHover() {
        if (resetBtn != null) {
            resetBtn.setOnMouseEntered(e -> {
                if (!resetBtn.isDisabled()) {
                    resetBtn.setStyle("-fx-background-color: #e0a800; -fx-text-fill: black; -fx-font-weight: bold;");
                }
            });
            resetBtn.setOnMouseExited(e -> {
                if (!resetBtn.isDisabled()) {
                    resetBtn.setStyle("-fx-background-color: #ffc107; -fx-text-fill: black; -fx-font-weight: bold;");
                }
            });
        }
    }

    private void configureBackButtonHover() {
        backButton.setOnMouseEntered(e ->
                backButton.setStyle("-fx-background-color: #5a6268; -fx-text-fill: white; -fx-font-weight: bold;"));
        backButton.setOnMouseExited(e ->
                backButton.setStyle("-fx-background-color: #6c757d; -fx-text-fill: white; -fx-font-weight: bold;"));
    }

    private void navigateBackToPhotos() {
        try {
            Parent fotosView = loadFotosView();
            replaceCurrentContent(fotosView);
            updateApplicationTitle("FOTOS");
        } catch (IOException e) {
            handleNavigationError(e);
        }
    }

    private Parent loadFotosView() throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/co/com/cliente/views/fotos-view.fxml"));
        return loader.load();
    }

    private void replaceCurrentContent(Parent newView) {
        StackPane contentArea = findContentArea();
        if (contentArea != null) {
            contentArea.getChildren().clear();
            contentArea.getChildren().add(newView);
        }
    }

    private void updateApplicationTitle(String title) {
        try {
            Parent root = backButton.getScene().getRoot();
            Label titleLabel = findTitleLabel(root);
            if (titleLabel != null) {
                titleLabel.setText(title);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void handleNavigationError(IOException e) {
        e.printStackTrace();
        showAlert(Alert.AlertType.ERROR, "Error", "No se pudo volver a la galería: " + e.getMessage());
    }

    private BufferedImage deepCopy(BufferedImage source) {
        if (source == null) return null;
        BufferedImage copy = new BufferedImage(source.getWidth(), source.getHeight(), source.getType());
        copy.createGraphics().drawImage(source, 0, 0, null);
        return copy;
    }

    private StackPane findContentArea() {
        try {
            Parent root = backButton.getScene().getRoot();
            return findStackPane(root);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private StackPane findStackPane(Parent parent) {
        if (isTargetStackPane(parent)) {
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

    private boolean isTargetStackPane(Parent parent) {
        return parent instanceof StackPane &&
                ((StackPane) parent).getId() != null &&
                ((StackPane) parent).getId().equals("contentArea");
    }

    private Label findTitleLabel(Parent parent) {
        if (isTargetTitleLabel(parent)) {
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

    private boolean isTargetTitleLabel(Parent parent) {
        return parent instanceof Label &&
                ((Label) parent).getId() != null &&
                ((Label) parent).getId().equals("titleLabel");
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
        shutdownExecutorService();
        cleanupBrightnessSliderListener();
        clearImageMemory();
    }

    private void shutdownExecutorService() {
        if (executorService != null && !executorService.isShutdown()) {
            executorService.shutdown();
            try {
                if (!executorService.awaitTermination(800, TimeUnit.MILLISECONDS)) {
                    executorService.shutdownNow();
                }
            } catch (InterruptedException e) {
                executorService.shutdownNow();
            }
        }
    }

    private void cleanupBrightnessSliderListener() {
        if (brightnessSlider != null && brightnessSliderListener != null) {
            brightnessSlider.valueProperty().removeListener(brightnessSliderListener);
        }
    }

    private void clearImageMemory() {
        currentBufferedImage = null;
        originalBufferedImage = null;
        imagenOriginal = null;
    }

    public boolean hasUnsavedChanges() {
        return hasUnsavedChanges;
    }

    public BufferedImage getCurrentImage() {
        return currentBufferedImage;
    }

    public float getCurrentBrightnessLevel() {
        return currentBrightnessLevel;
    }

    public void setBrightnessLevel(float level) {
        if (canSetBrightnessLevel(level)) {
            applyBrightnessFilter(level, "Programático");
        }
    }

    private boolean canSetBrightnessLevel(float level) {
        return currentBufferedImage != null &&
                level >= MIN_BRIGHTNESS &&
                level <= MAX_BRIGHTNESS;
    }

    public void forceSave() {
        if (currentBufferedImage != null) {
            saveEditedImage();
        }
    }
}