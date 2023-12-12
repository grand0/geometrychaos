package ru.kpfu.itis.gr201.ponomarev.geometrychaos.game.ui;

import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.layout.Background;
import javafx.scene.layout.StackPane;
import ru.kpfu.itis.gr201.ponomarev.geometrychaos.game.ui.common.SceneHeader;
import ru.kpfu.itis.gr201.ponomarev.geometrychaos.util.Theme;

public class LevelSelector extends StackPane {

    private Runnable onPlayPressed;

    public LevelSelector() {
        setBackground(Background.fill(Theme.BACKGROUND));

        SceneHeader header = new SceneHeader("Select level");
        StackPane.setAlignment(header, Pos.TOP_CENTER);

        Button btn = new Button("Play");
        btn.setOnAction(event -> {
            if (onPlayPressed != null) {
                onPlayPressed.run();
            }
        });
        StackPane.setAlignment(btn, Pos.CENTER);

        getChildren().addAll(header, btn);
    }

    public Runnable getOnPlayPressed() {
        return onPlayPressed;
    }

    public void setOnPlayPressed(Runnable onPlayPressed) {
        this.onPlayPressed = onPlayPressed;
    }
}
