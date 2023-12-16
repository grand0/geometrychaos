package ru.kpfu.itis.gr201.ponomarev.geometrychaos.gameapp.ui.screen;

import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

public class MainMenuScreen extends Screen {

    private Runnable onSinglePlayerPressed;
    private Runnable onMultiPlayerPressed;

    public MainMenuScreen() {
        super("Main Menu");

        Button singlePlayerBtn = new Button("Single Player");
        singlePlayerBtn.setOnAction(event -> {
            if (onSinglePlayerPressed != null) {
                onSinglePlayerPressed.run();
            }
        });

        Button multiPlayerBtn = new Button("Multi Player");
        multiPlayerBtn.setOnAction(event -> {
            if (onMultiPlayerPressed != null) {
                onMultiPlayerPressed.run();
            }
        });

        VBox buttonsBox = new VBox(singlePlayerBtn, multiPlayerBtn);
        buttonsBox.setSpacing(10);
        buttonsBox.setAlignment(Pos.CENTER);
        buttonsBox.setMinSize(USE_PREF_SIZE, USE_PREF_SIZE);
        buttonsBox.setMaxSize(USE_PREF_SIZE, USE_PREF_SIZE);
        StackPane.setAlignment(buttonsBox, Pos.CENTER);

        getChildren().addAll(buttonsBox);
    }

    public Runnable getOnSinglePlayerPressed() {
        return onSinglePlayerPressed;
    }

    public void setOnSinglePlayerPressed(Runnable onSinglePlayerPressed) {
        this.onSinglePlayerPressed = onSinglePlayerPressed;
    }

    public Runnable getOnMultiPlayerPressed() {
        return onMultiPlayerPressed;
    }

    public void setOnMultiPlayerPressed(Runnable onMultiPlayerPressed) {
        this.onMultiPlayerPressed = onMultiPlayerPressed;
    }
}
