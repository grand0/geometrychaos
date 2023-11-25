package ru.kpfu.itis.gr201.ponomarev.bheditor.ui;

import javafx.animation.KeyFrame;
import javafx.beans.InvalidationListener;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ObjectPropertyBase;
import javafx.geometry.VPos;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.layout.Pane;
import javafx.scene.text.TextAlignment;
import ru.kpfu.itis.gr201.ponomarev.bheditor.game.HittingObject;
import ru.kpfu.itis.gr201.ponomarev.bheditor.util.Theme;

public class KeyFramesTimeline extends Pane {

    private final static double TIMELINE_HEIGHT = 20;
    private final static double KEYFRAME_SIZE = 10;

    private final Canvas canvas;

    private final String name;
    private final String keyFrameNamePrefix;
    private final ObjectProperty<HittingObject> currentObject;

    public KeyFramesTimeline(String name, String keyFrameNamePrefix, ObjectProperty<HittingObject> currentObject) {
        this.name = name;
        this.keyFrameNamePrefix = keyFrameNamePrefix;

        this.currentObject = new ObjectPropertyBase<>() {
            @Override
            public Object getBean() {
                return this;
            }

            @Override
            public String getName() {
                return "currentObject";
            }
        };
        this.currentObject.bind(currentObject);
        InvalidationListener redrawOnChange = (obs) -> redraw();
        this.currentObject.addListener((obs, oldV, newV) -> {
            if (oldV != null) {
                oldV.removeListener(redrawOnChange);
            }
            if (newV != null) {
                newV.addListener(redrawOnChange);
            }
            redraw();
        });

        canvas = new Canvas();
        canvas.widthProperty().bind(widthProperty());
        canvas.heightProperty().bind(heightProperty());
        getChildren().add(canvas);

        setMinHeight(TIMELINE_HEIGHT);
        setVisible(false);
    }

    private void redraw() {
        if (currentObject.get() == null) {
            setVisible(false);
            return;
        }
        setVisible(true);

        GraphicsContext g = canvas.getGraphicsContext2D();

        g.clearRect(0, 0, getWidth(), getHeight());

        g.setTextBaseline(VPos.CENTER);
        g.setTextAlign(TextAlignment.RIGHT);
        g.setFill(Theme.ON_BACKGROUND.deriveColor(0, 1, 1, 0.3));
        g.fillText(name, getWidth() - 5, TIMELINE_HEIGHT / 2);

        g.setFill(Theme.PRIMARY);
        for (
                KeyFrame kf : currentObject.get()
                    .getTimeline()
                    .getKeyFrames()
                    .stream()
                    .filter(kf -> kf.getName().startsWith(keyFrameNamePrefix))
                    .toList()
        ) {
        	double centerX = msToPx((int) kf.getTime().toMillis());
            g.fillOval(centerX - KEYFRAME_SIZE / 2, TIMELINE_HEIGHT / 2 - KEYFRAME_SIZE / 2, KEYFRAME_SIZE, KEYFRAME_SIZE);
        }

        double cursorPositionOnScreen = msToPx(currentObject.get().getTime() - currentObject.get().getStartTime());
        g.setStroke(Theme.PRIMARY);
        g.setLineWidth(1.0);
        g.strokeLine(
                cursorPositionOnScreen,
                0,
                cursorPositionOnScreen,
                getHeight()
        );
    }

    public HittingObject getCurrentObject() {
        return currentObject.get();
    }

    public ObjectProperty<HittingObject> currentObjectProperty() {
        return currentObject;
    }

    public void setCurrentObject(HittingObject currentObject) {
        this.currentObject.set(currentObject);
    }

    private int pxToMs(double px) {
        return (int) (px / pxPerMs());
    }

    private double msToPx(int ms) {
        return pxPerMs() * ms;
    }

    private double pxPerMs() {
        return getWidth() / currentObject.get().getDuration();
    }
}
