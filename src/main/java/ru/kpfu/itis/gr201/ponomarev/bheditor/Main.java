package ru.kpfu.itis.gr201.ponomarev.bheditor;

import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.application.Application;
import javafx.beans.InvalidationListener;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ObjectPropertyBase;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import javafx.util.Duration;
import ru.kpfu.itis.gr201.ponomarev.bheditor.game.HittingObject;
import ru.kpfu.itis.gr201.ponomarev.bheditor.ui.*;
import ru.kpfu.itis.gr201.ponomarev.bheditor.game.GameObjectsManager;
import ru.kpfu.itis.gr201.ponomarev.bheditor.util.Theme;
import ru.kpfu.itis.gr201.ponomarev.bheditor.util.anim.ObjectKeyFrame;

import java.util.Arrays;

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

        ObjectsTimelineControls objectsTimelineControls = new ObjectsTimelineControls(objectsTimeline.cursorPositionProperty());
        objectsTimelineControls.setAddTimelineListener(() -> {
            GameObjectsManager.getInstance().addObject(
                    objectsTimeline.getCursorPosition(),
                    3000
            );
        });
        objectsTimelineControls.setPlayPauseListener((playing) -> {
            if (!playing) {
                cursorPositionTimeline.pause();
            } else {
                cursorPositionTimeline.stop();
                cursorPositionTimeline.getKeyFrames().remove(1);
                cursorPositionTimeline.getKeyFrames().add(getEndKeyFrame(objectsTimeline));
                cursorPositionTimeline.playFrom(new Duration(objectsTimeline.getCursorPosition()));
            }
        });
        objectsTimelineControls.setStopListener(() -> {
            cursorPositionTimeline.stop();
            objectsTimeline.setCursorPosition(0);
        });
        cursorPositionTimeline.setOnFinished(event -> objectsTimelineControls.setPlaying(false));

        objectsTimeline.prefWidthProperty().bind(primaryStage.widthProperty());

        VBox timelinePanel = new VBox(objectsTimelineControls, objectsTimeline);

        StackPane gamePane = new StackPane();
        gamePane.setMinSize(0, 0);
        GameField gameField = new GameField(objectsTimeline.cursorPositionProperty(), objectsTimeline.selectedObjectProperty());
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

        VBox gameObjectSettingsBox = new VBox();
        gameObjectSettingsBox.setPrefWidth(400);
        gameObjectSettingsBox.setBackground(Background.fill(Theme.BACKGROUND));
        gameObjectSettingsBox.setPadding(new Insets(20));
        gameObjectSettingsBox.setBorder(
                new Border(
                        new BorderStroke(
                                null, null, null, Theme.BACKGROUND.brighter(),
                                null, null, null, BorderStrokeStyle.SOLID,
                                null, new BorderWidths(0, 0, 0, 2), null
                        )
                )
        );

        GameObjectDetails gameObjectDetails = new GameObjectDetails(objectsTimeline.selectedObjectProperty());
        VBox.setMargin(gameObjectDetails, new Insets(0, 0, 10, 0));

        ObjectProperty<ObjectKeyFrame> selectedKeyFrame = new ObjectPropertyBase<>() {
            @Override
            public Object getBean() {
                return null;
            }

            @Override
            public String getName() {
                return "selectedKeyFrame";
            }
        };

        KeyFramesTimeline[] kfTimelines = new KeyFramesTimeline[] {
                new KeyFramesTimeline("PosX", HittingObject.POSITION_X_KEYFRAME_NAME_TAG, objectsTimeline.selectedObjectProperty()),
                new KeyFramesTimeline("PosY", HittingObject.POSITION_Y_KEYFRAME_NAME_TAG, objectsTimeline.selectedObjectProperty()),
                new KeyFramesTimeline("ScaleX", HittingObject.SCALE_X_KEYFRAME_NAME_TAG, objectsTimeline.selectedObjectProperty()),
                new KeyFramesTimeline("ScaleY", HittingObject.SCALE_Y_KEYFRAME_NAME_TAG, objectsTimeline.selectedObjectProperty()),
                new KeyFramesTimeline("Rot", HittingObject.ROTATION_KEYFRAME_NAME_TAG, objectsTimeline.selectedObjectProperty()),
                new KeyFramesTimeline("PivotX", HittingObject.PIVOT_X_KEYFRAME_NAME_TAG, objectsTimeline.selectedObjectProperty()),
                new KeyFramesTimeline("PivotY", HittingObject.PIVOT_Y_KEYFRAME_NAME_TAG, objectsTimeline.selectedObjectProperty()),
        };
        for (int i = 0; i < kfTimelines.length; i++) {
            KeyFramesTimeline kft = kfTimelines[i];
            kft.setBackground(Background.fill(Theme.RAINBOW_START_COLOR.deriveColor(i * (360.0 / 7.0), 1, 1, 1)));
            kft.prefWidthProperty().bind(gameObjectSettingsBox.widthProperty());
            kft.selectedKeyFrameProperty().addListener(obs -> {
                if (kft.getSelectedKeyFrame() != null) {
                    selectedKeyFrame.set(kft.getSelectedKeyFrame());
                    Arrays.stream(kfTimelines)
                            .filter(k -> !k.equals(kft))
                            .forEach(k -> k.setSelectedKeyFrame(null));
                } else if (kft.isLostKeyFrameFocus()) {
                    kft.setLostKeyFrameFocus(false);
                    selectedKeyFrame.set(null);
                    Arrays.stream(kfTimelines)
                            .filter(k -> !k.equals(kft))
                            .forEach(k -> k.setSelectedKeyFrame(null));
                }
            });
        }
        VBox.setMargin(kfTimelines[kfTimelines.length - 1], new Insets(0, 0, 10, 0));

        objectsTimeline.selectedObjectProperty().addListener(obs -> {
            selectedKeyFrame.set(null);
            Arrays.stream(kfTimelines)
                    .forEach(k -> k.setSelectedKeyFrame(null));
        });

        KeyFrameEditor keyFrameEditor = new KeyFrameEditor(selectedKeyFrame, objectsTimeline.selectedObjectProperty());
        keyFrameEditor.prefWidthProperty().bind(gameObjectSettingsBox.widthProperty());
        keyFrameEditor.keyFrameProperty().addListener(obs -> {
            selectedKeyFrame.set(keyFrameEditor.getKeyFrame());
        });
        VBox.setMargin(keyFrameEditor, new Insets(0, 0, 10, 0));

        gameObjectSettingsBox.getChildren().add(gameObjectDetails);
        gameObjectSettingsBox.getChildren().addAll(kfTimelines);
        gameObjectSettingsBox.getChildren().add(keyFrameEditor);

        root.setCenter(gamePane);
        root.setBottom(timelinePanel);
        root.setRight(gameObjectSettingsBox);

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
