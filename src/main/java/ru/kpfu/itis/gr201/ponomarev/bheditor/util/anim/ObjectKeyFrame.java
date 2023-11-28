package ru.kpfu.itis.gr201.ponomarev.bheditor.util.anim;

import javafx.beans.property.*;
import javafx.beans.value.*;
import ru.kpfu.itis.gr201.ponomarev.bheditor.util.InterpolatorType;

import java.util.Objects;

public class ObjectKeyFrame extends ObjectPropertyBase<ObjectKeyFrame> {
    private final IntegerProperty time;
    private final StringProperty tag;
    private final WritableValue<?> target;
    private final ObjectProperty<Object> endValue;
    private final ObjectProperty<InterpolatorType> interpolatorType;

    private final KeyFrameType type;

    public <T> ObjectKeyFrame(int time, String tag, WritableValue<T> target, T endValue, InterpolatorType interpolator) {
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

        this.tag = new StringPropertyBase() {
            @Override
            public Object getBean() {
                return null;
            }

            @Override
            public String getName() {
                return "name";
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

        this.type =
                (target instanceof WritableNumberValue) ?
                    (target instanceof WritableIntegerValue) ? KeyFrameType.INTEGER
                    : (target instanceof WritableLongValue) ? KeyFrameType.LONG
                    : (target instanceof WritableFloatValue) ? KeyFrameType.FLOAT
                    : (target instanceof WritableDoubleValue) ? KeyFrameType.DOUBLE
                    : KeyFrameType.OBJECT
                : (target instanceof WritableBooleanValue) ? KeyFrameType.BOOLEAN
                : KeyFrameType.OBJECT;
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

    public String getTag() {
        return tag.get();
    }

    public StringProperty tagProperty() {
        return tag;
    }

    public void setTag(String tag) {
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
