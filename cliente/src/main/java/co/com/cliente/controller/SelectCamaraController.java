package co.com.cliente.controller;

import co.com.cliente.dto.CamaraDTO;
import co.com.cliente.httpRequest.HttpService;
import co.com.cliente.httpRequest.JsonResponseHandler;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.layout.StackPane;
import javafx.util.Callback;

import java.net.URL;
import java.util.Arrays;
import java.util.List;
import java.util.ResourceBundle;

public class SelectCamaraController implements Initializable {

    @FXML
    private ListView<CamaraDTO> camaraListView;

    @FXML
    private Button seleccionarButton;

    @FXML
    private Label errorLabel;

    private static final String API_BASE_URL = "http://localhost:9000/api/camara/usuario/";
    private ObservableList<CamaraDTO> camaras = FXCollections.observableArrayList();

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        // Configurar la celda personalizada para el ListView
        camaraListView.setCellFactory(new Callback<ListView<CamaraDTO>, ListCell<CamaraDTO>>() {
            @Override
            public ListCell<CamaraDTO> call(ListView<CamaraDTO> param) {
                return new ListCell<CamaraDTO>() {
                    @Override
                    protected void updateItem(CamaraDTO camara, boolean empty) {
                        super.updateItem(camara, empty);
                        if (empty || camara == null) {
                            setText(null);
                        } else {
                            setText(camara.getDescripcion() + " - Resolución: " + camara.getResolucion());
                        }
                    }
                };
            }
        });

        camaraListView.setItems(camaras);
        
        // Cargar las cámaras
        loadCamaras();
    }

    private void loadCamaras() {
        try {
            String userId = HttpService.getInstance().getUserIdFromClaims();
            System.out.println(userId);
            if (userId == null || userId.isEmpty()) {
                showError("No se pudo obtener el ID del usuario.");
                return;
            }

            String jsonResponse = HttpService.getInstance().sendGetRequest(API_BASE_URL+ userId);
            List<CamaraDTO> camerasList = Arrays.asList(JsonResponseHandler.parseResponse(jsonResponse, CamaraDTO[].class));
            
            Platform.runLater(() -> {
                camaras.clear();
                
                if (camerasList.isEmpty()) {
                    showError("No hay cámaras registradas para este usuario.");
                    seleccionarButton.setDisable(true);
                } else {
                    camaras.addAll(camerasList);
                    seleccionarButton.setDisable(false);
                    errorLabel.setVisible(false);
                    
                    // Seleccionar la primera cámara por defecto
                    camaraListView.getSelectionModel().select(0);
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
            Platform.runLater(() -> {
                showError("Error al cargar las cámaras: " + e.getMessage());
                seleccionarButton.setDisable(true);
            });
        }
    }

    @FXML
    private void handleSeleccionarButtonClick() {
        CamaraDTO selectedCamara = camaraListView.getSelectionModel().getSelectedItem();
        
        if (selectedCamara == null) {
            showError("Por favor, seleccione una cámara.");
            return;
        }
        
        try {
            // Cargar la vista de grabar video con la cámara seleccionada
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/co/com/cliente/views/grabar-video-view.fxml"));
            Parent view = loader.load();
            
            // Obtener el controlador y pasarle la cámara seleccionada
            GrabarVideoController controller = loader.getController();
            controller.setCamara(selectedCamara);
            
            // Reemplazar la vista actual con la de grabación
            StackPane contentArea = (StackPane) camaraListView.getScene().lookup("#contentArea");
            if (contentArea != null) {
                contentArea.getChildren().clear();
                contentArea.getChildren().add(view);
            }
        } catch (Exception e) {
            e.printStackTrace();
            showError("Error al cargar la vista de grabación: " + e.getMessage());
        }
    }

    private void showError(String message) {
        errorLabel.setText(message);
        errorLabel.setVisible(true);
    }
}