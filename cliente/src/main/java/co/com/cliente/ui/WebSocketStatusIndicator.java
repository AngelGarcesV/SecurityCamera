package co.com.cliente.ui;

import javafx.application.Platform;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;

public class WebSocketStatusIndicator extends HBox {

    private Circle statusCircle;
    private Label statusLabel;

    public WebSocketStatusIndicator() {
        initializeComponents();
    }

    private void initializeComponents() {
        setAlignment(Pos.CENTER);
        setSpacing(8);

        // Círculo de estado
        statusCircle = new Circle(6);
        statusCircle.setFill(Color.RED);

        // Etiqueta de estado
        statusLabel = new Label("WebSocket: Desconectado");
        statusLabel.setStyle("-fx-font-size: 12px;");

        getChildren().addAll(statusCircle, statusLabel);

        // Estado inicial
        setDisconnected();
    }

    public void setConnected() {
        Platform.runLater(() -> {
            statusCircle.setFill(Color.GREEN);
            statusLabel.setText("WebSocket: Conectado");
            statusLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #00aa00;");
        });
    }

    public void setConnecting() {
        Platform.runLater(() -> {
            statusCircle.setFill(Color.ORANGE);
            statusLabel.setText("WebSocket: Conectando...");
            statusLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #ff9900;");
        });
    }

    public void setDisconnected() {
        Platform.runLater(() -> {
            statusCircle.setFill(Color.RED);
            statusLabel.setText("WebSocket: Desconectado");
            statusLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #cc0000;");
        });
    }

    public void setError(String error) {
        Platform.runLater(() -> {
            statusCircle.setFill(Color.RED);
            statusLabel.setText("WebSocket: Error - " + error);
            statusLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #cc0000;");
        });
    }
}