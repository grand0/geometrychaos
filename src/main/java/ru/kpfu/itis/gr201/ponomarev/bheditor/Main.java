package ru.kpfu.itis.gr201.ponomarev.bheditor;

import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.application.Application;
import javafx.beans.InvalidationListener;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import javafx.util.Duration;
import ru.kpfu.itis.gr201.ponomarev.bheditor.ui.GameField;
import ru.kpfu.itis.gr201.ponomarev.bheditor.ui.GameObjectDetails;
import ru.kpfu.itis.gr201.ponomarev.bheditor.ui.TimelineControlButtons;
import ru.kpfu.itis.gr201.ponomarev.bheditor.ui.ObjectsTimeline;
import ru.kpfu.itis.gr201.ponomarev.bheditor.util.GameObjectsManager;
import ru.kpfu.itis.gr201.ponomarev.bheditor.util.Theme;

public class Main extends Application {

    private static final double WINDOW_WIDTH = 1280;
    private static final double WINDOW_HEIGHT = 720;

    @Override
    public void start(Stage primaryStage) {
        BorderPane root = new BorderPane();

        ObjectsTimeline objectsTimeline = new ObjectsTimeline();

        Timeline cursorPositionTimeline = new Timeline(
                new KeyFrame(
                        Duration.ZERO,
                        new KeyValue(objectsTimeline.cursorPositionProperty(), 0)
                ),
                getEndKeyFrame(objectsTimeline)
        );

        TimelineControlButtons timelineControlButtons = new TimelineControlButtons();
        timelineControlButtons.setAddTimelineListener(() -> {
            GameObjectsManager.getInstance().addObject(
                    objectsTimeline.getCursorPosition(),
                    10000
            );
        });
        timelineControlButtons.setPlayPauseListener((playing) -> {
            if (!playing) {
                cursorPositionTimeline.pause();
            } else {
                cursorPositionTimeline.stop();
                cursorPositionTimeline.getKeyFrames().remove(1);
                cursorPositionTimeline.getKeyFrames().add(getEndKeyFrame(objectsTimeline));
                cursorPositionTimeline.playFrom(new Duration(objectsTimeline.getCursorPosition()));
            }
        });
        timelineControlButtons.setStopListener(() -> {
            cursorPositionTimeline.stop();
            objectsTimeline.setCursorPosition(0);
        });
        cursorPositionTimeline.setOnFinished(event -> timelineControlButtons.setPlaying(false));

        objectsTimeline.prefWidthProperty().bind(primaryStage.widthProperty());

        VBox timelinePanel = new VBox(timelineControlButtons, objectsTimeline);

        StackPane gamePane = new StackPane();
        gamePane.setMinSize(0, 0);
        GameField gameField = new GameField(objectsTimeline.cursorPositionProperty());
        StackPane.setMargin(gameField, new Insets(20));
        gameField.setMinSize(Region.USE_PREF_SIZE, Region.USE_PREF_SIZE);
        gameField.setMaxSize(Region.USE_PREF_SIZE, Region.USE_PREF_SIZE);
        gamePane.getChildren().add(gameField);
        InvalidationListener resizeListener = obs -> {
            Insets margin = StackPane.getMargin(gameField);
            double width = gamePane.getWidth() - margin.getLeft() - margin.getRight();
            double height = gamePane.getHeight() - margin.getTop() - margin.getBottom();
            double ratio = GameField.FIELD_ASPECT_RATIO;
            gameField.setPrefWidth(Math.min(width, height * ratio));
            gameField.setPrefHeight(Math.min(width / ratio, height));
        };
        gamePane.widthProperty().addListener(resizeListener);
        gamePane.heightProperty().addListener(resizeListener);

        GameObjectDetails gameObjectDetails = new GameObjectDetails(objectsTimeline.selectedObjectProperty());

        root.setCenter(gamePane);
        root.setBottom(timelinePanel);
        root.setRight(gameObjectDetails);

        root.setBackground(Background.fill(Theme.BACKGROUND));

        Scene scene = new Scene(root, WINDOW_WIDTH, WINDOW_HEIGHT);
        primaryStage.setScene(scene);
        primaryStage.setMaximized(true);
        primaryStage.show();
    }

    private static KeyFrame getEndKeyFrame(ObjectsTimeline objectsTimeline) {
        return new KeyFrame(
                objectsTimeline.getTotalDuration(),
                new KeyValue(objectsTimeline.cursorPositionProperty(), objectsTimeline.getTotalDuration().toMillis())
        );
    }

    public static void main(String[] args) {
        launch();
    }
}
