package ru.kpfu.itis.gr201.ponomarev.bheditor.ui;

import javafx.beans.InvalidationListener;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ObjectPropertyBase;
import javafx.collections.FXCollections;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.Spinner;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import ru.kpfu.itis.gr201.ponomarev.bheditor.game.HittingObject;
import ru.kpfu.itis.gr201.ponomarev.bheditor.game.Shape;
import ru.kpfu.itis.gr201.ponomarev.bheditor.util.ShapeStringConverter;
import ru.kpfu.itis.gr201.ponomarev.bheditor.util.Theme;

public class GameObjectDetails extends Pane {

    private final TextField nameField;
    private final Spinner<Integer> startTimeSpinner;
    private final Spinner<Integer> durationSpinner;
    private final ComboBox<Shape> shapeComboBox;
    private final GridPane gridPane;

    private final ObjectProperty<HittingObject> displayingObject = new ObjectPropertyBase<>() {
        @Override
        public Object getBean() {
            return null;
        }

        @Override
        public String getName() {
            return "displayingObject";
        }
    };

    public GameObjectDetails(ObjectProperty<HittingObject> objectToDisplay) {

        nameField = new TextField();
        nameField.textProperty().addListener(obs -> {
            HittingObject obj = displayingObject.get();
            String val = nameField.getText();
            if (obj != null && !obj.getName().equals(val)) {
                obj.setName(val);
            }
        });

        startTimeSpinner = new Spinner<>(0, Integer.MAX_VALUE, 0, 100);
        startTimeSpinner.setEditable(true);
        startTimeSpinner.valueProperty().addListener(obs -> {
            HittingObject obj = displayingObject.get();
            int val = startTimeSpinner.getValue();
            if (obj != null && obj.getStartTime() != val) {
                obj.setStartTime(val);
            }
        });

        durationSpinner = new Spinner<>(0, Integer.MAX_VALUE, 0, 100);
        durationSpinner.setEditable(true);
        durationSpinner.valueProperty().addListener(obs -> {
            HittingObject obj = displayingObject.get();
            int val = durationSpinner.getValue();
            if (obj != null && obj.getDuration() != val) {
                obj.setDuration(val);
            }
        });

        shapeComboBox = new ComboBox<>();
        shapeComboBox.setItems(FXCollections.observableArrayList(Shape.values()));
        shapeComboBox.setConverter(new ShapeStringConverter());
        shapeComboBox.valueProperty().addListener(obs -> {
            HittingObject obj = displayingObject.get();
            Shape val = shapeComboBox.getValue();
            if (obj != null && !obj.getShape().equals(val)) {
                obj.setShape(val);
            }
        });

        gridPane = new GridPane();
        gridPane.setVgap(10);
        gridPane.setHgap(10);
        gridPane.setVisible(false);
        gridPane.addRow(0, makeLabel("Name"), nameField);
        gridPane.addRow(1, makeLabel("Start time"), startTimeSpinner);
        gridPane.addRow(2, makeLabel("Duration"), durationSpinner);
        gridPane.addRow(3, makeLabel("Shape"), shapeComboBox);

        getChildren().add(gridPane);

        displayingObject.bind(objectToDisplay);
        InvalidationListener redrawOnChange = (obs) -> redraw();
        displayingObject.addListener((obs, oldV, newV) -> {
            if (oldV != null) {
                oldV.removeListener(redrawOnChange);
            }
            if (newV != null) {
                newV.addListener(redrawOnChange);
            }
            redraw();
        });
    }

    private void redraw() {
        HittingObject obj = displayingObject.get();

        if (obj == null) {
            gridPane.setVisible(false);
            return;
        }

        gridPane.setVisible(true);

        nameField.setText(obj.getName());
        startTimeSpinner.getEditor().setText(String.valueOf(obj.getStartTime()));
        durationSpinner.getEditor().setText(String.valueOf(obj.getDuration()));
        shapeComboBox.setValue(obj.getShape());
    }

    private Label makeLabel(String text) {
        Label label = new Label(text);
        label.setTextFill(Theme.ON_BACKGROUND);
        return label;
    }
}
