package co.com.cliente.controller;

import com.jfoenix.controls.JFXButton;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

public class MainController implements Initializable {
    @FXML
    private Label welcomeText;

    @FXML
    private TextField emailinput;

    @FXML
    private PasswordField passwordinput;

    @FXML
    private JFXButton loginButton;

    @FXML
    protected void onHelloButtonClick() {
        welcomeText.setText("Welcome to JavaFX Application!");
    }

    @FXML
    protected void onLoginButtonClick() {
        String email = emailinput.getText();
        String password = passwordinput.getText();

        // Validar credenciales
        if (email.equals("angellgarces@gmail.com") && password.equals("pruebaa")) {
            try {
                // Cargar la nueva vista
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/co/com/cliente/application-view.fxml"));
                Parent root = loader.load();

                // Obtener el stage actual
                Stage stage = (Stage) loginButton.getScene().getWindow();

                // Crear nueva escena y mostrarla
                Scene scene = new Scene(root, 1200, 600);
                stage.setScene(scene);
                stage.show();

            } catch (IOException e) {
                e.printStackTrace();
                showAlert("Error", "Ha ocurrido un error al cargar la aplicación: " + e.getMessage());
            }
        } else {
            // Mostrar alerta de credenciales incorrectas
            showAlert("Error de autenticación", "CREDENCIALES INCORRECTAS");
        }
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        // Inicialización del controlador
    }
}