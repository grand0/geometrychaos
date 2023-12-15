package ru.kpfu.itis.gr201.ponomarev.geometrychaos.game.ui.common;

import javafx.animation.*;
import javafx.scene.shape.Arc;
import javafx.scene.shape.StrokeLineCap;
import javafx.util.Duration;
import ru.kpfu.itis.gr201.ponomarev.geometrychaos.util.Theme;

public class LoadingSpinner extends Arc {

    public LoadingSpinner() {
        super(0, 0, 20, 20, 30, 300);

        setStroke(Theme.PRIMARY);
        setFill(null);
        setStrokeWidth(8);
        setStrokeLineCap(StrokeLineCap.ROUND);

        Timeline anim = new Timeline();
        anim.getKeyFrames().addAll(
                new KeyFrame(
                        Duration.ZERO,
                        new KeyValue(
                                rotateProperty(),
                                0.0
                        )
                ),
                new KeyFrame(
                        new Duration(1500),
                        new KeyValue(
                                rotateProperty(),
                                360.0,
                                Interpolator.LINEAR
                        )
                )
        );
        anim.setCycleCount(Animation.INDEFINITE);
        anim.play();
    }
}
