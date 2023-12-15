package ru.kpfu.itis.gr201.ponomarev.geometrychaos.common.anim.randomizer;

public enum ValueRandomizerType {
    DOUBLE_LINEAR("Linear"),
    DOUBLE_DISCRETE("Discrete"),
    ;

    private final String name;

    ValueRandomizerType(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }
}
