package ru.kpfu.itis.gr201.ponomarev.geometrychaos.commons.anim.randomizer;

import java.util.Random;

public class GlobalRandom {

    private final static Random rng;

    static {
        rng = new Random();
    }

    private GlobalRandom() {}

    public static Random getInstance() {
        return rng;
    }

    public static void setSeed(long seed) {
        rng.setSeed(seed);
    }
}
