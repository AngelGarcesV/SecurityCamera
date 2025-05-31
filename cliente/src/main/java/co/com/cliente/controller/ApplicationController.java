package co.com.cliente.controller;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.StackPane;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

public class ApplicationController implements Initializable {

    @FXML
    private AnchorPane videosOption;

    @FXML
    private AnchorPane fotosOption;

    @FXML
    private AnchorPane grabarVideoOption;

    @FXML
    private Label titleLabel;

    @FXML
    private StackPane contentArea;

    private AnchorPane[] optionPanes;
    private int currentViewIndex = -1;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        optionPanes = new AnchorPane[]{videosOption, fotosOption, grabarVideoOption};

        updateSelection(0);
    }

    @FXML
    private void handleMenuClick(MouseEvent event) {
        AnchorPane clickedPane = (AnchorPane) event.getSource();

        if (clickedPane.equals(videosOption)) {
            updateSelection(0);
        } else if (clickedPane.equals(fotosOption)) {
            updateSelection(1);
        } else if (clickedPane.equals(grabarVideoOption)) {
            updateSelection(2);
        }
    }

    private void updateSelection(int selectedIndex) {
        if (currentViewIndex == selectedIndex) {
            return;
        }

        for (int i = 0; i < optionPanes.length; i++) {
            if (i == selectedIndex) {
                optionPanes[i].setStyle("-fx-background-color: #333333;");
            } else {
                optionPanes[i].setStyle("-fx-background-color: #404040;");
            }
        }

        String title;

        switch (selectedIndex) {
            case 0:
                title = "VIDEOS";
                break;
            case 1:
                title = "FOTOS";
                break;
            case 2:
                title = "GRABAR VIDEO";
                break;
            default:
                title = "VIDEOS";
                break;
        }

        titleLabel.setText(title);

        loadViewByIndex(selectedIndex);

        currentViewIndex = selectedIndex;
    }

    private void loadViewByIndex(int viewIndex) {
        String fxmlPath = "";

        switch (viewIndex) {
            case 0:
                fxmlPath = "/co/com/cliente/views/videos-view.fxml";
                break;
            case 1:
                fxmlPath = "/co/com/cliente/views/fotos-view.fxml";
                break;
            case 2:
                // Cambiamos la ruta para que cargue la vista de selección de cámara primero
                fxmlPath = "/co/com/cliente/views/select-camara-view.fxml";
                break;
            default:
                fxmlPath = "/co/com/cliente/views/videos-view.fxml";
                break;
        }

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent view = loader.load();

            contentArea.getChildren().clear();
            contentArea.getChildren().add(view);

        } catch (IOException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Error", "No se pudo cargar la vista: " + e.getMessage());
        }
    }

    // Método público para cargar la vista de editar fotos desde FotosController
    public void loadEditarFotosView() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/co/com/cliente/views/editar-fotos-view.fxml"));
            Parent view = loader.load();

            contentArea.getChildren().clear();
            contentArea.getChildren().add(view);

            // Actualizar el título
            titleLabel.setText("EDITAR FOTOS");

        } catch (IOException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Error", "No se pudo cargar la vista de edición: " + e.getMessage());
        }
    }

    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}