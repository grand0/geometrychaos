package ru.kpfu.itis.gr201.ponomarev.geometrychaos.commons.io.level;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;
import ru.kpfu.itis.gr201.ponomarev.geometrychaos.commons.anim.InterpolatorType;
import ru.kpfu.itis.gr201.ponomarev.geometrychaos.commons.anim.KeyFrameTag;
import ru.kpfu.itis.gr201.ponomarev.geometrychaos.commons.anim.KeyFrameType;
import ru.kpfu.itis.gr201.ponomarev.geometrychaos.commons.anim.ObjectKeyFrame;
import ru.kpfu.itis.gr201.ponomarev.geometrychaos.commons.anim.randomizer.ValueRandomizer;
import ru.kpfu.itis.gr201.ponomarev.geometrychaos.commons.anim.randomizer.ValueRandomizerType;
import ru.kpfu.itis.gr201.ponomarev.geometrychaos.commons.anim.randomizer.impl.DiscreteDoubleValueRandomizer;
import ru.kpfu.itis.gr201.ponomarev.geometrychaos.commons.anim.randomizer.impl.LinearDoubleValueRandomizer;
import ru.kpfu.itis.gr201.ponomarev.geometrychaos.commons.game.GameObject;
import ru.kpfu.itis.gr201.ponomarev.geometrychaos.commons.game.shape.GameShape;
import ru.kpfu.itis.gr201.ponomarev.geometrychaos.commons.game.shape.GameShapeType;
import ru.kpfu.itis.gr201.ponomarev.geometrychaos.commons.game.shape.setting.ShapeSetting;
import ru.kpfu.itis.gr201.ponomarev.geometrychaos.commons.io.exception.LevelReadException;
import ru.kpfu.itis.gr201.ponomarev.geometrychaos.commons.io.exception.LevelWriteException;
import ru.kpfu.itis.gr201.ponomarev.geometrychaos.commons.util.ObjectCollidability;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.util.Map.entry;

public class LevelIO {

    private LevelIO() {}

    public static void writeLevel(File file, List<GameObject> objects) throws LevelWriteException {
        JSONArray jsonObjects = new JSONArray();
        for (GameObject o : objects) {
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
            Object shape;
            if (o.getShape().getSettings().length == 0) {
                shape = o.getShape().getType().name();
            } else {
                Map<String, Object> shapeMap = new HashMap<>();
                shapeMap.put("name", o.getShape().getType().name());
                for (ShapeSetting setting : o.getShape().getSettings()) {
                    shapeMap.put(setting.getName(), setting.getValue());
                }
                shape = shapeMap;
            }
            Map<String, Object> objMap = Map.ofEntries(
                    entry("name", o.getName()),
                    entry("startTime", o.getStartTime()),
                    entry("duration", o.getDuration()),
                    entry("timelineLayer", o.getTimelineLayer()),
                    entry("shape", shape),
                    entry("viewOrder", o.getViewOrder()),
                    entry("collidability", o.getObjectCollidability().name()),
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
            throw new LevelWriteException(e);
        }
    }

    public static List<GameObject> readLevel(File file) throws LevelReadException {
        List<GameObject> objects = new ArrayList<>();

        try (Reader reader = new BufferedReader(new FileReader(file))) {
            JSONObject jsonRoot = new JSONObject(new JSONTokener(reader));
            JSONArray jsonObjects = jsonRoot.getJSONArray("objects");
            for (Object jo : jsonObjects) {
                if (jo instanceof JSONObject jsonObj) {
                    String name = jsonObj.getString("name");
                    int startTime = jsonObj.getInt("startTime");
                    int duration = jsonObj.getInt("duration");
                    int timelineLayer = jsonObj.getInt("timelineLayer");
                    GameShape shape = null;
                    Object jsonShape = jsonObj.get("shape");
                    if (jsonShape instanceof String s) {
                        shape = new GameShape(GameShapeType.valueOf(jsonObj.getString("shape")));
                    } else if (jsonShape instanceof JSONObject m) {
                        shape = new GameShape(GameShapeType.valueOf((String) m.get("name")));
                        for (ShapeSetting setting : shape.getSettings()) {
                            Object val = m.get(setting.getName());
                            if (val != null) {
                                setting.setValue(val);
                            }
                        }
                    } else {
                        throw new LevelReadException("Unknown type of \"shape\" property.");
                    }
                    int viewOrder = jsonObj.getInt("viewOrder");
                    JSONArray jsonKeyFrames = jsonObj.getJSONArray("keyFrames");
                    ObjectCollidability collidability;
                    if (!jsonObj.has("collidability")) {
                        collidability = ObjectCollidability.OPACITY_BASED;
                    } else {
                        collidability = ObjectCollidability.valueOf(jsonObj.getString("collidability"));
                    }

                    GameObject obj = new GameObject(name, startTime, duration, timelineLayer);
                    obj.setShape(shape);
                    obj.setViewOrder(viewOrder);
                    obj.setObjectCollidability(collidability);
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
                                default -> throw new LevelReadException("Unknown key frame type.");
                            }
                            if (endValue == null) {
                                throw new LevelReadException("Couldn't get end value of key frame.");
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
                                    default -> throw new LevelReadException("Unknown randomizer.");
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
                            throw new LevelReadException("Not a key frame in array.");
                        }
                    }
                    objects.add(obj);
                } else {
                    throw new LevelReadException("Not an object in array.");
                }
            }
        } catch (JSONException | IOException | ClassCastException e) {
            throw new LevelReadException(e);
        }
        return objects;
    }
}
