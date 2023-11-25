package ru.kpfu.itis.gr201.ponomarev.bheditor.ui;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ObjectPropertyBase;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import ru.kpfu.itis.gr201.ponomarev.bheditor.game.HittingObject;
import ru.kpfu.itis.gr201.ponomarev.bheditor.game.Shape;
import ru.kpfu.itis.gr201.ponomarev.bheditor.util.DoubleStringConverter;
import ru.kpfu.itis.gr201.ponomarev.bheditor.util.IntStringConverter;
import ru.kpfu.itis.gr201.ponomarev.bheditor.util.ShapeStringConverter;
import ru.kpfu.itis.gr201.ponomarev.bheditor.util.Theme;

public class GameObjectDetails extends Pane {

    private final TextField nameField;
    private final Spinner<Integer> startTimeSpinner;
    private final Spinner<Integer> durationSpinner;
    private final ComboBox<Shape> shapeComboBox;
    private final Spinner<Double> positionXSpinner;
    private final Spinner<Double> positionYSpinner;
    private final GridPane gridPane;

    private ObjectProperty<HittingObject> displayingObject = new ObjectPropertyBase<>() {
        @Override
        public Object getBean() {
            return this;
        }

        @Override
        public String getName() {
            return "displayingObject";
        }
    };

    public GameObjectDetails(ObjectProperty<HittingObject> objectToDisplay) {
        nameField = new TextField();

        startTimeSpinner = new Spinner<>(0, Integer.MAX_VALUE, 0, 100);
        startTimeSpinner.setEditable(true);

        durationSpinner = new Spinner<>(0, Integer.MAX_VALUE, 0, 100);
        durationSpinner.setEditable(true);

        shapeComboBox = new ComboBox<>();
        shapeComboBox.setItems(FXCollections.observableArrayList(Shape.values()));
        shapeComboBox.setConverter(new ShapeStringConverter());

        positionXSpinner = new Spinner<>(-Double.MAX_VALUE, Double.MAX_VALUE, 0, 10);
        positionXSpinner.setEditable(true);

        positionYSpinner = new Spinner<>(-Double.MAX_VALUE, Double.MAX_VALUE, 0, 10);
        positionYSpinner.setEditable(true);

        gridPane = new GridPane();
        gridPane.setPadding(new Insets(20));
        gridPane.setVgap(10);
        gridPane.setHgap(10);
        gridPane.setVisible(false);
        gridPane.addRow(0, makeLabel("Name"), nameField);
        gridPane.addRow(1, makeLabel("Start time"), startTimeSpinner);
        gridPane.addRow(2, makeLabel("Duration"), durationSpinner);
        gridPane.addRow(3, makeLabel("Shape"), shapeComboBox);
        gridPane.addRow(4, makeLabel("Position"), new HBox(10, makeLabel("X"), positionXSpinner, makeLabel("Y"), positionYSpinner));

        getChildren().add(gridPane);

        displayingObject.bindBidirectional(objectToDisplay);
        displayingObject.addListener((obs, oldValue, newValue) -> {
            displayObject(oldValue, newValue);
        });
    }

    public void displayObject(HittingObject old, HittingObject obj) {
        if (old != null) {
            nameField.textProperty().unbindBidirectional(old.nameProperty());
            startTimeSpinner.getEditor().textProperty().unbindBidirectional(old.startTimeProperty());
            durationSpinner.getEditor().textProperty().unbindBidirectional(old.durationProperty());
            shapeComboBox.valueProperty().unbindBidirectional(old.shapeProperty());
            positionXSpinner.getEditor().textProperty().unbindBidirectional(old.positionXProperty());
            positionYSpinner.getEditor().textProperty().unbindBidirectional(old.positionYProperty());
        }
        if (obj != null) {
            gridPane.setVisible(true);
            nameField.textProperty().bindBidirectional(obj.nameProperty());
            startTimeSpinner.getEditor().textProperty().bindBidirectional(obj.startTimeProperty(), new IntStringConverter());
            durationSpinner.getEditor().textProperty().bindBidirectional(obj.durationProperty(), new IntStringConverter());
            shapeComboBox.valueProperty().bindBidirectional(obj.shapeProperty());
            positionXSpinner.getEditor().textProperty().bindBidirectional(obj.positionXProperty(), new DoubleStringConverter());
            positionYSpinner.getEditor().textProperty().bindBidirectional(obj.positionYProperty(), new DoubleStringConverter());
        } else {
            gridPane.setVisible(false);
        }
    }

    private Label makeLabel(String text) {
        Label label = new Label(text);
        label.setTextFill(Theme.ON_BACKGROUND);
        return label;
    }
}
