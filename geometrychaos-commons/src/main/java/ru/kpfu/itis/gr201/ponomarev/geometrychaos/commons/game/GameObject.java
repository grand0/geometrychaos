package ru.kpfu.itis.gr201.ponomarev.geometrychaos.commons.game;

import javafx.beans.property.*;
import javafx.beans.value.WritableValue;
import javafx.collections.ObservableList;
import ru.kpfu.itis.gr201.ponomarev.geometrychaos.commons.anim.InterpolatorType;
import ru.kpfu.itis.gr201.ponomarev.geometrychaos.commons.anim.KeyFrameTag;
import ru.kpfu.itis.gr201.ponomarev.geometrychaos.commons.anim.KeyFramesInterpolationDriver;
import ru.kpfu.itis.gr201.ponomarev.geometrychaos.commons.anim.ObjectKeyFrame;
import ru.kpfu.itis.gr201.ponomarev.geometrychaos.commons.anim.randomizer.ValueRandomizer;
import ru.kpfu.itis.gr201.ponomarev.geometrychaos.commons.game.shape.GameShape;
import ru.kpfu.itis.gr201.ponomarev.geometrychaos.commons.game.shape.GameShapeType;
import ru.kpfu.itis.gr201.ponomarev.geometrychaos.commons.util.ObjectCollidability;

import java.util.Objects;
import java.util.Optional;

public class GameObject extends ObjectPropertyBase<GameObject> {

    private final StringProperty name;
    private final IntegerProperty startTime;
    private final IntegerProperty duration;
    private final IntegerProperty timelineLayer;
    private final KeyFramesInterpolationDriver interpolationDriver;
    private final ObjectProperty<GameShape> shape;
    private final IntegerProperty viewOrder;
    private final ObjectProperty<ObjectCollidability> objectCollidability;

    private final IntegerProperty time;
    private final DoubleProperty positionX;
    private final DoubleProperty positionY;
    private final DoubleProperty scaleX;
    private final DoubleProperty scaleY;
    private final DoubleProperty rotation;
    private final DoubleProperty pivotX;
    private final DoubleProperty pivotY;
    private final DoubleProperty highlight;
    private final DoubleProperty stroke;

    private boolean changedKeyFrames = false;

    public GameObject(String name, int startTime, int duration, int timelineLayer) {
        this.name = new StringPropertyBase() {
            @Override
            protected void invalidated() {
                super.invalidated();
                GameObject.this.fireValueChangedEvent();
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
                GameObject.this.fireValueChangedEvent();
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
                GameObject.this.fireValueChangedEvent();
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
                GameObject.this.fireValueChangedEvent();
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
                GameObject.this.fireValueChangedEvent();
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
        this.shape.set(new GameShape(GameShapeType.SQUARE));

        this.viewOrder = new IntegerPropertyBase() {
            @Override
            protected void invalidated() {
                super.invalidated();
                GameObject.this.fireValueChangedEvent();
            }

            @Override
            public Object getBean() {
                return null;
            }

            @Override
            public String getName() {
                return "viewOrder";
            }
        };
        this.viewOrder.set(0);

        this.objectCollidability = new ObjectPropertyBase<>() {
            @Override
            protected void invalidated() {
                super.invalidated();
                GameObject.this.fireValueChangedEvent();
            }

            @Override
            public Object getBean() {
                return null;
            }

            @Override
            public String getName() {
                return "objectCollidability";
            }
        };
        this.objectCollidability.set(ObjectCollidability.OPACITY_BASED);

        this.time = new IntegerPropertyBase() {
            @Override
            protected void invalidated() {
                super.invalidated();
                GameObject.this.fireValueChangedEvent();
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

        this.highlight = makeDoublePropertyWithName("highlight");
        setHighlight(0.0);

        this.stroke = makeDoublePropertyWithName("stroke");
        setStroke(0.0);
    }

    private DoublePropertyBase makeDoublePropertyWithName(String name) {
        return new DoublePropertyBase() {
            @Override
            protected void invalidated() {
                super.invalidated();
                GameObject.this.fireValueChangedEvent();
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
        for (KeyFrameTag tag : KeyFrameTag.values()) {
            addKeyFrame(
                    getDefaultValueForTag(tag),
                    0,
                    InterpolatorType.INSTANT,
                    tag,
                    null
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

    public Optional<ObjectKeyFrame> getKeyFrame(int time, KeyFrameTag tag) {
        return interpolationDriver.getKeyFrames()
                .stream()
                .filter(
                        kf -> kf.getTag().equals(tag) && kf.getTime() == time
                )
                .findFirst();
    }

    public ObjectKeyFrame addKeyFrame(Object value, int time, InterpolatorType interpolator, KeyFrameTag tag, ValueRandomizer randomizer) {
        WritableValue<?> prop = null;
        switch (tag) {
            case POSITION_X -> prop = positionX;
            case POSITION_Y -> prop = positionY;
            case SCALE_X -> prop = scaleX;
            case SCALE_Y -> prop = scaleY;
            case ROTATION -> prop = rotation;
            case PIVOT_X -> prop = pivotX;
            case PIVOT_Y -> prop = pivotY;
            case HIGHLIGHT -> prop = highlight;
            case STROKE -> prop = stroke;
        }
        if (prop == null) {
            throw new IllegalArgumentException("Unknown property");
        }
        return addKeyFrame(value, time, interpolator, tag, randomizer, prop);
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private ObjectKeyFrame addKeyFrame(Object value, int time, InterpolatorType interpolator, KeyFrameTag tag, ValueRandomizer randomizer, WritableValue property) {
        getKeyFrame(time, tag).ifPresent(kf -> interpolationDriver.getKeyFrames().remove(kf));
        ObjectKeyFrame kf = new ObjectKeyFrame(
                time,
                tag,
                property,
                value,
                interpolator,
                randomizer
        );
        addKeyFrame(kf);
        return kf;
    }

    public void addKeyFrame(ObjectKeyFrame kf) {
        kf.randomize();
        interpolationDriver.getKeyFrames().add(kf);
        changedKeyFrames = true;
        fireValueChangedEvent();
    }

    public void removeKeyFrame(ObjectKeyFrame kf) {
        interpolationDriver.getKeyFrames().remove(kf);
        changedKeyFrames = true;
        fireValueChangedEvent();
    }

    public static Object getDefaultValueForTag(KeyFrameTag tag) {
        switch (tag) {
            case POSITION_X,
                    POSITION_Y,
                    ROTATION,
                    PIVOT_X,
                    PIVOT_Y,
                    HIGHLIGHT,
                    STROKE -> {
                return 0.0;
            }
            case SCALE_X,
                    SCALE_Y -> {
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
        this.timelineLayer.set(timelineLayer);
    }

    public KeyFramesInterpolationDriver getInterpolationDriver() {
        return interpolationDriver;
    }

    public GameShape getShape() {
        return shape.get();
    }

    public ObjectProperty<GameShape> shapeProperty() {
        return shape;
    }

    public void setShape(GameShape gameShape) {
        this.shape.set(gameShape);
    }
    
    public void setShapeType(GameShapeType type) {
        setShape(new GameShape(type));
    }

    public int getViewOrder() {
        return viewOrder.get();
    }

    public IntegerProperty viewOrderProperty() {
        return viewOrder;
    }

    public void setViewOrder(int viewOrder) {
        this.viewOrder.set(viewOrder);
    }

    public ObjectCollidability getObjectCollidability() {
        return objectCollidability.get();
    }

    public ObjectProperty<ObjectCollidability> objectCollidabilityProperty() {
        return objectCollidability;
    }

    public void setObjectCollidability(ObjectCollidability objectCollidability) {
        this.objectCollidability.set(objectCollidability);
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

    public double getHighlight() {
        return highlight.get();
    }

    public DoubleProperty highlightProperty() {
        return highlight;
    }

    public void setHighlight(double highlight) {
        this.highlight.set(highlight);
    }

    public double getStroke() {
        return stroke.get();
    }

    public DoubleProperty strokeProperty() {
        return stroke;
    }

    public void setStroke(double stroke) {
        this.stroke.set(stroke);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        GameObject that = (GameObject) o;
        return changedKeyFrames == that.changedKeyFrames && Objects.equals(getName(), that.getName()) && Objects.equals(getStartTime(), that.getStartTime()) && Objects.equals(getDuration(), that.getDuration()) && Objects.equals(getTimelineLayer(), that.getTimelineLayer()) && Objects.equals(getInterpolationDriver(), that.getInterpolationDriver()) && Objects.equals(getShape(), that.getShape()) && Objects.equals(getTime(), that.getTime()) && Objects.equals(getPositionX(), that.getPositionX()) && Objects.equals(getPositionY(), that.getPositionY()) && Objects.equals(getScaleX(), that.getScaleX()) && Objects.equals(getScaleY(), that.getScaleY()) && Objects.equals(getRotation(), that.getRotation()) && Objects.equals(getPivotX(), that.getPivotX()) && Objects.equals(getPivotY(), that.getPivotY()) && Objects.equals(getHighlight(), that.getHighlight());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getName(), getStartTime(), getDuration(), getTimelineLayer(), getInterpolationDriver(), getShape(), getTime(), getPositionX(), getPositionY(), getScaleX(), getScaleY(), getRotation(), getPivotX(), getPivotY(), getHighlight(), changedKeyFrames);
    }

    @Override
    public String toString() {
        return "GameObject{" +
                "name=" + name +
                ", startTime=" + startTime +
                ", duration=" + duration +
                ", timelineLayer=" + timelineLayer +
                ", interpolationDriver=" + interpolationDriver +
                ", shape=" + shape +
                ", time=" + time +
                ", positionX=" + positionX +
                ", positionY=" + positionY +
                ", scaleX=" + scaleX +
                ", scaleY=" + scaleY +
                ", rotation=" + rotation +
                ", pivotX=" + pivotX +
                ", pivotY=" + pivotY +
                ", highlight=" + highlight +
                ", changedKeyFrames=" + changedKeyFrames +
                '}';
    }
}
