package co.com.cliente;

import co.com.cliente.controller.GrabarVideoController;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

import java.io.IOException;
import java.util.Optional;

public class Main extends Application {

    private static GrabarVideoController activeVideoController = null;

    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(Main.class.getResource("hello-view.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), 1200, 600);
        stage.setScene(scene);

        // Configurar evento de cierre de la aplicación
        stage.setOnCloseRequest(this::handleCloseRequest);

        stage.show();
    }

    private void handleCloseRequest(WindowEvent event) {
        // Verificar si hay una grabación en curso
        if (activeVideoController != null && activeVideoController.isRecording()) {
            event.consume(); // Evita que la ventana se cierre automáticamente

            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Grabación en curso");
            alert.setHeaderText("Hay una grabación en curso");
            alert.setContentText("Si cierra la aplicación, la grabación será detenida y enviada al servidor. ¿Desea continuar?");

            Optional<ButtonType> result = alert.showAndWait();
            if (result.isPresent() && result.get() == ButtonType.OK) {
                // Usuario acepta terminar la grabación
                activeVideoController.stopAndSaveRecording();

                // Dar tiempo para que se procese la grabación antes de cerrar
                try {
                    Thread.sleep(1000); // Esperar brevemente para que comience el proceso de guardado
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }

                // Cerrar la aplicación
                Platform.exit();
            }
        }
    }

    // Método para registrar el controlador activo de grabación de video
    public static void setActiveVideoController(GrabarVideoController controller) {
        activeVideoController = controller;
    }

    // Método para eliminar el controlador activo
    public static void clearActiveVideoController() {
        activeVideoController = null;
    }

    public static void main(String[] args) {
        launch();
    }
}