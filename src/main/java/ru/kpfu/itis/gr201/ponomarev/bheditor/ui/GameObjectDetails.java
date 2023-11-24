package ru.kpfu.itis.gr201.ponomarev.bheditor.ui;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ObjectPropertyBase;
import javafx.collections.FXCollections;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import ru.kpfu.itis.gr201.ponomarev.bheditor.game.HittingObject;
import ru.kpfu.itis.gr201.ponomarev.bheditor.game.Shape;
import ru.kpfu.itis.gr201.ponomarev.bheditor.util.DoubleStringConverter;
import ru.kpfu.itis.gr201.ponomarev.bheditor.util.IntStringConverter;
import ru.kpfu.itis.gr201.ponomarev.bheditor.util.ShapeStringConverter;

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

        startTimeSpinner = new Spinner<>(new SpinnerValueFactory.IntegerSpinnerValueFactory(0, Integer.MAX_VALUE));
        startTimeSpinner.setEditable(true);

        durationSpinner = new Spinner<>(new SpinnerValueFactory.IntegerSpinnerValueFactory(0, Integer.MAX_VALUE));
        durationSpinner.setEditable(true);

        shapeComboBox = new ComboBox<>();
        shapeComboBox.setItems(FXCollections.observableArrayList(Shape.values()));
        shapeComboBox.setConverter(new ShapeStringConverter());

        positionXSpinner = new Spinner<>(new SpinnerValueFactory.DoubleSpinnerValueFactory(-Double.MIN_VALUE, Double.MAX_VALUE));
        positionXSpinner.setEditable(true);

        positionYSpinner = new Spinner<>(new SpinnerValueFactory.DoubleSpinnerValueFactory(-Double.MIN_VALUE, Double.MAX_VALUE));
        positionYSpinner.setEditable(true);

        gridPane = new GridPane();
        gridPane.setVisible(false);
        gridPane.addRow(0, new Label("Name"), nameField);
        gridPane.addRow(1, new Label("Start time"), startTimeSpinner);
        gridPane.addRow(2, new Label("Duration"), durationSpinner);
        gridPane.addRow(3, new Label("Shape"), shapeComboBox);
        gridPane.addRow(4, new Label("Position"), new Label("X"), positionXSpinner, new Label("Y"), positionYSpinner);

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
}
