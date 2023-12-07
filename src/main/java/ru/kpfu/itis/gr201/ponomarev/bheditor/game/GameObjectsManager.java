package ru.kpfu.itis.gr201.ponomarev.bheditor.game;

import javafx.beans.Observable;
import javafx.beans.property.ListProperty;
import javafx.beans.property.SimpleListProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;
import ru.kpfu.itis.gr201.ponomarev.bheditor.anim.KeyFrameTag;
import ru.kpfu.itis.gr201.ponomarev.bheditor.anim.KeyFrameType;
import ru.kpfu.itis.gr201.ponomarev.bheditor.anim.ObjectKeyFrame;
import ru.kpfu.itis.gr201.ponomarev.bheditor.exception.LevelLoadException;
import ru.kpfu.itis.gr201.ponomarev.bheditor.exception.LevelSaveException;
import ru.kpfu.itis.gr201.ponomarev.bheditor.ui.ObjectsTimeline;
import ru.kpfu.itis.gr201.ponomarev.bheditor.util.InterpolatorType;
import ru.kpfu.itis.gr201.ponomarev.bheditor.util.randomizer.ValueRandomizer;
import ru.kpfu.itis.gr201.ponomarev.bheditor.util.randomizer.ValueRandomizerType;
import ru.kpfu.itis.gr201.ponomarev.bheditor.util.randomizer.impl.DiscreteDoubleValueRandomizer;
import ru.kpfu.itis.gr201.ponomarev.bheditor.util.randomizer.impl.LinearDoubleValueRandomizer;

import java.io.*;
import java.util.List;
import java.util.Map;

import static java.util.Map.entry;

public class GameObjectsManager {

    private static GameObjectsManager instance;

    private final ListProperty<HittingObject> objects;

    private GameObjectsManager() {
        ObservableList<HittingObject> obsList = FXCollections.observableArrayList(obj -> new Observable[] { obj });
        objects = new SimpleListProperty<>(obsList);
    }

    public static GameObjectsManager getInstance() {
        if (instance == null) {
            instance = new GameObjectsManager();
        }
        return instance;
    }

    public void addObject(int start, int duration) {
        String name = "Object " + (int) (Math.random() * 1000 + 1);
        HittingObject hittingObject = new HittingObject(name, start, duration, 0);
        hittingObject.initStartKeyFrames();
        for (int i = 0; i < ObjectsTimeline.LAYERS_COUNT; i++) {
            int curLayer = i;
            List<HittingObject> objs = GameObjectsManager.getInstance()
                    .getObjects()
                    .stream()
                    .filter(ho -> ho.getTimelineLayer() == curLayer)
                    .toList();
            if (
                    objs.stream().noneMatch(
                            ho ->
                                    ho.getStartTime() < hittingObject.getEndTime()
                                            && hittingObject.getStartTime() < ho.getEndTime()
                    )
            ) {
                hittingObject.setTimelineLayer(curLayer);
                break;
            }
        }
        addObject(hittingObject);
    }

    public void addObject(HittingObject obj) {
        getObjects().add(obj);
    }

    public void removeObject(HittingObject obj) {
        getObjects().remove(obj);
    }

    public void saveObjects(File file) throws LevelSaveException {
        JSONArray jsonObjects = new JSONArray();
        for (HittingObject o : objects) {
            JSONArray jsonKeyFrames = new JSONArray();
            for (ObjectKeyFrame kf : o.getKeyFrames()) {
                Map<String, Object> kfMap = Map.ofEntries(
                        entry("time", kf.getTime()),
                        entry("tag", kf.getTag().name()),
                        entry("endValue", kf.getEndValue()),
                        entry("type", kf.getType().name()),
                        entry("interpolator", kf.getInterpolatorType().name())
                );
                JSONObject jsonKeyFrame = new JSONObject(kfMap);
                if (kf.getRandomizer() == null) {
                    jsonKeyFrame.put("randomizer", JSONObject.NULL);
                } else {
                    // TODO: move this to some other place, maybe to randomizers themselves
                    JSONObject jsonRandomizer = new JSONObject();
                    jsonRandomizer.put("name", kf.getRandomizer().getType().name());
                    if (kf.getRandomizer() instanceof LinearDoubleValueRandomizer rand) {
                        jsonRandomizer.put("startValue", rand.getStartValue());
                        jsonRandomizer.put("endValue", rand.getEndValue());
                    } else if (kf.getRandomizer() instanceof DiscreteDoubleValueRandomizer rand) {
                        jsonRandomizer.put("startValue", rand.getStartValue());
                        jsonRandomizer.put("endValue", rand.getEndValue());
                        jsonRandomizer.put("step", rand.getStep());
                    } else {
                        throw new RuntimeException("Unknown randomizer.");
                    }
                    jsonKeyFrame.put("randomizer", jsonRandomizer);
                }
                jsonKeyFrames.put(jsonKeyFrame);
            }
            Map<String, Object> objMap = Map.ofEntries(
                    entry("name", o.getName()),
                    entry("startTime", o.getStartTime()),
                    entry("duration", o.getDuration()),
                    entry("timelineLayer", o.getTimelineLayer()),
                    entry("shape", o.getShape().name()),
                    entry("viewOrder", o.getViewOrder()),
                    entry("keyFrames", jsonKeyFrames)
            );
            JSONObject jsonObj = new JSONObject(objMap);
            jsonObjects.put(jsonObj);
        }
        JSONObject jsonRoot = new JSONObject();
        jsonRoot.put("objects", jsonObjects);
        try (Writer writer = new BufferedWriter(new FileWriter(file))) {
            jsonRoot.write(writer);
        } catch (JSONException | IOException e) {
            throw new LevelSaveException(e);
        }
    }

    public void loadObjects(File file) throws LevelLoadException {
        objects.clear();

        try (Reader reader = new BufferedReader(new FileReader(file))) {
            JSONObject jsonRoot = new JSONObject(new JSONTokener(reader));
            JSONArray jsonObjects = jsonRoot.getJSONArray("objects");
            for (Object jo : jsonObjects) {
                if (jo instanceof JSONObject jsonObj) {
                    String name = jsonObj.getString("name");
                    int startTime = jsonObj.getInt("startTime");
                    int duration = jsonObj.getInt("duration");
                    int timelineLayer = jsonObj.getInt("timelineLayer");
                    Shape shape = Shape.valueOf(jsonObj.getString("shape"));
                    int viewOrder = jsonObj.getInt("viewOrder");
                    JSONArray jsonKeyFrames = jsonObj.getJSONArray("keyFrames");

                    HittingObject obj = new HittingObject(name, startTime, duration, timelineLayer);
                    obj.setShape(shape);
                    obj.setViewOrder(viewOrder);
                    for (Object jkf : jsonKeyFrames) {
                        if (jkf instanceof JSONObject jsonKeyFrame) {
                            int time = jsonKeyFrame.getInt("time");
                            KeyFrameTag tag = KeyFrameTag.valueOf(jsonKeyFrame.getString("tag"));
                            KeyFrameType keyFrameType = KeyFrameType.valueOf(jsonKeyFrame.getString("type"));
                            String endValueStr = jsonKeyFrame.get("endValue").toString();
                            Object endValue = null;
                            switch (keyFrameType) {
                                case BOOLEAN -> {
                                    if (endValueStr.equals("true")) endValue = true;
                                    else if (endValueStr.equals("false")) endValue = false;
                                }
                                case INTEGER -> {
                                    try {
                                        endValue = Integer.parseInt(endValueStr);
                                    } catch (NumberFormatException ignored) { }
                                }
                                case LONG -> {
                                    try {
                                        endValue = Long.parseLong(endValueStr);
                                    } catch (NumberFormatException ignored) { }
                                }
                                case FLOAT -> {
                                    try {
                                        endValue = Float.parseFloat(endValueStr);
                                    } catch (NumberFormatException ignored) { }
                                }
                                case DOUBLE -> {
                                    try {
                                        endValue = Double.parseDouble(endValueStr);
                                    } catch (NumberFormatException ignored) { }
                                }
                                default -> throw new LevelLoadException("Unknown key frame type.");
                            }
                            if (endValue == null) {
                                throw new LevelLoadException("Couldn't get end value of key frame.");
                            }
                            InterpolatorType interpolator = InterpolatorType.valueOf(jsonKeyFrame.getString("interpolator"));
                            ValueRandomizer randomizer = null;
                            if (jsonKeyFrame.has("randomizer") && jsonKeyFrame.get("randomizer") != JSONObject.NULL) {
                                JSONObject jsonRandomizer = jsonKeyFrame.getJSONObject("randomizer");
                                ValueRandomizerType randomizerType = ValueRandomizerType.valueOf(jsonRandomizer.getString("name"));
                                switch (randomizerType) {
                                    case DOUBLE_LINEAR -> {
                                        double start = jsonRandomizer.getDouble("startValue");
                                        double end = jsonRandomizer.getDouble("endValue");
                                        randomizer = new LinearDoubleValueRandomizer(start, end);
                                    }
                                    case DOUBLE_DISCRETE -> {
                                        double start = jsonRandomizer.getDouble("startValue");
                                        double end = jsonRandomizer.getDouble("endValue");
                                        double step = jsonRandomizer.getDouble("step");
                                        randomizer = new DiscreteDoubleValueRandomizer(start, end, step);
                                    }
                                    default -> throw new LevelLoadException("Unknown randomizer.");
                                }
                            }
                            obj.addKeyFrame(
                                    endValue,
                                    time,
                                    interpolator,
                                    tag,
                                    randomizer
                            );
                        } else {
                            throw new LevelLoadException("Not a key frame in array.");
                        }
                    }
                    addObject(obj);
                } else {
                    throw new LevelLoadException("Not an object in array.");
                }
            }
        } catch (JSONException | IOException | ClassCastException e) {
            throw new LevelLoadException(e);
        }
    }

    public ObservableList<HittingObject> getObjects() {
        return objects.get();
    }

    public ListProperty<HittingObject> objectsProperty() {
        return objects;
    }

    public void setObjects(ObservableList<HittingObject> objects) {
        this.objects.set(objects);
    }
}
