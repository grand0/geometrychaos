package ru.kpfu.itis.gr201.ponomarev.geometrychaos.commons.anim.randomizer.impl;

import ru.kpfu.itis.gr201.ponomarev.geometrychaos.commons.anim.randomizer.DoubleValueRandomizer;
import ru.kpfu.itis.gr201.ponomarev.geometrychaos.commons.anim.randomizer.GlobalRandom;
import ru.kpfu.itis.gr201.ponomarev.geometrychaos.commons.anim.randomizer.ValueRandomizerType;

public class LinearDoubleValueRandomizer extends DoubleValueRandomizer {

    public LinearDoubleValueRandomizer(double startValue, double endValue) {
        super(startValue, endValue);
    }

    @Override
    public Object randomize() {
        return GlobalRandom.getInstance().nextDouble(getStartValue(), getEndValue());
    }

    @Override
    public ValueRandomizerType getType() {
        return ValueRandomizerType.DOUBLE_LINEAR;
    }
}
