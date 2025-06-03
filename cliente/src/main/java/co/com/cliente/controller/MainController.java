package co.com.cliente.controller;

import co.com.cliente.httpRequest.HttpService;
import co.com.cliente.httpRequest.PropertiesLoader;
import com.jfoenix.controls.JFXButton;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.stage.Stage;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

public class MainController implements Initializable {

    private HttpService httpService = HttpService.getInstance();

    @FXML
    private Label welcomeText;

    @FXML
    private TextField emailinput;

    @FXML
    private PasswordField passwordinput;

    @FXML
    private JFXButton loginButton;

    @FXML
    private Label errorLabel;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        // Limpiar cualquier token JWT previo al iniciar la pantalla de login
        httpService.setJwtToken(null);

        // Configurar evento para presionar Enter en los campos de texto
        emailinput.setOnKeyPressed(this::handleEnterKeyPressed);
        passwordinput.setOnKeyPressed(this::handleEnterKeyPressed);

        // Inicialmente ocultar mensajes de error
        if (errorLabel != null) {
            errorLabel.setVisible(false);
        }
    }

    private void handleEnterKeyPressed(KeyEvent event) {
        if (event.getCode() == KeyCode.ENTER) {
            onLoginButtonClick();
        }
    }

    @FXML
    protected void onHelloButtonClick() {
        welcomeText.setText("Welcome to JavaFX Application!");
    }

    @FXML
    protected void onLoginButtonClick() {
        // Validar que los campos no estén vacíos
        if (isInputValid()) {
            String email = emailinput.getText().trim();
            String password = passwordinput.getText();

            // Mostrar indicador de carga (podrías añadir un spinner aquí)
            loginButton.setDisable(true);
            if (errorLabel != null) {
                errorLabel.setVisible(false);
            }

            // Realizar login en un hilo separado para no bloquear la UI
            new Thread(() -> {
                try {
                    boolean isAuthenticated = authenticateUser(email, password);

                    Platform.runLater(() -> {
                        if (isAuthenticated) {
                            navigateToMainApplication();
                        } else {
                            showError("Credenciales incorrectas");
                            loginButton.setDisable(false);
                        }
                    });
                } catch (Exception e) {
                    e.printStackTrace();
                    Platform.runLater(() -> {
                        showError("Error: " + e.getMessage());
                        loginButton.setDisable(false);
                    });
                }
            }).start();
        }
    }

    private boolean isInputValid() {
        String email = emailinput.getText().trim();
        String password = passwordinput.getText();

        if (email.isEmpty()) {
            showError("El correo electrónico es obligatorio");
            emailinput.requestFocus();
            return false;
        }

        if (!email.matches("^[\\w-\\.]+@([\\w-]+\\.)+[\\w-]{2,4}$")) {
            showError("Formato de correo electrónico inválido");
            emailinput.requestFocus();
            return false;
        }

        if (password.isEmpty()) {
            showError("La contraseña es obligatoria");
            passwordinput.requestFocus();
            return false;
        }

        return true;
    }

    private void showError(String message) {
        if (errorLabel != null) {
            errorLabel.setText(message);
            errorLabel.setVisible(true);
        } else {
            showAlert("Error", message);
        }
    }

    private boolean authenticateUser(String email, String password) {
        String url = PropertiesLoader.getBaseUrl()+ "/api/auth/login";

        // Crear el objeto JSON para la solicitud de autenticación
        JSONObject requestJson = new JSONObject();
        requestJson.put("correo", email);
        requestJson.put("password", password);

        try {
            String responseText = httpService.sendPostRequest(url, requestJson.toString());

            // Verificar si la respuesta es un JSON y extraer el token
            try {
                JSONObject responseJson = new JSONObject(responseText);
                if (responseJson.has("token")) {
                    String token = responseJson.getString("token");
                    httpService.setJwtToken(token);
                    return true;
                } else {
                    // Si la respuesta es JSON pero no tiene token
                    return false;
                }
            } catch (JSONException e) {
                // Si la respuesta no es un JSON, asumimos que el token está en texto plano
                httpService.setJwtToken(responseText);
                return true;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return false; // Authentication failed
        }
    }

    private void navigateToMainApplication() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/co/com/cliente/application-view.fxml"));
            Parent root = loader.load();

            Stage stage = (Stage) loginButton.getScene().getWindow();
            Scene scene = new Scene(root, 1200, 600);
            stage.setScene(scene);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            showAlert("Error", "Ha ocurrido un error al cargar la aplicación: " + e.getMessage());
        }
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}