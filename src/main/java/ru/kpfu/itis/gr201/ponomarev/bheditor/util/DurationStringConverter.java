package ru.kpfu.itis.gr201.ponomarev.bheditor.util;

import javafx.util.Duration;
import javafx.util.StringConverter;

import java.util.Locale;

public class DurationStringConverter extends StringConverter<Duration> {
    @Override
    public String toString(Duration object) {
        return String.format(Locale.US, "%.3f s", object.toSeconds());
    }

    @Override
    public Duration fromString(String string) {
        try {
            return Duration.seconds(Double.parseDouble(string.replaceAll("[^\\d.]", "")));
        } catch (NumberFormatException e) {
            return Duration.ZERO;
        }
    }
}
