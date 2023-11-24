package ru.kpfu.itis.gr201.ponomarev.bheditor.ui;

import javafx.scene.control.Button;
import javafx.scene.layout.HBox;

import java.util.function.Consumer;

public class TimelineControlButtons extends HBox {

    private final Button playPauseButton;
    private final Button stopButton;
    private final Button addTimelineButton;

    private boolean playing = false;

    public TimelineControlButtons() {
        super(12);

        playPauseButton = new Button("Play");
        playPauseButton.setOnAction(event -> defaultPlayPauseBehaviour());

        stopButton = new Button("Stop");
        stopButton.setOnAction(event -> defaultStopBehaviour());

        addTimelineButton = new Button("Object");

        getChildren().addAll(playPauseButton, stopButton, addTimelineButton);
    }

    public void setPlayPauseListener(Consumer<Boolean> handler) {
        playPauseButton.setOnAction(event -> {
            defaultPlayPauseBehaviour();
            handler.accept(playing);
        });
    }

    public void setStopListener(Runnable handler) {
        stopButton.setOnAction(event -> {
            defaultStopBehaviour();
            handler.run();
        });
    }

    public void setAddTimelineListener(Runnable handler) {
        addTimelineButton.setOnAction(event -> handler.run());
    }

    public void setPlaying(boolean playing) {
        this.playing = playing;
        playPauseButton.setText(playing ? "Pause" : "Play");
        addTimelineButton.setDisable(playing);
    }

    private void defaultPlayPauseBehaviour() {
        setPlaying(!playing);
    }

    private void defaultStopBehaviour() {
        setPlaying(false);
    }
}
