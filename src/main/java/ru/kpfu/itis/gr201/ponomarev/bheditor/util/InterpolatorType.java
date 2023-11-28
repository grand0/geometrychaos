package ru.kpfu.itis.gr201.ponomarev.bheditor.util;

import javafx.animation.Interpolator;

import java.util.Arrays;

public enum InterpolatorType {
    LINEAR("Linear", Interpolator.LINEAR),
    INSTANT("Instant", Interpolator.DISCRETE),
    EASE_IN_OUT("Ease in/out", Interpolator.EASE_BOTH),
    EASE_IN("Ease in", Interpolator.EASE_IN),
    EASE_OUT("Ease out", Interpolator.EASE_OUT);

    private final String name;
    private final Interpolator interpolator;

    InterpolatorType(String name, Interpolator interpolator) {
        this.name = name;
        this.interpolator = interpolator;
    }

    public static InterpolatorType byName(String name) {
        return Arrays.stream(values())
                .filter(i -> i.getName().equals(name))
                .findFirst()
                .orElse(null);
    }

    public static InterpolatorType byInterpolator(Interpolator interpolator) {
        return Arrays.stream(values())
                .filter(i -> i.getInterpolator().equals(interpolator))
                .findFirst()
                .orElse(null);
    }

    public String getName() {
        return name;
    }

    public Interpolator getInterpolator() {
        return interpolator;
    }
}
