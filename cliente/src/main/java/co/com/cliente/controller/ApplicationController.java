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
    private AnchorPane editarFotosOption;

    @FXML
    private Label titleLabel;

    @FXML
    private StackPane contentArea;

    private AnchorPane[] optionPanes;
    private int currentViewIndex = -1;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        // Inicializar array para facilitar la manipulación
        optionPanes = new AnchorPane[]{videosOption, fotosOption, grabarVideoOption, editarFotosOption};

        // Por defecto mostramos la opción "VIDEOS"
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
        } else if (clickedPane.equals(editarFotosOption)) {
            updateSelection(3);
        }
    }

    private void updateSelection(int selectedIndex) {
        // Evitar recargar la misma vista
        if (currentViewIndex == selectedIndex) {
            return;
        }

        // Actualizar el estilo del menú
        for (int i = 0; i < optionPanes.length; i++) {
            if (i == selectedIndex) {
                optionPanes[i].setStyle("-fx-background-color: #333333;"); // Color más oscuro para la selección
            } else {
                optionPanes[i].setStyle("-fx-background-color: #404040;"); // Color normal
            }
        }

        // Cambiar el título según la opción seleccionada
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
            case 3:
                title = "EDITAR FOTOS";
                break;
            default:
                title = "VIDEOS";
                break;
        }

        // Actualizar el título
        titleLabel.setText(title);

        // Cargar la vista correspondiente
        loadViewByIndex(selectedIndex);

        // Actualizar el índice de la vista actual
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
                fxmlPath = "/co/com/cliente/views/grabar-video-view.fxml";
                break;
            case 3:
                fxmlPath = "/co/com/cliente/views/editar-fotos-view.fxml";
                break;
            default:
                fxmlPath = "/co/com/cliente/views/videos-view.fxml";
                break;
        }

        try {
            // Cargar la vista FXML
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent view = loader.load();

            // Limpiar y añadir la nueva vista
            contentArea.getChildren().clear();
            contentArea.getChildren().add(view);

        } catch (IOException e) {
            e.printStackTrace();
            // En caso de error, mostrar mensaje
            showAlert(Alert.AlertType.ERROR, "Error", "No se pudo cargar la vista: " + e.getMessage());
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