package ru.kpfu.itis.gr201.ponomarev.geometrychaos.editorapp.ui;

import javafx.beans.InvalidationListener;
import javafx.beans.property.*;
import javafx.geometry.VPos;
import javafx.scene.Cursor;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.input.KeyCode;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.util.Duration;
import ru.kpfu.itis.gr201.ponomarev.geometrychaos.commons.anim.KeyFrameType;
import ru.kpfu.itis.gr201.ponomarev.geometrychaos.commons.anim.ObjectKeyFrame;
import ru.kpfu.itis.gr201.ponomarev.geometrychaos.commons.game.GameObject;
import ru.kpfu.itis.gr201.ponomarev.geometrychaos.commons.game.LevelManager;
import ru.kpfu.itis.gr201.ponomarev.geometrychaos.commons.ui.Theme;
import ru.kpfu.itis.gr201.ponomarev.geometrychaos.editorapp.audio.LevelAudio;
import ru.kpfu.itis.gr201.ponomarev.geometrychaos.editorapp.ui.dialog.CreateArrayDialog;

import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

public class ObjectsTimeline extends Pane {

    public final static int VISIBLE_LAYERS_COUNT = 10;

    private final static double TIMELINE_TIME_AXIS_HEIGHT = 30;
    private final static double TIMELINE_LAYER_HEIGHT = 20;
    private final static double TIMELINE_CURSOR_SIZE = 20;
    private final static int TIMELINE_MILLIS_PER_WIDTH = 5000;
    private final static double TIMELINE_CURSOR_SNAP_TOLERANCE = 10;

    private final Canvas canvas;
    private final Canvas waveformCanvas;

    private boolean hideObjects = false;

    private final IntegerProperty visualLayersOffset = new IntegerPropertyBase() {
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
            return "visualLayersOffset";
        }
    };

    private final IntegerProperty visualMillisOffset = new IntegerPropertyBase() {
        @Override
        protected void invalidated() {
            super.invalidated();
            redraw();
            drawWaveform();
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
            drawWaveform();
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

    private final ObjectProperty<GameObject> selectedObject = new ObjectPropertyBase<>() {
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

    private final ObjectProperty<LevelAudio> audioSamples = new ObjectPropertyBase<>() {
        @Override
        protected void invalidated() {
            super.invalidated();
            drawWaveform();
        }

        @Override
        public Object getBean() {
            return null;
        }

        @Override
        public String getName() {
            return "levelAudio";
        }
    };

    private final IntegerProperty hoveredLayerNumber = new IntegerPropertyBase() {
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
            return "hoveredLayerNumber";
        }
    };

    public ObjectsTimeline() {
        this.canvas = new Canvas();
        this.canvas.widthProperty().bind(widthProperty());
        this.canvas.heightProperty().bind(heightProperty());

        this.waveformCanvas = new Canvas();
        this.waveformCanvas.widthProperty().bind(widthProperty());
        this.waveformCanvas.heightProperty().bind(heightProperty());

        widthProperty().addListener(obs -> {
            redraw();
            drawWaveform();
        });
        heightProperty().addListener(obs -> {
            redraw();
            drawWaveform();
        });

        getChildren().addAll(waveformCanvas, canvas);

        setMinHeight(TIMELINE_LAYER_HEIGHT * VISIBLE_LAYERS_COUNT + TIMELINE_TIME_AXIS_HEIGHT);

        visualMillisOffset.set(0);
        zoom.set(1.0);

        setOnMousePressed(event -> {
            requestFocus();

            if (event.getY() <= TIMELINE_TIME_AXIS_HEIGHT) {
                changingCursorPos = true;
                setCursorPosition(pxToMs(event.getX()) + visualMillisOffset.get());
            } else {
                int layer = getLayerAtY(event.getY());
                int time = pxToMs(event.getX()) + visualMillisOffset.get();
                boolean prepareDrag = false;
                boolean prepareScale = false;
                for (GameObject obj : LevelManager.getInstance()
                        .getObjects()
                        .stream()
                        .filter(obj -> obj.getTimelineLayer() == layer)
                        .sorted(Comparator.comparing(GameObject::getStartTime).reversed())
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
                int newCursorPos = pxToMs(event.getX()) + visualMillisOffset.get();

                if (!event.isAltDown()) {
                    double cursorPx = msToPx(newCursorPos);
                    for (GameObject obj : LevelManager.getInstance().getObjects()) {
                        double objStartPx = msToPx(obj.getStartTime());
                        double objEndPx = msToPx(obj.getEndTime());
                        if (Math.abs(cursorPx - objStartPx) <= TIMELINE_CURSOR_SNAP_TOLERANCE) {
                            newCursorPos = obj.getStartTime();
                            break;
                        } else if (Math.abs(cursorPx - objEndPx) <= TIMELINE_CURSOR_SNAP_TOLERANCE) {
                            newCursorPos = obj.getEndTime();
                            break;
                        }
                    }
                }

                setCursorPosition(newCursorPos);
            } else if (objectDragStartX != null) {
                if (selectedObjectStartTimeBeforeDrag != null) {
                    int newStartTime = Math.max(0, selectedObjectStartTimeBeforeDrag + pxToMs(event.getX() - objectDragStartX));

                    if (!event.isAltDown()) {
                        double selectedStartTimePx = msToPx(newStartTime);
                        double selectedEndTimePx = msToPx(newStartTime + getSelectedObject().getDuration());
                        if (Math.abs(msToPx(getCursorPosition()) - selectedStartTimePx) <= TIMELINE_CURSOR_SNAP_TOLERANCE) {
                            newStartTime = getCursorPosition();
                        } else if (Math.abs(msToPx(getCursorPosition()) - selectedEndTimePx) <= TIMELINE_CURSOR_SNAP_TOLERANCE) {
                            newStartTime = getCursorPosition() - getSelectedObject().getDuration();
                        } else {
                            for (GameObject obj : LevelManager.getInstance().getObjects()) {
                                if (obj.equals(getSelectedObject())) {
                                    continue;
                                }

                                double objStartPx = msToPx(obj.getStartTime());
                                double objEndPx = msToPx(obj.getEndTime());
                                if (Math.abs(selectedStartTimePx - objStartPx) <= TIMELINE_CURSOR_SNAP_TOLERANCE) {
                                    newStartTime = obj.getStartTime();
                                    break;
                                } else if (Math.abs(selectedStartTimePx - objEndPx) <= TIMELINE_CURSOR_SNAP_TOLERANCE) {
                                    newStartTime = obj.getEndTime();
                                    break;
                                } else if (Math.abs(selectedEndTimePx - objStartPx) <= TIMELINE_CURSOR_SNAP_TOLERANCE) {
                                    newStartTime = obj.getStartTime() - getSelectedObject().getDuration();
                                    break;
                                } else if (Math.abs(selectedEndTimePx - objEndPx) <= TIMELINE_CURSOR_SNAP_TOLERANCE) {
                                    newStartTime = obj.getEndTime() - getSelectedObject().getDuration();
                                    break;
                                }
                            }
                        }
                    }

                    selectedObject.get().setStartTime(newStartTime);

                    int layer = getLayerAtY(event.getY());
                    if (selectedObject.get().getTimelineLayer() != layer) {
                        selectedObject.get().setTimelineLayer(layer);
                    }
                } else if (selectedObjectDurationBeforeDrag != null) {
                    int newDuration = Math.max(0, selectedObjectDurationBeforeDrag + pxToMs(event.getX() - objectDragStartX));

                    if (!event.isAltDown()) {
                        double selectedEndTimePx = msToPx(getSelectedObject().getStartTime() + newDuration);
                        if (Math.abs(msToPx(getCursorPosition()) - selectedEndTimePx) <= TIMELINE_CURSOR_SNAP_TOLERANCE) {
                            newDuration = getCursorPosition() - getSelectedObject().getStartTime();
                        } else {
                            for (GameObject obj : LevelManager.getInstance().getObjects()) {
                                if (obj.equals(getSelectedObject())) {
                                    continue;
                                }

                                double objStartPx = msToPx(obj.getStartTime());
                                double objEndPx = msToPx(obj.getEndTime());
                                if (Math.abs(selectedEndTimePx - objStartPx) <= TIMELINE_CURSOR_SNAP_TOLERANCE) {
                                    newDuration = obj.getStartTime() - getSelectedObject().getStartTime();
                                    break;
                                } else if (Math.abs(selectedEndTimePx - objEndPx) <= TIMELINE_CURSOR_SNAP_TOLERANCE) {
                                    newDuration = obj.getEndTime() - getSelectedObject().getStartTime();
                                    break;
                                }
                            }
                        }
                    }

                    selectedObject.get().setDuration(newDuration);
                }
            }
            updateHoveredLayer(event.getY());
        });
        setOnMouseReleased(event -> {
            changingCursorPos = false;
            objectDragStartX = null;
            selectedObjectStartTimeBeforeDrag = null;
            setCursor(Cursor.DEFAULT);
        });
        setOnMouseMoved(event -> updateHoveredLayer(event.getY()));
        setOnMouseExited(event -> hoveredLayerNumber.set(-1));
        setOnScroll(event -> {
            if (event.isControlDown()) {
                zoom.set(Math.max(0.1, Math.min(10, zoom.getValue() - event.getDeltaY() / event.getMultiplierY() * 0.1)));
            } else {
                int layersDelta = (int) (event.getDeltaY() / event.getMultiplierY());
                visualLayersOffset.set(Math.max(0, visualLayersOffset.get() - layersDelta));
                visualMillisOffset.set(Math.max(0, visualMillisOffset.get() - pxToMs(event.getDeltaX())));
                updateHoveredLayer(event.getY());
            }
        });
        setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.DELETE && getSelectedObject() != null) {
                if (event.isShiftDown()) {
                    String name = getSelectedObject().getName();
                    List<GameObject> toRemove = new LinkedList<>();
                    for (GameObject obj : LevelManager.getInstance().getObjects()) {
                        if (obj.getName().equals(name) && !obj.equals(getSelectedObject())) {
                            toRemove.add(obj);
                        }
                    }
                    for (GameObject obj : toRemove) {
                        LevelManager.getInstance().removeObject(obj);
                    }
                } else {
                    LevelManager.getInstance().removeObject(getSelectedObject());
                    setSelectedObject(null);
                }
            } else if (event.getCode() == KeyCode.D && event.isControlDown() && getSelectedObject() != null) {
                if (event.isShiftDown()) {
                    CreateArrayDialog dialog = new CreateArrayDialog();
                    dialog.initOwner(getScene().getWindow());
                    Optional<CreateArrayDialog.CreateArrayIntent> opt = dialog.showAndWait();
                    opt.ifPresent(intent -> {
                        for (int i = 0; i < intent.arraysCount(); i++) {
                            for (int j = (i == 0 ? 1 : 0); j < intent.count(); j++) {
                                int timelineLayer = getSelectedObject().getTimelineLayer() + i;
                                GameObject copy = new GameObject(
                                        getSelectedObject().getName(),
                                        getSelectedObject().getStartTime() + j * intent.interval(),
                                        getSelectedObject().getDuration(),
                                        timelineLayer
                                );
                                for (ObjectKeyFrame kf : getSelectedObject().getKeyFrames()) {
                                    Object endValue = kf.getEndValue();
                                    // TODO: maybe there is a better way?
                                    if (kf.getType() == KeyFrameType.DOUBLE) {
                                        double deltaX = intent.deltas().get(kf.getTag()).getKey();
                                        double deltaY = intent.deltas().get(kf.getTag()).getValue();
                                        endValue = (double) endValue + (i * deltaY + j * deltaX);
                                    }
                                    copy.addKeyFrame(
                                            endValue,
                                            kf.getTime(),
                                            kf.getInterpolatorType(),
                                            kf.getTag(),
                                            kf.getRandomizer()
                                    );
                                }
                                copy.setShape(getSelectedObject().getShape());
                                LevelManager.getInstance().addObject(copy);
                            }
                        }
                    });
                } else {
                    GameObject copy = new GameObject(
                            getSelectedObject().getName(),
                            getSelectedObject().getStartTime(),
                            getSelectedObject().getDuration(),
                            getSelectedObject().getTimelineLayer() + 1
                    );
                    for (ObjectKeyFrame kf : getSelectedObject().getKeyFrames()) {
                        copy.addKeyFrame(
                                kf.getEndValue(),
                                kf.getTime(),
                                kf.getInterpolatorType(),
                                kf.getTag(),
                                kf.getRandomizer()
                        );
                    }
                    copy.setShape(getSelectedObject().getShape());
                    LevelManager.getInstance().addObject(copy);
                    setSelectedObject(copy);
                }
            } else if (event.getCode() == KeyCode.Q && event.isControlDown() && getSelectedObject() != null) {
                for (GameObject obj : LevelManager.getInstance().getObjects()) {
                    if (obj.getTimelineLayer() == getSelectedObject().getTimelineLayer()
                            && obj.isVisible(getCursorPosition())
                            && obj.getStartTime() != getCursorPosition()) {
                        obj.setDuration(getCursorPosition() - obj.getStartTime());
                    }
                }
            } else if (event.getCode() == KeyCode.H) {
                hideObjects = true;
                redraw();
            }
        });
        setOnKeyReleased(event -> {
            if (event.getCode() == KeyCode.H) {
                hideObjects = false;
                redraw();
            }
        });

        InvalidationListener redrawOnInvalidate = obs1 -> redraw();

        LevelManager.getInstance().objectsProperty().addListener(redrawOnInvalidate);

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

    private void updateHoveredLayer(double y) {
        if (y > TIMELINE_TIME_AXIS_HEIGHT) {
            hoveredLayerNumber.set(getLayerAtY(y));
        } else {
            hoveredLayerNumber.set(-1);
        }
    }

    private int getLayerAtY(double y) {
        return (int) ((y - TIMELINE_TIME_AXIS_HEIGHT) / TIMELINE_LAYER_HEIGHT) + visualLayersOffset.get();
    }

    private double getYOfLayer(int layer) {
        return (layer - visualLayersOffset.get()) * TIMELINE_LAYER_HEIGHT + TIMELINE_TIME_AXIS_HEIGHT;
    }

    public void redraw() {
        if (getWidth() == 0 || getHeight() == 0) {
            return;
        }

        double millisPerWidth = TIMELINE_MILLIS_PER_WIDTH * zoom.get();

        GraphicsContext g = canvas.getGraphicsContext2D();

        g.setFill(Theme.BACKGROUND);
        g.clearRect(0, 0, getWidth(), getHeight());

        g.setFill(Theme.BACKGROUND.brighter().deriveColor(0, 1, 1, 0.5));
        for (int i = visualLayersOffset.get() % 2; i < VISIBLE_LAYERS_COUNT; i += 2) {
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
        for (int i = 0; i < VISIBLE_LAYERS_COUNT; i++) {
            int curLayer = visualLayersOffset.get() + i;
            int layerIndex = i;
            LevelManager.getInstance()
                    .getObjects()
                    .stream()
                    .filter(obj -> obj.getTimelineLayer() == curLayer)
                    .sorted(Comparator.comparing(GameObject::getStartTime))
                    .forEach(obj -> {
                        int start = obj.getStartTime();
                        int end = obj.getEndTime();
                        if (end >= visualMillisOffset.get() && start <= visualMillisOffset.get() + millisPerWidth) {
                            Color fillColor;
                            Color strokeColor;
                            if (obj.equals(getSelectedObject())) {
                                fillColor = Theme.ACCENT;
                                strokeColor = Theme.ACCENT.darker();
                                g.setLineWidth(2.0);
                            } else {
                                if (hideObjects) {
                                    fillColor = Color.TRANSPARENT;
                                    strokeColor = Theme.PRIMARY.darker().deriveColor(0, 1, 1, 0.1);
                                } else {
                                    fillColor = Theme.PRIMARY;
                                    strokeColor = Theme.PRIMARY.darker();
                                }
                                g.setLineWidth(1.0);
                            }
                            g.setFill(fillColor);
                            g.setStroke(strokeColor);
                            g.fillRoundRect(
                                    msToPx(start - visualMillisOffset.get()),
                                    TIMELINE_TIME_AXIS_HEIGHT + layerIndex * TIMELINE_LAYER_HEIGHT + 1,
                                    msToPx(end - start),
                                    TIMELINE_LAYER_HEIGHT - 2,
                                    5,
                                    5
                            );
                            g.strokeRoundRect(
                                    msToPx(start - visualMillisOffset.get()),
                                    TIMELINE_TIME_AXIS_HEIGHT + layerIndex * TIMELINE_LAYER_HEIGHT + 1,
                                    msToPx(end - start),
                                    TIMELINE_LAYER_HEIGHT - 2,
                                    5,
                                    5
                            );

                            if (!hideObjects || obj.equals(getSelectedObject())) {
                                g.setFill(Theme.BACKGROUND);
                                g.fillText(
                                        obj.getName(),
                                        msToPx(start - visualMillisOffset.get()),
                                        TIMELINE_TIME_AXIS_HEIGHT + layerIndex * TIMELINE_LAYER_HEIGHT + TIMELINE_LAYER_HEIGHT * 0.5,
                                        msToPx(end - start)
                                );
                            }
                        }
                    });
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

        if (hoveredLayerNumber.get() >= 0) {
            g.setFill(Theme.ON_BACKGROUND);
            g.fillText(String.valueOf(hoveredLayerNumber.get()), 10, getYOfLayer(hoveredLayerNumber.get()) + TIMELINE_LAYER_HEIGHT * 0.5);
        }
    }

    private void drawWaveform() {
        if (getAudioSamples() == null) {
            return;
        }

        LevelAudio samples = getAudioSamples();

        GraphicsContext g = waveformCanvas.getGraphicsContext2D();
        g.clearRect(0, 0, getWidth(), getHeight());

        double frameDuration = 1000.0 / samples.getRate();
        double framePx = msToPx(frameDuration);
        int startIndex = (int) (visualMillisOffset.get() / frameDuration);
        g.setStroke(Theme.BACKGROUND.brighter().brighter());
        g.beginPath();
        for (int i = startIndex; i < samples.getLength() && framePx * (i - startIndex) < getWidth(); i += (int) Math.max(5, 10 * zoom.get())) {
            int sample = samples.getSample(i, 0);
            double y = getHeight() - (double) (sample - samples.getMin()) / (samples.getMax() - samples.getMin()) * getHeight();
            double x = framePx * (i - startIndex);
            if (i == startIndex) {
                g.appendSVGPath("M" + x + "," + y);
            } else {
                g.appendSVGPath("L" + x + "," + y);
            }
        }
        g.stroke();
        g.closePath();
    }

    public Duration getTotalDuration() {
        GameObject lastObj = LevelManager.getInstance().getObjects().stream()
                .max(Comparator.comparingInt(GameObject::getEndTime))
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

    public GameObject getSelectedObject() {
        return selectedObject.get();
    }

    public ObjectProperty<GameObject> selectedObjectProperty() {
        return selectedObject;
    }

    public void setSelectedObject(GameObject selectedObject) {
        this.selectedObject.set(selectedObject);
    }

    public LevelAudio getAudioSamples() {
        return audioSamples.get();
    }

    public ObjectProperty<LevelAudio> audioSamplesProperty() {
        return audioSamples;
    }

    public void setAudioSamples(LevelAudio levelAudio) {
        this.audioSamples.set(levelAudio);
    }

    private int pxToMs(double px) {
        return (int) (px / pxPerMs());
    }

    private double msToPx(double ms) {
        return pxPerMs() * ms;
    }

    private double pxPerMs() {
        double millisPerWidth = TIMELINE_MILLIS_PER_WIDTH * zoom.get();
        return getWidth() / millisPerWidth;
    }
}
