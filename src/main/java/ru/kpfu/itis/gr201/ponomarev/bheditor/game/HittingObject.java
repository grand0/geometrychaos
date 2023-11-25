package ru.kpfu.itis.gr201.ponomarev.bheditor.game;

import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.beans.property.*;
import javafx.beans.value.WritableValue;
import javafx.collections.ObservableList;
import javafx.util.Duration;
import ru.kpfu.itis.gr201.ponomarev.bheditor.util.Interpolators;

import java.util.Optional;

public class HittingObject extends ObjectPropertyBase<HittingObject> {

    public static final String POSITION_X_KEYFRAME_NAME_PREFIX = "positionX";
    public static final String POSITION_Y_KEYFRAME_NAME_PREFIX = "positionY";

    private final StringProperty name;
    private final IntegerProperty startTime;
    private final IntegerProperty duration;
    private final IntegerProperty timelineLayer;
    private Timeline timeline;
    private final ObjectProperty<Shape> shape;

    private final IntegerProperty time;
    private final DoubleProperty positionX;
    private final DoubleProperty positionY;

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

        this.positionX = new DoublePropertyBase() {
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
                return "positionX";
            }
        };

        this.positionY = new DoublePropertyBase() {
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
                return "positionY";
            }
        };
    }

    @Override
    protected void fireValueChangedEvent() {
        super.fireValueChangedEvent();
        if (time != null) {
            if (changedKeyFrames || getTime() - getStartTime() != timeline.getCurrentTime().toMillis()) {
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
        timeline.getKeyFrames().add(kf);
        changedKeyFrames = true;
        fireValueChangedEvent();
        return kf;
    }

    public KeyFrame addKeyFrame(double value, int time, Interpolators interpolator, String namePrefix) {
        WritableValue<Number> prop = null;
        switch (namePrefix) {
            case POSITION_X_KEYFRAME_NAME_PREFIX -> prop = positionX;
            case POSITION_Y_KEYFRAME_NAME_PREFIX -> prop = positionY;
        }
        if (prop == null) {
            throw new IllegalArgumentException("Unknown property");
        }
        return addKeyFrame(value, time, interpolator, namePrefix, prop);
    }

    public void removeKeyFrame(KeyFrame keyFrame) {
        timeline.getKeyFrames().remove(keyFrame);
        fireValueChangedEvent();
    }

//    public void addPositionXKeyFrame(double value, int time, Interpolator interpolator) {
//        addKeyFrame(
//                value,
//                time,
//                interpolator,
//                POSITION_X_KEYFRAME_NAME_PREFIX,
//                positionX
//        );
//    }
//
//    public Optional<KeyFrame> getPositionXKeyFrame(int time) {
//        return getKeyFrame(time, POSITION_X_KEYFRAME_NAME_PREFIX);
//    }
//
//    public void addPositionYKeyFrame(double value, int time, Interpolator interpolator) {
//        addKeyFrame(
//                value,
//                time,
//                interpolator,
//                POSITION_Y_KEYFRAME_NAME_PREFIX,
//                positionY
//        );
//    }
//
//    public Optional<KeyFrame> getPositionYKeyFrame(int time) {
//        return getKeyFrame(time, POSITION_Y_KEYFRAME_NAME_PREFIX);
//    }

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
}
