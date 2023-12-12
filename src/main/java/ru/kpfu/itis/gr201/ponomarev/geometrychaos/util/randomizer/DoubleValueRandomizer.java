package ru.kpfu.itis.gr201.ponomarev.geometrychaos.util.randomizer;

import java.util.Random;

public abstract class DoubleValueRandomizer implements ValueRandomizer {
    private final double startValue;
    private final double endValue;
    protected final static Random rng;

    static {
        rng = new Random();
    }

    public DoubleValueRandomizer(double startValue, double endValue) {
        this.startValue = startValue;
        this.endValue = endValue;
    }

    public double getStartValue() {
        return startValue;
    }

    public double getEndValue() {
        return endValue;
    }
}
