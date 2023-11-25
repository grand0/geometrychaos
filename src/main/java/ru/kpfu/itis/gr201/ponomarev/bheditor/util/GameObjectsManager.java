package ru.kpfu.itis.gr201.ponomarev.bheditor.util;

import javafx.beans.Observable;
import javafx.beans.property.ListProperty;
import javafx.beans.property.SimpleListProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import ru.kpfu.itis.gr201.ponomarev.bheditor.game.HittingObject;
import ru.kpfu.itis.gr201.ponomarev.bheditor.ui.ObjectsTimeline;

import java.util.List;

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
        getObjects().add(hittingObject);
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
