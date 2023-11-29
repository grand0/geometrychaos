package ru.kpfu.itis.gr201.ponomarev.bheditor.util.converter;

import javafx.util.StringConverter;
import ru.kpfu.itis.gr201.ponomarev.bheditor.game.Shape;

public class ShapeStringConverter extends StringConverter<Shape> {
    @Override
    public String toString(Shape object) {
        return object == null ? "Not selected" : object.getName();
    }

    @Override
    public Shape fromString(String string) {
        Shape shape = Shape.byName(string);
        return shape == null ? Shape.SQUARE : shape;
    }
}