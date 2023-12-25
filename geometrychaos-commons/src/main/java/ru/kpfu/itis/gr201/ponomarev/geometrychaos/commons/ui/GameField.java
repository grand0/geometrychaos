package ru.kpfu.itis.gr201.ponomarev.geometrychaos.commons.ui;

import javafx.beans.InvalidationListener;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.IntegerPropertyBase;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ObjectPropertyBase;
import javafx.geometry.Point2D;
import javafx.scene.layout.Background;
import javafx.scene.layout.Pane;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.Shape;
import javafx.scene.shape.StrokeLineCap;
import javafx.scene.shape.StrokeType;
import ru.kpfu.itis.gr201.ponomarev.geometrychaos.commons.game.GameObject;
import ru.kpfu.itis.gr201.ponomarev.geometrychaos.commons.game.LevelManager;
import ru.kpfu.itis.gr201.ponomarev.geometrychaos.commons.game.Player;
import ru.kpfu.itis.gr201.ponomarev.geometrychaos.commons.ui.shapemaker.GameObjectShapeMaker;
import ru.kpfu.itis.gr201.ponomarev.geometrychaos.commons.ui.shapemaker.PlayerShapeMaker;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;

public class GameField extends Pane {

    public static final double FIELD_WIDTH = 1280;
    public static final double FIELD_HEIGHT = 720;
    public static final double FIELD_ASPECT_RATIO = FIELD_WIDTH / FIELD_HEIGHT;

    private boolean listenToObjectsChanges = true;
    private boolean disposed = false;

    private final List<Player> players;
    private Consumer<List<Shape>> newFrameCallback;

    private final IntegerProperty time = new IntegerPropertyBase() {
        @Override
        protected void invalidated() {
            super.invalidated();
            listenToObjectsChanges = false;
            redraw();
            listenToObjectsChanges = true;
        }

        @Override
        public Object getBean() {
            return null;
        }

        @Override
        public String getName() {
            return "time";
        }
    };
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
    private final Runnable removeAllBindingsAndListeners;

    public GameField(IntegerProperty timelineCursorPosition, ObjectProperty<GameObject> selectedObject) {
        players = new ArrayList<>();
        time.bind(timelineCursorPosition);
        if (selectedObject != null) {
            this.selectedObject.bind(selectedObject);
        }
        InvalidationListener invalidationConditionalListener = obs -> {
            if (listenToObjectsChanges) {
                redraw();
            }
        };
        InvalidationListener invalidationListener = obs -> redraw();
        LevelManager.getInstance().objectsProperty().addListener(invalidationConditionalListener);
        widthProperty().addListener(invalidationListener);
        heightProperty().addListener(invalidationListener);

        removeAllBindingsAndListeners = () -> {
            this.time.unbind();
            this.selectedObject.unbind();
            LevelManager.getInstance().objectsProperty().removeListener(invalidationConditionalListener);
            widthProperty().removeListener(invalidationListener);
            heightProperty().removeListener(invalidationListener);
        };

        setBackground(Background.fill(Theme.GAME_FIELD_BACKGROUND));
    }

    public void redraw() {
        if (disposed) return;

        getChildren().clear();

        Point2D centerOfScreen = new Point2D(getWidth() / 2.0, getHeight() / 2.0);
        double scalingFactor = (centerOfScreen.getX() * 2) / GameField.FIELD_WIDTH;

        if (selectedObject.get() != null && selectedObject.get().isVisible(getTime())) {
            GameObject obj = selectedObject.get();
            obj.setTime(getTime() - obj.getStartTime());
            Shape shape = GameObjectShapeMaker.make(obj, centerOfScreen, scalingFactor);
            if (shape != null) {
                Rectangle fieldRect = new Rectangle(0, 0, getWidth(), getHeight());
                Shape visibleOnField = Shape.intersect(fieldRect, shape);
                visibleOnField.setFill(null);
                visibleOnField.setStroke(Theme.SELECTED_OBJECT_OUTLINE);
                visibleOnField.setStrokeWidth(3.0);
                visibleOnField.setStrokeType(StrokeType.INSIDE);
                visibleOnField.setStrokeLineCap(StrokeLineCap.BUTT);
                visibleOnField.getStrokeDashArray().addAll(5.0, 5.0);
                visibleOnField.setViewOrder(-Double.MAX_VALUE);
                Shape selectedObjectHighlight = GameObjectShapeMaker.makeSelectedObjectHighlight(shape, obj, centerOfScreen, scalingFactor);
                getChildren().addAll(visibleOnField, selectedObjectHighlight);
            }
        }


        List<Shape> objectsShapes = new LinkedList<>(
                LevelManager.getInstance()
                        .getObjects()
                        .stream()
                        .filter(obj -> obj.isVisible(getTime()))
                        .map(obj -> {
                            obj.setTime(getTime() - obj.getStartTime());
                            return GameObjectShapeMaker.make(obj, centerOfScreen, scalingFactor);
                        })
                        .filter(Objects::nonNull)
                        .toList()
        );

        List<Shape> playersShapes = new LinkedList<>(
                players.stream()
                        .filter(player -> player.getHealthPoints() > 0)
                        .map((Player p) -> PlayerShapeMaker.make(p, scalingFactor))
                        .toList()
        );
        List<Shape> playersBackingsShapes = new LinkedList<>(
                players.stream()
                        .filter(player -> player.getHealthPoints() < Player.DEFAULT_HEALTH_POINTS)
                        .map((Player p) -> PlayerShapeMaker.makeBacking(p, scalingFactor))
                        .toList()
        );

        getChildren().addAll(objectsShapes);
        getChildren().addAll(playersShapes);
        getChildren().addAll(playersBackingsShapes);

        if (newFrameCallback != null && getTime() != 0) {
            newFrameCallback.accept(objectsShapes);
        }
    }

    public double getScalingFactor() {
        return getWidth() / GameField.FIELD_WIDTH;
    }

    public void dispose() {
        removeAllBindingsAndListeners.run();
        players.clear();
        newFrameCallback = null;
        disposed = true;
    }

    public int getTime() {
        return time.get();
    }

    public IntegerProperty timeProperty() {
        return time;
    }

    public void setTime(int time) {
        this.time.set(time);
    }

    public List<Player> getPlayers() {
        return players;
    }

    public Consumer<List<Shape>> getNewFrameCallback() {
        return newFrameCallback;
    }

    public void setNewFrameCallback(Consumer<List<Shape>> newFrameCallback) {
        this.newFrameCallback = newFrameCallback;
    }
}
