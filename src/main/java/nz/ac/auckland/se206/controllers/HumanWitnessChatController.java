package nz.ac.auckland.se206.controllers;

import java.io.File;
import java.io.IOException;
import javafx.animation.PauseTransition;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.control.TextArea;
import javafx.scene.effect.ColorAdjust;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.shape.Polygon;
import javafx.util.Duration;
import nz.ac.auckland.apiproxy.exceptions.ApiProxyException;
import nz.ac.auckland.se206.App;

/**
 * Controller class for the chat view. Handles user interactions and communication with the GPT
 * model via the API proxy.
 */
public class HumanWitnessChatController extends ChatControllerCentre {

  @FXML private TextArea txtaChat;
  @FXML private VBox flashbackMessage;
  @FXML private Slider slidingBar;
  @FXML private ImageView phoneInHand;
  @FXML private Label dialogue1;
  @FXML private Polygon dialogueText1;
  @FXML private ImageView musiciansCalling;
  @FXML private Label dialogue2;
  @FXML private Polygon dialogueText2;
  @FXML private Label dialogue3;
  @FXML private Polygon dialogueText3;
  @FXML private Label dialogue4;
  @FXML private Polygon dialogueText4;
  @FXML private Label dialogue5;
  @FXML private Polygon dialogueText5;
  @FXML private Label dialogue6;
  @FXML private Polygon dialogueText6;
  @FXML private ImageView musiciansStudio;
  @FXML private Label dialogue7;
  @FXML private Polygon dialogueText7;
  @FXML private Label dialogue8;
  @FXML private Polygon dialogueText8;
  @FXML private Label dialogue9;
  @FXML private Polygon dialogueText9;
  @FXML private ImageView celebrationParty;
  @FXML private Label dialogue10;
  @FXML private Polygon dialogueText10;
  @FXML private Label storyCompletionLabel;
  @FXML private ImageView backgroundImage;
  @FXML private Button continueButton;

  @Override
  @FXML
  public void initialize() {
    try {
      super.initialize();
    } catch (ApiProxyException e) {
      e.printStackTrace();
    }
    flashbackMessage.setVisible(true);

    slidingBar
        .valueProperty()
        .addListener(
            (obs, oldVal, newVal) -> {
              showDialogue(newVal.intValue());
              storyCompletionPercentage(newVal.intValue());
              backgroundHuePercentage(newVal.intValue());
            });

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
  }

  private void storyCompletionPercentage(int value) {
    // Shows the percentage of the story currently completed
    storyCompletionLabel.setText("Story completion: " + (int) ((value / 15.0) * 100) + " % ");
  }

  private void backgroundHuePercentage(int value) {
    // Adjusts the hue according to the story percentage completed
    ColorAdjust colorAdjust = new ColorAdjust();
    colorAdjust.setHue((-1.0 + (value / 15.0) * 2));
    backgroundImage.setEffect(colorAdjust);
    storyCompletionLabel.setEffect(colorAdjust);
  }

  private void showDialogue(int value) {
    // Plays through the dialogue as you move the slider
    // Background view number 1
    if (value >= 1 && value < 3) {
      phoneInHand.setVisible(true);
    } else if (value < 1 || value >= 3) {
      phoneInHand.setVisible(false);
    }
    if (value == 2) {
      dialogue1.setVisible(true);
      dialogueText1.setVisible(true);
    } else if (value != 2) {
      dialogue1.setVisible(false);
      dialogueText1.setVisible(false);
    }
    // Background view number 2
    if (value >= 3 && value < 9) {
      musiciansCalling.setVisible(true);
    } else if (value < 3 || value >= 9) {
      musiciansCalling.setVisible(false);
    }
    if (value == 4) {
      dialogue2.setVisible(true);
      dialogueText2.setVisible(true);
    } else if (value != 4) {
      dialogue2.setVisible(false);
      dialogueText2.setVisible(false);
    }
    if (value == 5) {
      dialogue3.setVisible(true);
      dialogueText3.setVisible(true);
    } else if (value != 5) {
      dialogue3.setVisible(false);
      dialogueText3.setVisible(false);
    }
    if (value == 6) {
      dialogue4.setVisible(true);
      dialogueText4.setVisible(true);
    } else if (value != 6) {
      dialogue4.setVisible(false);
      dialogueText4.setVisible(false);
    }
    if (value == 7) {
      dialogue5.setVisible(true);
      dialogueText5.setVisible(true);
    } else if (value != 7) {
      dialogue5.setVisible(false);
      dialogueText5.setVisible(false);
    }
    if (value == 8) {
      dialogue6.setVisible(true);
      dialogueText6.setVisible(true);
    } else if (value != 8) {
      dialogue6.setVisible(false);
      dialogueText6.setVisible(false);
    }
    // Background view number 3
    if (value >= 9 && value < 13) {
      musiciansStudio.setVisible(true);
    } else if (value < 9 || value >= 13) {
      musiciansStudio.setVisible(false);
    }
    if (value == 10) {
      dialogue7.setVisible(true);
      dialogueText7.setVisible(true);
    } else if (value != 10) {
      dialogue7.setVisible(false);
      dialogueText7.setVisible(false);
    }
    if (value == 11) {
      dialogue8.setVisible(true);
      dialogueText8.setVisible(true);
    } else if (value != 11) {
      dialogue8.setVisible(false);
      dialogueText8.setVisible(false);
    }
    if (value == 12) {
      dialogue9.setVisible(true);
      dialogueText9.setVisible(true);
    } else if (value != 12) {
      dialogue9.setVisible(false);
      dialogueText9.setVisible(false);
    }
    // Background view number 4
    if (value >= 13) {
      celebrationParty.setVisible(true);
    } else if (value < 13) {
      celebrationParty.setVisible(false);
    }
    if (value == 14) {
      dialogue10.setVisible(true);
      dialogueText10.setVisible(true);
    } else if (value != 14) {
      dialogue10.setVisible(false);
      dialogueText10.setVisible(false);
    }
    // Button to leave the flashback
    if (value == 15) {
      continueButton.setVisible(true);
      continueButton.setDisable(false);
    } else if (value != 15) {
      continueButton.setVisible(false);
      continueButton.setDisable(true);
    }
  }

  @FXML
  private void OngoChat(ActionEvent event) throws ApiProxyException, IOException {
    // SetRoot to the human witness chat room
    App.setRoot("room");
  }
}
