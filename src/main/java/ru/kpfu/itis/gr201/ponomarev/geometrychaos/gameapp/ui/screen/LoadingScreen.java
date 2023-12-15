package ru.kpfu.itis.gr201.ponomarev.geometrychaos.gameapp.ui.screen;

import javafx.geometry.Pos;
import javafx.scene.layout.StackPane;
import ru.kpfu.itis.gr201.ponomarev.geometrychaos.gameapp.ui.common.LoadingSpinner;

public class LoadingScreen extends Screen {

    public LoadingScreen() {
        super();
        LoadingSpinner loadingSpinner = new LoadingSpinner();
        StackPane.setAlignment(loadingSpinner, Pos.CENTER);
        getChildren().addAll(loadingSpinner);
    }
}
