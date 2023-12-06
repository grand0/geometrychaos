package ru.kpfu.itis.gr201.ponomarev.bheditor.ui;

import javafx.beans.InvalidationListener;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ObjectPropertyBase;
import javafx.geometry.VPos;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.Pane;
import javafx.scene.text.Font;
import javafx.scene.text.TextAlignment;
import ru.kpfu.itis.gr201.ponomarev.bheditor.anim.KeyFrameTag;
import ru.kpfu.itis.gr201.ponomarev.bheditor.game.HittingObject;
import ru.kpfu.itis.gr201.ponomarev.bheditor.util.InterpolatorType;
import ru.kpfu.itis.gr201.ponomarev.bheditor.util.Theme;
import ru.kpfu.itis.gr201.ponomarev.bheditor.anim.ObjectKeyFrame;

import java.util.List;
import java.util.Optional;

public class KeyFramesTimeline extends Pane {

    private final static double TIMELINE_HEIGHT = 20;
    private final static double KEYFRAME_SIZE = 6;
    private final static double SELECTED_KEYFRAME_SIZE = 8;
    private final static double TIMELINE_HORIZONTAL_GAP = KEYFRAME_SIZE / 2;
    private final static double ADD_KEYFRAME_BUTTON_SIZE = TIMELINE_HEIGHT;

    private final Canvas canvas;

    private final KeyFrameTag keyFrameTag;
    private final ObjectProperty<HittingObject> currentObject;
    private final ObjectProperty<ObjectKeyFrame> selectedKeyFrame;

    private boolean hoveringAddButton = false;

    private boolean lostKeyFrameFocus = false;

    public KeyFramesTimeline(KeyFrameTag tag, ObjectProperty<HittingObject> currentObject) {
        this.keyFrameTag = tag;

        this.currentObject = new ObjectPropertyBase<>() {
            @Override
            public Object getBean() {
                return null;
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
                return null;
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

        setOnMousePressed(event -> {
            requestFocus();
        });
        setOnMouseMoved(event -> {
            hoveringAddButton = event.getX() >= 0 && event.getX() <= ADD_KEYFRAME_BUTTON_SIZE;
            redraw(true);
        });
        setOnMouseExited(event -> {
            hoveringAddButton = false;
            redraw(true);
        });
        setOnMouseClicked(event -> {
            if (hoveringAddButton && currentObject.get() != null) {
                Optional<ObjectKeyFrame> opt = currentObject.get().getKeyFrame(currentObject.get().getTime(), tag);
                if (opt.isPresent()) {
                    setLostKeyFrameFocus(false);
                    setSelectedKeyFrame(opt.get());
                } else {
                    currentObject.get().addKeyFrame(
                            HittingObject.getDefaultValueForTag(tag),
                            currentObject.get().getTime(),
                            InterpolatorType.LINEAR,
                            tag,
                            null
                    );
                }
            } else {
                boolean selected = false;
                for (ObjectKeyFrame kf : getKeyFrames()) {
                    double centerX = msToPx(kf.getTime()) + ADD_KEYFRAME_BUTTON_SIZE + TIMELINE_HORIZONTAL_GAP;
                    if (event.getX() >= centerX - KEYFRAME_SIZE / 2 && event.getX() <= centerX + KEYFRAME_SIZE / 2) {
                        setLostKeyFrameFocus(false);
                        setSelectedKeyFrame(kf);
                        selected = true;
                        break;
                    }
                }
                if (!selected) {
                    setLostKeyFrameFocus(true);
                    setSelectedKeyFrame(null);
                }
            }
        });
        setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.DELETE && getCurrentObject() != null && getSelectedKeyFrame() != null) {
                getCurrentObject().removeKeyFrame(getSelectedKeyFrame());
                setLostKeyFrameFocus(true);
                setSelectedKeyFrame(null);
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

            g.setStroke(Theme.BACKGROUND.brighter());
            g.setLineWidth(1.0);
            g.strokeLine(
                    ADD_KEYFRAME_BUTTON_SIZE + TIMELINE_HORIZONTAL_GAP,
                    0,
                    ADD_KEYFRAME_BUTTON_SIZE + TIMELINE_HORIZONTAL_GAP,
                    TIMELINE_HEIGHT
            );
            g.strokeLine(
                    getWidth() - TIMELINE_HORIZONTAL_GAP,
                    0,
                    getWidth() - TIMELINE_HORIZONTAL_GAP,
                    TIMELINE_HEIGHT
            );

            g.setTextBaseline(VPos.CENTER);
            g.setTextAlign(TextAlignment.RIGHT);
            g.setFill(Theme.ON_BACKGROUND.deriveColor(0, 1, 1, 0.3));
            g.setFont(Font.font(16));
            g.fillText(keyFrameTag.getName(), getWidth() - 10, TIMELINE_HEIGHT / 2);

            for (ObjectKeyFrame kf : getKeyFrames()) {
                double kfSize = KEYFRAME_SIZE;
                if (getSelectedKeyFrame() != null && kf.equals(getSelectedKeyFrame())) {
                    g.setFill(Theme.ACCENT);
                    kfSize = SELECTED_KEYFRAME_SIZE;
                } else {
                    g.setFill(Theme.PRIMARY);
                }
                double centerX = msToPx(kf.getTime()) + ADD_KEYFRAME_BUTTON_SIZE + TIMELINE_HORIZONTAL_GAP;
                g.fillOval(centerX - kfSize / 2, TIMELINE_HEIGHT / 2 - kfSize / 2, kfSize, kfSize);
            }

            double cursorPositionOnScreen = msToPx(currentObject.get().getTime()) + ADD_KEYFRAME_BUTTON_SIZE + TIMELINE_HORIZONTAL_GAP;
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

    private List<ObjectKeyFrame> getKeyFrames() {
        return currentObject.get()
                .getInterpolationDriver()
                .getKeyFrames()
                .stream()
                .filter(kf -> kf.getTag().equals(keyFrameTag))
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

    public ObjectKeyFrame getSelectedKeyFrame() {
        return selectedKeyFrame.get();
    }

    public ObjectProperty<ObjectKeyFrame> selectedKeyFrameProperty() {
        return selectedKeyFrame;
    }

    public void setSelectedKeyFrame(ObjectKeyFrame selectedKeyFrame) {
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
        return getWidth() - ADD_KEYFRAME_BUTTON_SIZE - TIMELINE_HORIZONTAL_GAP * 2;
    }

    public boolean isLostKeyFrameFocus() {
        return lostKeyFrameFocus;
    }

    public void setLostKeyFrameFocus(boolean lostKeyFrameFocus) {
        this.lostKeyFrameFocus = lostKeyFrameFocus;
    }
}
