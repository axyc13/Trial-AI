package nz.ac.auckland.se206.controllers;

import java.io.File;
import java.io.IOException;
import javafx.animation.PauseTransition;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Button;
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
  @FXML private VBox flashbackMessage;

  @FXML
  private void onContinueFlashback() throws IOException {
    int currentState = AiWitnessStateManager.getInstance().getFlashbackState();
    int nextState = currentState + 1;
    AiWitnessStateManager.getInstance().setFlashbackState(nextState);

    switch (nextState) {
      case 2:
        // Show second flashback
        flashbackOne.setVisible(false);
        flashbackTwo.setVisible(true);
        flashbackThree.setVisible(false);
        break;
      case 3:
        // Show third flashback
        flashbackOne.setVisible(false);
        flashbackTwo.setVisible(false);
        flashbackThree.setVisible(true);
        if (continueButton != null) {
          continueButton.setText("Enter Memory");
        }
        break;
      default:
        // After third flashback, switch to chat room scene
        FXMLLoader loader = new FXMLLoader(App.class.getResource("/fxml/aiWitness.fxml"));
        Parent root = loader.load();
        ChatControllerCentre chatController = loader.getController();
        chatController.initialiseChatGpt("aiWitness.txt", "AI Witness");
        App.getScene().setRoot(root);
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
      if (flashbackOne != null && continueButton != null) {
        // Get saved state
        int state = AiWitnessStateManager.getInstance().getFlashbackState();

        // Set up UI based on saved state
        switch (state) {
          case 1:
            flashbackOne.setVisible(true);
            flashbackTwo.setVisible(false);
            flashbackThree.setVisible(false);
            continueButton.setText("Continue");
            break;
          case 2:
            flashbackOne.setVisible(false);
            flashbackTwo.setVisible(true);
            flashbackThree.setVisible(false);
            continueButton.setText("Continue");
            break;
          case 3:
            flashbackOne.setVisible(false);
            flashbackTwo.setVisible(false);
            flashbackThree.setVisible(true);
            continueButton.setText("Enter Memory");
            break;
        }
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
  }
}
