package ru.kpfu.itis.gr201.ponomarev.bheditor.ui;

import javafx.beans.property.IntegerProperty;
import javafx.scene.control.Button;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;
import javafx.scene.layout.HBox;
import javafx.util.Duration;
import ru.kpfu.itis.gr201.ponomarev.bheditor.util.converter.DurationStringConverter;

import java.util.function.Consumer;

public class ObjectsTimelineControls extends HBox {

    private final Button playPauseButton;
    private final Button stopButton;
    private final Button addTimelineButton;
    private final Spinner<Duration> timeSpinner;

    private boolean playing = false;

    public ObjectsTimelineControls(IntegerProperty time) {
        super(12);

        playPauseButton = new Button("Play");
        playPauseButton.setOnAction(event -> defaultPlayPauseBehaviour());

        stopButton = new Button("Stop");
        stopButton.setOnAction(event -> defaultStopBehaviour());

        addTimelineButton = new Button("Object");

        SpinnerValueFactory<Duration> valueFactory = new SpinnerValueFactory<>() {
            @Override
            public void decrement(int steps) {
                setValue(new Duration(Math.max(0, getValue().toMillis() - steps)));
            }

            @Override
            public void increment(int steps) {
                setValue(new Duration(getValue().toMillis() + steps));
            }
        };
        valueFactory.setConverter(new DurationStringConverter());
        valueFactory.setValue(new Duration(0));
        timeSpinner = new Spinner<>(valueFactory);
        timeSpinner.setEditable(true);
        timeSpinner.valueProperty().addListener(obs -> {
            time.set((int) timeSpinner.getValue().toMillis());
        });

        time.addListener(obs -> {
            timeSpinner.getEditor().setText(valueFactory.getConverter().toString(new Duration(time.get())));
        });

        getChildren().addAll(playPauseButton, stopButton, addTimelineButton, timeSpinner);
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
