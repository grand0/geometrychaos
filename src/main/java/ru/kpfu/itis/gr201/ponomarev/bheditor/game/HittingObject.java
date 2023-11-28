package ru.kpfu.itis.gr201.ponomarev.bheditor.game;

import javafx.beans.property.*;
import javafx.beans.value.WritableValue;
import javafx.collections.ObservableList;
import ru.kpfu.itis.gr201.ponomarev.bheditor.ui.ObjectsTimeline;
import ru.kpfu.itis.gr201.ponomarev.bheditor.util.InterpolatorType;
import ru.kpfu.itis.gr201.ponomarev.bheditor.util.anim.KeyFramesInterpolationDriver;
import ru.kpfu.itis.gr201.ponomarev.bheditor.util.anim.ObjectKeyFrame;

import java.util.Objects;
import java.util.Optional;

public class HittingObject extends ObjectPropertyBase<HittingObject> {

    public static final String POSITION_X_KEYFRAME_TAG = "positionX";
    public static final String POSITION_Y_KEYFRAME_TAG = "positionY";
    public static final String SCALE_X_KEYFRAME_TAG = "scaleX";
    public static final String SCALE_Y_KEYFRAME_TAG = "scaleY";
    public static final String ROTATION_KEYFRAME_TAG = "rotation";
    public static final String PIVOT_X_KEYFRAME_TAG = "pivotX";
    public static final String PIVOT_Y_KEYFRAME_TAG = "pivotY";

    public static final String[] KEYFRAME_TAGS = new String[] {
            POSITION_X_KEYFRAME_TAG,
            POSITION_Y_KEYFRAME_TAG,
            SCALE_X_KEYFRAME_TAG,
            SCALE_Y_KEYFRAME_TAG,
            ROTATION_KEYFRAME_TAG,
            PIVOT_X_KEYFRAME_TAG,
            PIVOT_Y_KEYFRAME_TAG,
    };

    private final StringProperty name;
    private final IntegerProperty startTime;
    private final IntegerProperty duration;
    private final IntegerProperty timelineLayer;
    private final KeyFramesInterpolationDriver interpolationDriver;
    private final ObjectProperty<Shape> shape;
    private final BooleanProperty isHelper;

    private final IntegerProperty time;
    private final DoubleProperty positionX;
    private final DoubleProperty positionY;
    private final DoubleProperty scaleX;
    private final DoubleProperty scaleY;
    private final DoubleProperty rotation;
    private final DoubleProperty pivotX;
    private final DoubleProperty pivotY;

    private boolean changedKeyFrames = false;

    public HittingObject(String name, int startTime, int duration, int timelineLayer) {
        this.name = new StringPropertyBase() {
            @Override
            protected void invalidated() {
                super.invalidated();
                HittingObject.this.fireValueChangedEvent();
            }

            @Override
            public Object getBean() {
                return null;
            }

            @Override
            public String getName() {
                return "name";
            }
        };
        this.name.set(name);

        this.startTime = new IntegerPropertyBase() {
            @Override
            protected void invalidated() {
                super.invalidated();
                HittingObject.this.fireValueChangedEvent();
            }

            @Override
            public Object getBean() {
                return null;
            }

            @Override
            public String getName() {
                return "startTime";
            }
        };
        this.startTime.set(startTime);

        this.duration = new IntegerPropertyBase() {
            @Override
            protected void invalidated() {
                super.invalidated();
                HittingObject.this.fireValueChangedEvent();
            }

            @Override
            public Object getBean() {
                return null;
            }

            @Override
            public String getName() {
                return "duration";
            }
        };
        this.duration.set(duration);

        this.timelineLayer = new IntegerPropertyBase() {
            @Override
            protected void invalidated() {
                super.invalidated();
                HittingObject.this.fireValueChangedEvent();
            }

            @Override
            public Object getBean() {
                return null;
            }

            @Override
            public String getName() {
                return "timelineLayer";
            }
        };
        this.timelineLayer.set(timelineLayer);

        this.interpolationDriver = new KeyFramesInterpolationDriver();

        this.shape = new ObjectPropertyBase<>() {
            @Override
            protected void invalidated() {
                super.invalidated();
                HittingObject.this.fireValueChangedEvent();
            }

            @Override
            public Object getBean() {
                return null;
            }

            @Override
            public String getName() {
                return "shape";
            }
        };
        this.shape.set(Shape.SQUARE);

        this.isHelper = new BooleanPropertyBase() {
            @Override
            protected void invalidated() {
                super.invalidated();
                HittingObject.this.fireValueChangedEvent();
            }

            @Override
            public Object getBean() {
                return null;
            }

            @Override
            public String getName() {
                return "isHelper";
            }
        };

        this.time = new IntegerPropertyBase() {
            @Override
            protected void invalidated() {
                super.invalidated();
                HittingObject.this.fireValueChangedEvent();
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

        this.positionX = makeDoublePropertyWithName("positionX");
        setPositionX(0.0);

        this.positionY = makeDoublePropertyWithName("positionY");
        setPositionY(0.0);

        this.scaleX = makeDoublePropertyWithName("scaleX");
        setScaleX(1.0);

        this.scaleY = makeDoublePropertyWithName("scaleY");
        setScaleY(1.0);

        this.rotation = makeDoublePropertyWithName("rotation");
        setRotation(0.0);

        this.pivotX = makeDoublePropertyWithName("pivotX");
        setPivotX(0.0);

        this.pivotY = makeDoublePropertyWithName("pivotY");
        setPivotY(0.0);
    }

    private DoublePropertyBase makeDoublePropertyWithName(String name) {
        return new DoublePropertyBase() {
            @Override
            protected void invalidated() {
                super.invalidated();
                HittingObject.this.fireValueChangedEvent();
            }

            @Override
            public Object getBean() {
                return null;
            }

            @Override
            public String getName() {
                return name;
            }
        };
    }

    public void initStartKeyFrames() {
        for (String tag : KEYFRAME_TAGS) {
            addKeyFrame(
                    getDefaultValueForTag(tag),
                    0,
                    InterpolatorType.INSTANT,
                    tag
            );
        }
    }

    @Override
    protected void fireValueChangedEvent() {
        super.fireValueChangedEvent();
        if (time != null) {
            if (changedKeyFrames || getTime() != interpolationDriver.getTime()) {
                changedKeyFrames = false;
                interpolationDriver.setTime(getTime());
            }
        }
    }

    public Optional<ObjectKeyFrame> getKeyFrame(int time, String tag) {
        return interpolationDriver.getKeyFrames()
                .stream()
                .filter(
                        kf -> kf.getTag().equals(tag) && kf.getTime() == time
                )
                .findFirst();
    }

    public ObjectKeyFrame addKeyFrame(Object value, int time, InterpolatorType interpolator, String tag) {
        WritableValue<?> prop = null;
        switch (tag) {
            case POSITION_X_KEYFRAME_TAG -> prop = positionX;
            case POSITION_Y_KEYFRAME_TAG -> prop = positionY;
            case SCALE_X_KEYFRAME_TAG -> prop = scaleX;
            case SCALE_Y_KEYFRAME_TAG -> prop = scaleY;
            case ROTATION_KEYFRAME_TAG -> prop = rotation;
            case PIVOT_X_KEYFRAME_TAG -> prop = pivotX;
            case PIVOT_Y_KEYFRAME_TAG -> prop = pivotY;
        }
        if (prop == null) {
            throw new IllegalArgumentException("Unknown property");
        }
        return addKeyFrame(value, time, interpolator, tag, prop);
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private ObjectKeyFrame addKeyFrame(Object value, int time, InterpolatorType interpolator, String tag, WritableValue property) {
        getKeyFrame(time, tag).ifPresent(kf -> interpolationDriver.getKeyFrames().remove(kf));
        ObjectKeyFrame kf = new ObjectKeyFrame(
                time,
                tag,
                property,
                value,
                interpolator
        );
        addKeyFrame(kf);
        return kf;
    }

    public void addKeyFrame(ObjectKeyFrame kf) {
        interpolationDriver.getKeyFrames().add(kf);
        changedKeyFrames = true;
        fireValueChangedEvent();
    }

    public void removeKeyFrame(ObjectKeyFrame kf) {
        interpolationDriver.getKeyFrames().remove(kf);
        changedKeyFrames = true;
        fireValueChangedEvent();
    }

    public static Object getDefaultValueForTag(String tag) {
        switch (tag) {
            case POSITION_X_KEYFRAME_TAG,
                    POSITION_Y_KEYFRAME_TAG,
                    ROTATION_KEYFRAME_TAG,
                    PIVOT_X_KEYFRAME_TAG,
                    PIVOT_Y_KEYFRAME_TAG -> {
                return 0.0;
            }
            case SCALE_X_KEYFRAME_TAG,
                    SCALE_Y_KEYFRAME_TAG -> {
                return 1.0;
            }
        }
        throw new RuntimeException("Unknown tag.");
    }

    public ObservableList<ObjectKeyFrame> getKeyFrames() {
        return interpolationDriver.getKeyFrames();
    }

    public boolean isVisible(int time) {
        return time >= getStartTime() && time <= getEndTime();
    }

    @Override
    public Object getBean() {
        return null;
    }

    @Override
    public String getName() {
        return name.get();
    }

    public StringProperty nameProperty() {
        return name;
    }

    public void setName(String name) {
        this.name.set(name);
    }

    public int getStartTime() {
        return startTime.get();
    }

    public IntegerProperty startTimeProperty() {
        return startTime;
    }

    public void setStartTime(int startTime) {
        this.startTime.set(startTime);
    }

    public int getDuration() {
        return duration.get();
    }

    public IntegerProperty durationProperty() {
        return duration;
    }

    public void setDuration(int duration) {
        this.duration.set(duration);
    }

    public int getEndTime() {
        return getStartTime() + getDuration();
    }

    public int getTimelineLayer() {
        return timelineLayer.get();
    }

    public IntegerProperty timelineLayerProperty() {
        return timelineLayer;
    }

    public void setTimelineLayer(int timelineLayer) {
        if (timelineLayer < 0) timelineLayer = 0;
        else if (timelineLayer >= ObjectsTimeline.LAYERS_COUNT) timelineLayer = ObjectsTimeline.LAYERS_COUNT - 1;
        this.timelineLayer.set(timelineLayer);
    }

    public KeyFramesInterpolationDriver getInterpolationDriver() {
        return interpolationDriver;
    }

    public Shape getShape() {
        return shape.get();
    }

    public ObjectProperty<Shape> shapeProperty() {
        return shape;
    }

    public void setShape(Shape shape) {
        this.shape.set(shape);
    }

    public boolean isHelper() {
        return isHelper.get();
    }

    public BooleanProperty isHelperProperty() {
        return isHelper;
    }

    public void setIsHelper(boolean isHelper) {
        this.isHelper.set(isHelper);
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

    public double getPositionX() {
        return positionX.get();
    }

    public DoubleProperty positionXProperty() {
        return positionX;
    }

    public void setPositionX(double positionX) {
        this.positionX.set(positionX);
    }

    public double getPositionY() {
        return positionY.get();
    }

    public DoubleProperty positionYProperty() {
        return positionY;
    }

    public void setPositionY(double positionY) {
        this.positionY.set(positionY);
    }

    public double getScaleX() {
        return scaleX.get();
    }

    public DoubleProperty scaleXProperty() {
        return scaleX;
    }

    public void setScaleX(double scaleX) {
        this.scaleX.set(scaleX);
    }

    public double getScaleY() {
        return scaleY.get();
    }

    public DoubleProperty scaleYProperty() {
        return scaleY;
    }

    public void setScaleY(double scaleY) {
        this.scaleY.set(scaleY);
    }

    public double getRotation() {
        return rotation.get();
    }

    public DoubleProperty rotationProperty() {
        return rotation;
    }

    public void setRotation(double rotation) {
        this.rotation.set(rotation);
    }

    public double getPivotX() {
        return pivotX.get();
    }

    public DoubleProperty pivotXProperty() {
        return pivotX;
    }

    public void setPivotX(double pivotX) {
        this.pivotX.set(pivotX);
    }

    public double getPivotY() {
        return pivotY.get();
    }

    public DoubleProperty pivotYProperty() {
        return pivotY;
    }

    public void setPivotY(double pivotY) {
        this.pivotY.set(pivotY);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        HittingObject that = (HittingObject) o;
        return changedKeyFrames == that.changedKeyFrames && Objects.equals(getName(), that.getName()) && Objects.equals(getStartTime(), that.getStartTime()) && Objects.equals(getDuration(), that.getDuration()) && Objects.equals(getTimelineLayer(), that.getTimelineLayer()) && Objects.equals(getInterpolationDriver(), that.getInterpolationDriver()) && Objects.equals(getShape(), that.getShape()) && Objects.equals(isHelper, that.isHelper) && Objects.equals(getTime(), that.getTime()) && Objects.equals(getPositionX(), that.getPositionX()) && Objects.equals(getPositionY(), that.getPositionY()) && Objects.equals(getScaleX(), that.getScaleX()) && Objects.equals(getScaleY(), that.getScaleY()) && Objects.equals(getRotation(), that.getRotation()) && Objects.equals(getPivotX(), that.getPivotX()) && Objects.equals(getPivotY(), that.getPivotY());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getName(), getStartTime(), getDuration(), getTimelineLayer(), getInterpolationDriver(), getShape(), isHelper, getTime(), getPositionX(), getPositionY(), getScaleX(), getScaleY(), getRotation(), getPivotX(), getPivotY(), changedKeyFrames);
    }

    @Override
    public String toString() {
        return "HittingObject{" + "name=" + name +
                ", startTime=" + startTime +
                ", duration=" + duration +
                ", timelineLayer=" + timelineLayer +
                ", interpolationDriver=" + interpolationDriver +
                ", shape=" + shape +
                ", isHelper=" + isHelper +
                ", time=" + time +
                ", positionX=" + positionX +
                ", positionY=" + positionY +
                ", scaleX=" + scaleX +
                ", scaleY=" + scaleY +
                ", rotation=" + rotation +
                ", pivotX=" + pivotX +
                ", pivotY=" + pivotY +
                ", changedKeyFrames=" + changedKeyFrames +
                '}';
    }
}
