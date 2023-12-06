package ru.kpfu.itis.gr201.ponomarev.bheditor.game;

public enum Shape {
    SQUARE("Square"),
    CIRCLE("Circle"),
    TRIANGLE("Triangle"),
    HEXAGON("Hexagon");

    public static final double DEFAULT_SHAPE_SIZE = 100;

    private final String name;

    Shape(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public static Shape byName(String name) {
        for (Shape s : values()) {
            if (s.name.equals(name)) {
                return s;
            }
        }
        return null;
    }
}
