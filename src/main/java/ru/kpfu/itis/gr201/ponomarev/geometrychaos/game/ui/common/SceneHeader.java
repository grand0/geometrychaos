package ru.kpfu.itis.gr201.ponomarev.geometrychaos.game.ui.common;

import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Line;
import javafx.scene.shape.Polygon;
import javafx.scene.shape.Shape;
import ru.kpfu.itis.gr201.ponomarev.geometrychaos.util.GlobalAudioSpectrumProvider;
import ru.kpfu.itis.gr201.ponomarev.geometrychaos.util.Theme;

public class SceneHeader extends VBox {

    private static final double HEADER_HEIGHT = 40;
    private static final double DEFAULT_HEADER_DIVIDER_WIDTH = 200;
    private static final double SENSITIVITY = 7.0;
    private Shape divider;

    public SceneHeader(String text) {
        Label headerText = new Label(text);
        headerText.setFont(Theme.HEADLINE_FONT);
        headerText.setTextFill(Theme.ON_BACKGROUND);
        headerText.setPadding(new Insets(20));
        getChildren().add(headerText);

        redrawDivider(DEFAULT_HEADER_DIVIDER_WIDTH);

        GlobalAudioSpectrumProvider.addListener(magnitudes -> {
            double sum = 0;
            for (float m : magnitudes) {
                sum += m;
            }
            double avg = sum / magnitudes.length;
            double frac = 1.0 - (avg + (60.0 - SENSITIVITY)) / -SENSITIVITY;
            redrawDivider(DEFAULT_HEADER_DIVIDER_WIDTH * frac);
        });
    }

    public void redrawDivider(double width) {
        getChildren().remove(divider);

        Line dividerLine = new Line(0, HEADER_HEIGHT, 500, HEADER_HEIGHT);
        Polygon dividerFigure = new Polygon(
                0, HEADER_HEIGHT,
                Math.max(30, width + 30), HEADER_HEIGHT,
                Math.max(20, width + 20), HEADER_HEIGHT + 10,
                0, HEADER_HEIGHT + 10
        );
        divider = Shape.union(dividerLine, dividerFigure);
        divider.setFill(Theme.ON_BACKGROUND);

        getChildren().add(divider);
    }
}
