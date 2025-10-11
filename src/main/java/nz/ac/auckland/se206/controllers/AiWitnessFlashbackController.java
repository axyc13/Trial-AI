package nz.ac.auckland.se206.controllers;

import java.io.File;
import java.io.IOException;
import javafx.animation.PauseTransition;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Slider;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.util.Duration;
import nz.ac.auckland.se206.AiWitnessStateManager;
import nz.ac.auckland.se206.App;
import nz.ac.auckland.se206.TimerManager;

public class AiWitnessFlashbackController extends ChatControllerCentre {

  @FXML private ImageView flashbackOne;
  @FXML private ImageView flashbackTwo;
  @FXML private ImageView flashbackThree;
  @FXML private Button continueButton;
  @FXML private Slider flashbackSlider;
  @FXML private VBox flashbackMessage;

  @FXML
  private void onContinueFlashback() throws IOException {
    // Only proceed to memory scene when button is clicked after reaching third flashback
    if (flashbackSlider.getValue() == 3) {
      // Transition to memory scene
      FXMLLoader loader = new FXMLLoader(App.class.getResource("/fxml/aiWitness.fxml"));
      Parent root = loader.load();
      ChatControllerCentre chatController = loader.getController();
      chatController.initialiseChatGpt("aiWitness.txt", "AI Witness");
      App.getScene().setRoot(root);
    }
  }

  /**
   * Handles flashback changes based on slider value (1-3 only)
   *
   * @param targetState The target flashback state (1-3)
   */
  private void handleFlashbackChange(int targetState) throws IOException {
    AiWitnessStateManager.getInstance().setFlashbackState(targetState);

    // Hide continue button by default
    if (continueButton != null) {
      continueButton.setVisible(false);
    }

    switch (targetState) {
      case 1:
        // Show first flashback
        flashbackOne.setVisible(true);
        flashbackTwo.setVisible(false);
        flashbackThree.setVisible(false);
        break;
      case 2:
        // Show second flashback
        flashbackOne.setVisible(false);
        flashbackTwo.setVisible(true);
        flashbackThree.setVisible(false);
        break;
      case 3:
        // Show third flashback and enable continue button
        flashbackOne.setVisible(false);
        flashbackTwo.setVisible(false);
        flashbackThree.setVisible(true);
        if (continueButton != null) {
          continueButton.setVisible(true);
          continueButton.setText("CONTINUE");
        }
        break;
    }
  }

  @Override
  public void initialize() {
    try {
      super.initialize(); // This will handle the timer setup
      TimerManager.getInstance().start();

      flashbackMessage.setVisible(true);
      Platform.runLater(
          () -> {
            PauseTransition pause = new PauseTransition(Duration.seconds(1));
            pause.setOnFinished(e -> flashbackMessage.setVisible(false));
            String audioFile = "src/main/resources/sounds/flashback.mp3";

            Media sound = new Media(new File(audioFile).toURI().toString());
            MediaPlayer mediaPlayer = new MediaPlayer(sound);

            mediaPlayer.play();
            pause.play();
          });

      // Initialize flashback UI
      if (flashbackOne != null && flashbackSlider != null) {
        // Get saved state
        int state = AiWitnessStateManager.getInstance().getFlashbackState();

        // Set up UI based on saved state
        switch (state) {
          case 1:
            flashbackOne.setVisible(true);
            flashbackTwo.setVisible(false);
            flashbackThree.setVisible(false);
            flashbackSlider.setValue(1);
            if (continueButton != null) {
              continueButton.setVisible(false);
            }
            break;
          case 2:
            flashbackOne.setVisible(false);
            flashbackTwo.setVisible(true);
            flashbackThree.setVisible(false);
            flashbackSlider.setValue(2);
            if (continueButton != null) {
              continueButton.setVisible(false);
            }
            break;
          case 3:
            flashbackOne.setVisible(false);
            flashbackTwo.setVisible(false);
            flashbackThree.setVisible(true);
            flashbackSlider.setValue(3);
            if (continueButton != null) {
              continueButton.setVisible(true);
              continueButton.setText("CONTINUE");
            }
            break;
        }

        // Add slider listener to handle value changes (only 1-3)
        flashbackSlider
            .valueProperty()
            .addListener(
                (obs, oldVal, newVal) -> {
                  int sliderValue = newVal.intValue();
                  if (sliderValue != oldVal.intValue() && sliderValue >= 1 && sliderValue <= 3) {
                    try {
                      handleFlashbackChange(sliderValue);
                    } catch (IOException e) {
                      e.printStackTrace();
                    }
                  }
                });
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
  }
}
