package ru.kpfu.itis.gr201.ponomarev.geometrychaos.gameapp.util;

import javafx.scene.media.AudioSpectrumListener;
import javafx.scene.media.MediaPlayer;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class GlobalAudioSpectrumProvider {

    private static MediaPlayer player;
    private static final List<Consumer<float[]>> listeners = new ArrayList<>();

    private GlobalAudioSpectrumProvider() {}

    public static void registerForMediaPlayer(MediaPlayer pl) {
        if (player != null) {
            player.setAudioSpectrumListener(null);
        }
        player = pl;
        if (player != null) {
            player.setAudioSpectrumInterval(0.05);
            player.setAudioSpectrumListener(new GlobalAudioSpectrumListener());
        }
    }

    public static void addListener(Consumer<float[]> callback) {
        listeners.add(callback);
    }

    public static void removeListener(Consumer<float[]> callback) {
        listeners.remove(callback);
    }

    private static class GlobalAudioSpectrumListener implements AudioSpectrumListener {
        @Override
        public void spectrumDataUpdate(double timestamp, double duration, float[] magnitudes, float[] phases) {
            for (Consumer<float[]> callback : listeners) {
                callback.accept(magnitudes);
            }
        }
    }
}
