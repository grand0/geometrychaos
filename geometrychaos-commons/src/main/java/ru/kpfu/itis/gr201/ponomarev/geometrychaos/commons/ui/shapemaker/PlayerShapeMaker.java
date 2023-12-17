package ru.kpfu.itis.gr201.ponomarev.geometrychaos.commons.ui.shapemaker;

import javafx.scene.paint.Color;
import javafx.scene.shape.*;
import javafx.scene.transform.Rotate;
import javafx.scene.transform.Scale;
import ru.kpfu.itis.gr201.ponomarev.geometrychaos.commons.game.Player;
import ru.kpfu.itis.gr201.ponomarev.geometrychaos.commons.ui.Theme;

import static ru.kpfu.itis.gr201.ponomarev.geometrychaos.commons.game.Shape.*;

public class PlayerShapeMaker {

    public static final int PLAYER_VIEW_ORDER = -1000;

    public static Shape make(Player player, double scalingFactor) {
        Shape shape = makeShape(player, scalingFactor);

        double arcLength = ((double) player.getHealthPoints() / Player.DEFAULT_HEALTH_POINTS) * 360.0;
        Arc arc = new Arc(
                player.getPositionX() * scalingFactor,
                player.getPositionY() * scalingFactor,
                Player.PLAYER_SIZE * scalingFactor,
                Player.PLAYER_SIZE * scalingFactor,
                40,
                arcLength
        );
        arc.setType(ArcType.ROUND);

        Shape playerShape = Shape.intersect(shape, arc);
        playerShape.setViewOrder(PLAYER_VIEW_ORDER);

        applyTransforms(playerShape, player, scalingFactor);
        applyColor(playerShape, player, player.isDamageCooldownActive() ? 0.6 : 1.0);

        return playerShape;
    }

    public static Shape makeBacking(Player player, double scalingFactor) {
        Shape shape = makeShape(player, scalingFactor);
        applyTransforms(shape, player, scalingFactor);
        applyColor(shape, player, player.getHealthPoints() != 0 && player.isDamageCooldownActive() ? 0.1 : 0.4);

        shape.setViewOrder(PLAYER_VIEW_ORDER + 1);
        return shape;
    }

    private static Shape makeShape(Player player, double scalingFactor) {
        ru.kpfu.itis.gr201.ponomarev.geometrychaos.commons.game.Shape shapeType = null;
        double rotateDegrees = 0;
        switch (player.getPlayerId() % 4) {
            case 0 -> shapeType = SQUARE;
            case 1 -> shapeType = CIRCLE;
            case 2 -> {
                shapeType = TRIANGLE;
                rotateDegrees = 90.0;
            }
            case 3 -> shapeType = HEXAGON;
        }

        ShapeMaker shapeMaker = new ShapeMaker(
                player.getPositionX(),
                player.getPositionY(),
                Player.PLAYER_SIZE,
                scalingFactor,
                shapeType
        );
        Shape shape = shapeMaker.make();
        if (rotateDegrees != 0) {
            shape.getTransforms().add(
                    new Rotate(
                            rotateDegrees,
                            player.getPositionX() * scalingFactor,
                            player.getPositionY() * scalingFactor
                    )
            );
        }
        return shape;
    }

    private static void applyTransforms(Shape shape, Player player, double scalingFactor) {
        shape.getTransforms().add(
                new Rotate(
                        playerRotation(player),
                        player.getPositionX() * scalingFactor,
                        player.getPositionY() * scalingFactor
                )
        );
        if (player.isDashing()) {
            double frac = (double) (System.nanoTime() - player.getLastDashTime()) / Player.DASH_DURATION_NS;
            shape.getTransforms().add(
                    new Scale(
                            2.0 - frac,
                            0.5 + frac * 0.5,
                            player.getPositionX() * scalingFactor,
                            player.getPositionY() * scalingFactor
                    )
            );
        }
    }

    private static void applyColor(Shape shape, Player player, double opacityFactor) {
        Color playerColor = Theme.PLAYERS_COLORS[player.getPlayerId() % Theme.PLAYERS_COLORS.length]
                .deriveColor(0, 1, 1, opacityFactor);
        if (player.isUnderDashDefense()) {
            double frac = 1.0 - (double) (System.nanoTime() - player.getLastDashTime()) / Player.DASH_DEFENSE_DURATION_NS;
            playerColor = playerColor.interpolate(Color.WHITE, frac);
        }
        shape.setFill(playerColor);
    }

    public static Shape makeHitBox(Player player, double scalingFactor) {
        return new Circle(
                player.getPositionX() * scalingFactor,
                player.getPositionY() * scalingFactor,
                Player.PLAYER_SIZE / 4.0 * scalingFactor
        );
    }

    private static double playerRotation(Player player) {
        double rotation = 0.0;
        double velXSign = Math.signum(player.getVelocityX());
        double velYSign = Math.signum(player.getVelocityY());
        if (velXSign == 0) {
            if (velYSign > 0) {
                rotation = 90.0;
            } else if (velYSign < 0) {
                rotation = -90.0;
            }
        } else if (velXSign > 0) {
            if (velYSign > 0) {
                rotation = 45.0;
            } else if (velYSign < 0) {
                rotation = -45.0;
            }
        } else { // velXSign < 0
            if (velYSign > 0) {
                rotation = 135.0;
            } else if (velYSign < 0) {
                rotation = -135.0;
            } else {
                rotation = 180.0;
            }
        }
        return rotation;
    }
}
