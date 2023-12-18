package ru.kpfu.itis.gr201.ponomarev.geometrychaos.gameapp.ui.screen;

import javafx.application.Platform;
import javafx.beans.property.ObjectProperty;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.*;
import javafx.scene.shape.Shape;
import ru.kpfu.itis.gr201.ponomarev.geometrychaos.commons.game.Player;
import ru.kpfu.itis.gr201.ponomarev.geometrychaos.commons.ui.shapemaker.PlayerShapeMaker;
import ru.kpfu.itis.gr201.ponomarev.geometrychaos.gameapp.gamemap.GameMapData;
import ru.kpfu.itis.gr201.ponomarev.geometrychaos.gameapp.gamemap.GameMapState;
import ru.kpfu.itis.gr201.ponomarev.geometrychaos.gameapp.ui.common.MapDataViewer;

public class RoomScreen extends Screen {

    private final GridPane playersList;
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

        playersList = new GridPane();
        playersList.setVgap(20);
        playersList.setPrefWidth(USE_COMPUTED_SIZE);
        playersList.setPrefHeight(USE_COMPUTED_SIZE);
        playersList.setAlignment(Pos.CENTER);
        playersList.getColumnConstraints().addAll(
                new ColumnConstraints(50, 50, 50, Priority.NEVER, HPos.CENTER, true),
                new ColumnConstraints(200, 200, 200, Priority.NEVER, HPos.LEFT, true),
                new ColumnConstraints(200, 200, 200, Priority.NEVER, HPos.LEFT, true)
        );
        StackPane.setAlignment(playersList, Pos.CENTER);

        this.players.addListener((ListChangeListener<? super Player>) c -> updateList());
        updateList();

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

        getChildren().addAll(playersList, mapDataViewer, readyBtn);
    }

    public void updateList() {
        Platform.runLater(() -> {
            playersList.getChildren().clear();
            players.forEach(player -> {
                Shape playerShape = PlayerShapeMaker.makeDefault(player);
                Label usernameLabel = new Label(player.getUsername());
                Label stateLabel = new Label(player.getState().getMessage());
                playersList.addRow(playersList.getRowCount(), playerShape, usernameLabel, stateLabel);
            });
        });
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
