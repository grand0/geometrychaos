package ru.kpfu.itis.gr201.ponomarev.geometrychaos.commons.ui.shapemaker;

import javafx.geometry.Point2D;
import javafx.scene.paint.Color;
import javafx.scene.shape.*;
import javafx.scene.transform.Rotate;
import javafx.scene.transform.Scale;
import ru.kpfu.itis.gr201.ponomarev.geometrychaos.commons.game.GameObject;
import ru.kpfu.itis.gr201.ponomarev.geometrychaos.commons.game.shape.GameShapeType;
import ru.kpfu.itis.gr201.ponomarev.geometrychaos.commons.ui.Theme;

public class GameObjectShapeMaker {

    public static final double PIVOT_CROSS_SIZE = 7;
    public static final double SHAPE_CENTER_POINT_SIZE = 5;
    
    public static Shape make(GameObject obj, Point2D centerOfScreen, double scalingFactor) {
        if (obj.getScaleX() == 0 || obj.getScaleY() == 0) {
            return null;
        }

        double shapeSize = GameShapeType.DEFAULT_SHAPE_SIZE;

        ShapeMaker shapeMaker = new ShapeMaker(
                obj.getPositionX(),
                obj.getPositionY(),
                shapeSize,
                scalingFactor,
                obj.getShape(),
                centerOfScreen
        );
        Shape shape = shapeMaker.make();

        double shapeCenterOnScreenX = (obj.getPositionX() * scalingFactor) + centerOfScreen.getX();
        double shapeCenterOnScreenY = (obj.getPositionY() * scalingFactor) + centerOfScreen.getY();

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
            shape.setStrokeWidth(obj.getStroke() * scalingFactor);
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
