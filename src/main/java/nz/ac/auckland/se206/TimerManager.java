package nz.ac.auckland.se206;

import java.io.IOException;
import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.util.Duration;

public class TimerManager {

  private static TimerManager instance;

  public static String formatTime(int totalSeconds) {
    // Formats the time displayed into minutes and seconds
    int minutes = totalSeconds / 60;
    int seconds = totalSeconds % 60;
    return String.format("%02d:%02d", minutes, seconds);
  }

  public static TimerManager getInstance() {
    if (instance == null) {
      instance = new TimerManager();
    }
    return instance;
  }

  public static String getAccentColor(double progress) {
    // Colour changes depending on duration
    if (progress >= 0.8) {
      return "red";
    } else if (progress >= 0.5) {
      return "orange";
    } else {
      return "green";
    }
  }

  private final int totalSeconds = 120;
  private final IntegerProperty secondsRemaining = new SimpleIntegerProperty(totalSeconds);
  private final DoubleProperty progress = new SimpleDoubleProperty(0);
  private Timeline timeline;

  private TimerManager() {}

  public void start() {
    // Starts 2:00 timer
    if (timeline != null && timeline.getStatus() == Animation.Status.RUNNING) {
      return;
    }

    timeline =
        new Timeline(
            new KeyFrame(
                Duration.seconds(1),
                e -> {
                  int remaining = secondsRemaining.get() - 1;
                  secondsRemaining.set(remaining);
                  progress.set((double) (totalSeconds - remaining) / totalSeconds);

                  if (remaining <= 0) {
                    timeline.stop();
                    try {
                      App.openFinalPage();
                    } catch (IOException e1) {
                      e1.printStackTrace();
                    }
                  }
                }));
    timeline.setCycleCount(totalSeconds);
    timeline.play();
  }

  public IntegerProperty getSecondsRemainingProperty() {
    return secondsRemaining;
  }

  public DoubleProperty getProgressProperty() {
    return progress;
  }

  public void stop() {
    if (timeline != null) {
      timeline.stop();
      secondsRemaining.set(totalSeconds);
      progress.set(0);
    }
  }
}
