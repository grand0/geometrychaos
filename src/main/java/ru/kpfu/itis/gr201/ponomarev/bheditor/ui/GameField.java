package ru.kpfu.itis.gr201.ponomarev.bheditor.ui;

import javafx.beans.InvalidationListener;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.IntegerPropertyBase;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ObjectPropertyBase;
import javafx.geometry.Point2D;
import javafx.scene.layout.Background;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.*;
import javafx.scene.transform.Rotate;
import javafx.scene.transform.Scale;
import ru.kpfu.itis.gr201.ponomarev.bheditor.game.HittingObject;
import ru.kpfu.itis.gr201.ponomarev.bheditor.game.Shape;
import ru.kpfu.itis.gr201.ponomarev.bheditor.game.GameObjectsManager;
import ru.kpfu.itis.gr201.ponomarev.bheditor.util.Theme;

import java.util.Objects;

public class GameField extends Pane {

    public static final double FIELD_WIDTH = 1280;
    public static final double FIELD_HEIGHT = 720;
    public static final double FIELD_ASPECT_RATIO = FIELD_WIDTH / FIELD_HEIGHT;

    public static final double PIVOT_CROSS_SIZE = 7;
    public static final double SHAPE_CENTER_POINT_SIZE = 5;

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
            if (shape != null) {
                Rectangle fieldRect = new Rectangle(0, 0, getWidth(), getHeight());
                javafx.scene.shape.Shape visibleOnField = javafx.scene.shape.Shape.intersect(fieldRect, shape);
                visibleOnField.setFill(null);
                visibleOnField.setStroke(Theme.SELECTED_OBJECT_OUTLINE);
                visibleOnField.setStrokeWidth(3.0);
                visibleOnField.setStrokeType(StrokeType.INSIDE);
                visibleOnField.setStrokeLineCap(StrokeLineCap.BUTT);
                visibleOnField.getStrokeDashArray().addAll(5.0, 5.0);
                visibleOnField.setViewOrder(-Double.MAX_VALUE);
                javafx.scene.shape.Shape pivotCross = makePivotCrossWithLineToObjCenterShape(shape, obj);
                getChildren().addAll(visibleOnField, pivotCross);
            }
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
                        .filter(Objects::nonNull)
                        .toList()
        );
    }
    
    private javafx.scene.shape.Shape makeShape(HittingObject obj) {
        if (obj.getScaleX() == 0 || obj.getScaleY() == 0) {
            return null;
        }

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
        if (obj.getHighlight() > 0.0) { // make brighter
            fillColor = fillColor.interpolate(Color.WHITE, obj.getHighlight());
        } else if (obj.getHighlight() < 0.0) { // make transparent
            fillColor = fillColor.deriveColor(0, 1, 1, 1 + obj.getHighlight());
        }
        double scaledPivotX = obj.getPivotX() * scalingFactor;
        double scaledPivotY = obj.getPivotY() * scalingFactor;
        shape.setFill(fillColor);
        shape.getTransforms().addAll(
                new Rotate(obj.getRotation(), shapeCenterOnScreenX + scaledPivotX, shapeCenterOnScreenY + scaledPivotY),
                new Scale(obj.getScaleX(), obj.getScaleY(), shapeCenterOnScreenX + scaledPivotX, shapeCenterOnScreenY + scaledPivotY)
        );
        return shape;
    }

    private javafx.scene.shape.Shape makePivotCrossWithLineToObjCenterShape(javafx.scene.shape.Shape shape, HittingObject obj) {
        double scalingFactor = getWidth() / FIELD_WIDTH;
        double objCenterOnScreenBeforeTransformX = (obj.getPositionX() * scalingFactor) + getWidth() / 2;
        double objCenterOnScreenBeforeTransformY = (obj.getPositionY() * scalingFactor) + getHeight() / 2;
        double pivotX = objCenterOnScreenBeforeTransformX + obj.getPivotX() * scalingFactor;
        double pivotY = objCenterOnScreenBeforeTransformY + obj.getPivotY() * scalingFactor;
        Point2D actualShapeCenterOnScreen = shape.getLocalToParentTransform().transform(objCenterOnScreenBeforeTransformX, objCenterOnScreenBeforeTransformY);
        Line vCross = new Line(
                pivotX,
                pivotY - PIVOT_CROSS_SIZE / 2,
                pivotX,
                pivotY + PIVOT_CROSS_SIZE / 2
        );
        Line hCross = new Line(
                pivotX - PIVOT_CROSS_SIZE / 2,
                pivotY,
                pivotX + PIVOT_CROSS_SIZE / 2,
                pivotY
        );
        Line pivotToCenter = new Line(
                pivotX,
                pivotY,
                actualShapeCenterOnScreen.getX(),
                actualShapeCenterOnScreen.getY()
        );
        pivotToCenter.getStrokeDashArray().addAll(10.0, 5.0);
        pivotToCenter.setStrokeLineCap(StrokeLineCap.BUTT);
        Circle shapeCenterCircle = new Circle(
                actualShapeCenterOnScreen.getX(),
                actualShapeCenterOnScreen.getY(),
                SHAPE_CENTER_POINT_SIZE / 2
        );
        javafx.scene.shape.Shape cross = javafx.scene.shape.Shape.union(vCross, hCross);
        javafx.scene.shape.Shape crossWithLine = javafx.scene.shape.Shape.union(cross, pivotToCenter);
        javafx.scene.shape.Shape crossWithLineAndObjCenter = javafx.scene.shape.Shape.union(crossWithLine, shapeCenterCircle);
        crossWithLineAndObjCenter.setFill(Theme.SELECTED_OBJECT_OUTLINE);
        crossWithLineAndObjCenter.setStroke(Theme.SELECTED_OBJECT_OUTLINE);
        crossWithLineAndObjCenter.setViewOrder(-Double.MAX_VALUE);
        return crossWithLineAndObjCenter;
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
