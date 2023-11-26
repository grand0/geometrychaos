package ru.kpfu.itis.gr201.ponomarev.bheditor.ui;

import javafx.beans.InvalidationListener;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.IntegerPropertyBase;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ObjectPropertyBase;
import javafx.scene.layout.Background;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.scene.shape.Ellipse;
import javafx.scene.shape.Polygon;
import javafx.scene.shape.Rectangle;
import javafx.scene.transform.Rotate;
import javafx.scene.transform.Scale;
import ru.kpfu.itis.gr201.ponomarev.bheditor.game.HittingObject;
import ru.kpfu.itis.gr201.ponomarev.bheditor.game.Shape;
import ru.kpfu.itis.gr201.ponomarev.bheditor.util.GameObjectsManager;
import ru.kpfu.itis.gr201.ponomarev.bheditor.util.Theme;

public class GameField extends Pane {

    public static final double FIELD_WIDTH = 1280;
    public static final double FIELD_HEIGHT = 720;
    public static final double FIELD_ASPECT_RATIO = FIELD_WIDTH / FIELD_HEIGHT;

    private boolean listenToObjectsChanges = true;

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

    public GameField(IntegerProperty timelineCursorPosition, ObjectProperty<HittingObject> selectedObject) {
        time.bind(timelineCursorPosition);
        this.selectedObject.bind(selectedObject);
        GameObjectsManager.getInstance().objectsProperty().addListener((InvalidationListener) obs -> {
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

        if (selectedObject.get() != null && selectedObject.get().isVisible(getTime())) {
            HittingObject obj = selectedObject.get();
            obj.setTime(getTime() - obj.getStartTime());
            javafx.scene.shape.Shape shape = makeShape(obj);
            shape.setFill(null);
            shape.setStroke(Theme.PRIMARY.darker());
            shape.setStrokeWidth(3.0);
            shape.getStrokeDashArray().addAll(10.0, 10.0);
            shape.setViewOrder(-Double.MAX_VALUE);
            getChildren().add(shape);
        }

        getChildren().addAll(
                GameObjectsManager.getInstance()
                        .getObjects()
                        .stream()
                        .filter(ho -> ho.isVisible(getTime()))
                        .map(ho -> {
                            ho.setTime(getTime() - ho.getStartTime());
                            return makeShape(ho);
                        })
                        .toList()
        );
    }
    
    private javafx.scene.shape.Shape makeShape(HittingObject obj) {
        double scalingFactor = getWidth() / FIELD_WIDTH;
        double shapeSize = Shape.DEFAULT_SHAPE_SIZE;

        javafx.scene.shape.Shape shape = null;
        double shapeCenterOnScreenX = (obj.getPositionX() * scalingFactor) + getWidth() / 2;
        double shapeCenterOnScreenY = (obj.getPositionY() * scalingFactor) + getHeight() / 2;
        switch (obj.getShape()) {
            case SQUARE -> {
                shape = new Rectangle(
                        ((obj.getPositionX() - shapeSize / 2) * scalingFactor) + getWidth() / 2,
                        ((obj.getPositionY() - shapeSize / 2) * scalingFactor) + getHeight() / 2,
                        shapeSize * scalingFactor,
                        shapeSize * scalingFactor
                );
            }
            case CIRCLE -> {
                shape = new Ellipse(
                        (obj.getPositionX() * scalingFactor) + getWidth() / 2,
                        (obj.getPositionY() * scalingFactor) + getHeight() / 2,
                        (shapeSize / 2) * scalingFactor,
                        (shapeSize / 2) * scalingFactor
                );
            }
            case TRIANGLE -> {
                double height = Math.sqrt(0.75 * shapeSize * shapeSize);
                double[] points = new double[] {
                        (obj.getPositionX()                ) * scalingFactor + getWidth() / 2, (obj.getPositionY() - height * (2.0 / 3.0)) * scalingFactor + getHeight() / 2,
                        (obj.getPositionX() + shapeSize / 2) * scalingFactor + getWidth() / 2, (obj.getPositionY() + height / 3.0        ) * scalingFactor + getHeight() / 2,
                        (obj.getPositionX() - shapeSize / 2) * scalingFactor + getWidth() / 2, (obj.getPositionY() + height / 3.0        ) * scalingFactor + getHeight() / 2,
                };
                shape = new Polygon(points);
            }
        }
        if (shape == null) {
            throw new IllegalArgumentException("Unknown shape.");
        }
        Color fillColor = Theme.PRIMARY;
        if (obj.isHelper()) {
            fillColor = fillColor.deriveColor(0, 1, 1, 0.5);
        }
        shape.setFill(fillColor);
        shape.getTransforms().addAll(
                new Rotate(obj.getRotation(), shapeCenterOnScreenX + obj.getPivotX(), shapeCenterOnScreenY + obj.getPivotY()),
                new Scale(obj.getScaleX(), obj.getScaleY(), shapeCenterOnScreenX + obj.getPivotX(), shapeCenterOnScreenY + obj.getPivotY())
        );
        return shape;
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
}
