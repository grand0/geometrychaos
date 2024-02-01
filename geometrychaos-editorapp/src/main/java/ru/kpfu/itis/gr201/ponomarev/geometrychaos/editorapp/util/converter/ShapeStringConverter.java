package ru.kpfu.itis.gr201.ponomarev.geometrychaos.editorapp.util.converter;

import javafx.util.StringConverter;
import ru.kpfu.itis.gr201.ponomarev.geometrychaos.commons.game.shape.GameShapeType;

public class ShapeStringConverter extends StringConverter<GameShapeType> {
    @Override
    public String toString(GameShapeType object) {
        return object == null ? "Not selected" : object.getName();
    }

    @Override
    public GameShapeType fromString(String string) {
        GameShapeType gameShapeType = GameShapeType.byName(string);
        return gameShapeType == null ? GameShapeType.SQUARE : gameShapeType;
    }
}
