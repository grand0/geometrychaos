package ru.kpfu.itis.gr201.ponomarev.bheditor.util.randomizer.impl;

import ru.kpfu.itis.gr201.ponomarev.bheditor.util.randomizer.DoubleValueRandomizer;
import ru.kpfu.itis.gr201.ponomarev.bheditor.util.randomizer.ValueRandomizerType;

public class DiscreteDoubleValueRandomizer extends DoubleValueRandomizer {

    private final double step;

    public DiscreteDoubleValueRandomizer(double startValue, double endValue, double step) {
        super(startValue, endValue);
        this.step = step;
    }

    @Override
    public Object randomize() {
        double val = rng.nextDouble(getStartValue(), getEndValue());
        double delta = val - getStartValue();
        int steps = (int) Math.round(delta / getStep());
        return getStartValue() + getStep() * steps;
    }

    public double getStep() {
        return step;
    }

    @Override
    public ValueRandomizerType getType() {
        return ValueRandomizerType.DOUBLE_DISCRETE;
    }
}
