package ru.kpfu.itis.gr201.ponomarev.geometrychaos.common.io.map;

import ru.kpfu.itis.gr201.ponomarev.geometrychaos.common.io.exception.LevelReadException;
import ru.kpfu.itis.gr201.ponomarev.geometrychaos.common.io.exception.MapReadException;
import ru.kpfu.itis.gr201.ponomarev.geometrychaos.common.io.exception.MapWriteException;

import java.io.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

public class MapIO {

    public static final String AUDIO_FILE_NAME = "audio";
    public static final String LEVEL_FILE_NAME = "level";

    private MapIO() {}

    public static MapDataFiles read(File file) throws MapReadException {
        try (InputStream in = new FileInputStream(file)) {
            return read(in.readAllBytes());
        } catch (IOException e) {
            throw new MapReadException(e);
        }
    }

    public static MapDataFiles read(byte[] bytes) throws MapReadException {
        File audio = null, level = null;
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
                    throw new LevelReadException("Unknown files in archive.");
                }
                zin.closeEntry();
            }
        } catch (IOException e) {
            throw new MapReadException(e);
        }
        return new MapDataFiles(audio, level);
    }

    public static void write(File file, MapDataFiles data) throws MapWriteException {
        try (ZipOutputStream zout = new ZipOutputStream(new FileOutputStream(file));
             FileInputStream audioIn = data.getAudio() == null ? null : new FileInputStream(data.getAudio());
             FileInputStream levelIn = data.getLevel() == null ? null : new FileInputStream(data.getLevel())) {
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
            throw new MapWriteException();
        }
    }
}
