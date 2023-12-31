package ru.kpfu.itis.gr201.ponomarev.geometrychaos.commons.anim.randomizer.impl;

import ru.kpfu.itis.gr201.ponomarev.geometrychaos.commons.anim.randomizer.DoubleValueRandomizer;
import ru.kpfu.itis.gr201.ponomarev.geometrychaos.commons.anim.randomizer.GlobalRandom;
import ru.kpfu.itis.gr201.ponomarev.geometrychaos.commons.anim.randomizer.ValueRandomizerType;

public class DiscreteDoubleValueRandomizer extends DoubleValueRandomizer {

    private final double step;

    public DiscreteDoubleValueRandomizer(double startValue, double endValue, double step) {
        super(startValue, endValue);
        this.step = step;
    }

    @Override
    public Object randomize() {
        double val = GlobalRandom.getInstance().nextDouble(getStartValue(), getEndValue());
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
