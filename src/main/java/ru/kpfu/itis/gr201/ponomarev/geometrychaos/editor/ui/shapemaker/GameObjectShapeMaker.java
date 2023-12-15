package ru.kpfu.itis.gr201.ponomarev.geometrychaos.editor.ui.shapemaker;

import javafx.geometry.Point2D;
import javafx.scene.paint.Color;
import javafx.scene.shape.*;
import javafx.scene.transform.Rotate;
import javafx.scene.transform.Scale;
import ru.kpfu.itis.gr201.ponomarev.geometrychaos.editor.game.GameObject;
import ru.kpfu.itis.gr201.ponomarev.geometrychaos.util.Theme;

public class GameObjectShapeMaker {

    public static final double SIN_30 = Math.sin(Math.PI / 6.0);
    public static final double COS_30 = Math.cos(Math.PI / 6.0);

    public static final double PIVOT_CROSS_SIZE = 7;
    public static final double SHAPE_CENTER_POINT_SIZE = 5;
    
    public static Shape make(GameObject obj, Point2D centerOfScreen, double scalingFactor) {
        if (obj.getScaleX() == 0 || obj.getScaleY() == 0) {
            return null;
        }

        double shapeSize = ru.kpfu.itis.gr201.ponomarev.geometrychaos.editor.game.Shape.DEFAULT_SHAPE_SIZE;

        Shape shape = null;
        double shapeCenterOnScreenX = (obj.getPositionX() * scalingFactor) + centerOfScreen.getX();
        double shapeCenterOnScreenY = (obj.getPositionY() * scalingFactor) + centerOfScreen.getY();
        switch (obj.getShape()) {
            case SQUARE -> {
                shape = new Rectangle(
                        ((obj.getPositionX() - shapeSize / 2) * scalingFactor) + centerOfScreen.getX(),
                        ((obj.getPositionY() - shapeSize / 2) * scalingFactor) + centerOfScreen.getY(),
                        shapeSize * scalingFactor,
                        shapeSize * scalingFactor
                );
            }
            case CIRCLE -> {
                shape = new Ellipse(
                        (obj.getPositionX() * scalingFactor) + centerOfScreen.getX(),
                        (obj.getPositionY() * scalingFactor) + centerOfScreen.getY(),
                        (shapeSize / 2) * scalingFactor,
                        (shapeSize / 2) * scalingFactor
                );
            }
            case TRIANGLE -> {
                double height = Math.sqrt(0.75 * shapeSize * shapeSize);
                double[] points = new double[] {
                        (obj.getPositionX()                ) * scalingFactor + centerOfScreen.getX(), (obj.getPositionY() - height * (2.0 / 3.0)) * scalingFactor + centerOfScreen.getY(),
                        (obj.getPositionX() + shapeSize / 2) * scalingFactor + centerOfScreen.getX(), (obj.getPositionY() + height / 3.0        ) * scalingFactor + centerOfScreen.getY(),
                        (obj.getPositionX() - shapeSize / 2) * scalingFactor + centerOfScreen.getX(), (obj.getPositionY() + height / 3.0        ) * scalingFactor + centerOfScreen.getY(),
                };
                shape = new Polygon(points);
            }
            case HEXAGON -> {
                double sideLength = shapeSize / (2 * SIN_30 + 1);
                double halfHeight = sideLength * COS_30;
                double[] points = new double[] {
                        (obj.getPositionX() - sideLength / 2) * scalingFactor + centerOfScreen.getX(), (obj.getPositionY() - halfHeight) * scalingFactor + centerOfScreen.getY(),
                        (obj.getPositionX() + sideLength / 2) * scalingFactor + centerOfScreen.getX(), (obj.getPositionY() - halfHeight) * scalingFactor + centerOfScreen.getY(),
                        (obj.getPositionX() + shapeSize  / 2) * scalingFactor + centerOfScreen.getX(), (obj.getPositionY()             ) * scalingFactor + centerOfScreen.getY(),
                        (obj.getPositionX() + sideLength / 2) * scalingFactor + centerOfScreen.getX(), (obj.getPositionY() + halfHeight) * scalingFactor + centerOfScreen.getY(),
                        (obj.getPositionX() - sideLength / 2) * scalingFactor + centerOfScreen.getX(), (obj.getPositionY() + halfHeight) * scalingFactor + centerOfScreen.getY(),
                        (obj.getPositionX() - shapeSize  / 2) * scalingFactor + centerOfScreen.getX(), (obj.getPositionY()             ) * scalingFactor + centerOfScreen.getY(),
                };
                shape = new Polygon(points);
            }
        }
        if (shape == null) {
            throw new IllegalArgumentException("Unknown shape.");
        }
        Color shapeColor = Theme.PRIMARY;
        if (obj.getHighlight() > 0.0) { // make brighter
            shapeColor = shapeColor.interpolate(Color.WHITE, obj.getHighlight());
        } else if (obj.getHighlight() < 0.0) { // make transparent
            shape.setOpacity(1.0 + obj.getHighlight());
        }
        double scaledPivotX = obj.getPivotX() * scalingFactor;
        double scaledPivotY = obj.getPivotY() * scalingFactor;
        if (obj.getStroke() <= 0.0) {
            shape.setFill(shapeColor);
        } else {
            shape.setStrokeWidth(obj.getStroke());
            shape.setStrokeType(StrokeType.INSIDE);
            shape.setStroke(shapeColor);
            shape.setFill(null);
        }
        shape.setViewOrder(obj.getViewOrder());
        shape.getTransforms().addAll(
                new Rotate(obj.getRotation(), shapeCenterOnScreenX + scaledPivotX, shapeCenterOnScreenY + scaledPivotY),
                new Scale(obj.getScaleX(), obj.getScaleY(), shapeCenterOnScreenX + scaledPivotX, shapeCenterOnScreenY + scaledPivotY)
        );
        return shape;
    }

    public static Shape makeSelectedObjectHighlight(Shape shape, GameObject obj, Point2D centerOfScreen, double scalingFactor) {
        double objCenterOnScreenBeforeTransformX = (obj.getPositionX() * scalingFactor) + centerOfScreen.getX();
        double objCenterOnScreenBeforeTransformY = (obj.getPositionY() * scalingFactor) + centerOfScreen.getY();
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
}
