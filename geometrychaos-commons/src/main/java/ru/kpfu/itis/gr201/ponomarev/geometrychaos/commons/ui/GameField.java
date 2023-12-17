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
import ru.kpfu.itis.gr201.ponomarev.geometrychaos.commons.game.CollisionsDriver;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

public class GameField extends Pane {

    public static final double FIELD_WIDTH = 1280;
    public static final double FIELD_HEIGHT = 720;
    public static final double FIELD_ASPECT_RATIO = FIELD_WIDTH / FIELD_HEIGHT;

    private boolean listenToObjectsChanges = true;

    private Player thisPlayer;
    private final List<Player> players;
    private Runnable hitThisPlayerCallback;

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
        List<Shape> playersBackingsShapes = new LinkedList<>(
                players.stream()
                        .filter(player -> player.getHealthPoints() < Player.DEFAULT_HEALTH_POINTS)
                        .map((Player p) -> PlayerShapeMaker.makeBacking(p, scalingFactor))
                        .toList()
        );

        getChildren().addAll(objectsShapes);
        getChildren().addAll(playersShapes);
        getChildren().addAll(playersBackingsShapes);

        // TODO: move to some other place
        if (getTime() != 0 && thisPlayer != null) {
            boolean hit = CollisionsDriver.checkPlayerObjectCollisions(thisPlayer, objectsShapes, scalingFactor);
            if (hit) {
                hitThisPlayerCallback.run();
            }
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

    public Runnable getHitThisPlayerCallback() {
        return hitThisPlayerCallback;
    }

    public void setHitThisPlayerCallback(Runnable hitThisPlayerCallback) {
        this.hitThisPlayerCallback = hitThisPlayerCallback;
    }

    public Player getThisPlayer() {
        return thisPlayer;
    }

    public void setThisPlayer(Player thisPlayer) {
        this.thisPlayer = thisPlayer;
    }
}
