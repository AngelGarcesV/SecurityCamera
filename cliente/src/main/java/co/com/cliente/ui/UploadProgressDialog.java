package co.com.cliente.ui;

import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.stage.Modality;
import javafx.scene.Scene;

public class UploadProgressDialog {

    private Stage dialogStage;
    private ProgressBar progressBar;
    private Label progressLabel;
    private Label speedLabel;
    private Label timeRemainingLabel;
    private Label fileNameLabel;
    private Button cancelButton;

    private long startTime;
    private boolean cancelled = false;
    private Runnable onCancelCallback;

    public UploadProgressDialog(Stage parentStage, String fileName) {
        createDialog(parentStage, fileName);
    }

    private void createDialog(Stage parentStage, String fileName) {
        dialogStage = new Stage();
        dialogStage.initModality(Modality.WINDOW_MODAL);
        dialogStage.initOwner(parentStage);
        dialogStage.setTitle("Enviando Video via WebSocket");
        dialogStage.setResizable(false);

        // Layout principal
        VBox mainLayout = new VBox(15);
        mainLayout.setPadding(new Insets(20));
        mainLayout.setAlignment(Pos.CENTER);

        // Título
        Label titleLabel = new Label("Enviando video al servidor");
        titleLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 16px;");

        // Nombre del archivo
        fileNameLabel = new Label("Archivo: " + fileName);
        fileNameLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #666;");

        // Barra de progreso
        progressBar = new ProgressBar(0);
        progressBar.setPrefWidth(300);
        progressBar.setPrefHeight(20);

        // Etiqueta de progreso
        progressLabel = new Label("0% (0 MB / 0 MB)");
        progressLabel.setStyle("-fx-font-size: 12px;");

        // Información adicional
        HBox infoBox = new HBox(20);
        infoBox.setAlignment(Pos.CENTER);

        speedLabel = new Label("Velocidad: -- MB/s");
        speedLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: #666;");

        timeRemainingLabel = new Label("Tiempo restante: --:--");
        timeRemainingLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: #666;");

        infoBox.getChildren().addAll(speedLabel, timeRemainingLabel);

        // Botón cancelar
        cancelButton = new Button("Cancelar");
        cancelButton.setOnAction(e -> {
            cancelled = true;
            if (onCancelCallback != null) {
                onCancelCallback.run();
            }
            dialogStage.close();
        });

        // Agregar todos los elementos
        mainLayout.getChildren().addAll(
                titleLabel,
                fileNameLabel,
                progressBar,
                progressLabel,
                infoBox,
                cancelButton
        );

        Scene scene = new Scene(mainLayout);
        dialogStage.setScene(scene);

        // Inicializar tiempo de inicio
        startTime = System.currentTimeMillis();
    }

    public void show() {
        dialogStage.show();
    }

    public void hide() {
        dialogStage.close();
    }

    public void updateProgress(double progressPercent, long sentBytes, long totalBytes) {
        Platform.runLater(() -> {
            // Actualizar barra de progreso
            progressBar.setProgress(progressPercent / 100.0);

            // Actualizar etiqueta de progreso
            String sentMB = String.format("%.1f", sentBytes / (1024.0 * 1024.0));
            String totalMB = String.format("%.1f", totalBytes / (1024.0 * 1024.0));
            progressLabel.setText(String.format("%.1f%% (%s MB / %s MB)",
                    progressPercent, sentMB, totalMB));

            // Calcular velocidad
            long elapsedTime = System.currentTimeMillis() - startTime;
            if (elapsedTime > 1000) { // Después de 1 segundo
                double speedMBps = (sentBytes / (1024.0 * 1024.0)) / (elapsedTime / 1000.0);
                speedLabel.setText(String.format("Velocidad: %.2f MB/s", speedMBps));

                // Calcular tiempo restante
                if (speedMBps > 0) {
                    long remainingBytes = totalBytes - sentBytes;
                    double remainingSeconds = (remainingBytes / (1024.0 * 1024.0)) / speedMBps;

                    int minutes = (int) (remainingSeconds / 60);
                    int seconds = (int) (remainingSeconds % 60);
                    timeRemainingLabel.setText(String.format("Tiempo restante: %02d:%02d", minutes, seconds));
                }
            }
        });
    }

    public void setOnCancel(Runnable callback) {
        this.onCancelCallback = callback;
    }

    public boolean isCancelled() {
        return cancelled;
    }

    public void showSuccess(String message) {
        Platform.runLater(() -> {
            progressBar.setProgress(1.0);
            progressLabel.setText("¡Completado!");
            speedLabel.setText("Upload finalizado exitosamente");
            timeRemainingLabel.setText("✅ " + message);

            cancelButton.setText("Cerrar");

            // Auto-cerrar después de 3 segundos
            new Thread(() -> {
                try {
                    Thread.sleep(3000);
                    Platform.runLater(() -> dialogStage.close());
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }).start();
        });
    }

    public void showError(String error) {
        Platform.runLater(() -> {
            progressLabel.setText("Error en el envío");
            speedLabel.setText("❌ " + error);
            timeRemainingLabel.setText("Reintentando con HTTP...");

            cancelButton.setText("Cerrar");
        });
    }
}