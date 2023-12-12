package ru.kpfu.itis.gr201.ponomarev.geometrychaos.game;

import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.beans.InvalidationListener;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.IntegerPropertyBase;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.geometry.Insets;
import javafx.geometry.Point2D;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCombination;
import javafx.scene.layout.Background;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.util.Duration;
import ru.kpfu.itis.gr201.ponomarev.geometrychaos.editor.exception.LevelLoadException;
import ru.kpfu.itis.gr201.ponomarev.geometrychaos.editor.game.GameObject;
import ru.kpfu.itis.gr201.ponomarev.geometrychaos.editor.game.LevelManager;
import ru.kpfu.itis.gr201.ponomarev.geometrychaos.editor.game.Player;
import ru.kpfu.itis.gr201.ponomarev.geometrychaos.editor.ui.GameField;
import ru.kpfu.itis.gr201.ponomarev.geometrychaos.game.ui.LevelSelector;
import ru.kpfu.itis.gr201.ponomarev.geometrychaos.game.ui.LoadingScreen;
import ru.kpfu.itis.gr201.ponomarev.geometrychaos.game.ui.common.AudioTimeBar;
import ru.kpfu.itis.gr201.ponomarev.geometrychaos.util.GlobalAudioSpectrumProvider;
import ru.kpfu.itis.gr201.ponomarev.geometrychaos.util.LevelDataFiles;
import ru.kpfu.itis.gr201.ponomarev.geometrychaos.util.Theme;

import java.io.File;
import java.net.URL;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Set;

public class GameApplication extends Application {

    public static final Point2D DEFAULT_SPAWN_POSITION = new Point2D(GameField.FIELD_WIDTH / 4.0, GameField.FIELD_HEIGHT / 2.0);

    private MediaPlayer audioPlayer;
    private GameField gameField;
    private StackPane gameFieldRoot;
    private Stage primaryStage;
    private Timeline gameTimeControl;
    private LevelSelector levelSelector;
    private Scene mainScene;
    private Player thisPlayer;
    private Timeline gameLoop;

    private MediaPlayer respawnSoundPlayer;

    private Set<KeyCode> pressedKeysInGame;

    private boolean paused = false;

    @Override
    public void start(Stage primaryStage) {
        this.primaryStage = primaryStage;

        URL introAudio = getClass().getResource("/audio/wavelight.mp3");
        if (introAudio != null) {
            this.audioPlayer = new MediaPlayer(new Media(introAudio.toExternalForm()));
            GlobalAudioSpectrumProvider.registerForMediaPlayer(audioPlayer);
            this.audioPlayer.play();
        }

        levelSelector = new LevelSelector();
        levelSelector.setOnPlayPressed(() -> {
            File file = chooseLevel();
            if (file == null) {
                return;
            }

            switchToLoadingScreen();
            Task<Boolean> task = new Task<>() {
                @Override
                protected Boolean call() {
                    try {
                        loadLevel(file);
                    } catch (LevelLoadException e) {
                        return false;
                    }
                    prepareGame();
                    return true;
                }
            };
            task.setOnSucceeded(event -> {
                boolean loaded = task.resultNow();
                if (loaded) {
                    switchToGameField();
                    startGame();
                } else {
                    switchToLevelSelector();
                }
            });
            new Thread(task).start();
        });

        gameLoop = new Timeline();
        gameLoop.getKeyFrames().addAll(
                new KeyFrame(
                        new Duration(1000.0 / 240.0),
                        this::gameLoopTick
                )
        );
        gameLoop.setCycleCount(Animation.INDEFINITE);

        pressedKeysInGame = new HashSet<>();

        mainScene = new Scene(levelSelector, 1280, 720);
        mainScene.setOnKeyPressed(event -> {
            if (mainScene.getRoot() == levelSelector) {
                if (event.getCode() == KeyCode.ESCAPE) {
                    Platform.exit();
                }
            } else if (mainScene.getRoot() == gameFieldRoot) {
                if (event.getCode() == KeyCode.ESCAPE) {
                    if (event.isShiftDown()) {
                        stopGame();
                        switchToLevelSelector();
                    } else if (paused) {
                        resumeGame();
                    } else {
                        pauseGame();
                    }
                }
                pressedKeysInGame.add(event.getCode());
            }
        });
        mainScene.setOnKeyReleased(event -> {
            if (mainScene.getRoot() == gameFieldRoot) {
                pressedKeysInGame.remove(event.getCode());
            }
        });

        respawnSoundPlayer = new MediaPlayer(new Media(getClass().getResource("/sounds/respawn.mp3").toExternalForm()));

        primaryStage.setScene(mainScene);
        primaryStage.setFullScreen(true);
        primaryStage.setFullScreenExitHint("");
        primaryStage.setFullScreenExitKeyCombination(KeyCombination.NO_MATCH);
        switchToLevelSelector();
        primaryStage.show();
    }

    private File chooseLevel() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Load map");
        FileChooser.ExtensionFilter extensionFilter = new FileChooser.ExtensionFilter("Map (*.gcmap)", "*.gcmap");
        fileChooser.getExtensionFilters().add(extensionFilter);
        return fileChooser.showOpenDialog(primaryStage);
    }

    private void loadLevel(File file) throws LevelLoadException {
        LevelDataFiles data = LevelDataFiles.read(file);
        LevelManager.getInstance().loadObjects(data.getLevel());
        if (audioPlayer != null) {
            audioPlayer.stop();
        }
        audioPlayer = new MediaPlayer(new Media(data.getAudio().toURI().toString()));
        GlobalAudioSpectrumProvider.registerForMediaPlayer(audioPlayer);
    }

    private void prepareGame() {
        thisPlayer = new Player();
        thisPlayer.setPositionX(DEFAULT_SPAWN_POSITION.getX());
        thisPlayer.setPositionY(DEFAULT_SPAWN_POSITION.getY());

        IntegerProperty time = new IntegerPropertyBase() {
            @Override
            public Object getBean() {
                return null;
            }

            @Override
            public String getName() {
                return "time";
            }
        };
        gameField = new GameField(time, null);
        gameField.setMinSize(Region.USE_PREF_SIZE, Region.USE_PREF_SIZE);
        gameField.setMaxSize(Region.USE_PREF_SIZE, Region.USE_PREF_SIZE);
        gameField.getPlayers().add(thisPlayer);
        gameField.setHitPlayerCallback(this::hitPlayerCallback);

        GameObject lastObj = LevelManager.getInstance().getObjects()
                .stream()
                .max(Comparator.comparingInt(GameObject::getEndTime))
                .orElse(null);
        int levelDuration = lastObj == null ? 0 : lastObj.getEndTime();
        gameTimeControl = new Timeline();
        gameTimeControl.getKeyFrames().addAll(
                new KeyFrame(
                        Duration.ZERO,
                        new KeyValue(time, 0)
                ),
                new KeyFrame(
                        new Duration(levelDuration),
                        event -> switchToLevelSelector(),
                        new KeyValue(time, levelDuration)
                )
        );

        AudioTimeBar timeBar = new AudioTimeBar(time, levelDuration);
        StackPane.setAlignment(timeBar, Pos.TOP_CENTER);
        StackPane.setMargin(timeBar, new Insets(20));
        gameFieldRoot = new StackPane(gameField, timeBar);
        InvalidationListener resizeListener = obs -> {
            double width = gameFieldRoot.getWidth();
            double height = gameFieldRoot.getHeight();
            double ratio = GameField.FIELD_ASPECT_RATIO;
            gameField.setPrefWidth(Math.min(width, height * ratio));
            gameField.setPrefHeight(Math.min(width / ratio, height));
        };
        gameFieldRoot.widthProperty().addListener(resizeListener);
        gameFieldRoot.heightProperty().addListener(resizeListener);
        gameFieldRoot.setBackground(Background.fill(Theme.BACKGROUND));

        pressedKeysInGame.clear();
    }

    private void switchToGameField() {
        mainScene.setRoot(gameFieldRoot);
    }

    private void switchToLevelSelector() {
        mainScene.setRoot(levelSelector);
    }

    private void switchToLoadingScreen() {
        mainScene.setRoot(new LoadingScreen());
    }

    private void startGame() {
        respawnSoundPlayer.seek(Duration.ZERO);
        respawnSoundPlayer.play();
        gameTimeControl.playFromStart();
        audioPlayer.stop();
        audioPlayer.play();
        gameLoop.playFromStart();
        paused = false;
    }

    private void pauseGame() {
        if (!paused) {
            gameTimeControl.pause();
            audioPlayer.pause();
            gameLoop.pause();
            paused = true;
        }
    }

    private void resumeGame() {
        if (paused) {
            gameTimeControl.play();
            audioPlayer.play();
            gameLoop.play();
            paused = false;
        }
    }

    private void stopGame() {
        gameTimeControl.stop();
        gameLoop.stop();
        audioPlayer.play(); // continue playing in menu
    }

    public static void main(String[] args) {
        launch();
    }

    private void gameLoopTick(ActionEvent event) {
        if (pressedKeysInGame.contains(KeyCode.SPACE)) {
            thisPlayer.dash();
        } else {
            double velX = 0, velY = 0;
            if (pressedKeysInGame.contains(KeyCode.W)) {
                velY--;
            }
            if (pressedKeysInGame.contains(KeyCode.S)) {
                velY++;
            }
            if (pressedKeysInGame.contains(KeyCode.A)) {
                velX--;
            }
            if (pressedKeysInGame.contains(KeyCode.D)) {
                velX++;
            }
            thisPlayer.setVelocityX(velX);
            thisPlayer.setVelocityY(velY);
        }

        thisPlayer.update();
    }

    private void hitPlayerCallback(Player player) {
        player.damage();
        if (player.getHealthPoints() == 0) {
            gameLoop.pause();
            Duration timeToPlayFrom;
            if (gameTimeControl.getCurrentTime().toSeconds() <= 5) {
                timeToPlayFrom = new Duration(0);
            } else {
                timeToPlayFrom = gameTimeControl.getCurrentTime().subtract(Duration.seconds(5));
            }

            Timeline rateAnim = new Timeline(
                    new KeyFrame(
                            Duration.ZERO,
                            new KeyValue(gameTimeControl.rateProperty(), 1.0),
                            new KeyValue(audioPlayer.rateProperty(), 1.0)
                    ),
                    new KeyFrame(
                            Duration.millis(1000),
                            new KeyValue(gameTimeControl.rateProperty(), 0.001), // for some reason Timeline doesn't like it when rate becomes == 0.0
                            new KeyValue(audioPlayer.rateProperty(), 0.0)
                    )
            );
            rateAnim.setOnFinished(event -> {
                player.restoreHealthPoints();
                respawnSoundPlayer.seek(Duration.ZERO);
                respawnSoundPlayer.play();
                gameLoop.play();
                gameTimeControl.setRate(1.0);
                gameTimeControl.jumpTo(timeToPlayFrom);
                audioPlayer.setRate(1.0);
                audioPlayer.seek(timeToPlayFrom);
                audioPlayer.play();
            });
            rateAnim.play();
        }
    }
}
