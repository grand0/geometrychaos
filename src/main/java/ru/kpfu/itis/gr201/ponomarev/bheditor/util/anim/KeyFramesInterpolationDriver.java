package ru.kpfu.itis.gr201.ponomarev.bheditor.util.anim;

import javafx.beans.InvalidationListener;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.IntegerPropertyBase;
import javafx.beans.value.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class KeyFramesInterpolationDriver {
    private final IntegerProperty time;
    private final ObservableList<ObjectKeyFrame> keyFrames;

    public KeyFramesInterpolationDriver() {
        this.time = new IntegerPropertyBase() {
            @Override
            protected void invalidated() {
                super.invalidated();
                updateValues();
            }

            @Override
            public Object getBean() {
                return null;
            }

            @Override
            public String getName() {
                return "time";
            }
        };

        this.keyFrames = FXCollections.observableArrayList();
        keyFrames.addListener((InvalidationListener) obs -> {
            updateValues();
        });
    }

    private void updateValues() {
        Map<? extends WritableValue<?>, List<ObjectKeyFrame>> propertyGroups = getKeyFrames().stream()
                .collect(Collectors.groupingBy(ObjectKeyFrame::getTarget));
        for (Map.Entry<? extends WritableValue<?>, List<ObjectKeyFrame>> group : propertyGroups.entrySet()) {
        	if (group.getValue().size() == 1) {
                ObjectKeyFrame kf = group.getValue().get(0);
                setValue(group.getKey(), kf.getEndValue(), kf.getType());
            } else if (group.getValue().size() > 1) {
                List<ObjectKeyFrame> kfs = group.getValue();
                kfs.sort(Comparator.comparingInt(ObjectKeyFrame::getTime));
                if (getTime() <= kfs.get(0).getTime()) {
                    ObjectKeyFrame kf = kfs.get(0);
                    setValue(group.getKey(), kf.getEndValue(), kf.getType());
                } else if (getTime() >= kfs.get(kfs.size() - 1).getTime()) {
                    ObjectKeyFrame kf = kfs.get(kfs.size() - 1);
                    setValue(group.getKey(), kf.getEndValue(), kf.getType());
                } else {
                    for (int i = 1; i < kfs.size(); i++) {
                        if (getTime() <= kfs.get(i).getTime()) {
                            ObjectKeyFrame start = kfs.get(i - 1);
                            ObjectKeyFrame end = kfs.get(i);
                            int duration = end.getTime() - start.getTime();
                            int passed = getTime() - start.getTime();
                            double frac = (double) passed / duration;
                            Object interpolated = end.getInterpolatorType().getInterpolator()
                                    .interpolate(start.getEndValue(), end.getEndValue(), frac);
                            setValue(group.getKey(), interpolated, end.getType());
                            break;
                        }
                    }
                }
            }
        }
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private void setValue(WritableValue<?> writable, Object value, KeyFrameType type) {
        switch (type) {
            case BOOLEAN -> ((WritableBooleanValue) writable).set((Boolean) value);
            case INTEGER -> ((WritableIntegerValue) writable).set((Integer) value);
            case LONG -> ((WritableLongValue) writable).set((Long) value);
            case FLOAT -> ((WritableFloatValue) writable).set((Float) value);
            case DOUBLE -> ((WritableDoubleValue) writable).set((Double) value);
            case OBJECT -> ((WritableObjectValue) writable).set(value);
        }
    }

    public int getTime() {
        return time.get();
    }

    public IntegerProperty timeProperty() {
        return time;
    }

    public void setTime(int time) {
        this.time.set(time);
    }

    public ObservableList<ObjectKeyFrame> getKeyFrames() {
        return keyFrames;
    }
}
