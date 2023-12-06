package ru.kpfu.itis.gr201.ponomarev.bheditor.anim;

public enum KeyFrameTag {
    POSITION_X("PosX"),
    POSITION_Y("PosY"),
    SCALE_X("ScaleX"),
    SCALE_Y("ScaleY"),
    ROTATION("Rot"),
    PIVOT_X("PivotX"),
    PIVOT_Y("PivotY"),
    HIGHLIGHT("Highlight"),
    STROKE("Stroke")
    ;

    private final String name;

    KeyFrameTag(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }
}
