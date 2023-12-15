package ru.kpfu.itis.gr201.ponomarev.geometrychaos.common.io.map;

import java.io.File;

public class MapDataFiles {

    // TODO: change to byte arrays?
    private final File audio;
    private final File level;

    public MapDataFiles(File audio, File level) {
        this.audio = audio;
        this.level = level;
    }

    public File getAudio() {
        return audio;
    }

    public File getLevel() {
        return level;
    }
}
