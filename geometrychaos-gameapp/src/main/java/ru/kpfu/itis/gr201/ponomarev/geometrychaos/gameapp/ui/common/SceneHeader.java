package ru.kpfu.itis.gr201.ponomarev.geometrychaos.gameapp.ui.common;

import javafx.beans.InvalidationListener;
import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Polygon;
import javafx.scene.shape.Shape;
import ru.kpfu.itis.gr201.ponomarev.geometrychaos.commons.ui.Theme;
import ru.kpfu.itis.gr201.ponomarev.geometrychaos.gameapp.util.GlobalAudioSpectrumProvider;

public class SceneHeader extends VBox {

    public SceneHeader(String text) {
        Label headerText = new Label(text);
        headerText.setFont(Theme.HEADLINE_FONT);
        headerText.setTextFill(Theme.ON_BACKGROUND);
        headerText.setPadding(new Insets(20));
        getChildren().addAll(headerText, AudioResponsiveHeaderDivider.getShape());
    }

    public static class AudioResponsiveHeaderDivider {

        private static final double HEADER_HEIGHT = 40;
        private static final double DEFAULT_HEADER_DIVIDER_WIDTH = 200;
        private static final double SENSITIVITY = 7.0;

        private static AudioResponsiveHeaderDivider instance;
        private final Polygon shape;

        private AudioResponsiveHeaderDivider() {
            shape = new Polygon();
            shape.setFill(Theme.ON_BACKGROUND);
            GlobalAudioSpectrumProvider.addListener(magnitudes -> {
                double sum = 0;
                for (float m : magnitudes) {
                    sum += m;
                }
                double avg = sum / magnitudes.length;
                double frac = 1.0 - (avg + (60.0 - SENSITIVITY)) / -SENSITIVITY;
                redraw(DEFAULT_HEADER_DIVIDER_WIDTH * frac);
            });
            redraw(DEFAULT_HEADER_DIVIDER_WIDTH);
        }

        public void redraw(double width) {
            shape.getPoints().setAll(
                    0.0, HEADER_HEIGHT,
                    500.0, HEADER_HEIGHT,
                    500.0, HEADER_HEIGHT + 1,
                    Math.max(30, width + 30), HEADER_HEIGHT + 1,
                    Math.max(20, width + 20), HEADER_HEIGHT + 10,
                    0.0, HEADER_HEIGHT + 10
            );
        }

        // WARNING: this method creates a new very frequently updated shape every time it's invoked.
        // may cause performance issues.
        public static Shape getShape() {
            if (instance == null) {
                instance = new AudioResponsiveHeaderDivider();
            }
            Polygon cloneShape = new Polygon();
            instance.shape.getPoints().addListener((InvalidationListener) obs -> cloneShape.getPoints().setAll(instance.shape.getPoints()));
            cloneShape.setFill(Theme.ON_BACKGROUND);
            return cloneShape;
        }
    }
}
