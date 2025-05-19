package co.com.cliente.controller;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.media.MediaView;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

import java.io.File;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Date;
import java.util.ResourceBundle;

public class VideosController implements Initializable {

    @FXML
    private GridPane videoGrid;

    private File recordingsDir;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        recordingsDir = new File(System.getProperty("user.home") + "/security_camera_recordings");
        if (!recordingsDir.exists()) {
            recordingsDir.mkdirs();
        }

        loadRecordedVideos();
    }

    private void loadRecordedVideos() {
        videoGrid.getChildren().clear();

        videoGrid.setHgap(10);
        videoGrid.setVgap(10);
        videoGrid.setPadding(new Insets(10));

        try {
            File[] videoFiles = recordingsDir.listFiles((dir, name) ->
                    name.toLowerCase().endsWith(".mp4") ||
                            name.toLowerCase().endsWith(".avi") ||
                            name.toLowerCase().endsWith(".mov"));

            if (videoFiles == null || videoFiles.length == 0) {
                Label noVideosLabel = new Label("No hay videos grabados disponibles.");
                noVideosLabel.setFont(Font.font("System", FontWeight.BOLD, 16));
                videoGrid.add(noVideosLabel, 0, 0);
                return;
            }

            Arrays.sort(videoFiles, Comparator.comparing(File::lastModified).reversed());

            int row = 0;
            int col = 0;
            int count = 0;

            for (File videoFile : videoFiles) {
                if (count >= 6) break;

                VBox videoItem = createVideoThumbnail(videoFile);
                videoGrid.add(videoItem, col, row);

                col++;
                if (col >= 3) {
                    col = 0;
                    row++;
                }

                count++;
            }
        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Error",
                    "Error al cargar los videos: " + e.getMessage());
        }
    }

    private VBox createVideoThumbnail(File videoFile) {
        VBox item = new VBox(5);
        item.setPrefSize(300, 200);

        ImageView thumbnailView = new ImageView();
        thumbnailView.setFitWidth(300);
        thumbnailView.setFitHeight(170);
        thumbnailView.setPreserveRatio(true);

        thumbnailView.setStyle("-fx-background-color: #333333;");

        String title = videoFile.getName();
        String duration = "";
        String date = new SimpleDateFormat("dd/MM/yyyy HH:mm").format(new Date(videoFile.lastModified()));

        HBox infoBox = new HBox();
        infoBox.setSpacing(5);

        Label titleLabel = new Label(title);
        titleLabel.setFont(Font.font("System", FontWeight.BOLD, 12));
        titleLabel.setWrapText(true);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Label dateLabel = new Label(date);
        dateLabel.setFont(Font.font("System", 11));
        dateLabel.setTextFill(Color.GRAY);

        infoBox.getChildren().addAll(titleLabel, spacer, dateLabel);

        item.getChildren().addAll(thumbnailView, infoBox);

        item.setOnMouseClicked(event -> playVideo(videoFile));

        return item;
    }

    private void playVideo(File videoFile) {
        try {
            java.awt.Desktop.getDesktop().open(videoFile);
        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Error",
                    "No se pudo reproducir el video: " + e.getMessage());
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

    public void refreshVideos() {
        loadRecordedVideos();
    }
}