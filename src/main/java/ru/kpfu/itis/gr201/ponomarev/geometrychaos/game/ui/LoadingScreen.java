package ru.kpfu.itis.gr201.ponomarev.geometrychaos.game.ui;

import javafx.animation.*;
import javafx.geometry.Pos;
import javafx.scene.layout.Background;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Arc;
import javafx.scene.shape.StrokeLineCap;
import javafx.util.Duration;
import ru.kpfu.itis.gr201.ponomarev.geometrychaos.util.Theme;

public class LoadingScreen extends StackPane {

    public LoadingScreen() {
        setBackground(Background.fill(Theme.BACKGROUND));

        Arc loadingSpinner = new Arc(0, 0, 20, 20, 30, 300);
        loadingSpinner.setStroke(Theme.PRIMARY);
        loadingSpinner.setFill(null);
        loadingSpinner.setStrokeWidth(8);
        loadingSpinner.setStrokeLineCap(StrokeLineCap.ROUND);
        StackPane.setAlignment(loadingSpinner, Pos.CENTER);

        Timeline loadingSpinnerAnim = new Timeline();
        loadingSpinnerAnim.getKeyFrames().addAll(
                new KeyFrame(
                        Duration.ZERO,
                        new KeyValue(
                                loadingSpinner.rotateProperty(),
                                0.0
                        )
                ),
                new KeyFrame(
                        new Duration(1500),
                        new KeyValue(
                                loadingSpinner.rotateProperty(),
                                360.0,
                                Interpolator.LINEAR
                        )
                )
        );
        loadingSpinnerAnim.setCycleCount(Animation.INDEFINITE);

        getChildren().addAll(loadingSpinner);

        loadingSpinnerAnim.play();
    }
}
