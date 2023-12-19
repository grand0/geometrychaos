package ru.kpfu.itis.gr201.ponomarev.geometrychaos.commons.game;

import javafx.beans.Observable;
import javafx.beans.property.ListProperty;
import javafx.beans.property.SimpleListProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import ru.kpfu.itis.gr201.ponomarev.geometrychaos.commons.anim.ObjectKeyFrame;
import ru.kpfu.itis.gr201.ponomarev.geometrychaos.commons.anim.randomizer.GlobalRandom;

import java.util.List;

import static ru.kpfu.itis.gr201.ponomarev.geometrychaos.commons.game.GameObject.LAYERS_COUNT;

public class LevelManager {

    private static LevelManager instance;

    private final ListProperty<GameObject> objects;

    private LevelManager() {
        ObservableList<GameObject> obsList = FXCollections.observableArrayList(obj -> new Observable[] { obj });
        objects = new SimpleListProperty<>(obsList);
    }

    public static LevelManager getInstance() {
        if (instance == null) {
            instance = new LevelManager();
        }
        return instance;
    }

    public void addObject(int start, int duration) {
        String name = "Object " + (int) (Math.random() * 1000 + 1);
        GameObject gameObject = new GameObject(name, start, duration, 0);
        gameObject.initStartKeyFrames();
        for (int i = 0; i < LAYERS_COUNT; i++) {
            int curLayer = i;
            List<GameObject> objs = LevelManager.getInstance()
                    .getObjects()
                    .stream()
                    .filter(obj -> obj.getTimelineLayer() == curLayer)
                    .toList();
            if (
                    objs.stream().noneMatch(
                            obj ->
                                    obj.getStartTime() < gameObject.getEndTime()
                                            && gameObject.getStartTime() < obj.getEndTime()
                    )
            ) {
                gameObject.setTimelineLayer(curLayer);
                break;
            }
        }
        addObject(gameObject);
    }

    public void addObject(GameObject obj) {
        getObjects().add(obj);
    }

    public void removeObject(GameObject obj) {
        getObjects().remove(obj);
    }

    public void randomizeObjects() {
        for (GameObject obj : objects) {
            for (ObjectKeyFrame kf : obj.getKeyFrames()) {
                kf.randomize();
            }
        }
    }

    public void randomizeObjectsWithSeed(long seed) {
        GlobalRandom.setSeed(seed);
        for (GameObject obj : objects) {
            for (ObjectKeyFrame kf : obj.getKeyFrames()) {
                kf.randomize();
            }
        }
    }

    public ObservableList<GameObject> getObjects() {
        return objects.get();
    }

    public ListProperty<GameObject> objectsProperty() {
        return objects;
    }

    public void setObjects(ObservableList<GameObject> objects) {
        this.objects.set(objects);
    }
}
