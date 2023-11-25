package ru.kpfu.itis.gr201.ponomarev.bheditor.ui;

import javafx.animation.Interpolator;
import javafx.animation.KeyFrame;
import javafx.beans.InvalidationListener;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ObjectPropertyBase;
import javafx.geometry.VPos;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.layout.Pane;
import javafx.scene.text.Font;
import javafx.scene.text.TextAlignment;
import ru.kpfu.itis.gr201.ponomarev.bheditor.game.HittingObject;
import ru.kpfu.itis.gr201.ponomarev.bheditor.util.Interpolators;
import ru.kpfu.itis.gr201.ponomarev.bheditor.util.Theme;

import java.util.List;

public class KeyFramesTimeline extends Pane {

    private final static double TIMELINE_HEIGHT = 20;
    private final static double ADD_KEYFRAME_BUTTON_SIZE = TIMELINE_HEIGHT;
    private final static double KEYFRAME_SIZE = 6;

    private final Canvas canvas;

    private final String name;
    private final String keyFrameNamePrefix;
    private final ObjectProperty<HittingObject> currentObject;
    private final ObjectProperty<KeyFrame> selectedKeyFrame;

    private boolean hoveringAddButton = false;

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

        selectedKeyFrame = new ObjectPropertyBase<>() {
            @Override
            public Object getBean() {
                return this;
            }

            @Override
            public String getName() {
                return "selectedKeyFrame";
            }
        };
        selectedKeyFrame.addListener(obs -> {
            redraw();
        });

        canvas = new Canvas();
        canvas.widthProperty().bind(widthProperty());
        canvas.heightProperty().bind(heightProperty());
        getChildren().add(canvas);

        setMinHeight(TIMELINE_HEIGHT);
        setVisible(false);

        canvas.setOnMouseMoved(event -> {
            hoveringAddButton = event.getX() >= 0 && event.getX() <= ADD_KEYFRAME_BUTTON_SIZE;
            redraw(true);
        });
        canvas.setOnMouseExited(event -> {
            hoveringAddButton = false;
            redraw(true);
        });
        canvas.setOnMouseClicked(event -> {
            if (hoveringAddButton && currentObject.get() != null) {
                currentObject.get().addKeyFrame(
                        0,
                        currentObject.get().getTime(),
                        Interpolators.LINEAR,
                        keyFrameNamePrefix
                );
            } else {
                for (KeyFrame kf : getKeyFrames()) {
                    double centerX = msToPx((int) kf.getTime().toMillis()) + ADD_KEYFRAME_BUTTON_SIZE;
                    if (event.getX() >= centerX - KEYFRAME_SIZE / 2 && event.getX() <= centerX + KEYFRAME_SIZE / 2) {
                        setSelectedKeyFrame(kf);
                        break;
                    }
                }
            }
        });
    }

    private void redraw(boolean onlyAddButton) {
        if (currentObject.get() == null) {
            setVisible(false);
            return;
        }
        setVisible(true);

        GraphicsContext g = canvas.getGraphicsContext2D();

        if (!onlyAddButton) {
            g.clearRect(0, 0, getWidth(), getHeight());

            g.setTextBaseline(VPos.CENTER);
            g.setTextAlign(TextAlignment.RIGHT);
            g.setFill(Theme.ON_BACKGROUND.deriveColor(0, 1, 1, 0.3));
            g.setFont(Font.font(20));
            g.fillText(name, getWidth() - 5, TIMELINE_HEIGHT / 2);

            for (KeyFrame kf : getKeyFrames()) {
                if (getSelectedKeyFrame() != null && kf.equals(getSelectedKeyFrame())) {
                    g.setFill(Theme.ACCENT);
                } else {
                    g.setFill(Theme.PRIMARY);
                }
                double centerX = msToPx((int) kf.getTime().toMillis()) + ADD_KEYFRAME_BUTTON_SIZE;
                g.fillOval(centerX - KEYFRAME_SIZE / 2, TIMELINE_HEIGHT / 2 - KEYFRAME_SIZE / 2, KEYFRAME_SIZE, KEYFRAME_SIZE);
            }

            double cursorPositionOnScreen = msToPx(currentObject.get().getTime()) + ADD_KEYFRAME_BUTTON_SIZE;
            g.setStroke(Theme.PRIMARY);
            g.setLineWidth(1.0);
            g.strokeLine(
                    cursorPositionOnScreen,
                    0,
                    cursorPositionOnScreen,
                    getHeight()
            );
        } else {
            g.clearRect(0, 0, ADD_KEYFRAME_BUTTON_SIZE, ADD_KEYFRAME_BUTTON_SIZE);
        }

        g.setTextBaseline(VPos.CENTER);
        g.setTextAlign(TextAlignment.CENTER);
        g.setFill(hoveringAddButton ? Theme.PRIMARY : Theme.BACKGROUND.brighter());
        g.fillRect(0, 0, ADD_KEYFRAME_BUTTON_SIZE, ADD_KEYFRAME_BUTTON_SIZE);
        g.setStroke(Theme.ON_BACKGROUND);
        g.setLineWidth(2.0);
        g.strokeLine(ADD_KEYFRAME_BUTTON_SIZE / 2, ADD_KEYFRAME_BUTTON_SIZE * 0.25, ADD_KEYFRAME_BUTTON_SIZE / 2, ADD_KEYFRAME_BUTTON_SIZE * 0.75);
        g.strokeLine(ADD_KEYFRAME_BUTTON_SIZE * 0.25, ADD_KEYFRAME_BUTTON_SIZE / 2, ADD_KEYFRAME_BUTTON_SIZE * 0.75, ADD_KEYFRAME_BUTTON_SIZE / 2);
    }

    private List<KeyFrame> getKeyFrames() {
        return currentObject.get()
                .getTimeline()
                .getKeyFrames()
                .stream()
                .filter(kf -> kf.getName().startsWith(keyFrameNamePrefix))
                .toList();
    }

    private void redraw() {
        redraw(false);
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

    public KeyFrame getSelectedKeyFrame() {
        return selectedKeyFrame.get();
    }

    public ObjectProperty<KeyFrame> selectedKeyFrameProperty() {
        return selectedKeyFrame;
    }

    public void setSelectedKeyFrame(KeyFrame selectedKeyFrame) {
        this.selectedKeyFrame.set(selectedKeyFrame);
    }

    private int pxToMs(double px) {
        return (int) (px / pxPerMs());
    }

    private double msToPx(int ms) {
        return pxPerMs() * ms;
    }

    private double pxPerMs() {
        return getTimelineWidth() / currentObject.get().getDuration();
    }

    private double getTimelineWidth() {
        return getWidth() - ADD_KEYFRAME_BUTTON_SIZE;
    }
}
