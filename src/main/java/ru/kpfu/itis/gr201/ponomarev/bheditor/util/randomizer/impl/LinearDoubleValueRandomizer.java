package ru.kpfu.itis.gr201.ponomarev.bheditor.util.randomizer.impl;

import ru.kpfu.itis.gr201.ponomarev.bheditor.util.randomizer.DoubleValueRandomizer;

public class LinearDoubleValueRandomizer extends DoubleValueRandomizer {

    public LinearDoubleValueRandomizer(double startValue, double endValue) {
        super(startValue, endValue);
    }

    @Override
    public Object randomize() {
        return rng.nextDouble(getStartValue(), getEndValue());
    }

    @Override
    public String getName() {
        return "Linear";
    }
}
