package ru.kpfu.itis.gr201.ponomarev.geometrychaos.gameapp.ui.common;

import javafx.beans.property.IntegerProperty;
import javafx.scene.layout.Background;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;
import ru.kpfu.itis.gr201.ponomarev.geometrychaos.commons.ui.Theme;

public class AudioTimeBar extends Pane {

    public static final double BAR_WIDTH = 500;
    public static final double BAR_HEIGHT = 10;

    private final int end;
    private final Line cursorLine;

    public AudioTimeBar(IntegerProperty time, int end) {
        this.end = end;
        this.cursorLine = new Line(0, 0, 0, BAR_HEIGHT);
        this.cursorLine.setStroke(Color.WHITE);
        getChildren().add(cursorLine);

        setBackground(Background.fill(Theme.BACKGROUND.deriveColor(0, 1, 2, 0.5)));
        setMaxWidth(BAR_WIDTH);
        setMaxHeight(BAR_HEIGHT);

        time.addListener(obs -> {
            redraw(time.get());
        });
    }

    public void redraw(int time) {
        double frac = (double) time / end;
        double x = BAR_WIDTH * frac;
        cursorLine.setStartX(x);
        cursorLine.setEndX(x);
    }
}
