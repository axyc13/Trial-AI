package nz.ac.auckland.se206.controllers;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import javafx.animation.PauseTransition;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.control.TextArea;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.text.TextAlignment;
import javafx.util.Duration;
import nz.ac.auckland.apiproxy.exceptions.ApiProxyException;

/**
 * Controller class for the AI Witness chat view. Handles user interactions and communication with
 * the GPT model via the API proxy.
 */
public class AiWitnessChatController extends ChatControllerCentre {

  @FXML private TextArea txtaChat;
  @FXML private Slider slider;
  @FXML private VBox flashbackMessage;
  private final Map<ImageView, Label> speechBubbleLabels = new HashMap<>();

  @FXML private ImageView speechBubble1;
  @FXML private ImageView speechBubble2;
  @FXML private ImageView speechBubble3;
  @FXML private ImageView speechBubble4;
  @FXML private ImageView speechBubble5;
  @FXML private ImageView speechBubble6;
  @FXML private ImageView speechBubble7;
  @FXML private ImageView speechBubble8;
  @FXML private ImageView speechBubble9;
  @FXML private ImageView speechBubble10;
  @FXML private ImageView speechBubble11;
  @FXML private ImageView speechBubble12;
  @FXML private Button clearNoiseBtn;
  @FXML private ImageView binImage;
  @FXML private ImageView rumourBin;

  @Override
  @FXML
  public void initialize() {
    try {
      super.initialize();
    } catch (ApiProxyException e) {
      e.printStackTrace();
    }
    flashbackMessage.setVisible(true);
    setupSpeechBubbleTexts();
    hideAllSpeechBubbles();
    clearNoiseBtn.setVisible(false);

    slider
        .valueProperty()
        .addListener(
            (obs, oldVal, newVal) -> {
              showSpeechBubble(newVal.intValue());
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

  private void hideAllSpeechBubbles() {
    speechBubble1.setVisible(false);
    speechBubble2.setVisible(false);
    speechBubble3.setVisible(false);
    speechBubble4.setVisible(false);
    speechBubble5.setVisible(false);
    speechBubble6.setVisible(false);
    speechBubble7.setVisible(false);
    speechBubble8.setVisible(false);
    speechBubble9.setVisible(false);
    speechBubble10.setVisible(false);
    speechBubble11.setVisible(false);
    speechBubble12.setVisible(false);

    // Hide all labels
    for (Label label : speechBubbleLabels.values()) {
      label.setVisible(false);
    }
  }

  private void addTextToSpeechBubble(ImageView bubble, String text) {
    // Create label with styling
    Label label = new Label(text);
    label.setWrapText(true);
    label.setTextAlignment(TextAlignment.CENTER);
    label.setAlignment(Pos.CENTER);

    // Set width to 80% of bubble width to prevent text touching edges
    label.setPrefWidth(bubble.getFitWidth() * 0.8);
    label.setPrefHeight(bubble.getFitHeight() * 0.8);

    // Add styling for better readability
    label.setStyle("-fx-font-size: 14px; -fx-font-weight: bold;");

    // Create StackPane to hold both bubble and text
    StackPane stack = new StackPane();
    stack.setLayoutX(bubble.getLayoutX());
    stack.setLayoutY(bubble.getLayoutY());

    // Remove bubble from its current parent and add to stack
    AnchorPane parent = (AnchorPane) bubble.getParent();
    parent.getChildren().remove(bubble);
    stack.getChildren().addAll(bubble, label);
    parent.getChildren().add(stack);

    // Reset the layout
    bubble.setLayoutX(0);
    bubble.setLayoutY(0);

    label.setVisible(false); // Always start hidden
    speechBubbleLabels.put(bubble, label);
  }

  private void setupSpeechBubbleTexts() {
    addTextToSpeechBubble(speechBubble1, "Did you hear about that new AI project?");
    addTextToSpeechBubble(speechBubble2, "Yeah, it was trained on a bunch of music.");
    addTextToSpeechBubble(speechBubble3, "Some people say the artists never agreed to it...");
    addTextToSpeechBubble(
        speechBubble4, "I thought I heard something about consent, but not sure.");
    addTextToSpeechBubble(speechBubble5, "Either way, musicians are upset.");
    addTextToSpeechBubble(speechBubble6, "They think their styles were copied.");
    addTextToSpeechBubble(speechBubble7, "It sounds like the AI just stole the music.");
    addTextToSpeechBubble(speechBubble8, "I don’t know... inspiration isn’t the same as stealing.");
    addTextToSpeechBubble(speechBubble9, "But everyone keeps calling it unethical.");
    addTextToSpeechBubble(speechBubble10, "Rumours spread so quickly about this stuff.");
    addTextToSpeechBubble(speechBubble11, "Hard to tell what’s true anymore...");
    addTextToSpeechBubble(
        speechBubble12, "Still, people say the whole story is clear — the AI crossed a line.");
  }

  private void showSpeechBubble(int value) {
    hideAllSpeechBubbles();
    if (value >= 2) {
      showBubbleWithText(speechBubble2);
    }
    if (value >= 3) {
      showBubbleWithText(speechBubble1);
    }
    if (value >= 4) {
      showBubbleWithText(speechBubble7);
    }
    if (value >= 5) {
      showBubbleWithText(speechBubble4);
    }
    if (value >= 6) {
      showBubbleWithText(speechBubble8);
    }
    if (value >= 7) {
      showBubbleWithText(speechBubble5);
    }
    if (value >= 8) {
      showBubbleWithText(speechBubble3);
    }
    if (value >= 9) {
      showBubbleWithText(speechBubble9);
      showBubbleWithText(speechBubble10);
    }
    if (value >= 10) {
      showBubbleWithText(speechBubble6);
      showBubbleWithText(speechBubble11);
    }
    if (value >= 11) {
      showBubbleWithText(speechBubble12);
      clearNoiseBtn.setVisible(true);
      slider.setVisible(false);
    }
  }

  private void showBubbleWithText(ImageView bubble) {
    bubble.setVisible(true);
    Label label = speechBubbleLabels.get(bubble);
    if (label != null) {
      label.setVisible(true);
    }
  }

  private void makeDraggableWithBinDetection(StackPane stack, ImageView bubble) {
    double[] deltaX = {0.0};
    double[] deltaY = {0.0};

    stack.setOnMousePressed(
        e -> {
          deltaX[0] = stack.getLayoutX() - e.getSceneX();
          deltaY[0] = stack.getLayoutY() - e.getSceneY();
        });

    stack.setOnMouseDragged(
        e -> {
          stack.setLayoutX(e.getSceneX() + deltaX[0]);
          stack.setLayoutY(e.getSceneY() + deltaY[0]);

          // Check for collision with rumour bin using a smaller detection area
          javafx.geometry.Bounds binBounds = rumourBin.localToScene(rumourBin.getBoundsInLocal());
          javafx.geometry.Bounds stackBounds = stack.localToScene(stack.getBoundsInLocal());

          // Create a smaller collision area in the center of the bin (40% of the original size)
          double binCenterX = binBounds.getMinX() + binBounds.getWidth() / 2;
          double binCenterY = binBounds.getMinY() + binBounds.getHeight() / 2;
          double collisionWidth = binBounds.getWidth() * 0.4;
          double collisionHeight = binBounds.getHeight() * 0.4;

          // Check if the stack's center point is within the smaller collision area
          double stackCenterX = stackBounds.getMinX() + stackBounds.getWidth() / 2;
          double stackCenterY = stackBounds.getMinY() + stackBounds.getHeight() / 2;

          if (Math.abs(stackCenterX - binCenterX) < collisionWidth / 2
              && Math.abs(stackCenterY - binCenterY) < collisionHeight / 2) {
            stack.setVisible(false); // Hide the whole stack (bubble + text)
            Label label = speechBubbleLabels.get(bubble);
            if (label != null) {
              label.setVisible(false);
            }
          }
        });
  }

  @FXML
  private void onClearNoiseBtnClick() {
    // Make the StackPanes containing bubbles and text draggable with bin detection
    for (ImageView bubble :
        new ImageView[] {
          speechBubble1, speechBubble2, speechBubble3, speechBubble4,
          speechBubble5, speechBubble6, speechBubble7, speechBubble8,
          speechBubble9, speechBubble10, speechBubble11, speechBubble12
        }) {
      if (bubble.getParent() instanceof StackPane) {
        makeDraggableWithBinDetection((StackPane) bubble.getParent(), bubble);
      }
    }
  }
}
