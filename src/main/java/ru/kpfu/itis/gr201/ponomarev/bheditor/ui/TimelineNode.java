package ru.kpfu.itis.gr201.ponomarev.bheditor.ui;

import javafx.beans.InvalidationListener;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.IntegerPropertyBase;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ObjectPropertyBase;
import javafx.geometry.VPos;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.layout.Pane;
import javafx.util.Duration;
import ru.kpfu.itis.gr201.ponomarev.bheditor.game.HittingObject;
import ru.kpfu.itis.gr201.ponomarev.bheditor.util.GameObjectsManager;
import ru.kpfu.itis.gr201.ponomarev.bheditor.util.Theme;

import java.util.*;

public class TimelineNode extends Pane {

    public final static int LAYERS_COUNT = 10;

    private final static double TIMELINE_TIME_AXIS_HEIGHT = 30;
    private final static double TIMELINE_LAYER_HEIGHT = 20;
    private final static double TIMELINE_CURSOR_SIZE = 20;
    private final static int TIMELINE_MILLIS_PER_WIDTH = 60000;

    private final Canvas canvas;

    private double visualMillisOffset = 0.0;
    private double zoom = 1.0;
    private boolean changingCursorPos = false;

    private final ObjectProperty<HittingObject> selectedObject = new ObjectPropertyBase<>() {
        @Override
        protected void invalidated() {
            super.invalidated();
            redraw();
        }

        @Override
        public Object getBean() {
            return this;
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
            return this;
        }

        @Override
        public String getName() {
            return "cursorPosition";
        }
    };

    public TimelineNode() {
        this.canvas = new Canvas();
        this.canvas.widthProperty().bind(widthProperty());
        this.canvas.heightProperty().bind(heightProperty());

        widthProperty().addListener(obs -> redraw());
        heightProperty().addListener(obs -> redraw());

        getChildren().add(canvas);

        setMinHeight(TIMELINE_LAYER_HEIGHT * LAYERS_COUNT + TIMELINE_TIME_AXIS_HEIGHT);

        setOnMousePressed(event -> {
            if (event.getY() <= TIMELINE_TIME_AXIS_HEIGHT) {
                changingCursorPos = true;
                setCursorPosition(pxToMs(event.getX()));
            } else {
                int layer = (int) ((event.getY() - TIMELINE_TIME_AXIS_HEIGHT) / TIMELINE_LAYER_HEIGHT);
                int time = pxToMs(event.getX());
                for (HittingObject obj : GameObjectsManager.getInstance().getObjects()) {
                	if (obj.getTimelineLayer() == layer && (time >= obj.getStartTime() && time <= obj.getEndTime()) && !obj.equals(getSelectedObject())) {
                        setSelectedObject(obj);
                        break;
                    }
                }
            }
        });
        setOnMouseDragged(event -> {
            if (changingCursorPos) {
                setCursorPosition(pxToMs(event.getX()));
            }
        });
        setOnMouseReleased(event -> {
            changingCursorPos = false;
        });

        GameObjectsManager.getInstance().objectsProperty().addListener((InvalidationListener) obs -> redraw());
    }

    public void redraw() {
        double millisPerWidth = TIMELINE_MILLIS_PER_WIDTH * zoom;

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
        int step = 5000; // TODO: calculate this value
        for (int i = 0; i <= millisPerWidth; i += step) {
            g.fillText(String.format("%.1fs", i / 1000.0), msToPx(i), TIMELINE_TIME_AXIS_HEIGHT / 2);
            g.strokeLine(msToPx(i), TIMELINE_TIME_AXIS_HEIGHT, msToPx(i), getHeight());
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
                if (end >= visualMillisOffset && start <= visualMillisOffset + millisPerWidth) {
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
                            msToPx((int) (start - visualMillisOffset)),
                            TIMELINE_TIME_AXIS_HEIGHT + i * TIMELINE_LAYER_HEIGHT + 1,
                            msToPx(end - start),
                            TIMELINE_LAYER_HEIGHT - 2,
                            5,
                            5
                    );
                    g.strokeRoundRect(
                            msToPx((int) (start - visualMillisOffset)),
                            TIMELINE_TIME_AXIS_HEIGHT + i * TIMELINE_LAYER_HEIGHT + 1,
                            msToPx(end - start),
                            TIMELINE_LAYER_HEIGHT - 2,
                            5,
                            5
                    );

                    g.setFill(Theme.BACKGROUND);
                    g.fillText(
                            ho.getName(),
                            msToPx((int) (start - visualMillisOffset)),
                            TIMELINE_TIME_AXIS_HEIGHT + i * TIMELINE_LAYER_HEIGHT + TIMELINE_LAYER_HEIGHT * 0.5,
                            msToPx(end - start)
                    );
                }
            }
        }

        double cursorPositionOnScreen = msToPx(getCursorPosition());
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
        double millisPerWidth = TIMELINE_MILLIS_PER_WIDTH * zoom;
        return getWidth() / millisPerWidth;
    }
}
