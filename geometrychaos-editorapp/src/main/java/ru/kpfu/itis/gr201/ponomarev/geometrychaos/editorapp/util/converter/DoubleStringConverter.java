package ru.kpfu.itis.gr201.ponomarev.geometrychaos.editorapp.util.converter;

import javafx.util.StringConverter;

public class DoubleStringConverter extends StringConverter<Double> {
    @Override
    public String toString(Double object) {
        return object.toString();
    }

    @Override
    public Double fromString(String string) {
        try {
            return Double.parseDouble(string.replaceAll("[^\\d.-]", ""));
        } catch (NumberFormatException e) {
            return 0.0;
        }
    }
}
