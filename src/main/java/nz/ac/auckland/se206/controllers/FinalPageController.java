package nz.ac.auckland.se206.controllers;

import java.io.File;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.util.Duration;
import nz.ac.auckland.apiproxy.exceptions.ApiProxyException;
import nz.ac.auckland.se206.TimerManager;

public class FinalPageController {

  @FXML private Label timer;
  @FXML private VBox overlay;
  @FXML private VBox overlaySucess;
  @FXML private VBox overlayFailure;
  @FXML private Button yesButton;
  @FXML private Button noButton;

  private Timeline timeline;
  private final int totalSeconds = 60;
  private int remainingSeconds = totalSeconds;

  /**
   * Initializes the final page.
   *
   * @throws ApiProxyException if there is an error communicating with the API proxy
   */
  @FXML
  public void initialize() throws ApiProxyException {
    // Stop 2:00 timer and play flashback tts
    TimerManager.getInstance().stop();

    String audioFile = "src/main/resources/sounds/oneMinuteLeft.mp3";

    Media sound = new Media(new File(audioFile).toURI().toString());
    MediaPlayer mediaPlayer = new MediaPlayer(sound);

    mediaPlayer.play();

    timer.setText(String.format("01:00"));

    // Start 60s timer
    timeline =
        new Timeline(
            new KeyFrame(
                Duration.seconds(1),
                e -> {
                  remainingSeconds--;
                  timer.setText(String.format("00:%02d", remainingSeconds % 60));

                  if (remainingSeconds <= 0) {
                    timeline.stop();
                    showOverlay();
                    String audioFile2 = "src/main/resources/sounds/gameOver.mp3";

                    Media sound2 = new Media(new File(audioFile2).toURI().toString());
                    MediaPlayer mediaPlayer2 = new MediaPlayer(sound2);

                    mediaPlayer2.play();
                  }
                }));
    timeline.setCycleCount(totalSeconds);
    timeline.play();
  }

  private void showOverlay() {
    Platform.runLater(
        () -> {
          overlay.setVisible(true);
        });
  }

  @FXML
  private void onYesClick() {
    overlaySucess.setVisible(true);
    timeline.stop();
  }

  @FXML
  private void onNoClick() {
    overlayFailure.setVisible(true);
    timeline.stop();
  }
}
