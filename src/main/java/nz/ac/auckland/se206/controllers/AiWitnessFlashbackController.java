package nz.ac.auckland.se206.controllers;

import java.io.IOException;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.image.ImageView;
import nz.ac.auckland.se206.App;
import nz.ac.auckland.se206.TimerManager;

public class AiWitnessFlashbackController extends ChatControllerCentre {

  private int flashbackStep = 1;
  @FXML private ImageView FlashbackOne;
  @FXML private ImageView FlashbackTwo;
  @FXML private ImageView FlashbackThree;
  @FXML private Button continueButton;

  @FXML
  private void onContinueFlashback() throws IOException {
    flashbackStep++;
    switch (flashbackStep) {
      case 2:
        // Show second flashback
        FlashbackOne.setVisible(false);
        FlashbackTwo.setVisible(true);
        FlashbackThree.setVisible(false);
        break;
      case 3:
        // Show third flashback
        FlashbackOne.setVisible(false);
        FlashbackTwo.setVisible(false);
        FlashbackThree.setVisible(true);
        if (continueButton != null) {
          continueButton.setText("Enter Memory");
        }
        break;
      default:
        // After third flashback, switch to memory scene
        App.setRoot("aiWitness");
        break;
    }
  }

  @Override
  public void initialize() {
    try {
      super.initialize(); // This will handle the timer setup
      TimerManager.getInstance().start(); // Start the timer

      // Initialize flashback UI
      if (FlashbackOne != null && continueButton != null) {
        // Show first flashback, hide others
        FlashbackOne.setVisible(true);
        FlashbackTwo.setVisible(false);
        FlashbackThree.setVisible(false);
        continueButton.setText("Continue");
        flashbackStep = 1;
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
  }
}
