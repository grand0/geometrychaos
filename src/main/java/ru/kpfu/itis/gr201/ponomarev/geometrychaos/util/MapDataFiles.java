package ru.kpfu.itis.gr201.ponomarev.geometrychaos.util;

import ru.kpfu.itis.gr201.ponomarev.geometrychaos.editor.exception.LevelLoadException;
import ru.kpfu.itis.gr201.ponomarev.geometrychaos.editor.exception.LevelSaveException;

import java.io.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

public class MapDataFiles {

    public static final String AUDIO_FILE_NAME = "audio";
    public static final String LEVEL_FILE_NAME = "level";

    private final File audio;
    private final File level;

    public MapDataFiles(File audio, File level) {
        this.audio = audio;
        this.level = level;
    }

    public static MapDataFiles read(File file) throws LevelLoadException {
        try (InputStream in = new FileInputStream(file)) {
            return read(in.readAllBytes());
        } catch (IOException e) {
            throw new LevelLoadException(e);
        }
    }

    public static MapDataFiles read(byte[] bytes) throws LevelLoadException {
        File audio = null, level = null; // TODO: change temp files with byte arrays
        try (ZipInputStream zin = new ZipInputStream(new ByteArrayInputStream(bytes))) {
            ZipEntry entry;
            while ((entry = zin.getNextEntry()) != null) {
                File tmp = File.createTempFile("gcmap", null);
                tmp.deleteOnExit();
                if (entry.getName().equals(AUDIO_FILE_NAME)) {
                    try (FileOutputStream fos = new FileOutputStream(tmp)) {
                        byte[] buf = zin.readAllBytes();
                        fos.write(buf);
                        fos.flush();
                    }
                    audio = tmp;
                } else if (entry.getName().equals(LEVEL_FILE_NAME)) {
                    try (FileOutputStream fos = new FileOutputStream(tmp)) {
                        byte[] buf = zin.readAllBytes();
                        fos.write(buf);
                        fos.flush();
                    }
                    level = tmp;
                } else {
                    throw new LevelLoadException("Unknown files in archive.");
                }
                zin.closeEntry();
            }
        } catch (IOException e) {
            throw new LevelLoadException(e);
        }
        return new MapDataFiles(audio, level);
    }

    public void write(File file) throws LevelSaveException {
        try (ZipOutputStream zout = new ZipOutputStream(new FileOutputStream(file));
                FileInputStream audioIn = audio == null ? null : new FileInputStream(audio);
                FileInputStream levelIn = level == null ? null : new FileInputStream(level)) {
            if (audioIn != null) {
                ZipEntry entry = new ZipEntry(AUDIO_FILE_NAME);
                zout.putNextEntry(entry);
                byte[] buf = audioIn.readAllBytes();
                zout.write(buf);
                zout.closeEntry();
            }
            if (levelIn != null) {
                ZipEntry entry = new ZipEntry(LEVEL_FILE_NAME);
                zout.putNextEntry(entry);
                byte[] buf = levelIn.readAllBytes();
                zout.write(buf);
                zout.closeEntry();
            }
            zout.flush();
        } catch (IOException e) {
            throw new LevelSaveException();
        }
    }

    public File getAudio() {
        return audio;
    }

    public File getLevel() {
        return level;
    }
}
