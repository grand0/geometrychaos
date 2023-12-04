package ru.kpfu.itis.gr201.ponomarev.bheditor.ui.dialog;

import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.Spinner;
import javafx.scene.layout.GridPane;

public class MassCopyDialog extends Dialog<MassCopyDialog.MassCopyIntent> {

    public MassCopyDialog() {
        setTitle("Mass copy");
        setHeaderText("Mass copy");
        getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        GridPane gridPane = new GridPane();
        gridPane.setHgap(10);
        gridPane.setVgap(10);
        gridPane.setPadding(new Insets(20));

        Spinner<Integer> countSpinner = new Spinner<>(1, Integer.MAX_VALUE, 10);
        countSpinner.setEditable(true);
        Spinner<Integer> intervalSpinner = new Spinner<>(1, Integer.MAX_VALUE, 100);
        intervalSpinner.setEditable(true);

        gridPane.addRow(0, new Label("Count"), countSpinner);
        gridPane.addRow(1, new Label("Interval"), intervalSpinner);

        getDialogPane().setContent(gridPane);

        Platform.runLater(countSpinner::requestFocus);

        setResultConverter(btn -> {
            if (btn == ButtonType.OK) {
                return new MassCopyIntent(countSpinner.getValue(), intervalSpinner.getValue());
            }
            return null;
        });
    }

    public record MassCopyIntent(int count, int interval) { }
}
