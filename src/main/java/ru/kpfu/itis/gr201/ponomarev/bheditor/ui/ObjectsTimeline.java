package ru.kpfu.itis.gr201.ponomarev.bheditor.ui;

import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.beans.InvalidationListener;
import javafx.beans.property.*;
import javafx.geometry.VPos;
import javafx.scene.Cursor;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.Pane;
import javafx.util.Duration;
import ru.kpfu.itis.gr201.ponomarev.bheditor.game.HittingObject;
import ru.kpfu.itis.gr201.ponomarev.bheditor.util.GameObjectsManager;
import ru.kpfu.itis.gr201.ponomarev.bheditor.util.Interpolators;
import ru.kpfu.itis.gr201.ponomarev.bheditor.util.Theme;

import java.util.*;

public class ObjectsTimeline extends Pane {

    public final static int LAYERS_COUNT = 10;

    private final static double TIMELINE_TIME_AXIS_HEIGHT = 30;
    private final static double TIMELINE_LAYER_HEIGHT = 20;
    private final static double TIMELINE_CURSOR_SIZE = 20;
    private final static int TIMELINE_MILLIS_PER_WIDTH = 5000;

    private final Canvas canvas;

    private final IntegerProperty visualMillisOffset = new IntegerPropertyBase() {
        @Override
        protected void invalidated() {
            super.invalidated();
            redraw();
        }

        @Override
        public Object getBean() {
            return null;
        }

        @Override
        public String getName() {
            return "visualMillisOffset";
        }
    };
    private final DoubleProperty zoom = new DoublePropertyBase() {
        @Override
        protected void invalidated() {
            super.invalidated();
            redraw();
        }

        @Override
        public Object getBean() {
            return null;
        }

        @Override
        public String getName() {
            return "zoom";
        }
    };
    private boolean changingCursorPos = false;
    private Double objectDragStartX = null;
    private Integer selectedObjectStartTimeBeforeDrag = null;
    private Integer selectedObjectDurationBeforeDrag = null;

    private final ObjectProperty<HittingObject> selectedObject = new ObjectPropertyBase<>() {
        @Override
        protected void invalidated() {
            super.invalidated();
            redraw();
        }

        @Override
        public Object getBean() {
            return null;
        }

        @Override
        public String getName() {
            return "selectedObject";
        }
    };

    private final IntegerProperty cursorPosition = new IntegerPropertyBase() {
        @Override
        protected void invalidated() {
            super.invalidated();
            redraw();
        }

        @Override
        public Object getBean() {
            return null;
        }

        @Override
        public String getName() {
            return "cursorPosition";
        }
    };

    public ObjectsTimeline() {
        this.canvas = new Canvas();
        this.canvas.widthProperty().bind(widthProperty());
        this.canvas.heightProperty().bind(heightProperty());

        widthProperty().addListener(obs -> redraw());
        heightProperty().addListener(obs -> redraw());

        getChildren().add(canvas);

        setMinHeight(TIMELINE_LAYER_HEIGHT * LAYERS_COUNT + TIMELINE_TIME_AXIS_HEIGHT);

        visualMillisOffset.set(0);
        zoom.set(1.0);

        setOnMousePressed(event -> {
            requestFocus();

            if (event.getY() <= TIMELINE_TIME_AXIS_HEIGHT) {
                changingCursorPos = true;
                setCursorPosition(pxToMs(event.getX()) + visualMillisOffset.get());
            } else {
                int layer = (int) ((event.getY() - TIMELINE_TIME_AXIS_HEIGHT) / TIMELINE_LAYER_HEIGHT);
                int time = pxToMs(event.getX()) + visualMillisOffset.get();
                boolean prepareDrag = false;
                boolean prepareScale = false;
                for (HittingObject obj : GameObjectsManager.getInstance()
                        .getObjects()
                        .stream()
                        .filter(ho -> ho.getTimelineLayer() == layer)
                        .sorted(Comparator.comparing(HittingObject::getStartTime).reversed())
                        .toList()) {
                	if (obj.getTimelineLayer() == layer && (time >= obj.getStartTime() && time <= obj.getEndTime())) {
                        if (time >= obj.getEndTime() - pxToMs(5)) {
                            prepareScale = true;
                            setCursor(Cursor.H_RESIZE);
                        } else {
                            prepareDrag = true;
                        }
                        setSelectedObject(obj);
                        break;
                    }
                }
                if (prepareDrag) {
                    objectDragStartX = event.getX();
                    selectedObjectStartTimeBeforeDrag = getSelectedObject().getStartTime();
                } else if (prepareScale) {
                    objectDragStartX = event.getX();
                    selectedObjectDurationBeforeDrag = getSelectedObject().getDuration();
                } else {
                    setSelectedObject(null);
                }
            }
        });
        setOnMouseDragged(event -> {
            if (changingCursorPos) {
                setCursorPosition(pxToMs(event.getX()) + visualMillisOffset.get());
            } else if (objectDragStartX != null) {
                if (selectedObjectStartTimeBeforeDrag != null) {
                    selectedObject.get().setStartTime(Math.max(0, selectedObjectStartTimeBeforeDrag + pxToMs(event.getX() - objectDragStartX)));
                    int layer = (int) ((event.getY() - TIMELINE_TIME_AXIS_HEIGHT) / TIMELINE_LAYER_HEIGHT);
                    if (selectedObject.get().getTimelineLayer() != layer) {
                        selectedObject.get().setTimelineLayer(layer);
                    }
                } else {
                    selectedObject.get().setDuration(Math.max(0, selectedObjectDurationBeforeDrag + pxToMs(event.getX() - objectDragStartX)));
                }
            }
        });
        setOnMouseReleased(event -> {
            changingCursorPos = false;
            objectDragStartX = null;
            selectedObjectStartTimeBeforeDrag = null;
            setCursor(Cursor.DEFAULT);
        });
        setOnScroll(event -> {
            zoom.set(Math.max(0.1, Math.min(10, zoom.getValue() + event.getDeltaY() / event.getMultiplierY() * 0.1)));
            visualMillisOffset.set(Math.max(0, visualMillisOffset.get() - pxToMs(event.getDeltaX())));
        });
        setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.DELETE && getSelectedObject() != null) {
                GameObjectsManager.getInstance().removeObject(getSelectedObject());
                setSelectedObject(null);
            } else if (event.getCode() == KeyCode.D && event.isControlDown() && getSelectedObject() != null) {
                HittingObject copy = new HittingObject(
                        getSelectedObject().getName(),
                        getSelectedObject().getStartTime(),
                        getSelectedObject().getDuration(),
                        getSelectedObject().getTimelineLayer() == LAYERS_COUNT - 1 ? 0 : getSelectedObject().getTimelineLayer() + 1
                );
                for (KeyFrame kf : getSelectedObject().getKeyFrames()) {
                    kf.getValues().stream().findFirst().ifPresent(kv -> {
                        copy.addKeyFrame(
                                (double) kv.getEndValue(),
                                (int) kf.getTime().toMillis(),
                                Interpolators.byInterpolator(kv.getInterpolator()),
                                HittingObject.getPrefixFromKeyFrameName(kf.getName())
                        );
                    });
                }
                copy.setShape(getSelectedObject().getShape());
                GameObjectsManager.getInstance().addObject(copy);
                setSelectedObject(copy);
            }
        });

        InvalidationListener redrawOnInvalidate = obs1 -> redraw();

        GameObjectsManager.getInstance().objectsProperty().addListener(redrawOnInvalidate);

        selectedObject.addListener((obs, oldV, newV) -> {
            if (oldV != null) {
                oldV.removeListener(redrawOnInvalidate);
            }
            if (selectedObject.get() != null) {
                selectedObject.get().addListener(redrawOnInvalidate);
            }
            redraw();
        });
    }

    public void redraw() {
        if (getWidth() == 0 || getHeight() == 0) {
            return;
        }

        double millisPerWidth = TIMELINE_MILLIS_PER_WIDTH * zoom.get();

        GraphicsContext g = canvas.getGraphicsContext2D();

        g.setFill(Theme.BACKGROUND);
        g.fillRect(0, 0, getWidth(), getHeight());

        g.setFill(Theme.BACKGROUND.brighter());
        for (int i = 1; i < LAYERS_COUNT; i += 2) {
            g.fillRect(0, TIMELINE_TIME_AXIS_HEIGHT + TIMELINE_LAYER_HEIGHT * i, getWidth(), TIMELINE_LAYER_HEIGHT);
        }

        g.setFill(Theme.ON_BACKGROUND);
        g.setStroke(Theme.BACKGROUND.brighter().brighter());
        g.setLineWidth(1.0);
        int step = 500;
        if (msToPx(step) >= 200) {
            step /= (int) Math.pow(2, (int) (msToPx(step) / 200) - 1);
            step = Math.max(63, step);
        } else {
            step *= (int) Math.pow(2, (int) (200 / msToPx(step)) - 1);
            step = Math.min(40000, step);
        }
        for (int i = step * (visualMillisOffset.get() / step); i <= millisPerWidth + visualMillisOffset.get(); i += step) {
            g.fillText(String.format("%.2fs", i / 1000.0), msToPx(i - visualMillisOffset.get()), TIMELINE_TIME_AXIS_HEIGHT / 2);
            g.strokeLine(msToPx(i - visualMillisOffset.get()), TIMELINE_TIME_AXIS_HEIGHT, msToPx(i - visualMillisOffset.get()), getHeight());
        }

        g.setTextBaseline(VPos.CENTER);
        for (int i = 0; i < LAYERS_COUNT; i++) {
            int curLayer = i;
            for (
                    HittingObject ho : GameObjectsManager.getInstance()
                        .getObjects()
                        .stream()
                        .filter(ho -> ho.getTimelineLayer() == curLayer)
                        .sorted(Comparator.comparing(HittingObject::getStartTime))
                        .toList()
            ) {
            	int start = ho.getStartTime();
                int end = ho.getEndTime();
                if (end >= visualMillisOffset.get() && start <= visualMillisOffset.get() + millisPerWidth) {
                    if (ho.equals(getSelectedObject())) {
                        g.setFill(Theme.ACCENT);
                        g.setStroke(Theme.ACCENT.darker());
                        g.setLineWidth(2.0);
                    } else {
                        g.setFill(Theme.PRIMARY);
                        g.setStroke(Theme.PRIMARY.darker());
                        g.setLineWidth(1.0);
                    }
                    g.fillRoundRect(
                            msToPx(start - visualMillisOffset.get()),
                            TIMELINE_TIME_AXIS_HEIGHT + i * TIMELINE_LAYER_HEIGHT + 1,
                            msToPx(end - start),
                            TIMELINE_LAYER_HEIGHT - 2,
                            5,
                            5
                    );
                    g.strokeRoundRect(
                            msToPx(start - visualMillisOffset.get()),
                            TIMELINE_TIME_AXIS_HEIGHT + i * TIMELINE_LAYER_HEIGHT + 1,
                            msToPx(end - start),
                            TIMELINE_LAYER_HEIGHT - 2,
                            5,
                            5
                    );

                    g.setFill(Theme.BACKGROUND);
                    g.fillText(
                            ho.getName(),
                            msToPx(start - visualMillisOffset.get()),
                            TIMELINE_TIME_AXIS_HEIGHT + i * TIMELINE_LAYER_HEIGHT + TIMELINE_LAYER_HEIGHT * 0.5,
                            msToPx(end - start)
                    );
                }
            }
        }

        double cursorPositionOnScreen = msToPx(getCursorPosition() - visualMillisOffset.get());
        g.setFill(Theme.PRIMARY);
        g.setStroke(Theme.PRIMARY);
        g.setLineWidth(1.0);
        g.fillRect(
                cursorPositionOnScreen - (TIMELINE_CURSOR_SIZE / 2.0),
                0,
                TIMELINE_CURSOR_SIZE,
                TIMELINE_CURSOR_SIZE
        );
        g.fillPolygon(
                new double[] {cursorPositionOnScreen - (TIMELINE_CURSOR_SIZE / 2.0), cursorPositionOnScreen, cursorPositionOnScreen + (TIMELINE_CURSOR_SIZE / 2.0)},
                new double[] {TIMELINE_CURSOR_SIZE, TIMELINE_TIME_AXIS_HEIGHT, TIMELINE_CURSOR_SIZE},
                3
        );
        g.strokeLine(
                cursorPositionOnScreen,
                TIMELINE_TIME_AXIS_HEIGHT,
                cursorPositionOnScreen,
                getHeight()
        );
    }

    public Duration getTotalDuration() {
        HittingObject lastObj = GameObjectsManager.getInstance().getObjects().stream()
                .max(Comparator.comparingInt(HittingObject::getEndTime))
                .orElse(null);
        return new Duration(lastObj == null ? 0 : lastObj.getEndTime());
    }

    public int getCursorPosition() {
        return cursorPosition.get();
    }

    public IntegerProperty cursorPositionProperty() {
        return cursorPosition;
    }

    public void setCursorPosition(int cursorPosition) {
        if (cursorPosition < 0) cursorPosition = 0;
        this.cursorPosition.set(cursorPosition);
    }

    public HittingObject getSelectedObject() {
        return selectedObject.get();
    }

    public ObjectProperty<HittingObject> selectedObjectProperty() {
        return selectedObject;
    }

    public void setSelectedObject(HittingObject selectedObject) {
        this.selectedObject.set(selectedObject);
    }

    private int pxToMs(double px) {
        return (int) (px / pxPerMs());
    }

    private double msToPx(int ms) {
        return pxPerMs() * ms;
    }

    private double pxPerMs() {
        double millisPerWidth = TIMELINE_MILLIS_PER_WIDTH * zoom.get();
        return getWidth() / millisPerWidth;
    }
}
