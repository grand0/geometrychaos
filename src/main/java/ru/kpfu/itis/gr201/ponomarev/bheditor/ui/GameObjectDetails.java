package ru.kpfu.itis.gr201.ponomarev.bheditor.ui;

import javafx.animation.Interpolator;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.beans.InvalidationListener;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ObjectPropertyBase;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import ru.kpfu.itis.gr201.ponomarev.bheditor.game.HittingObject;
import ru.kpfu.itis.gr201.ponomarev.bheditor.game.Shape;
import ru.kpfu.itis.gr201.ponomarev.bheditor.util.IntStringConverter;
import ru.kpfu.itis.gr201.ponomarev.bheditor.util.ShapeStringConverter;
import ru.kpfu.itis.gr201.ponomarev.bheditor.util.Theme;

import java.util.Optional;

public class GameObjectDetails extends Pane {

    private final TextField nameField;
    private final Spinner<Integer> startTimeSpinner;
    private final Spinner<Integer> durationSpinner;
    private final ComboBox<Shape> shapeComboBox;
    private final Spinner<Double> positionXSpinner;
    private final Spinner<Double> positionYSpinner;
    private final GridPane gridPane;

    private boolean redrawing = false;

    private final ObjectProperty<HittingObject> displayingObject = new ObjectPropertyBase<>() {
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

        positionXSpinner = new Spinner<>(-Double.MAX_VALUE, Double.MAX_VALUE, 0, 10);
        positionXSpinner.setEditable(true);
        positionXSpinner.valueProperty().addListener(obs -> {
            HittingObject obj = displayingObject.get();
            double val = positionXSpinner.getValue();
            if (obj != null) {
                Optional<KeyFrame> optKf = obj.getPositionXKeyFrame(obj.getTime());
                if (optKf.isPresent()) {
                    Optional<KeyValue> optKv = optKf.get().getValues().stream().findFirst();
                    if (optKv.isPresent() && !optKv.get().getEndValue().equals(val)) {
                        displayingObject.get().addPositionXKeyFrame(
                                val,
                                obj.getTime(),
                                Interpolator.LINEAR // TODO
                        );
                    }
                } else {
                    displayingObject.get().addPositionXKeyFrame(
                            val,
                            obj.getTime(),
                            Interpolator.LINEAR // TODO
                    );
                }
            }
        });

        positionYSpinner = new Spinner<>(-Double.MAX_VALUE, Double.MAX_VALUE, 0, 10);
        positionYSpinner.setEditable(true);
        positionYSpinner.getEditor().textProperty().addListener(obs -> {
            HittingObject obj = displayingObject.get();
            double val = positionYSpinner.getValue();
            if (obj != null) {
                Optional<KeyFrame> optKf = obj.getPositionYKeyFrame(obj.getTime());
                if (optKf.isPresent()) {
                    Optional<KeyValue> optKv = optKf.get().getValues().stream().findFirst();
                    if (optKv.isPresent() && !optKv.get().getEndValue().equals(val)) {
                        displayingObject.get().addPositionYKeyFrame(
                                val,
                                obj.getTime(),
                                Interpolator.LINEAR // TODO
                        );
                    }
                }
            }
        });

        gridPane = new GridPane();
        gridPane.setPadding(new Insets(20));
        gridPane.setVgap(10);
        gridPane.setHgap(10);
        gridPane.setVisible(false);
        gridPane.addRow(0, makeLabel("Name"), nameField);
        gridPane.addRow(1, makeLabel("Start time"), startTimeSpinner);
        gridPane.addRow(2, makeLabel("Duration"), durationSpinner);
        gridPane.addRow(3, makeLabel("Shape"), shapeComboBox);
        HBox positionBox = new HBox(10, makeLabel("X"), positionXSpinner, makeLabel("Y"), positionYSpinner);
        positionBox.setAlignment(Pos.CENTER_LEFT);
        gridPane.addRow(4, makeLabel("Position"), positionBox);

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

        redrawing = true;
        gridPane.setVisible(true);

        nameField.setText(obj.getName());
        startTimeSpinner.getEditor().setText(String.valueOf(obj.getStartTime()));
        durationSpinner.getEditor().setText(String.valueOf(obj.getDuration()));
        shapeComboBox.getEditor().setText(obj.getShape().getName());

        positionXSpinner.getEditor().setText(String.valueOf(obj.getPositionX()));
        positionYSpinner.getEditor().setText(String.valueOf(obj.getPositionY()));

        redrawing = false;
    }

    private Label makeLabel(String text) {
        Label label = new Label(text);
        label.setTextFill(Theme.ON_BACKGROUND);
        return label;
    }
}
