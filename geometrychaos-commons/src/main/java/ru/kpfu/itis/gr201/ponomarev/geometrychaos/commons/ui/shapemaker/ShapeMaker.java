package ru.kpfu.itis.gr201.ponomarev.geometrychaos.commons.ui.shapemaker;

import javafx.geometry.Point2D;
import javafx.geometry.VPos;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Polygon;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.Shape;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import ru.kpfu.itis.gr201.ponomarev.geometrychaos.commons.game.shape.GameShape;
import ru.kpfu.itis.gr201.ponomarev.geometrychaos.commons.util.TextOrigin;

public class ShapeMaker {

    public static final double SIN_30 = Math.sin(Math.PI / 6.0);
    public static final double COS_30 = Math.cos(Math.PI / 6.0);

    private final double centerX;
    private final double centerY;
    private final double size;
    private final double scalingFactor;
    private final GameShape gameShape;
    private final Point2D origin;

    public ShapeMaker(double centerX, double centerY, double size, double scalingFactor, GameShape gameShape) {
        this(centerX, centerY, size, scalingFactor, gameShape, null);
    }

    public ShapeMaker(double centerX, double centerY, double size, double scalingFactor, GameShape gameShape, Point2D origin) {
        this.centerX = centerX;
        this.centerY = centerY;
        this.size = size;
        this.scalingFactor = scalingFactor;
        this.gameShape = gameShape;
        if (origin == null) {
            this.origin = new Point2D(0, 0);
        } else {
            this.origin = origin;
        }
    }

    public Shape make() {
        Shape result = null;
        switch (gameShape.getType()) {
            case SQUARE -> result = makeSquare();
            case CIRCLE -> result = makeCircle();
            case TRIANGLE -> result = makeTriangle();
            case HEXAGON -> result = makeHexagon();
            case TEXT -> result = makeText();
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

    private Shape makeText() {
        String textStr = (String) gameShape.getSettings()[0].getValue();
        TextOrigin textOrigin = (TextOrigin) gameShape.getSettings()[1].getValue();
        Text text = new Text(textStr);
        text.setFont(Font.font("Monospaced", 32 * scalingFactor));
        switch (textOrigin) {
            case TOP_LEFT, TOP_CENTER, TOP_RIGHT -> text.setTextOrigin(VPos.TOP);
            case LEFT, CENTER, RIGHT -> text.setTextOrigin(VPos.CENTER);
            case BOTTOM_CENTER, BOTTOM_LEFT, BOTTOM_RIGHT -> text.setTextOrigin(VPos.BOTTOM);
        }
        Shape textShape = Shape.union(new Rectangle(0, 0), text);
        double posX = centerX * scalingFactor + origin.getX();
        switch (textOrigin) {
            case TOP_CENTER, CENTER, BOTTOM_CENTER -> posX -= textShape.prefWidth(-1) / 2.0;
            case TOP_RIGHT, RIGHT, BOTTOM_RIGHT -> posX -= textShape.prefWidth(-1);
        }
        text.setX(posX);
        text.setY(centerY * scalingFactor + origin.getY());
        return text;
    }
}
