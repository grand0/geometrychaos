package ru.kpfu.itis.gr201.ponomarev.bheditor.game;

import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.beans.property.*;
import javafx.beans.value.WritableValue;
import javafx.collections.ObservableList;
import javafx.util.Duration;
import ru.kpfu.itis.gr201.ponomarev.bheditor.ui.ObjectsTimeline;
import ru.kpfu.itis.gr201.ponomarev.bheditor.util.Interpolators;

import java.util.Optional;

public class HittingObject extends ObjectPropertyBase<HittingObject> {

    public static final String POSITION_X_KEYFRAME_NAME_PREFIX = "positionX";
    public static final String POSITION_Y_KEYFRAME_NAME_PREFIX = "positionY";
    public static final String SCALE_X_KEYFRAME_NAME_PREFIX = "scaleX";
    public static final String SCALE_Y_KEYFRAME_NAME_PREFIX = "scaleY";
    public static final String ROTATION_KEYFRAME_NAME_PREFIX = "rotation";
    public static final String PIVOT_X_KEYFRAME_NAME_PREFIX = "pivotX";
    public static final String PIVOT_Y_KEYFRAME_NAME_PREFIX = "pivotY";

    private final StringProperty name;
    private final IntegerProperty startTime;
    private final IntegerProperty duration;
    private final IntegerProperty timelineLayer;
    private Timeline timeline;
    private final ObjectProperty<Shape> shape;

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
                return this;
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
                return this;
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
                return this;
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
                return this;
            }

            @Override
            public String getName() {
                return "timelineLayer";
            }
        };
        this.timelineLayer.set(timelineLayer);

        this.timeline = new Timeline(
                new KeyFrame(
                        new Duration(duration),
                        "end"
                )
        );

        this.shape = new ObjectPropertyBase<>() {
            @Override
            protected void invalidated() {
                super.invalidated();
                HittingObject.this.fireValueChangedEvent();
            }

            @Override
            public Object getBean() {
                return this;
            }

            @Override
            public String getName() {
                return "shape";
            }
        };
        this.shape.set(Shape.SQUARE);

        this.time = new IntegerPropertyBase() {
            @Override
            protected void invalidated() {
                super.invalidated();
                HittingObject.this.fireValueChangedEvent();
            }

            @Override
            public Object getBean() {
                return this;
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
                return this;
            }

            @Override
            public String getName() {
                return name;
            }
        };
    }

    public void initStartKeyFrames() {
        addKeyFrame(
                0.0,
                1,
                Interpolators.INSTANT,
                POSITION_X_KEYFRAME_NAME_PREFIX
        );
        addKeyFrame(
                0.0,
                1,
                Interpolators.INSTANT,
                POSITION_Y_KEYFRAME_NAME_PREFIX
        );
        addKeyFrame(
                1.0,
                1,
                Interpolators.INSTANT,
                SCALE_X_KEYFRAME_NAME_PREFIX
        );
        addKeyFrame(
                1.0,
                1,
                Interpolators.INSTANT,
                SCALE_Y_KEYFRAME_NAME_PREFIX
        );
        addKeyFrame(
                0.0,
                1,
                Interpolators.INSTANT,
                ROTATION_KEYFRAME_NAME_PREFIX
        );
        addKeyFrame(
                0.0,
                1,
                Interpolators.INSTANT,
                PIVOT_X_KEYFRAME_NAME_PREFIX
        );
        addKeyFrame(
                0.0,
                1,
                Interpolators.INSTANT,
                PIVOT_Y_KEYFRAME_NAME_PREFIX
        );
    }

    @Override
    protected void fireValueChangedEvent() {
        super.fireValueChangedEvent();
        if (time != null) {
            if (changedKeyFrames || getTime() != (int) timeline.getCurrentTime().toMillis()) {
                timeline.stop();
                timeline.playFrom(new Duration(getTime()));
                timeline.pause();
                changedKeyFrames = false;
            }
        }
    }

    public Optional<KeyFrame> getKeyFrame(int time, String namePrefix) {
        return timeline.getKeyFrames()
                .stream()
                .filter(
                        kf -> kf.getName().startsWith(namePrefix)
                                && kf.getTime().toMillis() == time
                )
                .findFirst();
    }

    private KeyFrame addKeyFrame(double value, int time, Interpolators interpolator, String namePrefix, WritableValue<Number> property) {
        getKeyFrame(time, namePrefix).ifPresent(keyFrame -> timeline.getKeyFrames().remove(keyFrame));

        KeyFrame kf = new KeyFrame(
                new Duration(time),
                namePrefix + time,
                new KeyValue(
                        property,
                        value,
                        interpolator.getInterpolator()
                )
        );
        addKeyFrame(kf);
        return kf;
    }

    public KeyFrame addKeyFrame(double value, int time, Interpolators interpolator, String namePrefix) {
        WritableValue<Number> prop = null;
        switch (namePrefix) {
            case POSITION_X_KEYFRAME_NAME_PREFIX -> prop = positionX;
            case POSITION_Y_KEYFRAME_NAME_PREFIX -> prop = positionY;
            case SCALE_X_KEYFRAME_NAME_PREFIX -> prop = scaleX;
            case SCALE_Y_KEYFRAME_NAME_PREFIX -> prop = scaleY;
            case ROTATION_KEYFRAME_NAME_PREFIX -> prop = rotation;
            case PIVOT_X_KEYFRAME_NAME_PREFIX -> prop = pivotX;
            case PIVOT_Y_KEYFRAME_NAME_PREFIX -> prop = pivotY;
        }
        if (prop == null) {
            throw new IllegalArgumentException("Unknown property");
        }
        return addKeyFrame(value, time, interpolator, namePrefix, prop);
    }

    public void addKeyFrame(KeyFrame kf) {
        timeline.getKeyFrames().add(kf);
        changedKeyFrames = true;
        fireValueChangedEvent();
    }

    public void removeKeyFrame(KeyFrame keyFrame) {
        timeline.getKeyFrames().remove(keyFrame);
        changedKeyFrames = true;
        fireValueChangedEvent();
    }

    public ObservableList<KeyFrame> getKeyFrames() {
        return timeline.getKeyFrames();
    }

    public boolean isVisible(int time) {
        return time >= getStartTime() && time <= getEndTime();
    }

    @Override
    public Object getBean() {
        return this;
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

    public Timeline getTimeline() {
        return timeline;
    }

    public void setTimeline(Timeline timeline) {
        this.timeline = timeline;
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
}
