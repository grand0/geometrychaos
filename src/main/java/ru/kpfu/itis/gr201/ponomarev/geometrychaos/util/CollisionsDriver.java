package ru.kpfu.itis.gr201.ponomarev.geometrychaos.util;

import javafx.scene.shape.Shape;
import ru.kpfu.itis.gr201.ponomarev.geometrychaos.editor.game.Player;
import ru.kpfu.itis.gr201.ponomarev.geometrychaos.editor.ui.shapemaker.PlayerShapeMaker;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

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
