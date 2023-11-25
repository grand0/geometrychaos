package ru.kpfu.itis.gr201.ponomarev.bheditor.util;

import javafx.util.StringConverter;

public class DoubleStringConverter extends StringConverter<Number> {
    @Override
    public String toString(Number object) {
        return object.toString();
    }

    @Override
    public Number fromString(String string) {
        try {
            return Double.parseDouble(string.replaceAll("[^\\d.-]", ""));
        } catch (NumberFormatException e) {
            return 0.0;
        }
    }
}
