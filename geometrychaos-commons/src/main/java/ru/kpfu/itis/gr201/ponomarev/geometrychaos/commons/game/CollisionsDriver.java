package ru.kpfu.itis.gr201.ponomarev.geometrychaos.commons.game;

import javafx.scene.shape.Shape;
import ru.kpfu.itis.gr201.ponomarev.geometrychaos.commons.ui.shapemaker.PlayerShapeMaker;

import java.util.List;

public class CollisionsDriver {

    private static final double EPS = 1e-10;

    public static boolean checkPlayerObjectCollisions(Player player, List<Shape> objects, double scalingFactor) {
        Shape hitBox = PlayerShapeMaker.makeHitBox(player, scalingFactor);
        for (Shape objectShape : objects) {
            if (
                    Math.abs(1.0 - objectShape.getOpacity()) <= EPS
                        && hitBox.getBoundsInParent().intersects(objectShape.getBoundsInParent()) // cheap check
            ) {
                Shape intersection = Shape.intersect(hitBox, objectShape);
                if (intersection.getBoundsInParent().getWidth() > EPS) {
                    return true;
                }
            }
        }
        return false;
    }
}
