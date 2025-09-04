package nz.ac.auckland.se206.controllers;

import java.io.File;
import javafx.animation.PauseTransition;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.control.TextArea;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.shape.Polygon;
import javafx.util.Duration;
import nz.ac.auckland.apiproxy.exceptions.ApiProxyException;

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

  private void showDialogue(int value) {
    // Plays through the dialogue as you move the slider
    if (value >= 1) {
      phoneInHand.setVisible(true);
      musiciansCalling.setVisible(false);
    } else if (value < 1) {
      phoneInHand.setVisible(false);
      musiciansCalling.setVisible(true);
    }
    if (value == 2) {
      dialogue1.setVisible(true);
      dialogueText1.setVisible(true);
    } else if (value != 2) {
      dialogue1.setVisible(false);
      dialogueText1.setVisible(false);
    }
    if (value >= 3) {
      musiciansCalling.setVisible(true);
      phoneInHand.setVisible(false);
    } else if (value < 3) {
      musiciansCalling.setVisible(false);
      phoneInHand.setVisible(true);
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
    if (value >= 9) {
      musiciansStudio.setVisible(true);
    } else if (value < 9) {
      musiciansStudio.setVisible(false);
    }
  }
}
