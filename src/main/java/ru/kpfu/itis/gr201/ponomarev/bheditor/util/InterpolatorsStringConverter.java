package ru.kpfu.itis.gr201.ponomarev.bheditor.util;

import javafx.util.StringConverter;

public class InterpolatorsStringConverter extends StringConverter<Interpolators> {
    @Override
    public String toString(Interpolators object) {
        return object == null ? "Not selected" : object.getName();
    }

    @Override
    public Interpolators fromString(String string) {
        Interpolators i = Interpolators.byName(string);
        return i == null ? Interpolators.LINEAR : i;
    }
}
