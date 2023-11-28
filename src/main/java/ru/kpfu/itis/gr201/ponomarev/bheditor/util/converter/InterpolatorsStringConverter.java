package ru.kpfu.itis.gr201.ponomarev.bheditor.util.converter;

import javafx.util.StringConverter;
import ru.kpfu.itis.gr201.ponomarev.bheditor.util.InterpolatorType;

public class InterpolatorsStringConverter extends StringConverter<InterpolatorType> {
    @Override
    public String toString(InterpolatorType object) {
        return object == null ? "Not selected" : object.getName();
    }

    @Override
    public InterpolatorType fromString(String string) {
        InterpolatorType i = InterpolatorType.byName(string);
        return i == null ? InterpolatorType.LINEAR : i;
    }
}
