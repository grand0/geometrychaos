package ru.kpfu.itis.gr201.ponomarev.bheditor.util;

import javafx.util.StringConverter;

public class IntStringConverter extends StringConverter<Number> {
    @Override
    public String toString(Number object) {
        return object.toString();
    }

    @Override
    public Number fromString(String string) {
        try {
            return Integer.parseInt(string.replaceAll("\\D", ""));
        } catch (NumberFormatException e) {
            return 0;
        }
    }
}
