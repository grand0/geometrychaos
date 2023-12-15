package ru.kpfu.itis.gr201.ponomarev.geometrychaos.game.ui.screen;

import javafx.geometry.Pos;
import javafx.scene.layout.Background;
import javafx.scene.layout.StackPane;
import ru.kpfu.itis.gr201.ponomarev.geometrychaos.game.ui.common.SceneHeader;
import ru.kpfu.itis.gr201.ponomarev.geometrychaos.util.Theme;

public abstract class Screen extends StackPane {

    public Screen() {
        this(null);
    }

    public Screen(String title) {
        setBackground(Background.fill(Theme.BACKGROUND));

        if (title != null && !title.isEmpty()) {
            SceneHeader header = new SceneHeader(title);
            StackPane.setAlignment(header, Pos.TOP_CENTER);
            getChildren().add(header);
        }
    }
}
