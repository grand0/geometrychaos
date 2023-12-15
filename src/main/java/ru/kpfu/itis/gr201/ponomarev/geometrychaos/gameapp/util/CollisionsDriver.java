package ru.kpfu.itis.gr201.ponomarev.geometrychaos.gameapp.util;

import javafx.scene.shape.Shape;
import ru.kpfu.itis.gr201.ponomarev.geometrychaos.common.game.Player;
import ru.kpfu.itis.gr201.ponomarev.geometrychaos.common.ui.shapemaker.PlayerShapeMaker;

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
                    System.out.println("HIT " + objectShape);
                    return true;
                }
            }
        }
        return false;
    }
}
