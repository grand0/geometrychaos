package ru.kpfu.itis.gr201.ponomarev.geometrychaos;

import javafx.animation.*;
import javafx.application.Application;
import javafx.beans.property.DoublePropertyBase;
import javafx.geometry.Bounds;
import javafx.scene.Scene;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Paint;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.Shape;
import javafx.scene.transform.Rotate;
import javafx.stage.Stage;
import javafx.util.Duration;

public class CollisionsDemo extends Application {

    public static final String PLAYER_COLOR = "#00FFFF";
    public static final double WINDOW_WIDTH = 1280;
    public static final double WINDOW_HEIGHT = 720;
    public static final double PLAYER_SIZE = 100;
    public static final double RAY_WIDTH = 30;

    @Override
    public void start(Stage stage) {

        Pane root = new Pane();

        Scene scene = new Scene(root, WINDOW_WIDTH, WINDOW_HEIGHT);

        Rectangle player = new Rectangle(WINDOW_WIDTH / 2, WINDOW_HEIGHT / 2, PLAYER_SIZE, PLAYER_SIZE);
        player.setFill(Paint.valueOf(PLAYER_COLOR));
        player.setStrokeWidth(3.0);

        Rectangle ray = new Rectangle(100, 480, RAY_WIDTH, WINDOW_HEIGHT);

        Circle circle = new Circle(ray.getX() + ray.getWidth() / 2, ray.getY(), 4);
        circle.setFill(Paint.valueOf("#FF0000"));

        Rotate rotate = new Rotate(-30);
        rotate.pivotXProperty().bind(new DoublePropertyBase() {
            @Override
            public double get() {
                return ray.getX() + (ray.getWidth() / 2);
            }

            @Override
            public Object getBean() {
                return ray;
            }

            @Override
            public String getName() {
                return "";
            }
        });
        rotate.pivotYProperty().bind(ray.yProperty());
        ray.getTransforms().add(rotate);

        Rectangle rayBoundary = new Rectangle();
        rayBoundary.setStroke(Paint.valueOf("#FF0000"));
        rayBoundary.setStrokeWidth(1.0);
        rayBoundary.setFill(null);

        Rectangle playerBoundary = new Rectangle();
        playerBoundary.setStroke(Paint.valueOf("#FF0000"));
        playerBoundary.setStrokeWidth(1.0);
        playerBoundary.setFill(null);

        AnimationTimer timer = new AnimationTimer() {
            private Shape lastIntersection;

            @Override
            public void handle(long now) {
                Bounds rayBounds = ray.getBoundsInParent();
                rayBoundary.setX(rayBounds.getMinX());
                rayBoundary.setY(rayBounds.getMinY());
                rayBoundary.setWidth(rayBounds.getWidth());
                rayBoundary.setHeight(rayBounds.getHeight());

                Bounds playerBounds = player.getBoundsInParent();
                playerBoundary.setX(playerBounds.getMinX());
                playerBoundary.setY(playerBounds.getMinY());
                playerBoundary.setWidth(playerBounds.getWidth());
                playerBoundary.setHeight(playerBounds.getHeight());

                Shape intersection = Shape.intersect(ray, player);
                intersection.setStroke(Paint.valueOf("#00FF00"));
                intersection.setStrokeWidth(3.0);
                intersection.setFill(null);
                intersection.setViewOrder(-999);
                if (lastIntersection != null) {
                    root.getChildren().remove(lastIntersection);
                }
                root.getChildren().add(intersection);
                lastIntersection = intersection;


                if (intersection.getBoundsInParent().getWidth() > 0) {
                    player.setStroke(Paint.valueOf("#0000FF"));
                } else if (playerBounds.intersects(rayBounds)) {
                    player.setStroke(Paint.valueOf("#FF0000"));
                } else {
                    player.setStroke(null);
                }
            }
        };
        timer.start();

        Timeline rayRotationTimeline = new Timeline();
        rayRotationTimeline.setCycleCount(Animation.INDEFINITE);
        rayRotationTimeline.setAutoReverse(true);
        KeyFrame rayRotationKf = new KeyFrame(
                Duration.seconds(10),
                new KeyValue(
                        rotate.angleProperty(),
                        -150,
                        Interpolator.LINEAR
                )
        );
        rayRotationTimeline.getKeyFrames().addAll(rayRotationKf);
        rayRotationTimeline.play();

        Timeline playerRotationTimeline = new Timeline();
        playerRotationTimeline.setCycleCount(Animation.INDEFINITE);
        playerRotationTimeline.setAutoReverse(false);
        KeyFrame playerRotationKf = new KeyFrame(
                Duration.seconds(5),
                new KeyValue(
                        player.rotateProperty(),
                        360,
                        Interpolator.LINEAR
                )
        );
        playerRotationTimeline.getKeyFrames().addAll(playerRotationKf);
        playerRotationTimeline.play();

        root.getChildren().addAll(player, ray, circle, rayBoundary, playerBoundary);

        stage.setScene(scene);
        stage.show();

    }

    public static void main(String[] args) {
        launch();
    }
}
