package ru.kpfu.itis.gr201.ponomarev.bheditor.ui;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ObjectPropertyBase;
import javafx.collections.FXCollections;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import ru.kpfu.itis.gr201.ponomarev.bheditor.game.HittingObject;
import ru.kpfu.itis.gr201.ponomarev.bheditor.util.anim.KeyFrameType;
import ru.kpfu.itis.gr201.ponomarev.bheditor.util.anim.ObjectKeyFrame;
import ru.kpfu.itis.gr201.ponomarev.bheditor.util.InterpolatorType;
import ru.kpfu.itis.gr201.ponomarev.bheditor.util.converter.DoubleStringConverter;
import ru.kpfu.itis.gr201.ponomarev.bheditor.util.converter.InterpolatorsStringConverter;
import ru.kpfu.itis.gr201.ponomarev.bheditor.util.Theme;

import java.util.function.Consumer;

public class KeyFrameEditor extends Pane {

    private final GridPane gridPane;

    private final Spinner<Integer> timeSpinner;
    private final ComboBox<InterpolatorType> interpolatorComboBox;

    private final ObjectProperty<ObjectKeyFrame> keyFrame;
    private final ObjectProperty<HittingObject> kfParent;

    private final Consumer<Object> keyFrameChanger;

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

        keyFrameChanger = (val) -> {
            changeKeyFrame(
                    selectedKeyFrame,
                    val,
                    keyFrame.get().getTime(),
                    keyFrame.get().getInterpolatorType()
            );
        };

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

        gridPane = new GridPane();
        gridPane.setHgap(10);
        gridPane.setVgap(10);

        getChildren().add(gridPane);

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
        Node valueSelector = makeValueSelector(keyFrame.get().getType(), keyFrame.get().getEndValue());
        interpolatorComboBox.setValue(keyFrame.get().getInterpolatorType());

        gridPane.getChildren().clear();
        gridPane.addRow(0, makeLabel("Time"), timeSpinner);
        gridPane.addRow(1, makeLabel("Value"), valueSelector);
        gridPane.addRow(2, makeLabel("Interpolator"), interpolatorComboBox);
    }

    private Node makeValueSelector(KeyFrameType type, Object value) {
        switch (type) {
            case BOOLEAN -> {
                CheckBox checkBox = new CheckBox();
                checkBox.setSelected((Boolean) value);
                checkBox.selectedProperty().addListener(obs -> {
                    ObjectKeyFrame kf = keyFrame.get();
                    if (kf != null && !kf.getEndValue().equals(checkBox.isSelected())) {
                        keyFrameChanger.accept(checkBox.isSelected());
                    }
                });
                return checkBox;
            }
            case INTEGER -> {
                Spinner<Integer> spinner = new Spinner<>(0, Integer.MAX_VALUE, (Integer) value);
                spinner.setEditable(true);
                spinner.valueProperty().addListener(obs -> {
                    ObjectKeyFrame kf = keyFrame.get();
                    if (kf != null && !kf.getEndValue().equals(spinner.getValue())) {
                        keyFrameChanger.accept(spinner.getValue());
                    }
                });
                return spinner;
            }
            case LONG -> {
                SpinnerValueFactory<Long> factory = new SpinnerValueFactory<>() {
                    @Override
                    public void decrement(int steps) {
                        setValue(getValue() - steps);
                    }

                    @Override
                    public void increment(int steps) {
                        setValue(getValue() + steps);
                    }
                };
                factory.setValue((Long) value);
                long min = 0;
                long max = Long.MAX_VALUE;
                factory.valueProperty().addListener((o, oldV, newV) -> {
                    if (newV < min) {
                        factory.setValue(min);
                    } else if (newV > max) { // maybe in future max won't be MAX_VALUE
                        factory.setValue(max);
                    }
                });
                Spinner<Long> spinner = new Spinner<>(factory);
                spinner.setEditable(true);
                spinner.valueProperty().addListener(obs -> {
                    ObjectKeyFrame kf = keyFrame.get();
                    if (kf != null && !kf.getEndValue().equals(spinner.getValue())) {
                        keyFrameChanger.accept(spinner.getValue());
                    }
                });
                return spinner;
            }
            case FLOAT -> {
                SpinnerValueFactory<Float> factory = new SpinnerValueFactory<>() {
                    @Override
                    public void decrement(int steps) {
                        setValue(getValue() - steps);
                    }

                    @Override
                    public void increment(int steps) {
                        setValue(getValue() + steps);
                    }
                };
                factory.setValue((Float) value);
                float min = -Float.MAX_VALUE;
                float max = Float.MAX_VALUE;
                factory.valueProperty().addListener((o, oldV, newV) -> {
                    if (newV < min) {
                        factory.setValue(min);
                    } else if (newV > max) { // maybe in future max won't be MAX_VALUE
                        factory.setValue(max);
                    }
                });
                Spinner<Float> spinner = new Spinner<>(factory);
                spinner.setEditable(true);
                spinner.valueProperty().addListener(obs -> {
                    ObjectKeyFrame kf = keyFrame.get();
                    if (kf != null && !kf.getEndValue().equals(spinner.getValue())) {
                        keyFrameChanger.accept(spinner.getValue());
                    }
                });
                return spinner;
            }
            case DOUBLE -> {
                SpinnerValueFactory.DoubleSpinnerValueFactory valueFactory = new SpinnerValueFactory.DoubleSpinnerValueFactory(
                        -Double.MAX_VALUE, Double.MAX_VALUE, (Double) value
                );
                valueFactory.setConverter(new DoubleStringConverter());
                Spinner<Double> spinner = new Spinner<>(valueFactory);
                spinner.setEditable(true);
                spinner.valueProperty().addListener(obs -> {
                    ObjectKeyFrame kf = keyFrame.get();
                    if (kf != null && !kf.getEndValue().equals(spinner.getValue())) {
                        keyFrameChanger.accept(spinner.getValue());
                    }
                });
                return spinner;
            }
            case OBJECT -> {
                // TODO: maybe remove object keyframe type and instead make more specific ones?
                throw new RuntimeException("Editor for OBJECT key frames is not supported yet.");
            }
        }
        throw new RuntimeException("Unknown type.");
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
