package ru.kpfu.itis.gr201.ponomarev.geometrychaos.gameapp.ui.screen;

import javafx.geometry.HPos;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import ru.kpfu.itis.gr201.ponomarev.geometrychaos.commons.ui.Theme;

import java.util.function.Consumer;

public class ConnectScreen extends Screen {

    private Consumer<ConnectScreenData> onConnectPressed;
    private final Label errorLabel;

    public ConnectScreen() {
        super("Connect");

        TextField usernameField = new TextField();
        TextField hostField = new TextField();
        Button connectBtn = new Button("Connect");
        connectBtn.setOnAction(event -> {
            if (onConnectPressed != null) {
                onConnectPressed.accept(new ConnectScreenData(usernameField.getText(), hostField.getText()));
            }
        });
        GridPane.setHalignment(connectBtn, HPos.CENTER);

        GridPane gridPane = new GridPane();
        gridPane.setAlignment(Pos.CENTER);
        gridPane.setVgap(10);
        gridPane.setHgap(10);
        gridPane.addRow(0, makeLabel("Username"), usernameField);
        gridPane.addRow(1, makeLabel("Host"), hostField);
        gridPane.add(connectBtn, 0, 2, 2, 1);
        errorLabel = new Label();
        errorLabel.getStyleClass().add("error");
        errorLabel.setMaxWidth(500);
        errorLabel.setWrapText(true);
        gridPane.add(errorLabel, 0, 3, 2, 1);
        gridPane.setMinSize(USE_PREF_SIZE, USE_PREF_SIZE);
        gridPane.setMaxSize(USE_PREF_SIZE, USE_PREF_SIZE);

        getChildren().addAll(gridPane);
    }

    private Node makeLabel(String text) {
        Label label = new Label(text);
        label.setTextFill(Theme.ON_BACKGROUND);
        return label;
    }

    public Consumer<ConnectScreenData> getOnConnectPressed() {
        return onConnectPressed;
    }

    public void setOnConnectPressed(Consumer<ConnectScreenData> onConnectPressed) {
        this.onConnectPressed = onConnectPressed;
    }

    public String getError() {
        return errorLabel.getText();
    }

    public void setError(String error) {
        errorLabel.setText(error);
    }

    public record ConnectScreenData(String username, String host) {}
}
