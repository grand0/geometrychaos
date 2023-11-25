package ru.kpfu.itis.gr201.ponomarev.bheditor.game;

import javafx.animation.Interpolator;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.beans.property.*;
import javafx.collections.ObservableList;
import javafx.util.Duration;

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
                timeline.playFrom(new Duration(get()));
                timeline.pause();
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

    public void addPositionXKeyFrame(double value, int time, Interpolator interpolator) {
        if (
                timeline.getKeyFrames()
                    .stream()
                    .noneMatch(
                            kf -> kf.getName().startsWith(POSITION_X_KEYFRAME_NAME_PREFIX)
                                    && kf.getTime().toMillis() == time
                    )
        ) {
            timeline.getKeyFrames().add(
                    new KeyFrame(
                            new Duration(time),
                            POSITION_X_KEYFRAME_NAME_PREFIX + time,
                            new KeyValue(
                                    positionX,
                                    value,
                                    interpolator
                            )
                    )
            );
        }
    }

    public void addPositionYKeyFrame(double value, int time, Interpolator interpolator) {
        if (
                timeline.getKeyFrames()
                        .stream()
                        .noneMatch(
                                kf -> kf.getName().startsWith(POSITION_Y_KEYFRAME_NAME_PREFIX)
                                        && kf.getTime().toMillis() == time
                        )
        ) {
            timeline.getKeyFrames().add(
                    new KeyFrame(
                            new Duration(time),
                            POSITION_Y_KEYFRAME_NAME_PREFIX + time,
                            new KeyValue(
                                    positionY,
                                    value,
                                    interpolator
                            )
                    )
            );
        }
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
