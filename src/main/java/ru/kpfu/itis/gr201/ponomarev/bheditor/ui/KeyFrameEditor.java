package ru.kpfu.itis.gr201.ponomarev.bheditor.ui;

import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
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
import ru.kpfu.itis.gr201.ponomarev.bheditor.util.DoubleStringConverter;
import ru.kpfu.itis.gr201.ponomarev.bheditor.util.Interpolators;
import ru.kpfu.itis.gr201.ponomarev.bheditor.util.InterpolatorsStringConverter;
import ru.kpfu.itis.gr201.ponomarev.bheditor.util.Theme;

public class KeyFrameEditor extends Pane {

    private final Spinner<Integer> timeSpinner;
    private final Spinner<Double> valueSpinner;
    private final ComboBox<Interpolators> interpolatorComboBox;

    private final ObjectProperty<KeyFrame> keyFrame;
    private final ObjectProperty<HittingObject> kfParent;

    private boolean listenToKeyFrameChanges = true;

    public KeyFrameEditor(ObjectProperty<KeyFrame> selectedKeyFrame, ObjectProperty<HittingObject> selectedObject) {
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
            KeyFrame kf = keyFrame.get();
            if (kf != null && (int) kf.getTime().toMillis() != timeSpinner.getValue()) {
                kf.getValues().stream().findFirst().ifPresent(kv -> changeKeyFrame(
                        selectedKeyFrame,
                        (double) kv.getEndValue(),
                        timeSpinner.getValue(),
                        Interpolators.byInterpolator(kv.getInterpolator())
                ));
            }
        });

        SpinnerValueFactory.DoubleSpinnerValueFactory valueFactory = new SpinnerValueFactory.DoubleSpinnerValueFactory(
                -Double.MAX_VALUE, Double.MAX_VALUE, 0.0
        );
        valueFactory.setConverter(new DoubleStringConverter());
        valueSpinner = new Spinner<>(valueFactory);
        valueSpinner.setEditable(true);
        valueSpinner.valueProperty().addListener(obs -> {
            KeyFrame kf = keyFrame.get();
            if (kf != null) {
                kf.getValues().stream().findFirst().ifPresent(kv -> {
                    if (Double.compare((double) kv.getEndValue(), valueSpinner.getValue()) != 0) {
                        changeKeyFrame(
                                selectedKeyFrame,
                                valueSpinner.getValue(),
                                (int) kf.getTime().toMillis(),
                                Interpolators.byInterpolator(kv.getInterpolator())
                        );
                    }
                });
            }
        });

        interpolatorComboBox = new ComboBox<>();
        interpolatorComboBox.setItems(FXCollections.observableArrayList(Interpolators.values()));
        interpolatorComboBox.setConverter(new InterpolatorsStringConverter());
        interpolatorComboBox.valueProperty().addListener(obs -> {
            KeyFrame kf = keyFrame.get();
            if (kf != null) {
                kf.getValues().stream().findFirst().ifPresent(kv -> {
                    if (!Interpolators.byInterpolator(kv.getInterpolator()).equals(interpolatorComboBox.getValue())) {
                        changeKeyFrame(
                                selectedKeyFrame,
                                (Double) kv.getEndValue(),
                                (int) kf.getTime().toMillis(),
                                interpolatorComboBox.getValue()
                        );
                    }
                });
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

    private void changeKeyFrame(ObjectProperty<KeyFrame> selectedKeyFrame, double value, int time, Interpolators interpolator) {
        listenToKeyFrameChanges = false;
        KeyFrame kf = keyFrame.get();
        HittingObject obj = kfParent.get();
        if (kf != null && obj != null) {
            KeyValue kv = kf.getValues().stream().findFirst().orElse(null);
            if (kv != null) {
                obj.removeKeyFrame(kf);
                kf = obj.addKeyFrame(
                        value,
                        time,
                        interpolator,
                        kf.getName().replaceAll("\\d", "")
                );
                keyFrame.unbind();
                keyFrame.set(kf);
                keyFrame.bind(selectedKeyFrame);
            }
        }
        listenToKeyFrameChanges = true;
    }

    private void redraw() {
        if (keyFrame.get() == null) {
            setVisible(false);
            return;
        }

        setVisible(true);

        timeSpinner.getEditor().setText(String.valueOf((int) keyFrame.get().getTime().toMillis()));

        keyFrame.get().getValues().stream().findFirst().ifPresent(kv -> {
            valueSpinner.getEditor().setText(String.valueOf((double) kv.getEndValue()));
            interpolatorComboBox.setValue(Interpolators.byInterpolator(kv.getInterpolator()));
        });
    }

    private Label makeLabel(String text) {
        Label label = new Label(text);
        label.setTextFill(Theme.ON_BACKGROUND);
        return label;
    }

    public KeyFrame getKeyFrame() {
        return keyFrame.get();
    }

    public ObjectProperty<KeyFrame> keyFrameProperty() {
        return keyFrame;
    }

    public void setKeyFrame(KeyFrame keyFrame) {
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
