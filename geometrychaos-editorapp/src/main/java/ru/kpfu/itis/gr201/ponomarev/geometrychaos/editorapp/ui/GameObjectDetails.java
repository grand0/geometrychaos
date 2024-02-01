package ru.kpfu.itis.gr201.ponomarev.geometrychaos.editorapp.ui;

import javafx.beans.InvalidationListener;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ObjectPropertyBase;
import javafx.collections.FXCollections;
import javafx.scene.Node;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.Spinner;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import ru.kpfu.itis.gr201.ponomarev.geometrychaos.commons.game.GameObject;
import ru.kpfu.itis.gr201.ponomarev.geometrychaos.commons.game.shape.GameShapeType;
import ru.kpfu.itis.gr201.ponomarev.geometrychaos.commons.game.shape.setting.ShapeDropdownSetting;
import ru.kpfu.itis.gr201.ponomarev.geometrychaos.commons.game.shape.setting.ShapeSetting;
import ru.kpfu.itis.gr201.ponomarev.geometrychaos.commons.game.shape.setting.ShapeStringSetting;
import ru.kpfu.itis.gr201.ponomarev.geometrychaos.commons.ui.Theme;
import ru.kpfu.itis.gr201.ponomarev.geometrychaos.editorapp.util.converter.ShapeStringConverter;

import java.util.HashMap;
import java.util.List;

public class GameObjectDetails extends Pane {

    private final TextField nameField;
    private final Spinner<Integer> startTimeSpinner;
    private final Spinner<Integer> durationSpinner;
    private final ComboBox<GameShapeType> shapeComboBox;
    private final Spinner<Integer> viewOrderSpinner;
    private final GridPane gridPane;

    private final ObjectProperty<GameObject> displayingObject = new ObjectPropertyBase<>() {
        @Override
        public Object getBean() {
            return null;
        }

        @Override
        public String getName() {
            return "displayingObject";
        }
    };

    public GameObjectDetails(ObjectProperty<GameObject> objectToDisplay) {

        nameField = new TextField();
        nameField.textProperty().addListener(obs -> {
            GameObject obj = displayingObject.get();
            String val = nameField.getText();
            if (obj != null && !obj.getName().equals(val)) {
                obj.setName(val);
            }
        });

        startTimeSpinner = new Spinner<>(0, Integer.MAX_VALUE, 0, 100);
        startTimeSpinner.setEditable(true);
        startTimeSpinner.valueProperty().addListener(obs -> {
            GameObject obj = displayingObject.get();
            int val = startTimeSpinner.getValue();
            if (obj != null && obj.getStartTime() != val) {
                obj.setStartTime(val);
            }
        });

        durationSpinner = new Spinner<>(0, Integer.MAX_VALUE, 0, 100);
        durationSpinner.setEditable(true);
        durationSpinner.valueProperty().addListener(obs -> {
            GameObject obj = displayingObject.get();
            int val = durationSpinner.getValue();
            if (obj != null && obj.getDuration() != val) {
                obj.setDuration(val);
            }
        });

        shapeComboBox = new ComboBox<>();
        shapeComboBox.setItems(FXCollections.observableArrayList(GameShapeType.values()));
        shapeComboBox.setConverter(new ShapeStringConverter());
        shapeComboBox.valueProperty().addListener(obs -> {
            GameObject obj = displayingObject.get();
            GameShapeType val = shapeComboBox.getValue();
            if (obj != null && !obj.getShape().getType().equals(val)) {
                obj.setShapeType(val);
                redraw();
            }
        });

        viewOrderSpinner = new Spinner<>(Integer.MIN_VALUE, Integer.MAX_VALUE, 0, 1);
        viewOrderSpinner.setEditable(true);
        viewOrderSpinner.valueProperty().addListener(obs -> {
            GameObject obj = displayingObject.get();
            int val = viewOrderSpinner.getValue();
            if (obj != null && obj.getViewOrder() != val) {
                obj.setViewOrder(val);
            }
        });

        gridPane = new GridPane();
        gridPane.setVgap(10);
        gridPane.setHgap(10);
        gridPane.setVisible(false);
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

    public void redraw() {
        GameObject obj = displayingObject.get();

        if (obj == null) {
            gridPane.setVisible(false);
            return;
        }

        gridPane.setVisible(true);
        gridPane.getChildren().clear();
        gridPane.addRow(0, LabelFactory.makeLabel("Name"), nameField);
        gridPane.addRow(1, LabelFactory.makeLabel("Start time"), startTimeSpinner);
        gridPane.addRow(2, LabelFactory.makeLabel("Duration"), durationSpinner);
        gridPane.addRow(3, LabelFactory.makeLabel("View order"), viewOrderSpinner);
        gridPane.addRow(4, LabelFactory.makeLabel("Shape"), shapeComboBox);

        nameField.setText(obj.getName());
        startTimeSpinner.getEditor().setText(String.valueOf(obj.getStartTime()));
        durationSpinner.getEditor().setText(String.valueOf(obj.getDuration()));
        viewOrderSpinner.getEditor().setText(String.valueOf(obj.getViewOrder()));
        shapeComboBox.setValue(obj.getShape().getType());

        for (ShapeSetting setting : obj.getShape().getSettings()) {
            Label settingLabel = LabelFactory.makeLabel(setting.getName());
            Node settingNode = makeSettingNode(setting);
            gridPane.addRow(gridPane.getRowCount(), settingLabel, settingNode);
        }
    }

    private Node makeSettingNode(ShapeSetting setting) {
        if (setting instanceof ShapeStringSetting s) {
            TextField field = new TextField(s.getValue());
            field.textProperty().addListener(obs -> s.setValue(field.getText()));
            return field;
        } else if (setting instanceof ShapeDropdownSetting<?> s) {
            ComboBox<String> comboBox = new ComboBox<>();
            comboBox.setItems(FXCollections.observableList(List.of(s.getEnumNames())));
            comboBox.setValue(s.getValue().toString());
            comboBox.valueProperty().addListener(obs -> s.setValue(comboBox.getValue()));
            return comboBox;
        }
        throw new IllegalArgumentException("Unknown setting");
    }

    private static class LabelFactory {
        private static final HashMap<String, Label> cache = new HashMap<>();

        public static Label makeLabel(String text) {
            Label label = cache.get(text);
            if (label == null) {
                label = new Label(text);
                label.setTextFill(Theme.ON_BACKGROUND);
                cache.put(text, label);
            }
            return label;
        }
    }
}
