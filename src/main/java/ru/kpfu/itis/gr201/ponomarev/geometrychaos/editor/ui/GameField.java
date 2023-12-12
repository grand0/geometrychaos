package ru.kpfu.itis.gr201.ponomarev.geometrychaos.editor.ui;

import javafx.beans.InvalidationListener;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.IntegerPropertyBase;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ObjectPropertyBase;
import javafx.geometry.Point2D;
import javafx.scene.layout.Background;
import javafx.scene.layout.Pane;
import javafx.scene.shape.*;
import ru.kpfu.itis.gr201.ponomarev.geometrychaos.editor.game.GameObject;
import ru.kpfu.itis.gr201.ponomarev.geometrychaos.editor.game.Player;
import ru.kpfu.itis.gr201.ponomarev.geometrychaos.editor.game.LevelManager;
import ru.kpfu.itis.gr201.ponomarev.geometrychaos.editor.ui.shapemaker.GameObjectShapeMaker;
import ru.kpfu.itis.gr201.ponomarev.geometrychaos.editor.ui.shapemaker.PlayerShapeMaker;
import ru.kpfu.itis.gr201.ponomarev.geometrychaos.util.CollisionsDriver;
import ru.kpfu.itis.gr201.ponomarev.geometrychaos.util.Theme;

import java.util.*;
import java.util.function.Consumer;

public class GameField extends Pane {

    public static final double FIELD_WIDTH = 1280;
    public static final double FIELD_HEIGHT = 720;
    public static final double FIELD_ASPECT_RATIO = FIELD_WIDTH / FIELD_HEIGHT;

    private boolean listenToObjectsChanges = true;

    private final List<Player> players;
    private Consumer<Player> hitPlayerCallback;

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

    public GameField(IntegerProperty timelineCursorPosition, ObjectProperty<GameObject> selectedObject) {
        players = new ArrayList<>();
        time.bind(timelineCursorPosition);
        if (selectedObject != null) {
            this.selectedObject.bind(selectedObject);
        }
        LevelManager.getInstance().objectsProperty().addListener((InvalidationListener) obs -> {
            if (listenToObjectsChanges) {
                redraw();
            }
        });
        widthProperty().addListener(obs -> redraw());
        heightProperty().addListener(obs -> redraw());

        setBackground(Background.fill(Theme.GAME_FIELD_BACKGROUND));
    }

    public void redraw() {
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
        List<Shape> transparentPlayersShapes = new LinkedList<>(
                players.stream()
                        .filter(player -> player.getHealthPoints() > 0 && player.getHealthPoints() < Player.DEFAULT_HEALTH_POINTS)
                        .map((Player p) -> PlayerShapeMaker.makeBacking(p, scalingFactor))
                        .toList()
        );

        getChildren().addAll(objectsShapes);
        getChildren().addAll(playersShapes);
        getChildren().addAll(transparentPlayersShapes);

        // TODO: move to some other place
        Set<Player> hitPlayers = CollisionsDriver.checkPlayerObjectCollisions(players, objectsShapes, scalingFactor);
        for (Player hitPlayer : hitPlayers) {
            hitPlayerCallback.accept(hitPlayer);
        }
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

    public Consumer<Player> getHitPlayerCallback() {
        return hitPlayerCallback;
    }

    public void setHitPlayerCallback(Consumer<Player> hitPlayerCallback) {
        this.hitPlayerCallback = hitPlayerCallback;
    }
}
