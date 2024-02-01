package ru.kpfu.itis.gr201.ponomarev.geometrychaos.commons.game.shape.setting;

public class ShapeStringSetting extends ShapeSetting {
    private String value;

    public ShapeStringSetting(String name) {
        super(name);
        value = "";
    }

    @Override
    public String getValue() {
        return value;
    }

    @Override
    public void setValue(Object o) {
        this.value = o.toString();
    }
}
