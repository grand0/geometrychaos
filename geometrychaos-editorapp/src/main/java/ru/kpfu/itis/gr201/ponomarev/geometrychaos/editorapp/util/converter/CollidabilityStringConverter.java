package ru.kpfu.itis.gr201.ponomarev.geometrychaos.editorapp.util.converter;

import javafx.util.StringConverter;
import ru.kpfu.itis.gr201.ponomarev.geometrychaos.commons.util.ObjectCollidability;

public class CollidabilityStringConverter extends StringConverter<ObjectCollidability> {

    @Override
    public String toString(ObjectCollidability collidability) {
        return collidability == null ? "Not selected" : collidability.toString();
    }

    @Override
    public ObjectCollidability fromString(String name) {
        return ObjectCollidability.byName(name);
    }
}
