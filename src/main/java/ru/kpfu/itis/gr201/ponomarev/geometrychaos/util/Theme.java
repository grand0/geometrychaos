package ru.kpfu.itis.gr201.ponomarev.geometrychaos.util;

import javafx.scene.paint.Color;
import javafx.scene.text.Font;

public class Theme {

    /*
    Original palette:
    #191825
    #865DFF
    #E384FF
    #FFA3FD
     */

    public static final Color BACKGROUND = Color.web("#191825");
    public static final Color PRIMARY = Color.web("#865DFF");
    public static final Color ACCENT = Color.web("#E384FF");
    public static final Color ON_BACKGROUND = Color.web("#EFD2FF");
    public static final Color GAME_FIELD_BACKGROUND = Color.rgb(10, 0, 24);
    public static final Color SELECTED_OBJECT_OUTLINE = ACCENT;
    public static final Color PLAYER = Color.web("#A581FF");

    public static final Color RAINBOW_START_COLOR = Color.rgb(255, 0, 0, 0.2);

    public static final Font HEADLINE_FONT = Font.loadFont(Theme.class.getResource("/fonts/Gruppo.ttf").toExternalForm(), 36);
}
