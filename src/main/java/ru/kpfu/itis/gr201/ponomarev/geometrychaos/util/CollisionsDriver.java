package ru.kpfu.itis.gr201.ponomarev.geometrychaos.util;

import javafx.scene.shape.Shape;
import ru.kpfu.itis.gr201.ponomarev.geometrychaos.editor.game.Player;
import ru.kpfu.itis.gr201.ponomarev.geometrychaos.editor.ui.shapemaker.PlayerShapeMaker;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class CollisionsDriver {

    public static Set<Player> checkPlayerObjectCollisions(List<Player> players, List<Shape> objects, double scalingFactor) {
        Set<Player> hitPlayers = new HashSet<>();
        for (Player player : players) {
            Shape hitBox = PlayerShapeMaker.makeHitBox(player, scalingFactor);
            boolean hit = false;
            for (Shape objectShape : objects) {
                Shape intersection = Shape.intersect(hitBox, objectShape);
                if (intersection.getBoundsInParent().getWidth() > 0) {
                    hit = true;
                    break;
                }
            }
            if (hit) {
                hitPlayers.add(player);
            }
        }
        return hitPlayers;
    }
}
