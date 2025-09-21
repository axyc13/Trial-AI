package nz.ac.auckland.se206.controllers;

import java.io.IOException;
import javafx.animation.FadeTransition;
import javafx.animation.PauseTransition;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import javafx.util.Duration;
import nz.ac.auckland.apiproxy.exceptions.ApiProxyException;
import nz.ac.auckland.se206.App;
import nz.ac.auckland.se206.DraggableMaker;

/**
 * Controller class for the chat view. Handles user interactions and communication with the GPT
 * model via the API proxy.
 */
public class HumanWitnessMemoryController extends ChatControllerCentre {

  private int currentSongIndex = 0;
  private String[] songs = {
    "Song 1", "Song 2", "Song 3", "Song 4", "Song 5", "Song 6", "Song 7", "Song 8"
  };

  @FXML private TextArea txtaChat;
  @FXML private Label timer;
  @FXML private ImageView cassetteTape;
  @FXML private VBox flashbackMessage;
  @FXML private Label songLabel;

  @Override
  @FXML
  public void initialize() {
    try {
      super.initialize();
    } catch (ApiProxyException e) {
      e.printStackTrace();
    }

    // Disable and turn visibility off the cassette tape
    cassetteTape.setVisible(false);
    cassetteTape.setDisable(true);

    // Set initial song
    songLabel.setText(songs[currentSongIndex]);

    // Display flashback message at the beginning
    flashbackMessage.setVisible(true);
    Platform.runLater(
        () -> {
          PauseTransition pause = new PauseTransition(Duration.seconds(1));
          pause.setOnFinished(e -> flashbackMessage.setVisible(false));
          pause.play();
        });

    DraggableMaker.makeDraggable(cassetteTape);
  }

  @FXML
  private void turnOnCassetteTape() {
    // Enable and turn visibility on the cassette tape
    cassetteTape.setVisible(true);
    cassetteTape.setDisable(false);
  }

  @FXML
  private void goToNextSong() {
    // Fade transitions
    FadeTransition fadeOut = new FadeTransition(Duration.millis(300), songLabel);
    fadeOut.setFromValue(1.0);
    fadeOut.setToValue(0.0);

    FadeTransition fadeIn = new FadeTransition(Duration.millis(300), songLabel);
    fadeIn.setFromValue(0.0);
    fadeIn.setToValue(1.0);

    // Play animation
    fadeOut.setOnFinished(
        e -> {
          // Update label
          currentSongIndex = (currentSongIndex + 1) % songs.length;
          songLabel.setText(songs[currentSongIndex]);
          fadeIn.play();
        });

    fadeOut.play();
  }

  @FXML
  private void goToPreviousSong() {
    // Fade transitions
    FadeTransition fadeOut = new FadeTransition(Duration.millis(300), songLabel);
    fadeOut.setFromValue(1.0);
    fadeOut.setToValue(0.0);

    FadeTransition fadeIn = new FadeTransition(Duration.millis(300), songLabel);
    fadeIn.setFromValue(0.0);
    fadeIn.setToValue(1.0);

    // Play animation
    fadeOut.setOnFinished(
        e -> {
          // Update label
          currentSongIndex = (currentSongIndex - 1 + songs.length) % songs.length;
          songLabel.setText(songs[currentSongIndex]);
          fadeIn.play();
        });

    fadeOut.play();
  }

  @FXML
  private void onGoBack(ActionEvent event) throws ApiProxyException, IOException {
    App.setRoot("room");
  }
}
