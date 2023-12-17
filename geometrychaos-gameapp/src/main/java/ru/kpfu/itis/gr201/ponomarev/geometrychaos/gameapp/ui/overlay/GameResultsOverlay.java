package ru.kpfu.itis.gr201.ponomarev.geometrychaos.gameapp.ui.overlay;

import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.geometry.VPos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.Background;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.StackPane;
import ru.kpfu.itis.gr201.ponomarev.geometrychaos.commons.game.GameResultRank;
import ru.kpfu.itis.gr201.ponomarev.geometrychaos.commons.game.Player;
import ru.kpfu.itis.gr201.ponomarev.geometrychaos.commons.ui.Theme;

import java.util.List;

public class GameResultsOverlay extends StackPane {

    private Runnable onReturnPressed;

    public GameResultsOverlay(List<Player> players) {
        setBackground(Background.fill(Theme.BACKGROUND.deriveColor(0, 1, 1, 0.7)));

        Button returnBtn = new Button("Return to room");
        returnBtn.setOnAction(event -> {
            if (onReturnPressed != null) {
                onReturnPressed.run();
            }
        });
        StackPane.setAlignment(returnBtn, Pos.BOTTOM_RIGHT);
        StackPane.setMargin(returnBtn, new Insets(20));

        GridPane gridPane = new GridPane();
        gridPane.setVgap(20);
        gridPane.setHgap(100);
        gridPane.setAlignment(Pos.CENTER);
        StackPane.setAlignment(gridPane, Pos.CENTER);
        for (Player player : players) {
            Label usernameLabel = new Label(player.getUsername());
            usernameLabel.setTextFill(Theme.ON_BACKGROUND);
            GridPane.setHalignment(usernameLabel, HPos.LEFT);
            GridPane.setValignment(usernameLabel, VPos.CENTER);

            GameResultRank rank = GameResultRank.rankPlayer(player);
            Label rankLabel = new Label(rank == null ? null : rank.getDisplayString());
            rankLabel.setTextFill(Theme.ON_BACKGROUND);
            rankLabel.setFont(Theme.HEADLINE_FONT);
            GridPane.setHalignment(rankLabel, HPos.CENTER);
            GridPane.setValignment(rankLabel, VPos.CENTER);

            gridPane.addRow(gridPane.getRowCount(), usernameLabel, rankLabel);
        }

        getChildren().addAll(gridPane, returnBtn);
    }

    public Runnable getOnReturnPressed() {
        return onReturnPressed;
    }

    public void setOnReturnPressed(Runnable onReturnPressed) {
        this.onReturnPressed = onReturnPressed;
    }
}
