package ru.kpfu.itis.gr201.ponomarev.geometrychaos.commons.ui;

import javafx.scene.Scene;
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
    public static final Color[] PLAYERS_COLORS = new Color[] {
            Color.web("#81F0FF"),
            Color.web("#CEFC4E"),
            Color.web("#FCB74E"),
            Color.web("#FA6464"),
    };

    public static final Color RAINBOW_START_COLOR = Color.rgb(255, 0, 0, 0.2);

//    public static final Font HEADLINE_FONT = Font.loadFont(Theme.class.getResourceAsStream("/fonts/Gruppo.ttf"), 36);
//    public static final Font LABEL_FONT = Font.loadFont(Theme.class.getResourceAsStream("/fonts/Rubik-Regular.ttf"), 14);

    static {
        Font.loadFont(Theme.class.getResourceAsStream("/fonts/Gruppo.ttf"), 36);
        Font.loadFont(Theme.class.getResourceAsStream("/fonts/Rubik-Regular.ttf"), 14);
    }

    public static void applyStylesheetsToScene(Scene scene) {
        scene.getStylesheets().add(Theme.class.getResource("/styles/style.css").toExternalForm());
    }
}
