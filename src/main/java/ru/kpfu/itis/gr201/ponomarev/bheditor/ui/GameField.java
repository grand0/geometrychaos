package ru.kpfu.itis.gr201.ponomarev.bheditor.ui;

import javafx.beans.InvalidationListener;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.IntegerPropertyBase;
import javafx.scene.layout.Background;
import javafx.scene.layout.Pane;
import javafx.scene.shape.Ellipse;
import javafx.scene.shape.Polygon;
import javafx.scene.shape.Rectangle;
import javafx.scene.transform.Rotate;
import javafx.scene.transform.Scale;
import ru.kpfu.itis.gr201.ponomarev.bheditor.game.Shape;
import ru.kpfu.itis.gr201.ponomarev.bheditor.util.GameObjectsManager;
import ru.kpfu.itis.gr201.ponomarev.bheditor.util.Theme;

import java.util.Arrays;

public class GameField extends Pane {

    public static final double FIELD_WIDTH = 1280;
    public static final double FIELD_HEIGHT = 720;
    public static final double FIELD_ASPECT_RATIO = FIELD_WIDTH / FIELD_HEIGHT;

    private boolean listenToObjectsChanges = true;

    private final IntegerProperty time = new IntegerPropertyBase() {
        @Override
        protected void invalidated() {
            super.invalidated();
            listenToObjectsChanges = false;
            redraw();
            listenToObjectsChanges = true;
        }

        @Override
        public Object getBean() {
            return null;
        }

        @Override
        public String getName() {
            return "time";
        }
    };

    public GameField(IntegerProperty timelineCursorPosition) {
        time.bind(timelineCursorPosition);
        GameObjectsManager.getInstance().objectsProperty().addListener((InvalidationListener) obs -> {
            if (listenToObjectsChanges) {
                redraw();
            }
        });
        widthProperty().addListener(obs -> redraw());
        heightProperty().addListener(obs -> redraw());

        setBackground(Background.fill(Theme.GAME_FIELD_BACKGROUND));
    }

    public void redraw() {
        double scalingFactor = getWidth() / FIELD_WIDTH;
        double shapeSize = Shape.DEFAULT_SHAPE_SIZE;

        getChildren().clear();
        getChildren().addAll(
                GameObjectsManager.getInstance()
                        .getObjects()
                        .stream()
                        .filter(ho -> ho.isVisible(getTime()))
                        .map(ho -> {
                            ho.setTime(getTime() - ho.getStartTime());
                            javafx.scene.shape.Shape shape = null;
                            double shapeCenterOnScreenX = (ho.getPositionX() * scalingFactor) + getWidth() / 2;
                            double shapeCenterOnScreenY = (ho.getPositionY() * scalingFactor) + getHeight() / 2;
                            switch (ho.getShape()) {
                                case SQUARE -> {
                                    shape = new Rectangle(
                                            ((ho.getPositionX() - shapeSize / 2) * scalingFactor) + getWidth() / 2,
                                            ((ho.getPositionY() - shapeSize / 2) * scalingFactor) + getHeight() / 2,
                                            shapeSize * scalingFactor,
                                            shapeSize * scalingFactor
                                    );
                                }
                                case CIRCLE -> {
                                    shape = new Ellipse(
                                            (ho.getPositionX() * scalingFactor) + getWidth() / 2,
                                            (ho.getPositionY() * scalingFactor) + getHeight() / 2,
                                            (shapeSize / 2) * scalingFactor,
                                            (shapeSize / 2) * scalingFactor
                                    );
                                }
                                case TRIANGLE -> {
                                    double height = Math.sqrt(0.75 * shapeSize * shapeSize);
                                    double[] points = new double[] {
                                            (ho.getPositionX()                ) * scalingFactor + getWidth() / 2, (ho.getPositionY() - height * (2.0 / 3.0)) * scalingFactor + getHeight() / 2,
                                            (ho.getPositionX() + shapeSize / 2) * scalingFactor + getWidth() / 2, (ho.getPositionY() + height / 3.0        ) * scalingFactor + getHeight() / 2,
                                            (ho.getPositionX() - shapeSize / 2) * scalingFactor + getWidth() / 2, (ho.getPositionY() + height / 3.0        ) * scalingFactor + getHeight() / 2,
                                    };
                                    shape = new Polygon(points);
                                }
                            }
                            shape.setFill(Theme.PRIMARY);
                            shape.getTransforms().addAll(
                                    new Rotate(ho.getRotation(), shapeCenterOnScreenX + ho.getPivotX(), shapeCenterOnScreenY + ho.getPivotY()),
                                    new Scale(ho.getScaleX(), ho.getScaleY(), shapeCenterOnScreenX + ho.getPivotX(), shapeCenterOnScreenY + ho.getPivotY())
                            );
                            return shape;
                        })
                        .toList()
        );
    }

    public int getTime() {
        return time.get();
    }

    public IntegerProperty timeProperty() {
        return time;
    }

    public void setTime(int time) {
        this.time.set(time);
    }
}
