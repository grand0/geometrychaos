package ru.kpfu.itis.gr201.ponomarev.geometrychaos.commons.game.shape;

import ru.kpfu.itis.gr201.ponomarev.geometrychaos.commons.game.shape.setting.ShapeSetting;

public class GameShape {
    private final GameShapeType type;
    private final ShapeSetting[] settings;

    public GameShape(GameShapeType type) {
        this.type = type;
        this.settings = new ShapeSetting[type.getSettingsSuppliers().length];
        for (int i = 0; i < settings.length; i++) {
            settings[i] = type.getSettingsSuppliers()[i].get();
        }
    }

    public GameShapeType getType() {
        return type;
    }

    public ShapeSetting[] getSettings() {
        return settings;
    }
}
