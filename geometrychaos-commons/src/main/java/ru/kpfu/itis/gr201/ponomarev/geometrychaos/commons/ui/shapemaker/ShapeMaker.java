package ru.kpfu.itis.gr201.ponomarev.geometrychaos.commons.ui.shapemaker;

import javafx.geometry.Point2D;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Polygon;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.Shape;

public class ShapeMaker {

    public static final double SIN_30 = Math.sin(Math.PI / 6.0);
    public static final double COS_30 = Math.cos(Math.PI / 6.0);

    private final double centerX;
    private final double centerY;
    private final double size;
    private final double scalingFactor;
    private final ru.kpfu.itis.gr201.ponomarev.geometrychaos.commons.game.Shape shape;
    private final Point2D origin;

    public ShapeMaker(double centerX, double centerY, double size, double scalingFactor, ru.kpfu.itis.gr201.ponomarev.geometrychaos.commons.game.Shape shape) {
        this(centerX, centerY, size, scalingFactor, shape, null);
    }

    public ShapeMaker(double centerX, double centerY, double size, double scalingFactor, ru.kpfu.itis.gr201.ponomarev.geometrychaos.commons.game.Shape shape, Point2D origin) {
        this.centerX = centerX;
        this.centerY = centerY;
        this.size = size;
        this.scalingFactor = scalingFactor;
        this.shape = shape;
        if (origin == null) {
            this.origin = new Point2D(0, 0);
        } else {
            this.origin = origin;
        }
    }

    public Shape make() {
        Shape result = null;
        switch (shape) {
            case SQUARE -> result = makeSquare();
            case CIRCLE -> result = makeCircle();
            case TRIANGLE -> result = makeTriangle();
            case HEXAGON -> result = makeHexagon();
        }
        if (result == null) {
            throw new RuntimeException("Unknown shape type");
        }
        return result;
    }

    private Shape makeSquare() {
        return new Rectangle(
                ((centerX - size / 2.0) * scalingFactor) + origin.getX(),
                ((centerY - size / 2.0) * scalingFactor) + origin.getY(),
                size * scalingFactor,
                size * scalingFactor
        );
    }

    private Shape makeCircle() {
        return new Circle(
                (centerX * scalingFactor) + origin.getX(),
                (centerY * scalingFactor) + origin.getY(),
                size * scalingFactor / 2.0
        );
    }

    private Shape makeTriangle() {
        double height = Math.sqrt(0.75 * size * size);
        return new Polygon(
                (centerX           ) * scalingFactor + origin.getX(), (centerY - height * (2.0 / 3.0)) * scalingFactor + origin.getY(),
                (centerX + size / 2) * scalingFactor + origin.getX(), (centerY + height / 3.0        ) * scalingFactor + origin.getY(),
                (centerX - size / 2) * scalingFactor + origin.getX(), (centerY + height / 3.0        ) * scalingFactor + origin.getY()
        );
    }

    private Shape makeHexagon() {
        double sideLength = size / (2 * SIN_30 + 1);
        double halfHeight = sideLength * COS_30;
        return new Polygon(
                (centerX - sideLength / 2) * scalingFactor + origin.getX(), (centerY - halfHeight) * scalingFactor + origin.getY(),
                (centerX + sideLength / 2) * scalingFactor + origin.getX(), (centerY - halfHeight) * scalingFactor + origin.getY(),
                (centerX + size       / 2) * scalingFactor + origin.getX(), (centerY             ) * scalingFactor + origin.getY(),
                (centerX + sideLength / 2) * scalingFactor + origin.getX(), (centerY + halfHeight) * scalingFactor + origin.getY(),
                (centerX - sideLength / 2) * scalingFactor + origin.getX(), (centerY + halfHeight) * scalingFactor + origin.getY(),
                (centerX - size       / 2) * scalingFactor + origin.getX(), (centerY             ) * scalingFactor + origin.getY()
        );
    }
}
