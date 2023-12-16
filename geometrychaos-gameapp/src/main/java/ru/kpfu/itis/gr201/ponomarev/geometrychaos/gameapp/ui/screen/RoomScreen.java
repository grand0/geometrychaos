package ru.kpfu.itis.gr201.ponomarev.geometrychaos.gameapp.ui.screen;

import javafx.application.Platform;
import javafx.beans.property.ObjectProperty;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.Background;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import ru.kpfu.itis.gr201.ponomarev.geometrychaos.commons.game.Player;
import ru.kpfu.itis.gr201.ponomarev.geometrychaos.commons.ui.Theme;
import ru.kpfu.itis.gr201.ponomarev.geometrychaos.gameapp.gamemap.GameMapData;
import ru.kpfu.itis.gr201.ponomarev.geometrychaos.gameapp.gamemap.GameMapState;
import ru.kpfu.itis.gr201.ponomarev.geometrychaos.gameapp.ui.common.MapDataViewer;

public class RoomScreen extends Screen {

    private final VBox playersVBox;
    private final MapDataViewer mapDataViewer;
    private Integer thisPlayerId;

    private final ObservableList<Player> players;
    private final ObjectProperty<GameMapData> mapData;
    private final ObjectProperty<GameMapState> mapState;
    private final Button readyBtn;

    private Runnable onReadyPressed;

    public RoomScreen(ObservableList<Player> players, ObjectProperty<GameMapData> mapData, ObjectProperty<GameMapState> mapState) {
        super("Room");

        this.players = players;
        this.mapData = mapData;
        this.mapState = mapState;

        ScrollPane playersScrollPane = new ScrollPane();
        playersScrollPane.setBackground(Background.fill(Theme.BACKGROUND));
        playersScrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        playersScrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.ALWAYS);
        playersScrollPane.setMinWidth(200);
        playersScrollPane.setMaxWidth(200);
        playersScrollPane.setMinHeight(200);
        playersScrollPane.setMaxHeight(200);
        StackPane.setAlignment(playersScrollPane, Pos.BOTTOM_LEFT);
        StackPane.setMargin(playersScrollPane, new Insets(20));

        playersVBox = new VBox();
        this.players.addListener((ListChangeListener<? super Player>) c -> updateList());
        updateList();
        playersScrollPane.setContent(playersVBox);
        playersScrollPane.setBackground(Background.fill(Theme.BACKGROUND));

        mapDataViewer = new MapDataViewer(this.mapData, this.mapState);
        StackPane.setAlignment(mapDataViewer, Pos.BOTTOM_CENTER);
        StackPane.setMargin(mapDataViewer, new Insets(20));

        readyBtn = new Button("Ready");
        readyBtn.setOnAction(event -> {
            if (onReadyPressed != null) {
                onReadyPressed.run();
            }
        });
        readyBtn.setVisible(false);
        StackPane.setAlignment(readyBtn, Pos.BOTTOM_RIGHT);
        StackPane.setMargin(readyBtn, new Insets(20));

        getChildren().addAll(playersScrollPane, mapDataViewer, readyBtn);
    }

    public void updateList() {
        Platform.runLater(() -> {
            playersVBox.getChildren().setAll(
                    players.stream()
                            .map(this::makePlayerListItem)
                            .toList()
            );
        });
    }

    private Node makePlayerListItem(Player player) {
        StringBuilder text = new StringBuilder(player.getUsername());
        if (player.getPlayerId() != thisPlayerId) {
            text.append(" - ").append(player.getState());
        }
        Label label = new Label(text.toString());
        return label;
    }

    public Runnable getOnSelectPressed() {
        return mapDataViewer.getOnSelectPressed();
    }

    public void setOnSelectPressed(Runnable onSelectPressed) {
        mapDataViewer.setOnSelectPressed(onSelectPressed);
    }

    public void setThisPlayerId(int playerId) {
        this.thisPlayerId = playerId;
        updateList();
    }

    public Runnable getOnReadyPressed() {
        return onReadyPressed;
    }

    public void setOnReadyPressed(Runnable onReadyPressed) {
        this.onReadyPressed = onReadyPressed;
    }

    public void setReadyButtonVisible(boolean visible) {
        readyBtn.setVisible(visible);
    }
}
