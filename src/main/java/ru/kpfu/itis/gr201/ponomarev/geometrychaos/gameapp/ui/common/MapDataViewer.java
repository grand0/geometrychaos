package ru.kpfu.itis.gr201.ponomarev.geometrychaos.gameapp.ui.common;

import javafx.application.Platform;
import javafx.beans.property.ObjectProperty;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.Background;
import javafx.scene.layout.GridPane;
import ru.kpfu.itis.gr201.ponomarev.geometrychaos.common.ui.Theme;
import ru.kpfu.itis.gr201.ponomarev.geometrychaos.gameapp.gamemap.GameMapData;
import ru.kpfu.itis.gr201.ponomarev.geometrychaos.gameapp.gamemap.GameMapState;

public class MapDataViewer extends GridPane {

    private final Label mapName;
    private final Label status;
    private final LoadingSpinner loadingSpinner;
    private final Button selectButton;

    private Runnable onSelectPressed;

    public MapDataViewer(ObjectProperty<GameMapData> mapData, ObjectProperty<GameMapState> mapState) {
        setBackground(Background.fill(Theme.BACKGROUND.brighter()));

        mapName = new Label();
        mapName.setFont(Theme.HEADLINE_FONT);
        mapName.setTextFill(Theme.ON_BACKGROUND);
        status = new Label();
        status.setTextFill(Theme.ON_BACKGROUND);
        loadingSpinner = new LoadingSpinner();
        GridPane.setHalignment(loadingSpinner, HPos.RIGHT);
        selectButton = new Button("Select map");
        selectButton.setOnAction(event -> {
            if (onSelectPressed != null) {
                onSelectPressed.run();
            }
        });
        GridPane.setHalignment(selectButton, HPos.RIGHT);

        add(mapName, 0, 0, 2, 1);
        add(status, 0, 1);
        add(loadingSpinner, 1, 1);
        add(selectButton, 1, 2);

        setMinSize(USE_PREF_SIZE, USE_PREF_SIZE);
        setMaxSize(USE_PREF_SIZE, USE_PREF_SIZE);
        setHgap(10);
        setVgap(10);
        setPadding(new Insets(20));

        mapData.addListener(obs -> Platform.runLater(() -> updateGameMapData(mapData.get())));
        updateGameMapData(mapData.get());
        if (mapState != null) {
            mapState.addListener(obs -> Platform.runLater(() -> updateGameMapState(mapState.get())));
            updateGameMapState(mapState.get());
        } else {
            updateGameMapState(null);
        }
    }

    private void updateGameMapData(GameMapData data) {
        if (data == null) {
            mapName.setText("Not selected");
        } else {
            mapName.setText(data.name());
        }
    }

    private void updateGameMapState(GameMapState state) {
        if (state == null) {
            status.setText("");
            loadingSpinner.setVisible(false);
        } else {
            status.setText(state.getMessage());
            switch (state) {
                case DOWNLOADING, UPLOADING -> {
                    loadingSpinner.setVisible(true);
                }
                case FINISHED -> {
                    loadingSpinner.setVisible(false);
                }
            }
        }
    }

    public Runnable getOnSelectPressed() {
        return onSelectPressed;
    }

    public void setOnSelectPressed(Runnable onSelectPressed) {
        this.onSelectPressed = onSelectPressed;
    }
}
