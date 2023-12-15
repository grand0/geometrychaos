package ru.kpfu.itis.gr201.ponomarev.geometrychaos.editor.anim;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.IntegerPropertyBase;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ObjectPropertyBase;
import javafx.beans.value.*;
import ru.kpfu.itis.gr201.ponomarev.geometrychaos.util.InterpolatorType;
import ru.kpfu.itis.gr201.ponomarev.geometrychaos.util.anim.KeyFrameTag;
import ru.kpfu.itis.gr201.ponomarev.geometrychaos.util.anim.KeyFrameType;
import ru.kpfu.itis.gr201.ponomarev.geometrychaos.util.randomizer.ValueRandomizer;

import java.util.Objects;

public class ObjectKeyFrame extends ObjectPropertyBase<ObjectKeyFrame> {
    private final IntegerProperty time;
    private final ObjectProperty<KeyFrameTag> tag;
    private final WritableValue<?> target;
    private final ObjectProperty<Object> endValue;
    private final ObjectProperty<InterpolatorType> interpolatorType;
    private final ObjectProperty<ValueRandomizer> randomizer;
    private final KeyFrameType type;

    public <T> ObjectKeyFrame(int time, KeyFrameTag tag, WritableValue<T> target, T endValue, InterpolatorType interpolator, ValueRandomizer randomizer) {
        this.target = target;

        this.time = new IntegerPropertyBase() {
            @Override
            public Object getBean() {
                return null;
            }

            @Override
            public String getName() {
                return "time";
            }
        };
        this.time.set(time);

        this.tag = new ObjectPropertyBase<>() {
            @Override
            public Object getBean() {
                return null;
            }

            @Override
            public String getName() {
                return "tag";
            }
        };
        this.tag.set(tag);

        this.endValue = new ObjectPropertyBase<>() {
            @Override
            public Object getBean() {
                return null;
            }

            @Override
            public String getName() {
                return "endValue";
            }
        };
        this.endValue.set(endValue);

        this.interpolatorType = new ObjectPropertyBase<>() {
            @Override
            public Object getBean() {
                return null;
            }

            @Override
            public String getName() {
                return "interpolator";
            }
        };
        this.interpolatorType.set(interpolator);

        this.randomizer = new ObjectPropertyBase<>() {
            @Override
            public Object getBean() {
                return null;
            }

            @Override
            public String getName() {
                return "randomizer";
            }
        };
        this.randomizer.set(randomizer);

        this.type =
                (target instanceof WritableNumberValue) ?
                    (target instanceof WritableIntegerValue) ? KeyFrameType.INTEGER
                    : (target instanceof WritableLongValue) ? KeyFrameType.LONG
                    : (target instanceof WritableFloatValue) ? KeyFrameType.FLOAT
                    : (target instanceof WritableDoubleValue) ? KeyFrameType.DOUBLE
                    : null
                : (target instanceof WritableBooleanValue) ? KeyFrameType.BOOLEAN
                : null;
        if (this.type == null) {
            throw new IllegalArgumentException("Unsupported target type");
        }
    }

    public void randomize() {
        if (getRandomizer() != null) {
            setEndValue(getRandomizer().randomize());
        }
    }

    @Override
    public Object getBean() {
        return null;
    }

    @Override
    public String getName() {
        return "";
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

    public KeyFrameTag getTag() {
        return tag.get();
    }

    public ObjectProperty<KeyFrameTag> tagProperty() {
        return tag;
    }

    public void setTag(KeyFrameTag tag) {
        this.tag.set(tag);
    }

    public WritableValue<?> getTarget() {
        return target;
    }

    public Object getEndValue() {
        return endValue.get();
    }

    public ObjectProperty<Object> endValueProperty() {
        return endValue;
    }

    public void setEndValue(Object endValue) {
        this.endValue.set(endValue);
    }

    public InterpolatorType getInterpolatorType() {
        return interpolatorType.get();
    }

    public ObjectProperty<InterpolatorType> interpolatorTypeProperty() {
        return interpolatorType;
    }

    public void setInterpolatorType(InterpolatorType interpolatorType) {
        this.interpolatorType.set(interpolatorType);
    }

    public ValueRandomizer getRandomizer() {
        return randomizer.get();
    }

    public ObjectProperty<ValueRandomizer> randomizerProperty() {
        return randomizer;
    }

    public void setRandomizer(ValueRandomizer randomizer) {
        this.randomizer.set(randomizer);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ObjectKeyFrame that = (ObjectKeyFrame) o;
        return Objects.equals(getTime(), that.getTime()) && Objects.equals(getTag(), that.getTag()) && Objects.equals(getTarget(), that.getTarget()) && Objects.equals(getEndValue(), that.getEndValue()) && Objects.equals(getInterpolatorType(), that.getInterpolatorType());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getTime(), getTag(), getTarget(), getEndValue(), getInterpolatorType());
    }

    public KeyFrameType getType() {
        return type;
    }
}
