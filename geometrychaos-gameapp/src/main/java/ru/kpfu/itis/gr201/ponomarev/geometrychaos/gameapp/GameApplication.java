package ru.kpfu.itis.gr201.ponomarev.geometrychaos.gameapp;

import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.beans.InvalidationListener;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.IntegerPropertyBase;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ObjectPropertyBase;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.layout.Background;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.media.AudioClip;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.util.Duration;
import ru.kpfu.itis.gr201.ponomarev.geometrychaos.commons.game.GameObject;
import ru.kpfu.itis.gr201.ponomarev.geometrychaos.commons.game.LevelManager;
import ru.kpfu.itis.gr201.ponomarev.geometrychaos.commons.game.Player;
import ru.kpfu.itis.gr201.ponomarev.geometrychaos.commons.game.PlayerState;
import ru.kpfu.itis.gr201.ponomarev.geometrychaos.commons.io.exception.LevelReadException;
import ru.kpfu.itis.gr201.ponomarev.geometrychaos.commons.io.exception.MapReadException;
import ru.kpfu.itis.gr201.ponomarev.geometrychaos.commons.io.level.LevelIO;
import ru.kpfu.itis.gr201.ponomarev.geometrychaos.commons.io.map.MapDataFiles;
import ru.kpfu.itis.gr201.ponomarev.geometrychaos.commons.io.map.MapIO;
import ru.kpfu.itis.gr201.ponomarev.geometrychaos.commons.ui.GameField;
import ru.kpfu.itis.gr201.ponomarev.geometrychaos.commons.ui.Theme;
import ru.kpfu.itis.gr201.ponomarev.geometrychaos.gameapp.gamemap.GameMapData;
import ru.kpfu.itis.gr201.ponomarev.geometrychaos.gameapp.gamemap.GameMapState;
import ru.kpfu.itis.gr201.ponomarev.geometrychaos.gameapp.net.client.GameClient;
import ru.kpfu.itis.gr201.ponomarev.geometrychaos.gameapp.ui.common.AudioTimeBar;
import ru.kpfu.itis.gr201.ponomarev.geometrychaos.gameapp.ui.overlay.GameResultsOverlay;
import ru.kpfu.itis.gr201.ponomarev.geometrychaos.gameapp.ui.screen.*;
import ru.kpfu.itis.gr201.ponomarev.geometrychaos.gameapp.util.GlobalAudioSpectrumProvider;
import ru.kpfu.itis.gr201.ponomarev.geometrychaos.net.protocol.ConnectionDenialReason;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.*;

@SuppressWarnings("ClassEscapesDefinedScope")
public class GameApplication extends Application {

    private Stage primaryStage;
    private Scene mainScene;
    private MainMenuScreen mainMenuScreen;
    private LevelSelector levelSelector;
    private ConnectScreen connectScreen;
    private RoomScreen roomScreen;
    private StackPane gameFieldRoot;

    private GameField gameField;
    private Label awaitingPlayersLabel;

    private MediaPlayer audioPlayer;
    private AudioClip respawnAudioClip;

    private Timeline gameTimeControl;
    private Timeline gameLoop;

    private Player thisPlayer;
    private ObservableList<Player> players;

    private ObjectProperty<GameMapData> selectedGameMap;
    private ObjectProperty<GameMapState> selectedGameMapState;

    private Set<KeyCode> pressedKeysInGame;

    private boolean paused = false;

    @Override
    public void start(Stage primaryStage) {
        this.primaryStage = primaryStage;

        GameClient.getInstance().initApplication(this);

        players = FXCollections.observableArrayList();

        URL introAudio = getClass().getResource("/audio/wavelight.mp3");
        if (introAudio != null) {
            this.audioPlayer = new MediaPlayer(new Media(introAudio.toExternalForm()));
            GlobalAudioSpectrumProvider.registerForMediaPlayer(audioPlayer);
            this.audioPlayer.play();
        }

        awaitingPlayersLabel = new Label("Awaiting players...");
        awaitingPlayersLabel.setFont(Theme.HEADLINE_FONT);
        awaitingPlayersLabel.setTextFill(Theme.ON_BACKGROUND);
        StackPane.setAlignment(awaitingPlayersLabel, Pos.CENTER);

        mainMenuScreen = new MainMenuScreen();
        mainMenuScreen.setOnSinglePlayerPressed(() -> switchScene(levelSelector));
        mainMenuScreen.setOnMultiPlayerPressed(() -> {
            connectScreen.setError(null);
            switchScene(connectScreen);
        });

        levelSelector = new LevelSelector();
        levelSelector.setOnSelectPressed(() -> {
            File file = chooseLevel();
            if (file == null) {
                return;
            }

            switchScene(new LoadingScreen());
            Task<Boolean> task = new Task<>() {
                @Override
                protected Boolean call() {
                    try {
                        loadMap(file);
                    } catch (MapReadException | LevelReadException e) {
                        return false;
                    }
                    initThisPlayer(0, "Player");
                    prepareGame();
                    return true;
                }
            };
            task.setOnSucceeded(event -> {
                boolean loaded = task.resultNow();
                if (loaded) {
                    switchScene(gameFieldRoot);
                    startGame();
                } else {
                    switchScene(levelSelector);
                }
            });
            new Thread(task).start();
        });

        connectScreen = new ConnectScreen();
        connectScreen.setOnConnectPressed(data -> {
            connect(data.username(), data.host());
        });

        roomScreen = new RoomScreen(players, selectedGameMapProperty(), selectedGameMapStateProperty());
        roomScreen.setOnSelectPressed(() -> {
            File file = chooseLevel();
            if (file == null) {
                return;
            }

            switchScene(new LoadingScreen());
            Task<byte[]> readFileTask = new Task<>() {
                @Override
                protected byte[] call() {
                    try (InputStream in = new FileInputStream(file)) {
                        return in.readAllBytes();
                    } catch (IOException e) {
                        return null;
                    }
                }
            };
            Task<Void> uploadTask = new Task<>() {
                @Override
                protected Void call() throws Exception {
                    GameClient.getInstance().uploadSelectedMap();
                    return null;
                }
            };
            readFileTask.setOnSucceeded(event -> {
                byte[] bytes = readFileTask.getValue();
                switchScene(roomScreen);
                roomScreen.setReadyButtonVisible(false);
                if (bytes != null) {
                    selectedGameMap.set(new GameMapData(file.getName(), bytes));
                    selectedGameMapState.set(GameMapState.UPLOADING);
                    new Thread(uploadTask).start();
                }
            });
            uploadTask.setOnFailed(event -> {
                selectedGameMap.set(null);
                selectedGameMapState.set(null);
                GameClient.getInstance().disconnect();
                connectScreen.setError(uploadTask.getException().toString());
                switchScene(connectScreen);
            });
            uploadTask.setOnSucceeded(event -> {
                players.forEach(player -> player.setState(PlayerState.DOWNLOADING));
                roomScreen.updateList();
                roomScreen.setReadyButtonVisible(true);
            });

            new Thread(readFileTask).start();
        });
        roomScreen.setOnReadyPressed(() -> {
            switchScene(new LoadingScreen());
            Task<Void> loadAndPrepareGameTask = new Task<>() {
                @Override
                protected Void call() throws MapReadException, LevelReadException {
                    loadSelectedMap();
                    prepareGame();
                    return null;
                }
            };
            loadAndPrepareGameTask.setOnSucceeded(event -> {
                switchScene(gameFieldRoot);
                GameClient.getInstance().thisPlayerReady();
                playerReady(thisPlayer.getPlayerId());
            });
            loadAndPrepareGameTask.setOnFailed(event -> {
                loadAndPrepareGameTask.getException().printStackTrace(System.err);

                GameClient.getInstance().disconnect();
                connectScreen.setError(loadAndPrepareGameTask.getException().toString());
                switchScene(connectScreen);
            });
            new Thread(loadAndPrepareGameTask).start();
        });

        gameLoop = new Timeline();
        gameLoop.getKeyFrames().addAll(
                new KeyFrame(
                        new Duration(1000.0 / 60.0),
                        this::gameLoopTick
                )
        );
        gameLoop.setCycleCount(Animation.INDEFINITE);

        pressedKeysInGame = new HashSet<>();

        mainScene = new Scene(levelSelector, 1280, 720);
        mainScene.setOnKeyPressed(event -> {
            if (mainScene.getRoot() == mainMenuScreen) {
                if (event.getCode() == KeyCode.ESCAPE) {
                    Platform.exit();
                }
            } else if (mainScene.getRoot() == levelSelector || mainScene.getRoot() == connectScreen) {
                if (event.getCode() == KeyCode.ESCAPE) {
                    switchScene(mainMenuScreen);
                }
            } else if (mainScene.getRoot() == roomScreen) {
                if (event.getCode() == KeyCode.ESCAPE) {
                    GameClient.getInstance().disconnect();
                    connectScreen.setError(null);
                    switchScene(connectScreen);
                }
            } else if (mainScene.getRoot() == gameFieldRoot) {
                if (event.getCode() == KeyCode.ESCAPE) {
                    if (event.isShiftDown()) {
                        stopGame();
                        GameClient.getInstance().disconnect();
                        switchScene(mainMenuScreen);
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

        respawnAudioClip = new AudioClip(getClass().getResource("/sounds/respawn.mp3").toExternalForm());

        primaryStage.setScene(mainScene);
        primaryStage.setFullScreen(true);
        primaryStage.setFullScreenExitHint("");
        primaryStage.setFullScreenExitKeyCombination(new KeyCodeCombination(KeyCode.F11));
        switchScene(mainMenuScreen);
        primaryStage.show();
    }

    @Override
    public void stop() {
        GameClient.getInstance().disconnect();
    }

    public void connect(String username, String host) {
        switchScene(new LoadingScreen());
        Task<Void> task = new Task<>() {
            @Override
            protected Void call() throws Exception {
                GameClient.getInstance().connect(username, host);
                return null;
            }
        };
        task.setOnFailed(event -> {
            connectScreen.setError("Exception: " + event.getSource().getException());
            switchScene(connectScreen);
        });
        new Thread(task).start();
    }

    public void initThisPlayer(int playerId, String username) {
        if (thisPlayer != null) {
            players.remove(thisPlayer);
        }
        thisPlayer = new Player(playerId, username);
        roomScreen.setThisPlayerId(playerId);
        players.add(thisPlayer);
    }

    public void addPlayer(int playerId, String username) {
        Player player = new Player(playerId, username);
        players.add(player);
    }

    public void removePlayer(int playerId) {
        players.removeIf(player -> player.getPlayerId() == playerId);
    }

    public void clearPlayers() {
        players.clear();
    }

    public void connectionApproved() {
        switchScene(roomScreen);
    }

    public void connectionDenied(ConnectionDenialReason reason) {
        connectScreen.setError(reason.getMessage());
        switchScene(connectScreen);
    }

    public void mapUploaded() {
        selectedGameMapStateProperty().set(GameMapState.FINISHED);
        roomScreen.setReadyButtonVisible(true);
    }

    public void mapChanged(String mapName, int mapSize) {
        players.forEach(player -> player.setState(PlayerState.DOWNLOADING));
        roomScreen.updateList();
        roomScreen.setReadyButtonVisible(false);
        selectedGameMapProperty().set(new GameMapData(mapName, new byte[0]));
        selectedGameMapStateProperty().set(GameMapState.DOWNLOADING);
        Task<Void> downloadTask = new Task<>() {
            @Override
            protected Void call() throws Exception {
                GameClient.getInstance().startMapDownloading(mapSize);
                return null;
            }
        };
        downloadTask.setOnFailed(event -> {
            connectScreen.setError(downloadTask.getException().toString());
            switchScene(connectScreen);
            GameClient.getInstance().disconnect();
        });
        new Thread(downloadTask).start();
    }

    public void mapDownloaded(byte[] mapBytes) {
        selectedGameMapProperty().set(new GameMapData(getSelectedGameMap().name(), mapBytes));
        selectedGameMapStateProperty().set(GameMapState.FINISHED);
        roomScreen.setReadyButtonVisible(true);
    }

    public void playerDownloadedMap(int playerId) {
        for (Player player : players) {
            if (player.getPlayerId() == playerId) {
                player.setState(PlayerState.IN_ROOM);
                break;
            }
        }
        roomScreen.updateList();
    }

    public void playerReady(int playerId) {
        for (Player player : players) {
            if (player.getPlayerId() == playerId) {
                if (player.getState() != PlayerState.IN_GAME) {
                    player.setState(PlayerState.IN_GAME);
                } else {
                    player.setState(PlayerState.IN_ROOM);
                }
                break;
            }
        }
        roomScreen.updateList();
    }

    public void playerHit(int playerId, Integer syncHealthPoints) {
        for (Player player : players) {
            if (player.getPlayerId() == playerId) {
                player.damage();
                if (syncHealthPoints != null) {
                    player.setHealthPoints(syncHealthPoints);
                }
                break;
            }
        }
        if (players.stream().allMatch(player -> player.getHealthPoints() == 0)) {
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
                            audioPlayer != null
                                    ? new KeyValue(audioPlayer.rateProperty(), 1.0)
                                    : null
                    ),
                    new KeyFrame(
                            Duration.millis(1000),
                            new KeyValue(gameTimeControl.rateProperty(), 0.001), // for some reason Timeline doesn't like it when rate becomes == 0.0
                            audioPlayer != null
                                    ? new KeyValue(audioPlayer.rateProperty(), 0.0)
                                    : null
                    )
            );
            rateAnim.setOnFinished(event -> {
                players.forEach(Player::restoreHealthPoints);
                respawnAudioClip.play();
                gameLoop.play();
                gameTimeControl.setRate(1.0);
                gameTimeControl.jumpTo(timeToPlayFrom);
                if (audioPlayer != null) {
                    audioPlayer.setRate(1.0);
                    audioPlayer.seek(timeToPlayFrom);
                    audioPlayer.play();
                }
            });
            rateAnim.play();
        }
    }

    public void clientDisconnectedWithError(Throwable t) {
        connectScreen.setError(t.toString());
        switchScene(connectScreen);
    }

    private File chooseLevel() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Load map");
        FileChooser.ExtensionFilter extensionFilter = new FileChooser.ExtensionFilter("Map (*.gcmap)", "*.gcmap");
        fileChooser.getExtensionFilters().add(extensionFilter);
        return fileChooser.showOpenDialog(primaryStage);
    }

    // TODO: delete this method in favor of loadSelectedMap()
    private void loadMap(File file) throws MapReadException, LevelReadException {
        MapDataFiles data = MapIO.read(file);
        List<GameObject> objects = LevelIO.readLevel(data.getLevel());
        LevelManager.getInstance().getObjects().setAll(objects);
        if (audioPlayer != null) {
            audioPlayer.stop();
        }
        if (data.getAudio() != null) {
            audioPlayer = new MediaPlayer(new Media(data.getAudio().toURI().toString()));
            GlobalAudioSpectrumProvider.registerForMediaPlayer(audioPlayer);
        } else {
            audioPlayer = null;
            GlobalAudioSpectrumProvider.registerForMediaPlayer(null);
        }
    }

    private void loadSelectedMap() throws MapReadException, LevelReadException {
        MapDataFiles data = MapIO.read(getSelectedGameMap().data());
        List<GameObject> objects = LevelIO.readLevel(data.getLevel());
        LevelManager.getInstance().getObjects().setAll(objects);
        if (audioPlayer != null) {
            audioPlayer.stop();
        }
        if (data.getAudio() != null) {
            audioPlayer = new MediaPlayer(new Media(data.getAudio().toURI().toString()));
            GlobalAudioSpectrumProvider.registerForMediaPlayer(audioPlayer);
        } else {
            audioPlayer = null;
            GlobalAudioSpectrumProvider.registerForMediaPlayer(null);
        }
    }

    private void prepareGame() {
        players.forEach(Player::restoreInitialState);

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
        gameField.getPlayers().addAll(players);
        gameField.setThisPlayer(thisPlayer);
        gameField.setHitThisPlayerCallback(this::thisPlayerHitCallback);

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
                        event -> {
                            GameResultsOverlay resultsOverlay = new GameResultsOverlay(players);
                            resultsOverlay.setOnReturnPressed(() -> {
                                if (GameClient.getInstance().isConnected()) {
                                    switchScene(roomScreen);
                                    GameClient.getInstance().thisPlayerReady();
                                    playerReady(thisPlayer.getPlayerId());
                                } else {
                                    players.clear();
                                    thisPlayer = null;
                                    switchScene(levelSelector);
                                }
                            });
                            gameFieldRoot.getChildren().add(resultsOverlay);
                            stopGame();
                        },
                        new KeyValue(time, levelDuration)
                )
        );

        AudioTimeBar timeBar = new AudioTimeBar(time, levelDuration);
        StackPane.setAlignment(timeBar, Pos.TOP_CENTER);
        StackPane.setMargin(timeBar, new Insets(20));

        gameFieldRoot = new StackPane(gameField, timeBar, awaitingPlayersLabel);
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

    private void switchScene(Parent node) {
        mainScene.setRoot(node);
    }

    public void startGame() {
        Platform.runLater(() -> gameFieldRoot.getChildren().remove(awaitingPlayersLabel));

        respawnAudioClip.play();
        gameTimeControl.playFromStart();
        if (audioPlayer != null) {
            audioPlayer.stop();
            audioPlayer.play();
        }
        gameLoop.playFromStart();
        paused = false;
    }

    private void pauseGame() {
        if (!paused) {
            gameTimeControl.pause();
            if (audioPlayer != null) {
                audioPlayer.pause();
            }
            gameLoop.pause();
            paused = true;
        }
    }

    private void resumeGame() {
        if (paused) {
            gameTimeControl.play();
            if (audioPlayer != null) {
                audioPlayer.play();
            }
            gameLoop.play();
            paused = false;
        }
    }

    private void stopGame() {
        gameTimeControl.stop();
        gameLoop.stop();
        if (audioPlayer != null) {
            audioPlayer.play(); // continue playing in menu
        }
    }

    public static void main(String[] args) {
        launch();
    }

    private void gameLoopTick(ActionEvent event) {
        if (pressedKeysInGame.contains(KeyCode.SPACE)) {
            if (thisPlayer.canDash()) {
                thisPlayer.dash();
                GameClient.getInstance().thisPlayerDash(thisPlayer.getVelocityX(), thisPlayer.getVelocityY());
            }
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

        players.forEach(Player::update);
        GameClient.getInstance().thisPlayerUpdate(thisPlayer.getPositionX(), thisPlayer.getPositionY(), thisPlayer.getVelocityX(), thisPlayer.getVelocityY(), thisPlayer.getHealthPoints());
    }

    private void thisPlayerHitCallback() {
        GameClient.getInstance().thisPlayerHit(thisPlayer.getHealthPoints());
        playerHit(thisPlayer.getPlayerId(), null);
    }

    public List<Player> getPlayers() {
        return Collections.unmodifiableList(players);
    }

    public GameMapData getSelectedGameMap() {
        return selectedGameMapProperty().get();
    }

    public ObjectProperty<GameMapData> selectedGameMapProperty() {
        if (selectedGameMap == null) {
            selectedGameMap = new ObjectPropertyBase<>() {
                @Override
                public Object getBean() {
                    return null;
                }

                @Override
                public String getName() {
                    return null;
                }
            };
        }
        return selectedGameMap;
    }

    public GameMapState getSelectedGameMapState() {
        return selectedGameMapStateProperty().get();
    }

    public ObjectProperty<GameMapState> selectedGameMapStateProperty() {
        if (selectedGameMapState == null) {
            selectedGameMapState = new ObjectPropertyBase<>() {
                @Override
                public Object getBean() {
                    return null;
                }

                @Override
                public String getName() {
                    return null;
                }
            };
        }
        return selectedGameMapState;
    }
}
