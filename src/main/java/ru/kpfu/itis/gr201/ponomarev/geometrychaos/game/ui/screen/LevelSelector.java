package ru.kpfu.itis.gr201.ponomarev.geometrychaos.game.ui.screen;

import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.layout.StackPane;

public class LevelSelector extends Screen {

    private Runnable onSelectPressed;

    public LevelSelector() {
        super("Select level");

        Button btn = new Button("Select");
        btn.setOnAction(event -> {
            if (onSelectPressed != null) {
                onSelectPressed.run();
            }
        });
        StackPane.setAlignment(btn, Pos.CENTER);

        getChildren().addAll(btn);
    }

    public Runnable getOnSelectPressed() {
        return onSelectPressed;
    }

    public void setOnSelectPressed(Runnable onSelectPressed) {
        this.onSelectPressed = onSelectPressed;
    }
}
