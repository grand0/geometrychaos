package ru.kpfu.itis.gr201.ponomarev.bheditor.ui;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ObjectPropertyBase;
import javafx.collections.FXCollections;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import ru.kpfu.itis.gr201.ponomarev.bheditor.game.HittingObject;
import ru.kpfu.itis.gr201.ponomarev.bheditor.util.anim.ObjectKeyFrame;
import ru.kpfu.itis.gr201.ponomarev.bheditor.util.converter.DoubleStringConverter;
import ru.kpfu.itis.gr201.ponomarev.bheditor.util.InterpolatorType;
import ru.kpfu.itis.gr201.ponomarev.bheditor.util.converter.InterpolatorsStringConverter;
import ru.kpfu.itis.gr201.ponomarev.bheditor.util.Theme;

public class KeyFrameEditor extends Pane {

    private final Spinner<Integer> timeSpinner;
    private final Spinner<Double> valueSpinner;
    private final ComboBox<InterpolatorType> interpolatorComboBox;

    private final ObjectProperty<ObjectKeyFrame> keyFrame;
    private final ObjectProperty<HittingObject> kfParent;

    private boolean listenToKeyFrameChanges = true;

    public KeyFrameEditor(ObjectProperty<ObjectKeyFrame> selectedKeyFrame, ObjectProperty<HittingObject> selectedObject) {
        kfParent = new ObjectPropertyBase<>() {
            @Override
            public Object getBean() {
                return null;
            }

            @Override
            public String getName() {
                return "kfParent";
            }
        };
        kfParent.bind(selectedObject);

        keyFrame = new ObjectPropertyBase<>() {
            @Override
            public Object getBean() {
                return null;
            }

            @Override
            public String getName() {
                return "keyFrame";
            }
        };
        keyFrame.bind(selectedKeyFrame);
        keyFrame.addListener(obs -> {
            if (listenToKeyFrameChanges) {
                redraw();
            }
        });

        timeSpinner = new Spinner<>(0, Integer.MAX_VALUE, 0);
        timeSpinner.setEditable(true);
        timeSpinner.valueProperty().addListener(obs -> {
            ObjectKeyFrame kf = keyFrame.get();
            if (kf != null && kf.getTime() != timeSpinner.getValue()) {
                changeKeyFrame(
                        selectedKeyFrame,
                        kf.getEndValue(),
                        timeSpinner.getValue(),
                        kf.getInterpolatorType()
                );
            }
        });

        SpinnerValueFactory.DoubleSpinnerValueFactory valueFactory = new SpinnerValueFactory.DoubleSpinnerValueFactory(
                -Double.MAX_VALUE, Double.MAX_VALUE, 0.0
        );
        valueFactory.setConverter(new DoubleStringConverter());
        valueSpinner = new Spinner<>(valueFactory);
        valueSpinner.setEditable(true);
        valueSpinner.valueProperty().addListener(obs -> {
            ObjectKeyFrame kf = keyFrame.get();
            if (kf != null && !kf.getEndValue().equals(valueSpinner.getValue())) {
                changeKeyFrame(
                        selectedKeyFrame,
                        valueSpinner.getValue(),
                        kf.getTime(),
                        kf.getInterpolatorType()
                );
            }
        });

        interpolatorComboBox = new ComboBox<>();
        interpolatorComboBox.setItems(FXCollections.observableArrayList(InterpolatorType.values()));
        interpolatorComboBox.setConverter(new InterpolatorsStringConverter());
        interpolatorComboBox.valueProperty().addListener(obs -> {
            ObjectKeyFrame kf = keyFrame.get();
            if (kf != null && !kf.getInterpolatorType().equals(interpolatorComboBox.getValue())) {
                changeKeyFrame(
                        selectedKeyFrame,
                        kf.getEndValue(),
                        kf.getTime(),
                        interpolatorComboBox.getValue()
                );
            }
        });

        GridPane pane = new GridPane();
        pane.setHgap(10);
        pane.setVgap(10);
        pane.addRow(0, makeLabel("Time"), timeSpinner);
        pane.addRow(1, makeLabel("Value"), valueSpinner);
        pane.addRow(2, makeLabel("Interpolator"), interpolatorComboBox);

        getChildren().add(pane);

        redraw();
    }

    private void changeKeyFrame(ObjectProperty<ObjectKeyFrame> selectedKeyFrame, Object value, int time, InterpolatorType interpolator) {
        listenToKeyFrameChanges = false;
        ObjectKeyFrame kf = keyFrame.get();
        HittingObject obj = kfParent.get();
        if (kf != null && obj != null) {
            obj.removeKeyFrame(kf);
            kf = obj.addKeyFrame(
                    value,
                    time,
                    interpolator,
                    kf.getTag()
            );
            keyFrame.unbind();
            keyFrame.set(kf);
            keyFrame.bind(selectedKeyFrame);
        }
        listenToKeyFrameChanges = true;
    }

    private void redraw() {
        if (keyFrame.get() == null) {
            setVisible(false);
            return;
        }

        setVisible(true);

        timeSpinner.getEditor().setText(String.valueOf(keyFrame.get().getTime()));
        valueSpinner.getEditor().setText(String.valueOf(keyFrame.get().getEndValue()));
        interpolatorComboBox.setValue(keyFrame.get().getInterpolatorType());
    }

    private Label makeLabel(String text) {
        Label label = new Label(text);
        label.setTextFill(Theme.ON_BACKGROUND);
        return label;
    }

    public ObjectKeyFrame getKeyFrame() {
        return keyFrame.get();
    }

    public ObjectProperty<ObjectKeyFrame> keyFrameProperty() {
        return keyFrame;
    }

    public void setKeyFrame(ObjectKeyFrame keyFrame) {
        this.keyFrame.set(keyFrame);
    }

    public HittingObject getKfParent() {
        return kfParent.get();
    }

    public ObjectProperty<HittingObject> kfParentProperty() {
        return kfParent;
    }

    public void setKfParent(HittingObject kfParent) {
        this.kfParent.set(kfParent);
    }
}
