package ru.kpfu.itis.gr201.ponomarev.geometrychaos.util;

import java.nio.file.Path;

public class AppDirs {

    private static final String APP_DIR = ".geometrychaos";

    public static final Path rootDir;
    public static final Path mapsCache;

    static {
        Path home = Path.of(System.getProperty("user.home"));
        rootDir = home.resolve(APP_DIR);
        mapsCache = home.resolve("maps");
    }

    private AppDirs() { }
}
