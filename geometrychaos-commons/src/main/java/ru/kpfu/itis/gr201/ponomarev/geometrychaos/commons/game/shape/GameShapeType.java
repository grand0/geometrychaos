package ru.kpfu.itis.gr201.ponomarev.geometrychaos.commons.game.shape;

import ru.kpfu.itis.gr201.ponomarev.geometrychaos.commons.game.shape.setting.ShapeDropdownSetting;
import ru.kpfu.itis.gr201.ponomarev.geometrychaos.commons.game.shape.setting.ShapeSetting;
import ru.kpfu.itis.gr201.ponomarev.geometrychaos.commons.game.shape.setting.ShapeStringSetting;
import ru.kpfu.itis.gr201.ponomarev.geometrychaos.commons.util.TextOrigin;

import java.util.function.Supplier;

public enum GameShapeType {
    SQUARE("Square"),
    CIRCLE("Circle"),
    TRIANGLE("Triangle"),
    HEXAGON("Hexagon"),
    TEXT(
            "Text",
            () -> new ShapeStringSetting("Text"),
            () -> new ShapeDropdownSetting<>("Origin", TextOrigin.class, TextOrigin.CENTER)
    );

    public static final double DEFAULT_SHAPE_SIZE = 100;

    private final String name;
    private final Supplier<ShapeSetting>[] settingsSuppliers;

    @SuppressWarnings("unchecked")
    GameShapeType(String name) {
        this.name = name;
        settingsSuppliers = new Supplier[0];
    }

    @SafeVarargs
    GameShapeType(String name, Supplier<ShapeSetting>... settingsSuppliers) {
        this.name = name;
        this.settingsSuppliers = settingsSuppliers;
    }

    public String getName() {
        return name;
    }

    public Supplier<ShapeSetting>[] getSettingsSuppliers() {
        return settingsSuppliers;
    }

    public static GameShapeType byName(String name) {
        for (GameShapeType s : values()) {
            if (s.name.equals(name)) {
                return s;
            }
        }
        return null;
    }
}
