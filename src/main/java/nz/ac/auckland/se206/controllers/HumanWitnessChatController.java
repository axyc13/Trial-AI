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

    // Set visuals based off the sliding bar value
    slidingBar
        .valueProperty()
        .addListener(
            (obs, oldVal, newVal) -> {
              showDialogue(newVal.intValue());
              viewStoryCompletionPercentage(newVal.intValue());
              backgroundHuePercentage(newVal.intValue());
            });

    // Play the flashback sound
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

  private void viewStoryCompletionPercentage(int value) {
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
    // Background view
    phoneInHand.setVisible((value >= 1 && value < 3));
    musiciansCalling.setVisible((value >= 3 && value < 9));
    musiciansStudio.setVisible((value >= 9 && value < 13));
    celebrationParty.setVisible((value >= 13 && value < 15));

    setVisibility(value == 2, dialogue1, dialogueText1);
    setVisibility(value == 4, dialogue2, dialogueText2);
    setVisibility(value == 5, dialogue3, dialogueText3);
    setVisibility(value == 6, dialogue4, dialogueText4);
    setVisibility(value == 7, dialogue5, dialogueText5);
    setVisibility(value == 8, dialogue6, dialogueText6);
    setVisibility(value == 10, dialogue7, dialogueText7);
    setVisibility(value == 11, dialogue8, dialogueText8);
    setVisibility(value == 12, dialogue9, dialogueText9);
    setVisibility(value == 14, dialogue10, dialogueText10);

    // Button to leave the flashback
    continueButton.setVisible((value == 15));
    continueButton.setDisable((value != 15));
  }

  private void setVisibility(boolean visible, Label speechBubble, Polygon speechBubbleArrow) {
    speechBubbleArrow.setVisible(visible);
    speechBubble.setVisible(visible);
  }

  @FXML
  private void onGoChat(ActionEvent event) throws ApiProxyException, IOException {
    // SetRoot to the human witness chat room
    App.setRoot("HumanWitnessMemory");
  }
}
