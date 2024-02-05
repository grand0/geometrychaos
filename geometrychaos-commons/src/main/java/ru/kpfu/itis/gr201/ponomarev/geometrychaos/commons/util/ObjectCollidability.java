package ru.kpfu.itis.gr201.ponomarev.geometrychaos.commons.util;

public enum ObjectCollidability {
    OPACITY_BASED("Opacity-based"),
    ALWAYS("Always"),
    NEVER("Never");

    private final String name;

    ObjectCollidability(String name) {
        this.name = name;
    }

    public static ObjectCollidability byName(String name) {
        for (ObjectCollidability oc : values()) {
            if (oc.name.equals(name)) {
                return oc;
            }
        }
        return null;
    }

    public String getName() {
        return name;
    }

    @Override
    public String toString() {
        return name;
    }
}
