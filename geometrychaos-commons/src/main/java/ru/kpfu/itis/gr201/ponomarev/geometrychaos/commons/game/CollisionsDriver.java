package ru.kpfu.itis.gr201.ponomarev.geometrychaos.commons.game;

import javafx.scene.shape.Shape;
import ru.kpfu.itis.gr201.ponomarev.geometrychaos.commons.ui.shapemaker.PlayerShapeMaker;
import ru.kpfu.itis.gr201.ponomarev.geometrychaos.commons.util.ObjectCollidability;

import java.util.Map;

public class CollisionsDriver {

    private static final double EPS = 1e-10;

    public static boolean checkPlayerObjectCollisions(Player player, Map<GameObject, Shape> objects, double scalingFactor) {
        Shape hitBox = PlayerShapeMaker.makeHitBox(player, scalingFactor);
        for (Map.Entry<GameObject, Shape> objectShape : objects.entrySet()) {
            if (
                    isObjectCollidable(objectShape.getKey())
                            && hitBox.getBoundsInParent().intersects(objectShape.getValue().getBoundsInParent()) // cheap check
            ) {
                Shape intersection = Shape.intersect(hitBox, objectShape.getValue());
                if (intersection.getBoundsInParent().getWidth() > EPS) {
                    return true;
                }
            }
        }
        return false;
    }

    public static boolean isObjectCollidable(GameObject obj) {
        return obj.getObjectCollidability() == ObjectCollidability.ALWAYS
                || (obj.getObjectCollidability() == ObjectCollidability.OPACITY_BASED && obj.getHighlight() >= 0.0);
    }
}
