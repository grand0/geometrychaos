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
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.layout.*;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.stage.Stage;
import javafx.util.Duration;
import ru.kpfu.itis.gr201.ponomarev.bheditor.anim.KeyFrameTag;
import ru.kpfu.itis.gr201.ponomarev.bheditor.audio.AudioSamples;
import ru.kpfu.itis.gr201.ponomarev.bheditor.game.HittingObject;
import ru.kpfu.itis.gr201.ponomarev.bheditor.ui.*;
import ru.kpfu.itis.gr201.ponomarev.bheditor.game.GameObjectsManager;
import ru.kpfu.itis.gr201.ponomarev.bheditor.ui.dialog.OpenAudioDialog;
import ru.kpfu.itis.gr201.ponomarev.bheditor.util.Theme;
import ru.kpfu.itis.gr201.ponomarev.bheditor.anim.ObjectKeyFrame;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import java.io.File;
import java.util.Arrays;
import java.util.Optional;

public class Main extends Application {

    private static final double WINDOW_WIDTH = 1280;
    private static final double WINDOW_HEIGHT = 720;

    @Override
    public void start(Stage primaryStage) {
        BorderPane root = new BorderPane();

        ObjectProperty<AudioSamples> audioSamples = new ObjectPropertyBase<>() {
            @Override
            public Object getBean() {
                return null;
            }

            @Override
            public String getName() {
                return "audioSamples";
            }
        };
        ObjectProperty<MediaPlayer> mediaPlayer = new ObjectPropertyBase<>() {
            @Override
            public Object getBean() {
                return null;
            }

            @Override
            public String getName() {
                return "mediaPlayer";
            }
        };

        MenuItem openAudioMenuItem = new MenuItem("Open...");
        openAudioMenuItem.setOnAction(event -> {
            OpenAudioDialog dialog = new OpenAudioDialog();
            dialog.setOwner(primaryStage);
            Optional<File> opt = dialog.showAndWait();
            opt.ifPresent(file -> {
                try (AudioInputStream ais = AudioSystem.getAudioInputStream(file)) {
                    audioSamples.set(new AudioSamples(ais));
                    mediaPlayer.set(new MediaPlayer(new Media(file.toURI().toString())));
                } catch (Exception e) {
                    audioSamples.set(null);
                    mediaPlayer.set(null);
                }
            });
        });
        Menu audioMenu = new Menu("Audio", null, openAudioMenuItem);
        MenuBar menuBar = new MenuBar(audioMenu);

        ObjectsTimeline objectsTimeline = new ObjectsTimeline();
        objectsTimeline.audioSamplesProperty().bind(audioSamples);

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
                if (mediaPlayer.get() != null) {
                    mediaPlayer.get().pause();
                }
            } else {
                Duration start = new Duration(objectsTimeline.getCursorPosition());
                cursorPositionTimeline.stop();
                cursorPositionTimeline.getKeyFrames().remove(1);
                cursorPositionTimeline.getKeyFrames().add(getEndKeyFrame(objectsTimeline));
                cursorPositionTimeline.playFrom(start);
                if (mediaPlayer.get() != null) {
                    mediaPlayer.get().setStartTime(start);
                    mediaPlayer.get().play();
                }
            }
        });
        objectsTimelineControls.setStopListener(() -> {
            cursorPositionTimeline.stop();
            objectsTimeline.setCursorPosition(0);
            if (mediaPlayer.get() != null) {
                mediaPlayer.get().stop();
            }
        });
        cursorPositionTimeline.setOnFinished(event -> {
            objectsTimelineControls.setPlaying(false);
            if (mediaPlayer.get() != null) {
                mediaPlayer.get().pause();
            }
        });

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

        KeyFramesTimeline[] kfTimelines = Arrays.stream(KeyFrameTag.values())
                .map(t -> new KeyFramesTimeline(t, objectsTimeline.selectedObjectProperty()))
                .toArray(KeyFramesTimeline[]::new);
        for (int i = 0; i < kfTimelines.length; i++) {
            KeyFramesTimeline kft = kfTimelines[i];
            kft.setBackground(Background.fill(Theme.RAINBOW_START_COLOR.deriveColor(i * (360.0 / kfTimelines.length), 1, 1, 1)));
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

        root.setTop(menuBar);
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
