package ru.kpfu.itis.gr201.ponomarev.bheditor.ui.dialog;

import javafx.stage.FileChooser;
import javafx.stage.Window;

import java.io.File;
import java.util.*;

public class OpenAudioDialog {

    private static final List<String> AUDIO_FILE_FORMATS;
    private Window owner;
    private final FileChooser fileChooser;

    static {
        AUDIO_FILE_FORMATS = new ArrayList<>();
        AUDIO_FILE_FORMATS.add("*.wav");
        AUDIO_FILE_FORMATS.add("*.mp3");
    }

    public OpenAudioDialog() {
        fileChooser = new FileChooser();
        fileChooser.setTitle("Open audio file");
        fileChooser.setSelectedExtensionFilter(new FileChooser.ExtensionFilter("Audio", AUDIO_FILE_FORMATS));
    }

    public Optional<File> showAndWait() {
        File file = fileChooser.showOpenDialog(owner);
        return Optional.ofNullable(file);
    }

    public Window getOwner() {
        return owner;
    }

    public void setOwner(Window owner) {
        this.owner = owner;
    }
}
