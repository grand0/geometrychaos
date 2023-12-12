package ru.kpfu.itis.gr201.ponomarev.geometrychaos.editor.audio;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.UnsupportedAudioFileException;
import java.io.File;
import java.io.IOException;

public class LevelAudio {

    private final File file;
    private final int[][] samples;
    private final int min;
    private final int max;
    private final int length;
    private final int channels;
    private final float rate;

    public LevelAudio(File file) throws UnsupportedAudioFileException, IOException {
        this.file = file;
        AudioInputStream ais = AudioSystem.getAudioInputStream(file);
        length = (int) ais.getFrameLength();
        rate = ais.getFormat().getFrameRate();
        int frameSize = ais.getFormat().getFrameSize();
        byte[] bytes = new byte[frameSize * length];
        ais.read(bytes);
        channels = ais.getFormat().getChannels();
        samples = new int[channels][length];
        for (int bytesIndex = 0, sampleIndex = 0; bytesIndex < bytes.length; sampleIndex++) {
            for (int c = 0; c < channels; c++) {
                int lo = bytes[bytesIndex++];
                int hi = bytes[bytesIndex++];
                int sample = getSixteenBitSample(hi, lo);
                samples[c][sampleIndex] = sample;
            }
        }

        int min = Integer.MAX_VALUE, max = Integer.MIN_VALUE;
        for (int c = 0; c < channels; c++) {
            for (int sample : samples[c]) {
                min = Math.min(min, sample);
                max = Math.max(max, sample);
            }
        }
        this.min = min;
        this.max = max;
    }

    public int getSample(int index, int channel) {
        return samples[channel][index];
    }

    public File getFile() {
        return file;
    }

    public int getChannels() {
        return channels;
    }

    public int getLength() {
        return length;
    }

    public int getMin() {
        return min;
    }

    public int getMax() {
        return max;
    }

    private int getSixteenBitSample(int hi, int lo) {
        return (hi << 8) + (lo & 0x00ff);
    }

    public float getRate() {
        return rate;
    }
}
