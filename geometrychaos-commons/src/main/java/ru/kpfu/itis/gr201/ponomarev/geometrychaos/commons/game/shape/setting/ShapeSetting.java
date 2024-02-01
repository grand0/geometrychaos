package ru.kpfu.itis.gr201.ponomarev.geometrychaos.commons.game.shape.setting;

public abstract class ShapeSetting {
    private final String name;

    public ShapeSetting(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public abstract Object getValue();

    public abstract void setValue(Object o);
}
