package ru.kpfu.itis.gr201.ponomarev.geometrychaos.commons.anim.randomizer;

public abstract class DoubleValueRandomizer implements ValueRandomizer {
    private final double startValue;
    private final double endValue;

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
