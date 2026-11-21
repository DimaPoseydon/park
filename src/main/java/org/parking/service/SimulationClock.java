package org.parking.service;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.util.Duration;
import java.time.LocalDateTime;

public class SimulationClock {
    private final ObjectProperty<LocalDateTime> currentTime;
    private final Timeline timeline;

    public SimulationClock() {
        LocalDateTime startTime = LocalDateTime.of(2027, 1, 1, 8, 0, 0);
        this.currentTime = new SimpleObjectProperty<>(startTime);

        this.timeline = new Timeline(new KeyFrame(Duration.seconds(1), event -> {
            currentTime.set(currentTime.get().plusMinutes(1));
        }));
        this.timeline.setCycleCount(Timeline.INDEFINITE);
    }

    public void start() {
        timeline.play();
    }

    public void stop() {
        timeline.stop();
    }

    public LocalDateTime getCurrentTime() {
        return currentTime.get();
    }

    public ObjectProperty<LocalDateTime> currentTimeProperty() {
        return currentTime;
    }
}
