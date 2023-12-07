package ru.kpfu.itis.gr201.ponomarev.bheditor.ui;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ObjectPropertyBase;
import javafx.collections.FXCollections;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import javafx.util.StringConverter;
import ru.kpfu.itis.gr201.ponomarev.bheditor.game.HittingObject;
import ru.kpfu.itis.gr201.ponomarev.bheditor.anim.KeyFrameType;
import ru.kpfu.itis.gr201.ponomarev.bheditor.anim.ObjectKeyFrame;
import ru.kpfu.itis.gr201.ponomarev.bheditor.util.InterpolatorType;
import ru.kpfu.itis.gr201.ponomarev.bheditor.util.converter.DoubleStringConverter;
import ru.kpfu.itis.gr201.ponomarev.bheditor.util.converter.InterpolatorsStringConverter;
import ru.kpfu.itis.gr201.ponomarev.bheditor.util.Theme;
import ru.kpfu.itis.gr201.ponomarev.bheditor.util.randomizer.DoubleValueRandomizer;
import ru.kpfu.itis.gr201.ponomarev.bheditor.util.randomizer.ValueRandomizer;
import ru.kpfu.itis.gr201.ponomarev.bheditor.util.randomizer.impl.DiscreteDoubleValueRandomizer;
import ru.kpfu.itis.gr201.ponomarev.bheditor.util.randomizer.impl.LinearDoubleValueRandomizer;

import java.util.ArrayList;
import java.util.function.Consumer;

public class KeyFrameEditor extends Pane {

    private final GridPane gridPane;

    private final Spinner<Integer> timeSpinner;
    private final ComboBox<InterpolatorType> interpolatorComboBox;

    private final ObjectProperty<ObjectKeyFrame> keyFrame;
    private final ObjectProperty<HittingObject> kfParent;

    private final Consumer<Object> keyFrameValueChanger;
    private final Consumer<ValueRandomizer> keyFrameRandomizerChanger;

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
                        kf.getInterpolatorType(),
                        kf.getRandomizer()
                );
            }
        });

        keyFrameValueChanger = (val) -> {
            changeKeyFrame(
                    selectedKeyFrame,
                    val,
                    keyFrame.get().getTime(),
                    keyFrame.get().getInterpolatorType(),
                    keyFrame.get().getRandomizer()
            );
        };

        keyFrameRandomizerChanger = (randomizer) -> {
            changeKeyFrame(
                    selectedKeyFrame,
                    keyFrame.get().getEndValue(),
                    keyFrame.get().getTime(),
                    keyFrame.get().getInterpolatorType(),
                    randomizer
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
                        interpolatorComboBox.getValue(),
                        kf.getRandomizer()
                );
            }
        });

        gridPane = new GridPane();
        gridPane.setHgap(10);
        gridPane.setVgap(10);

        getChildren().add(gridPane);

        redraw();
    }

    private void changeKeyFrame(ObjectProperty<ObjectKeyFrame> selectedKeyFrame, Object value, int time, InterpolatorType interpolator, ValueRandomizer randomizer) {
        listenToKeyFrameChanges = false;
        ObjectKeyFrame kf = keyFrame.get();
        HittingObject obj = kfParent.get();
        if (kf != null && obj != null) {
            obj.removeKeyFrame(kf);
            kf = obj.addKeyFrame(
                    value,
                    time,
                    interpolator,
                    kf.getTag(),
                    randomizer
            );
            keyFrame.unbind();
            keyFrame.set(kf);
            keyFrame.bind(selectedKeyFrame);
        }
        listenToKeyFrameChanges = true;
        redraw();
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

        boolean showRandomizer = true;
        ComboBox<ValueRandomizer> randomizerComboBox = new ComboBox<>();
        ArrayList<Node> randomizerPropertiesNodes = new ArrayList<>();
        ValueRandomizer originalRandomizer = keyFrame.get().getRandomizer();
        switch (keyFrame.get().getType()) {
            case DOUBLE -> {
                DoubleValueRandomizer originalDoubleRandomizer = (DoubleValueRandomizer) originalRandomizer;
                LinearDoubleValueRandomizer linear = null;
                DiscreteDoubleValueRandomizer discrete = null;
                ValueRandomizer valueToSet = null;
                if (originalDoubleRandomizer instanceof LinearDoubleValueRandomizer) {
                    linear = (LinearDoubleValueRandomizer) originalDoubleRandomizer;
                    valueToSet = linear;
                    Spinner<Double> startValueSpinner = makeDoubleSpinner(linear.getStartValue());
                    startValueSpinner.valueProperty().addListener(obs -> {
                        keyFrameRandomizerChanger.accept(
                                new LinearDoubleValueRandomizer(startValueSpinner.getValue(), originalDoubleRandomizer.getEndValue())
                        );
                    });
                    randomizerPropertiesNodes.add(startValueSpinner);
                    Spinner<Double> endValueSpinner = makeDoubleSpinner(linear.getEndValue());
                    endValueSpinner.valueProperty().addListener(obs -> {
                        keyFrameRandomizerChanger.accept(
                                new LinearDoubleValueRandomizer(originalDoubleRandomizer.getStartValue(), endValueSpinner.getValue())
                        );
                    });
                    randomizerPropertiesNodes.add(endValueSpinner);
                } else if (originalDoubleRandomizer instanceof DiscreteDoubleValueRandomizer) {
                    discrete = (DiscreteDoubleValueRandomizer) originalDoubleRandomizer;
                    valueToSet = discrete;
                    Spinner<Double> startValueSpinner = makeDoubleSpinner(discrete.getStartValue());
                    DiscreteDoubleValueRandomizer finalDiscrete = discrete;
                    startValueSpinner.valueProperty().addListener(obs -> {
                        keyFrameRandomizerChanger.accept(
                                new DiscreteDoubleValueRandomizer(startValueSpinner.getValue(), originalDoubleRandomizer.getEndValue(), finalDiscrete.getStep())
                        );
                    });
                    randomizerPropertiesNodes.add(startValueSpinner);
                    Spinner<Double> endValueSpinner = makeDoubleSpinner(discrete.getEndValue());
                    endValueSpinner.valueProperty().addListener(obs -> {
                        keyFrameRandomizerChanger.accept(
                                new DiscreteDoubleValueRandomizer(originalDoubleRandomizer.getStartValue(), endValueSpinner.getValue(), finalDiscrete.getStep())
                        );
                    });
                    randomizerPropertiesNodes.add(endValueSpinner);
                    Spinner<Double> stepSpinner = makeDoubleSpinner(discrete.getStep());
                    stepSpinner.valueProperty().addListener(obs -> {
                        keyFrameRandomizerChanger.accept(
                                new DiscreteDoubleValueRandomizer(originalDoubleRandomizer.getStartValue(), originalDoubleRandomizer.getEndValue(), stepSpinner.getValue())
                        );
                    });
                    randomizerPropertiesNodes.add(stepSpinner);
                }
                if (linear == null) {
                    linear = new LinearDoubleValueRandomizer(0, 1);
                }
                if (discrete == null) {
                    discrete = new DiscreteDoubleValueRandomizer(0, 1, 0.1);
                }
                randomizerComboBox.setItems(FXCollections.observableArrayList(
                        null,
                        linear,
                        discrete
                ));
                randomizerComboBox.setValue(valueToSet);
                randomizerComboBox.setConverter(new StringConverter<>() {
                    @Override
                    public String toString(ValueRandomizer object) {
                        return object == null ? "No randomizer" : object.getType().getName();
                    }

                    @Override
                    public ValueRandomizer fromString(String string) {
                        // we can skip this because combobox isn't editable
                        throw new RuntimeException("Not implemented");
                    }
                });
            }
            default -> showRandomizer = false;
        }

        gridPane.getChildren().clear();
        gridPane.addRow(0, makeLabel("Time"), timeSpinner);
        gridPane.addRow(1, makeLabel("Value"), valueSelector);
        gridPane.addRow(2, makeLabel("Interpolator"), interpolatorComboBox);
        if (showRandomizer) {
            randomizerComboBox.valueProperty().addListener(obs -> {
                keyFrameRandomizerChanger.accept(randomizerComboBox.getValue());
            });
            gridPane.addRow(3, makeLabel("Randomizer"), randomizerComboBox);
            for (Node node : randomizerPropertiesNodes) {
                gridPane.addRow(4, node);
            }
        }
    }

    private Node makeValueSelector(KeyFrameType type, Object value) {
        switch (type) {
            case BOOLEAN -> {
                CheckBox checkBox = new CheckBox();
                checkBox.setSelected((Boolean) value);
                checkBox.selectedProperty().addListener(obs -> {
                    ObjectKeyFrame kf = keyFrame.get();
                    if (kf != null && !kf.getEndValue().equals(checkBox.isSelected())) {
                        keyFrameValueChanger.accept(checkBox.isSelected());
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
                        keyFrameValueChanger.accept(spinner.getValue());
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
                        keyFrameValueChanger.accept(spinner.getValue());
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
                        keyFrameValueChanger.accept(spinner.getValue());
                    }
                });
                return spinner;
            }
            case DOUBLE -> {
                Spinner<Double> spinner = makeDoubleSpinner((Double) value);
                spinner.valueProperty().addListener(obs -> {
                    ObjectKeyFrame kf = keyFrame.get();
                    if (kf != null && !kf.getEndValue().equals(spinner.getValue())) {
                        keyFrameValueChanger.accept(spinner.getValue());
                    }
                });
                return spinner;
            }
        }
        throw new RuntimeException("Unknown type.");
    }

    private Spinner<Double> makeDoubleSpinner(double initialValue) {
        SpinnerValueFactory.DoubleSpinnerValueFactory valueFactory = new SpinnerValueFactory.DoubleSpinnerValueFactory(
                -Double.MAX_VALUE, Double.MAX_VALUE, initialValue
        );
        valueFactory.setConverter(new DoubleStringConverter());
        Spinner<Double> spinner = new Spinner<>(valueFactory);
        spinner.setEditable(true);
        return spinner;
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
