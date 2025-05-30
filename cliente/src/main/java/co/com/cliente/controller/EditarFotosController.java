package co.com.cliente.controller;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.image.PixelReader;
import javafx.scene.image.PixelWriter;
import javafx.scene.image.WritableImage;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfByte;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.ResourceBundle;

public class EditarFotosController implements Initializable {

    @FXML
    private ImageView photoPreview;

    @FXML
    private Label placeholderLabel;

    @FXML
    private Button selectImageBtn;

    @FXML
    private Slider brightnessSlider;

    @FXML
    private Slider contrastSlider;

    @FXML
    private Slider saturationSlider;

    @FXML
    private Button applyBtn;

    @FXML
    private Button resetBtn;

    @FXML
    private Button saveBtn;

    private Image originalImage;
    private Mat originalMat;
    private Mat processedMat;
    private File currentImageFile;
    private File outputDir;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        // Cargar la biblioteca nativa de OpenCV
        try {
            // Intenta cargar usando el nombre de biblioteca nativa estándar
            System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
            System.out.println("OpenCV cargado correctamente: " + Core.VERSION);
        } catch (UnsatisfiedLinkError e) {
            try {
                // Si falla, intenta cargar específicamente opencv_java4110.dll
                System.loadLibrary("opencv_java4110");
                System.out.println("OpenCV 4.11.0 cargado correctamente.");
            } catch (UnsatisfiedLinkError e2) {
                System.err.println("Error al cargar OpenCV: " + e2.getMessage());
                // No mostramos alerta aquí para evitar errores con la ventana
            }
        }

        // Crear directorio para imágenes editadas
        File recordingsDir = new File(System.getProperty("user.home") + "/security_camera_recordings");
        outputDir = new File(recordingsDir, "edited_photos");
        if (!outputDir.exists()) {
            outputDir.mkdirs();
        }

        // Configurar sliders
        brightnessSlider.setValue(0);
        contrastSlider.setValue(0);
        saturationSlider.setValue(0);

        // Configurar el evento change de los sliders
        brightnessSlider.valueProperty().addListener((obs, oldVal, newVal) -> {
            // No aplicar cambios inmediatamente para evitar procesamiento excesivo
            // Se aplicarán los cambios al presionar el botón "Aplicar Cambios"
        });

        contrastSlider.valueProperty().addListener((obs, oldVal, newVal) -> {
            // Similar al slider de brillo
        });

        saturationSlider.valueProperty().addListener((obs, oldVal, newVal) -> {
            // Similar al slider de brillo
        });

        // Botones inicialmente deshabilitados hasta que se cargue una imagen
        applyBtn.setDisable(true);
        saveBtn.setDisable(true);
        resetBtn.setDisable(true);

        // Mostrar mensaje de placeholder
        placeholderLabel.setVisible(true);
    }

    @FXML
    private void handleSelectImage() {
        loadImageToEdit();
    }

    private void loadImageToEdit() {
        try {
            // Crear un selector de archivos
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Seleccionar Imagen para Editar");

            // Configurar filtros para archivos de imagen
            fileChooser.getExtensionFilters().add(
                    new FileChooser.ExtensionFilter("Imágenes", "*.png", "*.jpg", "*.jpeg")
            );

            // Directorio inicial: carpeta de snapshots
            File snapshotsDir = new File(new File(System.getProperty("user.home") + "/security_camera_recordings"), "snapshots");
            if (snapshotsDir.exists() && snapshotsDir.isDirectory()) {
                fileChooser.setInitialDirectory(snapshotsDir);
            }

            // Mostrar el diálogo y obtener el archivo seleccionado
            Stage stage = (Stage) photoPreview.getScene().getWindow();
            currentImageFile = fileChooser.showOpenDialog(stage);

            if (currentImageFile != null) {
                // Cargar la imagen original
                originalImage = new Image(new FileInputStream(currentImageFile));
                photoPreview.setImage(originalImage);

                // Ocultar el mensaje placeholder
                placeholderLabel.setVisible(false);

                // Convertir la imagen a formato Mat de OpenCV
                originalMat = imageToMat(originalImage);
                processedMat = originalMat.clone();

                // Habilitar botones
                applyBtn.setDisable(false);
                saveBtn.setDisable(false);
                resetBtn.setDisable(false);
            }
        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Error",
                    "Error al cargar la imagen: " + e.getMessage());
        }
    }

    @FXML
    private void handleApplyEffects() {
        if (originalMat == null) {
            return;
        }

        try {
            // Obtener valores de los sliders
            double brightness = brightnessSlider.getValue();
            double contrast = 1.0 + (contrastSlider.getValue() / 100.0);
            double saturation = 1.0 + (saturationSlider.getValue() / 100.0);

            // Clonar la imagen original para no modificarla
            processedMat = originalMat.clone();

            // Aplicar brillo y contraste
            processedMat.convertTo(processedMat, -1, contrast, brightness);

            // Aplicar saturación
            if (saturation != 1.0) {
                // Convertir a HSV para ajustar la saturación
                Mat hsvMat = new Mat();
                Imgproc.cvtColor(processedMat, hsvMat, Imgproc.COLOR_BGR2HSV);

                // Dividir en canales H, S, V
                java.util.List<Mat> hsvChannels = new java.util.ArrayList<>();
                Core.split(hsvMat, hsvChannels);

                // Ajustar el canal S (saturación)
                Core.multiply(hsvChannels.get(1), new org.opencv.core.Scalar(saturation), hsvChannels.get(1));

                // Combinar canales de nuevo
                Core.merge(hsvChannels, hsvMat);

                // Convertir de vuelta a BGR
                Imgproc.cvtColor(hsvMat, processedMat, Imgproc.COLOR_HSV2BGR);

                // Liberar memoria
                hsvMat.release();
                for (Mat channel : hsvChannels) {
                    channel.release();
                }
            }

            // Convertir Mat a Image para mostrar en la UI
            Image processedImage = matToImage(processedMat);
            photoPreview.setImage(processedImage);

        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Error",
                    "Error al aplicar los efectos: " + e.getMessage());
        }
    }

    @FXML
    private void handleResetImage() {
        if (originalImage != null) {
            // Restablecer sliders
            brightnessSlider.setValue(0);
            contrastSlider.setValue(0);
            saturationSlider.setValue(0);

            // Mostrar imagen original
            photoPreview.setImage(originalImage);

            // Restablecer Mat procesado
            if (originalMat != null) {
                processedMat = originalMat.clone();
            }
        }
    }

    @FXML
    private void handleSaveImage() {
        if (processedMat == null) {
            return;
        }

        try {
            // Crear nombre de archivo con timestamp
            String timestamp = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss").format(new Date());
            String baseName = currentImageFile != null ? currentImageFile.getName() : "edited_image";

            // Eliminar la extensión si existe
            int dotIndex = baseName.lastIndexOf('.');
            if (dotIndex > 0) {
                baseName = baseName.substring(0, dotIndex);
            }

            String editedFileName = outputDir.getAbsolutePath() + "/edited_" + baseName + "_" + timestamp + ".jpg";

            // Guardar la imagen procesada
            boolean success = Imgcodecs.imwrite(editedFileName, processedMat);

            if (!success) {
                throw new Exception("No se pudo guardar la imagen.");
            }

            // Notificar al usuario
            showAlert(Alert.AlertType.INFORMATION, "Imagen Guardada",
                    "La imagen editada se ha guardado en:\n" + editedFileName);

        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Error",
                    "Error al guardar la imagen: " + e.getMessage());
        }
    }

    private Mat imageToMat(Image image) {
        try {
            // Obtener dimensiones de la imagen
            int width = (int) image.getWidth();
            int height = (int) image.getHeight();

            // Crear un Mat vacío para almacenar la imagen
            Mat mat = new Mat(height, width, CvType.CV_8UC3);

            // Obtener los píxeles de la imagen de JavaFX
            PixelReader pixelReader = image.getPixelReader();

            // Transferir los píxeles a la matriz OpenCV
            byte[] buffer = new byte[width * height * 3];

            for (int y = 0; y < height; y++) {
                for (int x = 0; x < width; x++) {
                    javafx.scene.paint.Color color = pixelReader.getColor(x, y);

                    // OpenCV usa formato BGR
                    buffer[(y * width + x) * 3] = (byte) (color.getBlue() * 255);
                    buffer[(y * width + x) * 3 + 1] = (byte) (color.getGreen() * 255);
                    buffer[(y * width + x) * 3 + 2] = (byte) (color.getRed() * 255);
                }
            }

            mat.put(0, 0, buffer);
            return mat;

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private Image matToImage(Mat mat) {
        try {
            // Crear un buffer para almacenar la imagen codificada
            MatOfByte buffer = new MatOfByte();

            // Codificar el mat en formato PNG
            Imgcodecs.imencode(".png", mat, buffer);

            // Convertir el buffer a un array de bytes
            byte[] imageData = buffer.toArray();

            // Crear una Image de JavaFX a partir del array de bytes
            return new Image(new ByteArrayInputStream(imageData));

        } catch (Exception e) {
            e.printStackTrace();
            return null;
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

    // Método para liberar recursos
    public void onClose() {
        if (originalMat != null) {
            originalMat.release();
        }
        if (processedMat != null) {
            processedMat.release();
        }
    }
}