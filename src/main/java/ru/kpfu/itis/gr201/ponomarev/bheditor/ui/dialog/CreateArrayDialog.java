package ru.kpfu.itis.gr201.ponomarev.bheditor.ui.dialog;

import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.VPos;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.Spinner;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.RowConstraints;
import javafx.util.Pair;
import ru.kpfu.itis.gr201.ponomarev.bheditor.anim.KeyFrameTag;
import ru.kpfu.itis.gr201.ponomarev.bheditor.game.HittingObject;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

public class CreateArrayDialog extends Dialog<CreateArrayDialog.CreateArrayIntent> {

    public CreateArrayDialog() {
        setTitle("Array creation");
        setHeaderText("Create an array of objects");
        getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        GridPane gridPane = new GridPane();
        gridPane.setHgap(10);
        gridPane.setVgap(10);
        gridPane.setPadding(new Insets(20));

        Spinner<Integer> countSpinner = new Spinner<>(1, Integer.MAX_VALUE, 10);
        countSpinner.setEditable(true);

        Spinner<Integer> intervalSpinner = new Spinner<>(0, Integer.MAX_VALUE, 100);
        intervalSpinner.setEditable(true);

        Spinner<Integer> arraysCountSpinner = new Spinner<>(1, Integer.MAX_VALUE, 1);
        arraysCountSpinner.setEditable(true);

        gridPane.addRow(0, new Label("Count"), countSpinner);
        gridPane.addRow(1, new Label("Interval"), intervalSpinner);
        gridPane.addRow(2, new Label("Arrays count"), arraysCountSpinner);
        gridPane.addRow(3, new Label("Per array deltas"), new Label("x"), new Label("y"));
        gridPane.getRowConstraints().addAll(
                new RowConstraints(),
                new RowConstraints(),
                new RowConstraints(),
                new RowConstraints(50)
        );
        gridPane.getRowConstraints().get(3).setValignment(VPos.BOTTOM);

        // TODO: allow not only doubles
        Map<KeyFrameTag, Pair<Spinner<Double>, Spinner<Double>>> deltaSpinnersMap = new HashMap<>();
        for (KeyFrameTag tag : KeyFrameTag.values()) {
            Spinner<Double> xSpinner = new Spinner<>(
                    -Double.MAX_VALUE, Double.MAX_VALUE, 0.0, 1.0
            );
            Spinner<Double> ySpinner = new Spinner<>(
                    -Double.MAX_VALUE, Double.MAX_VALUE, 0.0, 1.0
            );
            xSpinner.setEditable(true);
            ySpinner.setEditable(true);
            deltaSpinnersMap.put(tag, new Pair<>(xSpinner, ySpinner));
            gridPane.addRow(gridPane.getRowCount(), new Label(tag.getName()), xSpinner, ySpinner);
        }

        getDialogPane().setContent(gridPane);

        Platform.runLater(countSpinner::requestFocus);

        setResultConverter(btn -> {
            if (btn == ButtonType.OK) {
                Map<KeyFrameTag, Pair<Double, Double>> deltasMap = deltaSpinnersMap.entrySet()
                        .stream()
                        .collect(Collectors.toMap(Map.Entry::getKey, entry -> new Pair<>(
                                entry.getValue().getKey().getValue(),
                                entry.getValue().getValue().getValue()
                        )));

                return new CreateArrayIntent(
                        countSpinner.getValue(),
                        intervalSpinner.getValue(),
                        arraysCountSpinner.getValue(),
                        deltasMap
                );
            }
            return null;
        });
    }

    // TODO: allow not only doubles
    public record CreateArrayIntent(int count, int interval, int arraysCount, Map<KeyFrameTag, Pair<Double, Double>> deltas) { }
}
