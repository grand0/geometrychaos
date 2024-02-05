package ru.kpfu.itis.gr201.ponomarev.geometrychaos.commons.ui.shapemaker;

import javafx.geometry.Point2D;
import javafx.scene.paint.Color;
import javafx.scene.shape.*;
import javafx.scene.transform.Rotate;
import javafx.scene.transform.Scale;
import ru.kpfu.itis.gr201.ponomarev.geometrychaos.commons.game.GameObject;
import ru.kpfu.itis.gr201.ponomarev.geometrychaos.commons.game.LevelManager;
import ru.kpfu.itis.gr201.ponomarev.geometrychaos.commons.game.shape.GameShapeType;
import ru.kpfu.itis.gr201.ponomarev.geometrychaos.commons.ui.Theme;
import ru.kpfu.itis.gr201.ponomarev.geometrychaos.commons.util.ObjectCollidability;

public class GameObjectShapeMaker {

    public static final double PIVOT_CROSS_SIZE = 7;
    public static final double SHAPE_CENTER_POINT_SIZE = 5;

    public static boolean shouldMake(GameObject obj) {
        return obj.getScaleX() != 0 && obj.getScaleY() != 0;
    }
    
    public static Shape make(GameObject obj, Point2D centerOfScreen, double scalingFactor) {
        double shapeSize = GameShapeType.DEFAULT_SHAPE_SIZE;

        GameObject parent = LevelManager.getInstance().findParent(obj);
        double posX = obj.getPositionX() + (parent == null ? 0 : parent.getPositionX());
        double posY = obj.getPositionY() + (parent == null ? 0 : parent.getPositionY());
        double highlight = obj.getHighlight() + (parent == null ? 0 : parent.getHighlight());
        double pivotX = obj.getPivotX() + (parent == null ? 0 : parent.getPivotX());
        double pivotY = obj.getPivotY() + (parent == null ? 0 : parent.getPivotY());
        double viewOrder = obj.getViewOrder() + (parent == null ? 0 : parent.getViewOrder());
        double rotation = obj.getRotation() + (parent == null ? 0 : parent.getRotation());
        double scaleX = obj.getScaleX() * (parent == null ? 1 : parent.getScaleX());
        double scaleY = obj.getScaleY() * (parent == null ? 1 : parent.getScaleY());
        double stroke = obj.getStroke();

        ShapeMaker shapeMaker = new ShapeMaker(
                posX,
                posY,
                shapeSize,
                scalingFactor,
                obj.getShape(),
                centerOfScreen
        );
        Shape shape = shapeMaker.make();

        double shapeCenterOnScreenX = (posX * scalingFactor) + centerOfScreen.getX();
        double shapeCenterOnScreenY = (posY * scalingFactor) + centerOfScreen.getY();

        Color shapeColor = Theme.PRIMARY;
        if (highlight > 0.0) { // make brighter
            shapeColor = shapeColor.interpolate(Color.WHITE, highlight);
        } else if (highlight < 0.0) { // make transparent
            shape.setOpacity(1.0 + highlight);
        }
        double scaledPivotX = pivotX * scalingFactor;
        double scaledPivotY = pivotY * scalingFactor;
        if (stroke <= 0.0) {
            shape.setFill(shapeColor);
        } else {
            shape.setStrokeWidth(stroke * scalingFactor);
            shape.setStrokeType(StrokeType.INSIDE);
            shape.setStroke(shapeColor);
            shape.setFill(null);
        }
        shape.setViewOrder(viewOrder);
        shape.getTransforms().addAll(
                new Rotate(rotation, shapeCenterOnScreenX + scaledPivotX, shapeCenterOnScreenY + scaledPivotY),
                new Scale(scaleX, scaleY, shapeCenterOnScreenX + scaledPivotX, shapeCenterOnScreenY + scaledPivotY)
        );
        return shape;
    }

    public static Shape makeSelectedObjectHighlight(Shape shape, GameObject obj, Point2D centerOfScreen, double scalingFactor) {
        GameObject parent = LevelManager.getInstance().findParent(obj);
        double posX = obj.getPositionX() + (parent == null ? 0 : parent.getPositionX());
        double posY = obj.getPositionY() + (parent == null ? 0 : parent.getPositionY());

        double objCenterOnScreenBeforeTransformX = (posX * scalingFactor) + centerOfScreen.getX();
        double objCenterOnScreenBeforeTransformY = (posY * scalingFactor) + centerOfScreen.getY();
        double pivotX = objCenterOnScreenBeforeTransformX + (obj.getPivotX() + (parent == null ? 0 : parent.getPivotX())) * scalingFactor;
        double pivotY = objCenterOnScreenBeforeTransformY + (obj.getPivotY() + (parent == null ? 0 : parent.getPivotY())) * scalingFactor;
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
