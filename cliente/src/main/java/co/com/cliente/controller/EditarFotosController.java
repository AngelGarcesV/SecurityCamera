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

public class EditarFotosController implements Initializable {

    // Componentes FXML - Vista de imagen
    @FXML
    private ImageView imageView;

    @FXML
    private Label placeholderLabel;

    @FXML
    private AnchorPane imageContainer;

    // Componentes FXML - Controles básicos
    @FXML
    private Button selectImageBtn;

    @FXML
    private Button rotateBtn;

    @FXML
    private Button grayscaleBtn;

    @FXML
    private Button saveImageBtn;

    @FXML
    private Button backButton;

    @FXML
    private Button resetBtn;

    @FXML
    private Label imageInfoLabel;

    // Componentes FXML - Controles de brillo
    @FXML
    private Button increaseBrightnessBtn;

    @FXML
    private Button decreaseBrightnessBtn;

    @FXML
    private Label brightnessLevelLabel;

    @FXML
    private ProgressBar brightnessProgressBar;

    @FXML
    private Slider brightnessSlider;

    @FXML
    private Button darkBtn;

    @FXML
    private Button normalBtn;

    @FXML
    private Button brightBtn;

    // Variables de control
    private ImagenDTO imagenOriginal;
    private BufferedImage currentBufferedImage;
    private BufferedImage originalBufferedImage;
    private boolean hasUnsavedChanges = false;
    private ExecutorService executorService;

    // Pools de filtros
    private PoolFiltroBrillo poolBrillo;
    private PoolFiltroRotar poolRotar;
    private PoolFiltroEscalaGrises poolEscalaGrises;

    // Control de brillo
    private float currentBrightnessLevel = 100; // Porcentaje
    private static final float BRIGHTNESS_STEP = 20; // Incremento/decremento por clic
    private static final float MIN_BRIGHTNESS = 10;  // Mínimo 10%
    private static final float MAX_BRIGHTNESS = 300; // Máximo 300%

    // Listener para el slider
    private ChangeListener<Number> brightnessSliderListener;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        executorService = Executors.newFixedThreadPool(3);

        // Inicializar pools
        poolBrillo = PoolFiltroBrillo.getInstance();
        poolRotar = PoolFiltroRotar.getInstance();
        poolEscalaGrises = PoolFiltroEscalaGrises.getInstance();

        // Configurar estado inicial
        updateUIState(false);
        resetBrightnessLevel();

        // Configurar listener del slider
        initializeBrightnessSlider();

        // Configurar estilos hover
        configureButtonHoverEffects();

        // Configurar tooltips
        configureTooltips();
    }

    /**
     * Método llamado desde FotosController para establecer la imagen a editar
     */
    public void setImagenToEdit(ImagenDTO imagen) {
        if (imagen != null) {
            this.imagenOriginal = imagen;
            loadImageFromDTO(imagen);
        }
    }

    // ==================== MANEJADORES DE EVENTOS ====================

    @FXML
    private void handleSelectImage(ActionEvent event) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Seleccionar Imagen");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Imágenes", "*.png", "*.jpg", "*.jpeg", "*.gif", "*.bmp"),
                new FileChooser.ExtensionFilter("PNG", "*.png"),
                new FileChooser.ExtensionFilter("JPEG", "*.jpg", "*.jpeg"),
                new FileChooser.ExtensionFilter("Todos los archivos", "*.*")
        );

        File selectedFile = fileChooser.showOpenDialog(selectImageBtn.getScene().getWindow());
        if (selectedFile != null) {
            loadImageFromFile(selectedFile);
        }
    }

    @FXML
    private void handleIncreaseBrightness(ActionEvent event) {
        if (currentBufferedImage != null) {
            float newBrightness = Math.min(MAX_BRIGHTNESS, currentBrightnessLevel + BRIGHTNESS_STEP);
            applyBrightnessFilter(newBrightness, "Aumentar");
        }
    }

    @FXML
    private void handleDecreaseBrightness(ActionEvent event) {
        if (currentBufferedImage != null) {
            float newBrightness = Math.max(MIN_BRIGHTNESS, currentBrightnessLevel - BRIGHTNESS_STEP);
            applyBrightnessFilter(newBrightness, "Disminuir");
        }
    }

    @FXML
    private void handleBrightnessSlider() {
        if (currentBufferedImage != null && !brightnessSlider.isValueChanging()) {
            float newBrightness = (float) brightnessSlider.getValue();
            if (Math.abs(newBrightness - currentBrightnessLevel) > 5) { // Evitar cambios menores
                applyBrightnessFilter(newBrightness, "Slider");
            }
        }
    }

    @FXML
    private void handleDarkPreset(ActionEvent event) {
        if (currentBufferedImage != null) {
            applyBrightnessFilter(50, "Preset oscuro");
        }
    }

    @FXML
    private void handleNormalPreset(ActionEvent event) {
        if (currentBufferedImage != null) {
            applyBrightnessFilter(100, "Preset normal");
        }
    }

    @FXML
    private void handleBrightPreset(ActionEvent event) {
        if (currentBufferedImage != null) {
            applyBrightnessFilter(200, "Preset brillante");
        }
    }

    @FXML
    private void handleRotate(ActionEvent event) {
        if (currentBufferedImage != null) {
            applyRotateFilter();
        }
    }

    @FXML
    private void handleGrayscale(ActionEvent event) {
        if (currentBufferedImage != null) {
            applyGrayscaleFilter();
        }
    }

    @FXML
    private void handleResetImage(ActionEvent event) {
        if (originalBufferedImage != null) {
            currentBufferedImage = deepCopy(originalBufferedImage);
            displayImage(currentBufferedImage);
            resetBrightnessLevel();
            hasUnsavedChanges = false;
            updateSaveButtonState();
            updateResetButtonState();
            updateImageInfo("Imagen restablecida a original", currentBufferedImage);
        }
    }

    @FXML
    private void handleSaveImage(ActionEvent event) {
        if (currentBufferedImage != null && hasUnsavedChanges) {
            saveEditedImage();
        }
    }

    @FXML
    private void handleBackButton(ActionEvent event) {
        if (hasUnsavedChanges) {
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Cambios sin guardar");
            alert.setHeaderText("Tienes cambios sin guardar");
            alert.setContentText("¿Deseas guardar los cambios antes de volver a la galería?");

            alert.showAndWait().ifPresent(response -> {
                if (response.getButtonData().isDefaultButton()) {
                    saveEditedImage();
                }
                goBackToPhotosView();
            });
        } else {
            goBackToPhotosView();
        }
    }

    // ==================== MÉTODOS DE CARGA DE IMÁGENES ====================

    private void loadImageFromDTO(ImagenDTO imagen) {
        Task<Void> loadTask = new Task<Void>() {
            @Override
            protected Void call() throws Exception {
                try {
                    byte[] imageData = imagen.getImagen();
                    ByteArrayInputStream bis = new ByteArrayInputStream(imageData);
                    originalBufferedImage = ImageIO.read(bis);
                    currentBufferedImage = deepCopy(originalBufferedImage);

                    Platform.runLater(() -> {
                        displayImage(currentBufferedImage);
                        updateImageInfo(imagen.getNombre(), originalBufferedImage);
                        updateUIState(true);
                        placeholderLabel.setVisible(false);
                        resetBrightnessLevel();
                        hasUnsavedChanges = false;
                        updateSaveButtonState();
                        updateResetButtonState();
                    });

                } catch (Exception e) {
                    Platform.runLater(() -> {
                        showAlert(Alert.AlertType.ERROR, "Error",
                                "No se pudo cargar la imagen: " + e.getMessage());
                    });
                }
                return null;
            }
        };

        executorService.submit(loadTask);
    }

    private void loadImageFromFile(File file) {
        Task<Void> loadTask = new Task<Void>() {
            @Override
            protected Void call() throws Exception {
                try {
                    originalBufferedImage = ImageIO.read(file);
                    currentBufferedImage = deepCopy(originalBufferedImage);

                    Platform.runLater(() -> {
                        displayImage(currentBufferedImage);
                        updateImageInfo(file.getName(), originalBufferedImage);
                        updateUIState(true);
                        placeholderLabel.setVisible(false);
                        resetBrightnessLevel();
                        hasUnsavedChanges = false;
                        updateSaveButtonState();
                        updateResetButtonState();
                    });

                } catch (Exception e) {
                    Platform.runLater(() -> {
                        showAlert(Alert.AlertType.ERROR, "Error",
                                "No se pudo cargar la imagen: " + e.getMessage());
                    });
                }
                return null;
            }
        };

        executorService.submit(loadTask);
    }

    // ==================== MÉTODOS DE APLICACIÓN DE FILTROS ====================

    private void applyBrightnessFilter(float targetBrightness, String action) {
        Task<Void> filterTask = new Task<Void>() {
            @Override
            protected Void call() throws Exception {
                FiltroBrillo filtro = poolBrillo.obtener();
                try {
                    // Mostrar indicador de procesamiento
                    Platform.runLater(() -> {
                        if (brightnessLevelLabel != null) {
                            brightnessLevelLabel.setText("Procesando...");
                        }
                        if (brightnessProgressBar != null) {
                            brightnessProgressBar.setProgress(-1); // Indeterminado
                        }
                    });

                    // Configurar el filtro
                    filtro.setBrilloRelativo((int) targetBrightness);

                    long startTime = System.currentTimeMillis();

                    // Aplicar siempre sobre la imagen original para evitar degradación
                    BufferedImage sourceImage = originalBufferedImage != null ? originalBufferedImage : currentBufferedImage;
                    BufferedImage result = filtro.aplicar(sourceImage);

                    long endTime = System.currentTimeMillis();

                    Platform.runLater(() -> {
                        if (result != null) {
                            currentBufferedImage = result;
                            currentBrightnessLevel = targetBrightness;
                            displayImage(currentBufferedImage);
                            hasUnsavedChanges = true;
                            updateSaveButtonState();
                            updateResetButtonState();

                            String message = String.format("%s brillo: %.0f%% (Tiempo: %dms)",
                                    action, targetBrightness, (endTime - startTime));
                            updateImageInfo(message, currentBufferedImage);

                            // Actualizar todos los controles
                            updateBrightnessButtonsState();

                            // Log para debug
                            System.out.println(String.format("Brillo aplicado: %.0f%%, Factor: %.2f",
                                    targetBrightness, filtro.getFactorBrillo()));
                        } else {
                            showAlert(Alert.AlertType.ERROR, "Error",
                                    "No se pudo aplicar el filtro de brillo");

                            // Restaurar indicadores
                            updateBrightnessIndicators();
                        }
                    });

                } catch (Exception e) {
                    Platform.runLater(() -> {
                        showAlert(Alert.AlertType.ERROR, "Error",
                                "Error al procesar brillo: " + e.getMessage());
                        updateBrightnessIndicators(); // Restaurar indicadores
                    });
                    e.printStackTrace();
                } finally {
                    poolBrillo.liberar(filtro);
                }
                return null;
            }
        };

        executorService.submit(filterTask);
    }

    private void applyRotateFilter() {
        Task<Void> filterTask = new Task<Void>() {
            @Override
            protected Void call() throws Exception {
                FiltroRotar filtro = poolRotar.obtener();
                try {
                    filtro.setRotacion(org.imgscalr.Scalr.Rotation.CW_90);

                    long startTime = System.currentTimeMillis();
                    BufferedImage result = filtro.aplicar(currentBufferedImage);
                    long endTime = System.currentTimeMillis();

                    Platform.runLater(() -> {
                        if (result != null) {
                            currentBufferedImage = result;
                            originalBufferedImage = deepCopy(result); // Actualizar original para rotación
                            displayImage(currentBufferedImage);
                            hasUnsavedChanges = true;
                            updateSaveButtonState();
                            updateResetButtonState();
                            updateImageInfo("Imagen rotada 90° (Tiempo: " + (endTime - startTime) + "ms)", currentBufferedImage);
                        }
                    });

                } finally {
                    poolRotar.liberar(filtro);
                }
                return null;
            }
        };

        executorService.submit(filterTask);
    }

    private void applyGrayscaleFilter() {
        Task<Void> filterTask = new Task<Void>() {
            @Override
            protected Void call() throws Exception {
                FiltroEscalaGrises filtro = poolEscalaGrises.obtener();
                try {
                    long startTime = System.currentTimeMillis();
                    BufferedImage result = filtro.aplicar(currentBufferedImage);
                    long endTime = System.currentTimeMillis();

                    Platform.runLater(() -> {
                        if (result != null) {
                            currentBufferedImage = result;
                            displayImage(currentBufferedImage);
                            hasUnsavedChanges = true;
                            updateSaveButtonState();
                            updateResetButtonState();
                            updateImageInfo("Filtro escala de grises aplicado (Tiempo: " + (endTime - startTime) + "ms)", currentBufferedImage);
                        }
                    });

                } finally {
                    poolEscalaGrises.liberar(filtro);
                }
                return null;
            }
        };

        executorService.submit(filterTask);
    }

    // ==================== MÉTODOS DE GUARDADO ====================

    private void saveEditedImage() {
        if (currentBufferedImage == null) return;

        Task<Void> saveTask = new Task<Void>() {
            @Override
            protected Void call() throws Exception {
                try {
                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    ImageIO.write(currentBufferedImage, "PNG", baos);
                    byte[] imageBytes = baos.toByteArray();
                    String base64Image = Base64.getEncoder().encodeToString(imageBytes);

                    // Crear JSON para enviar al servidor
                    JSONObject jsonRequest = new JSONObject();
                    String timestamp = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss").format(new Date());
                    String editedImageName = "Editada_" + timestamp;

                    if (imagenOriginal != null) {
                        // Si es una imagen existente, crear nueva entrada
                        jsonRequest.put("nombre", imagenOriginal.getNombre() + "_editada_" + timestamp);
                        jsonRequest.put("camaraId", imagenOriginal.getCamaraId());
                        jsonRequest.put("usuarioId", imagenOriginal.getUsuarioId());
                    } else {
                        // Si es una imagen nueva, crear registro
                        jsonRequest.put("nombre", editedImageName);
                        String userId = HttpService.getInstance().getUserIdFromClaims();
                        if (userId != null) {
                            jsonRequest.put("usuarioId", Integer.valueOf(userId));
                        }
                        jsonRequest.put("camaraId", 1); // Valor por defecto
                    }

                    jsonRequest.put("imagen", base64Image);
                    jsonRequest.put("resolucion", currentBufferedImage.getWidth() + "x" + currentBufferedImage.getHeight());
                    SimpleDateFormat isoFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
                    jsonRequest.put("fecha", isoFormat.format(new Date()));

                    // Enviar al servidor
                    HttpService.getInstance().sendPostRequest(
                            "http://localhost:9000/api/imagenes/save",
                            jsonRequest.toString()
                    );

                    Platform.runLater(() -> {
                        hasUnsavedChanges = false;
                        updateSaveButtonState();
                        updateResetButtonState();
                        showAlert(Alert.AlertType.INFORMATION, "Éxito",
                                "Imagen guardada correctamente en el servidor");
                        updateImageInfo("Imagen guardada exitosamente", currentBufferedImage);
                    });

                } catch (Exception e) {
                    Platform.runLater(() -> {
                        showAlert(Alert.AlertType.ERROR, "Error",
                                "No se pudo guardar la imagen: " + e.getMessage());
                    });
                }
                return null;
            }
        };

        executorService.submit(saveTask);
    }

    // ==================== MÉTODOS DE VISUALIZACIÓN ====================

    private void displayImage(BufferedImage bufferedImage) {
        if (bufferedImage != null) {
            try {
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                ImageIO.write(bufferedImage, "PNG", baos);
                ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
                Image fxImage = new Image(bais);

                imageView.setImage(fxImage);
                imageView.setVisible(true);

                // Ajustar el tamaño de la imagen al contenedor
                imageView.setFitWidth(580);
                imageView.setFitHeight(400);
                imageView.setPreserveRatio(true);

            } catch (IOException e) {
                showAlert(Alert.AlertType.ERROR, "Error",
                        "No se pudo mostrar la imagen: " + e.getMessage());
            }
        }
    }

    private void updateImageInfo(String imageName, BufferedImage image) {
        if (image != null) {
            String info = String.format("Nombre: %s\nDimensiones: %dx%d\nTipo: %s\nBrillo: %.0f%%",
                    imageName,
                    image.getWidth(),
                    image.getHeight(),
                    getImageTypeString(image.getType()),
                    currentBrightnessLevel
            );
            imageInfoLabel.setText(info);
        } else {
            imageInfoLabel.setText("Ninguna imagen seleccionada");
        }
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

    // ==================== MÉTODOS DE CONTROL DE BRILLO ====================

    private void initializeBrightnessSlider() {
        if (brightnessSlider != null) {
            // Crear listener para evitar eventos circulares
            brightnessSliderListener = (obs, oldVal, newVal) -> {
                if (!brightnessSlider.isValueChanging()) {
                    float newBrightness = newVal.floatValue();
                    if (Math.abs(newBrightness - currentBrightnessLevel) > 5) {
                        applyBrightnessFilter(newBrightness, "Slider");
                    }
                }
            };

            brightnessSlider.valueProperty().addListener(brightnessSliderListener);

            // Configurar el slider para actualizaciones en tiempo real durante drag
            brightnessSlider.setOnMouseDragged(e -> {
                if (currentBufferedImage != null) {
                    float newBrightness = (float) brightnessSlider.getValue();
                    currentBrightnessLevel = newBrightness;
                    updateBrightnessIndicators();
                }
            });
        }
    }

    private void resetBrightnessLevel() {
        currentBrightnessLevel = 100; // Brillo normal
        updateBrightnessButtonsState();
        updateBrightnessIndicators();
    }

    private void updateBrightnessButtonsState() {
        // Deshabilitar botones según límites
        boolean canIncrease = currentBrightnessLevel < MAX_BRIGHTNESS;
        boolean canDecrease = currentBrightnessLevel > MIN_BRIGHTNESS;

        increaseBrightnessBtn.setDisable(!canIncrease);
        decreaseBrightnessBtn.setDisable(!canDecrease);

        // Actualizar texto de los botones con información contextual
        increaseBrightnessBtn.setText(canIncrease ? "Aumentar (+)" : "Máximo");
        decreaseBrightnessBtn.setText(canDecrease ? "Disminuir (-)" : "Mínimo");

        // Actualizar estilo de botones de preset según el nivel actual
        updatePresetButtonsStyle();

        // Actualizar indicadores visuales
        updateBrightnessIndicators();
    }

    private void updateBrightnessIndicators() {
        if (brightnessLevelLabel != null) {
            brightnessLevelLabel.setText(String.format("%.0f%%", currentBrightnessLevel));
        }

        if (brightnessProgressBar != null) {
            // Actualizar progress bar (normalizar entre 0 y 1)
            double progress = (currentBrightnessLevel - MIN_BRIGHTNESS) / (MAX_BRIGHTNESS - MIN_BRIGHTNESS);
            brightnessProgressBar.setProgress(Math.max(0, Math.min(1, progress)));

            // Cambiar color del progress bar según el nivel
            if (currentBrightnessLevel < 70) {
                brightnessProgressBar.setStyle("-fx-accent: #dc3545;"); // Rojo para bajo
            } else if (currentBrightnessLevel > 150) {
                brightnessProgressBar.setStyle("-fx-accent: #ffc107;"); // Amarillo para alto
            } else {
                brightnessProgressBar.setStyle("-fx-accent: #28a745;"); // Verde para normal
            }
        }

        if (brightnessSlider != null && brightnessSliderListener != null) {
            // Actualizar slider sin disparar el evento
            brightnessSlider.valueProperty().removeListener(brightnessSliderListener);
            brightnessSlider.setValue(currentBrightnessLevel);
            brightnessSlider.valueProperty().addListener(brightnessSliderListener);
        }
    }

    private void updatePresetButtonsStyle() {
        // Resetear estilos
        String normalStyle = "-fx-background-color: #6c757d; -fx-text-fill: white; -fx-font-size: 10px;";
        String activeStyle = "-fx-background-color: #007bff; -fx-text-fill: white; -fx-font-size: 10px; -fx-font-weight: bold;";

        if (darkBtn != null) darkBtn.setStyle(normalStyle);
        if (normalBtn != null) normalBtn.setStyle(normalStyle);
        if (brightBtn != null) brightBtn.setStyle(normalStyle);

        // Marcar el preset activo
        if (currentBrightnessLevel <= 60 && darkBtn != null) {
            darkBtn.setStyle(activeStyle);
        } else if (currentBrightnessLevel >= 90 && currentBrightnessLevel <= 110 && normalBtn != null) {
            normalBtn.setStyle(activeStyle);
        } else if (currentBrightnessLevel >= 180 && brightBtn != null) {
            brightBtn.setStyle(activeStyle);
        }
    }

    // ==================== MÉTODOS DE ESTADO DE UI ====================

    private void updateUIState(boolean imageLoaded) {
        // Controles básicos
        increaseBrightnessBtn.setDisable(!imageLoaded);
        decreaseBrightnessBtn.setDisable(!imageLoaded);
        rotateBtn.setDisable(!imageLoaded);
        grayscaleBtn.setDisable(!imageLoaded);

        // Controles de brillo avanzados
        if (brightnessSlider != null) {
            brightnessSlider.setDisable(!imageLoaded);
        }
        if (darkBtn != null) darkBtn.setDisable(!imageLoaded);
        if (normalBtn != null) normalBtn.setDisable(!imageLoaded);
        if (brightBtn != null) brightBtn.setDisable(!imageLoaded);

        if (imageLoaded) {
            updateBrightnessButtonsState();
        } else {
            // Reset cuando no hay imagen
            if (brightnessLevelLabel != null) {
                brightnessLevelLabel.setText("--");
            }
            if (brightnessProgressBar != null) {
                brightnessProgressBar.setProgress(0);
            }
        }

        updateSaveButtonState();
        updateResetButtonState();
    }

    private void updateSaveButtonState() {
        saveImageBtn.setDisable(!hasUnsavedChanges || currentBufferedImage == null);
    }

    private void updateResetButtonState() {
        if (resetBtn != null) {
            resetBtn.setDisable(!hasUnsavedChanges || originalBufferedImage == null);
        }
    }

    // ==================== MÉTODOS DE CONFIGURACIÓN DE UI ====================

    private void configureButtonHoverEffects() {
        // Efecto hover para botón de seleccionar imagen
        selectImageBtn.setOnMouseEntered(e ->
                selectImageBtn.setStyle("-fx-background-color: #3367d6; -fx-text-fill: white; -fx-font-weight: bold;"));
        selectImageBtn.setOnMouseExited(e ->
                selectImageBtn.setStyle("-fx-background-color: #4285f4; -fx-text-fill: white; -fx-font-weight: bold;"));

        // Efecto hover para botón de aumentar brillo
        increaseBrightnessBtn.setOnMouseEntered(e ->
                increaseBrightnessBtn.setStyle("-fx-background-color: #4285f4; -fx-text-fill: white; -fx-font-weight: bold;"));
        increaseBrightnessBtn.setOnMouseExited(e ->
                increaseBrightnessBtn.setStyle("-fx-background-color: #5ba3f5; -fx-text-fill: white; -fx-font-weight: bold;"));

        // Efecto hover para botón de disminuir brillo
        decreaseBrightnessBtn.setOnMouseEntered(e ->
                decreaseBrightnessBtn.setStyle("-fx-background-color: #218838; -fx-text-fill: white; -fx-font-weight: bold;"));
        decreaseBrightnessBtn.setOnMouseExited(e ->
                decreaseBrightnessBtn.setStyle("-fx-background-color: #28a745; -fx-text-fill: white; -fx-font-weight: bold;"));

        // Efecto hover para botón rotar
        rotateBtn.setOnMouseEntered(e ->
                rotateBtn.setStyle("-fx-background-color: #e0a800; -fx-text-fill: black; -fx-font-weight: bold;"));
        rotateBtn.setOnMouseExited(e ->
                rotateBtn.setStyle("-fx-background-color: #ffc107; -fx-text-fill: black; -fx-font-weight: bold;"));

        // Efecto hover para botón escala de grises
        grayscaleBtn.setOnMouseEntered(e ->
                grayscaleBtn.setStyle("-fx-background-color: #c82333; -fx-text-fill: white; -fx-font-weight: bold;"));
        grayscaleBtn.setOnMouseExited(e ->
                grayscaleBtn.setStyle("-fx-background-color: #dc3545; -fx-text-fill: white; -fx-font-weight: bold;"));

        // Efecto hover para botón guardar
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

        // Efecto hover para botón reset
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

        // Efecto hover para botón atrás
        backButton.setOnMouseEntered(e ->
                backButton.setStyle("-fx-background-color: #5a6268; -fx-text-fill: white; -fx-font-weight: bold;"));
        backButton.setOnMouseExited(e ->
                backButton.setStyle("-fx-background-color: #6c757d; -fx-text-fill: white; -fx-font-weight: bold;"));
    }

    private void configureTooltips() {
        if (increaseBrightnessBtn != null) {
            Tooltip.install(increaseBrightnessBtn,
                    new Tooltip("Aumenta el brillo en " + (int)BRIGHTNESS_STEP + "%"));
        }

        if (decreaseBrightnessBtn != null) {
            Tooltip.install(decreaseBrightnessBtn,
                    new Tooltip("Reduce el brillo en " + (int)BRIGHTNESS_STEP + "%"));
        }

        if (brightnessSlider != null) {
            Tooltip.install(brightnessSlider,
                    new Tooltip("Arrastra para ajustar el brillo de " + (int)MIN_BRIGHTNESS + "% a " + (int)MAX_BRIGHTNESS + "%"));
        }

        if (resetBtn != null) {
            Tooltip.install(resetBtn,
                    new Tooltip("Restablecer imagen a su estado original"));
        }

        if (rotateBtn != null) {
            Tooltip.install(rotateBtn,
                    new Tooltip("Rotar imagen 90 grados en sentido horario"));
        }

        if (grayscaleBtn != null) {
            Tooltip.install(grayscaleBtn,
                    new Tooltip("Convertir imagen a escala de grises"));
        }

        if (darkBtn != null) {
            Tooltip.install(darkBtn, new Tooltip("Aplicar preset oscuro (50%)"));
        }

        if (normalBtn != null) {
            Tooltip.install(normalBtn, new Tooltip("Aplicar preset normal (100%)"));
        }

        if (brightBtn != null) {
            Tooltip.install(brightBtn, new Tooltip("Aplicar preset brillante (200%)"));
        }
    }

    // ==================== MÉTODOS AUXILIARES ====================

    private BufferedImage deepCopy(BufferedImage source) {
        if (source == null) return null;

        BufferedImage copy = new BufferedImage(source.getWidth(), source.getHeight(), source.getType());
        copy.createGraphics().drawImage(source, 0, 0, null);
        return copy;
    }

    private void goBackToPhotosView() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/co/com/cliente/views/fotos-view.fxml"));
            Parent fotosView = loader.load();

            StackPane contentArea = getContentArea();
            if (contentArea != null) {
                contentArea.getChildren().clear();
                contentArea.getChildren().add(fotosView);
                updateTitle("FOTOS");
            }

        } catch (IOException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Error", "No se pudo volver a la galería: " + e.getMessage());
        }
    }

    // Métodos auxiliares para navegación
    private StackPane getContentArea() {
        try {
            Parent root = backButton.getScene().getRoot();
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
            Parent root = backButton.getScene().getRoot();
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

    private void showAlert(Alert.AlertType type, String title, String message) {
        Platform.runLater(() -> {
            Alert alert = new Alert(type);
            alert.setTitle(title);
            alert.setHeaderText(null);
            alert.setContentText(message);
            alert.showAndWait();
        });
    }

    // ==================== MÉTODOS DE LIMPIEZA ====================

    public void onClose() {
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

        // Limpiar listeners
        if (brightnessSlider != null && brightnessSliderListener != null) {
            brightnessSlider.valueProperty().removeListener(brightnessSliderListener);
        }

        // Limpiar imágenes de memoria
        currentBufferedImage = null;
        originalBufferedImage = null;
        imagenOriginal = null;
    }

    // ==================== MÉTODOS PÚBLICOS PARA INTEGRACIÓN ====================

    /**
     * Método público para verificar si hay cambios sin guardar
     */
    public boolean hasUnsavedChanges() {
        return hasUnsavedChanges;
    }

    /**
     * Método público para obtener la imagen actual
     */
    public BufferedImage getCurrentImage() {
        return currentBufferedImage;
    }

    /**
     * Método público para obtener el nivel de brillo actual
     */
    public float getCurrentBrightnessLevel() {
        return currentBrightnessLevel;
    }

    /**
     * Método público para aplicar un nivel específico de brillo
     */
    public void setBrightnessLevel(float level) {
        if (currentBufferedImage != null && level >= MIN_BRIGHTNESS && level <= MAX_BRIGHTNESS) {
            applyBrightnessFilter(level, "Programático");
        }
    }

    /**
     * Método público para forzar guardado
     */
    public void forceSave() {
        if (currentBufferedImage != null) {
            saveEditedImage();
        }
    }
}