package ru.kpfu.itis.gr201.ponomarev.geometrychaos.commons.game.shape.setting;

import java.util.Arrays;

public class ShapeDropdownSetting<E extends Enum<E>> extends ShapeSetting {

    private final Class<E> enumClass;
    private E value;

    public ShapeDropdownSetting(String name, Class<E> enumClass, E defaultValue) {
        super(name);
        this.enumClass = enumClass;
        this.value = defaultValue;
    }

    public String[] getEnumNames() {
        return Arrays.stream(enumClass.getEnumConstants()).map(Enum::toString).toArray(String[]::new);
    }

    @Override
    public E getValue() {
        return value;
    }

    @Override
    public void setValue(Object o) {
        if (o instanceof String s) {
            value = Enum.valueOf(enumClass, s);
        } else {
            value = (E) o;
        }
    }
}
