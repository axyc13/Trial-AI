package nz.ac.auckland.se206.controllers;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
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
import nz.ac.auckland.apiproxy.chat.openai.ChatMessage;
import nz.ac.auckland.apiproxy.exceptions.ApiProxyException;
import nz.ac.auckland.se206.AiWitnessStateManager;
import nz.ac.auckland.se206.ChatStorage;

/**
 * Controller class for the AI Witness chat view. Handles user interactions and communication with
 * the GPT model via the API proxy.
 */
public class AiWitnessChatController extends ChatControllerCentre {

  // Tracks how many times the AI witness has been clicked
  private int aiWitnessClickCount = 0;
  private int flashbackStep = 1;
  @FXML private ImageView FlashbackOne;
  @FXML private ImageView FlashbackTwo;
  @FXML private ImageView FlashbackThree;
  @FXML private Button continueButton;

  /**
   * Handles clicks on the AI witness. Shows flashback scene on first click, memory on subsequent
   * clicks.
   */
  @FXML
  private void onAiWitnessClicked() {
    if (aiWitnessClickCount == 0) {
      // Show flashback scene
      showFlashbackScene();
    } else {
      // Show memory scene directly
      showMemoryScene();
    }
    aiWitnessClickCount++;
  }

  /** Shows the flashback scene. Replace this with your actual scene-switching logic. */
  private void showFlashbackScene() {
    try {
      javafx.fxml.FXMLLoader loader =
          new javafx.fxml.FXMLLoader(getClass().getResource("/fxml/AIWitnessFlashback.fxml"));
      javafx.scene.Parent root = loader.load();
      javafx.scene.Scene scene = new javafx.scene.Scene(root);
      // Get the current stage from any node (e.g., txtaChat)
      javafx.stage.Stage stage = (javafx.stage.Stage) txtaChat.getScene().getWindow();
      stage.setScene(scene);
      stage.show();
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  /** Shows the memory scene. Replace this with your actual scene-switching logic. */
  private void showMemoryScene() {
    try {
      javafx.fxml.FXMLLoader loader =
          new javafx.fxml.FXMLLoader(getClass().getResource("/fxml/aiWitness.fxml"));
      javafx.scene.Parent root = loader.load();
      javafx.scene.Scene scene = new javafx.scene.Scene(root);
      // Get the current stage from any node (e.g., txtaChat)
      javafx.stage.Stage stage = (javafx.stage.Stage) txtaChat.getScene().getWindow();
      stage.setScene(scene);
      stage.show();
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  @FXML private TextArea txtaChat;
  @FXML private Slider slider;
  @FXML private VBox flashbackMessage;
  private final Map<ImageView, Label> speechBubbleLabels = new HashMap<>();
  private Label instructionLabel;

  @FXML private ImageView speechBubble1;
  @FXML private ImageView speechBubble2;
  // @FXML private ImageView speechBubble3;
  @FXML private ImageView speechBubble4;
  @FXML private ImageView speechBubble5;
  @FXML private ImageView speechBubble6;
  @FXML private ImageView speechBubble7;
  @FXML private ImageView speechBubble8;
  @FXML private ImageView speechBubble9;
  // @FXML private ImageView speechBubble10;
  // @FXML private ImageView speechBubble11;
  // @FXML private ImageView speechBubble12;
  @FXML private Button clearNoiseBtn;
  @FXML private ImageView rumourBin;
  @FXML private TextArea chatTextArea;

  private Label completionLabel;
  private List<String> playerActions = new ArrayList<>();

  /**
   * Logs a player action for tracking their progress through the memory/puzzle.
   *
   * @param action The description of the action performed
   */
  private void logAction(String action) {
    playerActions.add(action);
    System.out.println("Player action: " + action);

    if (action.startsWith("Disposed:")) {
      // Get the updated count after the bubble was just disposed
      AiWitnessStateManager stateManager = AiWitnessStateManager.getInstance();
      int bubblesInBin = stateManager.getBubblesInBin();
      System.out.println("Bubbles in bin: " + (bubblesInBin + 1) + "/8");

      // Check if all speech bubbles have been disposed
      if (stateManager.getBubblesInBin() == 7) {
        Platform.runLater(
            () -> {
              // Simple fade in for the completion label
              javafx.animation.FadeTransition fadeIn =
                  new javafx.animation.FadeTransition(Duration.seconds(1.0), completionLabel);
              fadeIn.setFromValue(0.0);
              fadeIn.setToValue(1.0);

              // Show the conclusion message
              // Show the completion label and save its state
              completionLabel.setVisible(true);
              AiWitnessStateManager.getInstance().setEndLabelVisible(true);
              fadeIn.play();

              // Add context to chat storage
              ChatMessage actionMsg =
                  new ChatMessage(
                      "system",
                      "Player has cleared all the rumours. The AI witness should now make a final"
                          + " comment on the situation.");
              actionMsg.setSystemPrompt(true);
              ChatStorage.addMessage("system", actionMsg);
            });
      }
    }
  }

  private void addAiComment(String action) {
    final String comment;
    if (action.contains("cleared")) {
      comment = "Hold up, what noise?? This is obviously true.";
    } else if (action.contains("revealed all")) {
      comment = "I heard these things while I was on the train.";
    } else if (action.contains("disposed")) {
      // Get the text of the disposed bubble
      String bubbleText = action.substring(action.indexOf(":") + 1).trim();

      // Only respond to key statements about the ethical concerns
      if (bubbleText.contains("never agreed to it")) {
        comment = "Woah woah woah, why are you getting rid of that one?!";
      } else if (bubbleText.contains("stole the music")) {
        comment = "No, this one is definitely true. Im sure of it.";
      } else if (bubbleText.contains("crossed a line")) {
        comment =
            "Excuse me but that one is definitely true. The AI's actions were clearly unethical."
                + " They said it themselves.";
      } else if (bubbleText.contains("same as stealing")) {
        // Only respond to this attempt to justify the behavior
        comment = "oh yeah that one you can get rid of. I don't think it's right.";
      } else {
        // Stay silent for other bubbles, including other attempts to minimize the issue
        comment = "";
      }
    } else {
      comment = "";
    }

    if (!comment.isEmpty()) {
      String finalComment = comment;
      Platform.runLater(
          () -> {
            txtaChat.appendText("AI Witness: " + finalComment + "\n\n");
          });
    }
  }

  @FXML
  private void onContinueFlashback() {
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
        showMemoryScene();
        break;
    }
  }

  @Override
  @FXML
  public void initialize() {
    try {
      super.initialize();
    } catch (ApiProxyException e) {
      e.printStackTrace();
    }

    // Initialize flashback UI if we're in the flashback scene
    if (FlashbackOne != null && continueButton != null) {
      // Show first flashback, hide others
      FlashbackOne.setVisible(true);
      FlashbackTwo.setVisible(false);
      FlashbackThree.setVisible(false);
      continueButton.setText("Continue");
      flashbackStep = 1;
    }

    flashbackMessage.setVisible(true);
    setupSpeechBubbleTexts();

    // Restore state from manager
    AiWitnessStateManager state = AiWitnessStateManager.getInstance();

    // Start with all bubbles hidden
    hideAllSpeechBubbles();
    clearNoiseBtn.setVisible(false);
    rumourBin.setVisible(false);

    // Restore slider value and bubbles
    if (state.hasShownAllBubbles()) {
      slider.setValue(11);
      slider.setVisible(false);

      // Get list of disposed bubbles first
      List<Integer> disposedBubbles = state.getDisposedBubbles();

      // Show only bubbles that haven't been disposed
      for (ImageView bubble :
          new ImageView[] {
            speechBubble1,
            speechBubble2,
            speechBubble4,
            speechBubble5,
            speechBubble6,
            speechBubble7,
            speechBubble8,
            speechBubble9
          }) {
        int bubbleNumber = getBubbleNumber(bubble);
        if (!disposedBubbles.contains(bubbleNumber)) {
          showBubbleWithText(bubble);
        }
      }

      // If clear noise was clicked, show bin and make bubbles draggable
      if (state.hasClickedClearNoise()) {
        rumourBin.setVisible(true);
        // Make all non-disposed bubbles draggable
        for (ImageView bubble :
            new ImageView[] {
              speechBubble1,
              speechBubble2,
              speechBubble4,
              speechBubble5,
              speechBubble6,
              speechBubble7,
              speechBubble8,
              speechBubble9
            }) {
          if (bubble.getParent() instanceof StackPane && bubble.isVisible()) {
            makeDraggableWithBinDetection((StackPane) bubble.getParent(), bubble);
          }
        }
      } else {
        // If not clicked clear noise yet, show the button
        clearNoiseBtn.setVisible(true);
      }
    } else {
      double sliderValue = state.getSliderValue();
      slider.setValue(sliderValue);
      // Show bubbles up to the saved slider value
      showSpeechBubble((int) sliderValue);
    }

    // Restore disposed bubbles
    for (int bubbleNumber : state.getDisposedBubbles()) {
      ImageView bubble = getBubbleByNumber(bubbleNumber);
      if (bubble != null && bubble.getParent() instanceof StackPane) {
        StackPane stack = (StackPane) bubble.getParent();
        stack.setVisible(false);
        Label label = speechBubbleLabels.get(bubble);
        if (label != null) {
          label.setVisible(false);
        }
      }
    }

    // If all bubbles are disposed, make sure the bin is visible
    if (state.getBubblesInBin() == 8) {
      rumourBin.setVisible(true);
    }

    // Create and style the instruction label
    instructionLabel = new Label("Drag the rumours into the bin");
    instructionLabel.setStyle(
        "-fx-font-size: 50px; -fx-text-fill: white; -fx-background-color: rgba(0, 0, 0, 0.7);"
            + " -fx-padding: 10px;");
    instructionLabel.setLayoutX(50); // Center horizontally
    instructionLabel.setLayoutY(200); // Position near top
    instructionLabel.setVisible(false);
    ((AnchorPane) slider.getParent()).getChildren().add(instructionLabel);

    // Create and style the completion label
    completionLabel = new Label("The AI witness testimony is unreliable and based on rumours.");
    completionLabel.setStyle(
        "-fx-font-size: 30px; -fx-text-fill: white; -fx-background-color: rgba(0, 0, 0, 0.8);"
            + " -fx-padding: 20px; -fx-background-radius: 10px;");
    completionLabel.setWrapText(true);
    completionLabel.setPrefWidth(600);
    completionLabel.setAlignment(Pos.CENTER);
    completionLabel.setTextAlignment(TextAlignment.CENTER);

    // Position in the center of the screen
    completionLabel.setLayoutX(200);
    completionLabel.setLayoutY(250);

    // Restore the visibility state of the completion label
    completionLabel.setVisible(state.isEndLabelVisible());

    ((AnchorPane) slider.getParent()).getChildren().add(completionLabel);

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
    // speechBubble3.setVisible(false);
    speechBubble4.setVisible(false);
    speechBubble5.setVisible(false);
    speechBubble6.setVisible(false);
    speechBubble7.setVisible(false);
    speechBubble8.setVisible(false);
    speechBubble9.setVisible(false);
    // speechBubble10.setVisible(false);
    // speechBubble11.setVisible(false);
    // speechBubble12.setVisible(false);

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

    // Set width to 60% of bubble width to prevent text touching edges
    label.setPrefWidth(bubble.getFitWidth() * 0.6);
    label.setPrefHeight(bubble.getFitHeight() * 0.8);

    // Add styling for better readability
    label.setStyle("-fx-font-size: 12.5px; -fx-font-weight: bold;");

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
    addTextToSpeechBubble(speechBubble2, "Did you hear about that new AI project?");
    addTextToSpeechBubble(speechBubble1, "Yeah, it was trained on a bunch of music.");
    addTextToSpeechBubble(speechBubble7, "Some people say the artists never agreed to it...");
    addTextToSpeechBubble(
        speechBubble4, "I thought I heard something about consent, but not sure.");
    addTextToSpeechBubble(speechBubble8, "Either way, musicians are upset.");
    addTextToSpeechBubble(speechBubble5, "They think their styles were copied.");
    // addTextToSpeechBubble(speechBubble3, "It sounds like the AI just stole the music.");
    addTextToSpeechBubble(speechBubble9, "I don’t know... inspiration isn’t the same as stealing.");
    // addTextToSpeechBubble(speechBubble10, "But everyone keeps calling it unethical.");
    addTextToSpeechBubble(speechBubble6, "Rumours spread so quickly about this stuff.");
    //   addTextToSpeechBubble(speechBubble11, "Hard to tell what’s true anymore...");
    //   addTextToSpeechBubble(
    //       speechBubble12, "Still, people say the whole story is clear — the AI crossed a line.");
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
    // if (value >= 8) {
    //   showBubbleWithText(speechBubble3);
    // }
    if (value >= 9) {
      showBubbleWithText(speechBubble9);
      // showBubbleWithText(speechBubble10);
    }
    if (value >= 10) {
      showBubbleWithText(speechBubble6);
      // showBubbleWithText(speechBubble11);
    }
    if (value >= 11) {
      // showBubbleWithText(speechBubble12);
      // Only show clear button if it hasn't been clicked yet
      clearNoiseBtn.setVisible(!AiWitnessStateManager.getInstance().hasClickedClearNoise());
      slider.setVisible(false);
      logAction("Revealed all rumors");
      addAiComment("revealed all");
      AiWitnessStateManager.getInstance().setShownAllBubbles(true);
    }
    AiWitnessStateManager.getInstance().setSliderValue(value);
  }

  private void showBubbleWithText(ImageView bubble) {
    bubble.setVisible(true);
    Label label = speechBubbleLabels.get(bubble);
    if (label != null) {
      label.setVisible(true);
    }
  }

  private int getBubbleNumber(ImageView bubble) {
    // Mapping based on order of appearance in showSpeechBubble method
    if (bubble == speechBubble2) return 1; // First visible
    if (bubble == speechBubble1) return 2; // Second visible
    if (bubble == speechBubble7) return 3; // Third visible
    if (bubble == speechBubble4) return 4; // Fourth visible
    if (bubble == speechBubble8) return 5; // Fifth visible
    if (bubble == speechBubble5) return 6; // Sixth visible
    if (bubble == speechBubble9) return 7; // Seventh visible
    if (bubble == speechBubble6) return 8; // Eighth visible
    return -1;
  }

  private ImageView getBubbleByNumber(int number) {
    // Map each number back to bubbles in order of appearance
    switch (number) {
      case 1:
        return speechBubble2; // First visible
      case 2:
        return speechBubble1; // Second visible
      case 3:
        return speechBubble7; // Third visible
      case 4:
        return speechBubble4; // Fourth visible
      case 5:
        return speechBubble8; // Fifth visible
      case 6:
        return speechBubble5; // Sixth visible
      case 7:
        return speechBubble9; // Seventh visible
      case 8:
        return speechBubble6; // Eighth visible
      default:
        return null;
    }
  }

  private void makeDraggableWithBinDetection(StackPane stack, ImageView bubble) {
    double[] deltaX = {0.0};
    double[] deltaY = {0.0};
    boolean[] isDragging = {false};
    boolean[] isOverBin = {false};

    stack.setOnMousePressed(
        e -> {
          deltaX[0] = stack.getLayoutX() - e.getSceneX();
          deltaY[0] = stack.getLayoutY() - e.getSceneY();
          isDragging[0] = true;
        });

    stack.setOnMouseDragged(
        e -> {
          if (!isDragging[0]) return;

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

          isOverBin[0] =
              Math.abs(stackCenterX - binCenterX) < collisionWidth / 2
                  && Math.abs(stackCenterY - binCenterY) < collisionHeight / 2;
        });

    stack.setOnMouseReleased(
        e -> {
          if (isDragging[0] && isOverBin[0]) {
            stack.setVisible(false); // Hide the whole stack (bubble + text)
            Label label = speechBubbleLabels.get(bubble);
            if (label != null) {
              label.setVisible(false);
              String bubbleText = label.getText();
              // Log the action and pass the bubble's text
              logAction("Disposed: " + bubbleText);
              addAiComment("disposed: " + bubbleText);

              // Save state
              int bubbleNumber = getBubbleNumber(bubble);
              if (bubbleNumber != -1) {
                AiWitnessStateManager.getInstance().addDisposedBubble(bubbleNumber);
              }
            }
          }
          isDragging[0] = false;
          isOverBin[0] = false;
        });
  }

  @FXML
  private void onClearNoiseBtnClick() {
    // Show the instruction label
    instructionLabel.setVisible(true);
    rumourBin.setVisible(true);
    logAction("Clear Noise button clicked");
    addAiComment("cleared");

    // Save state
    AiWitnessStateManager.getInstance().setClearNoiseClicked(true);

    // Create a fade transition for the instruction
    javafx.animation.FadeTransition fadeOut =
        new javafx.animation.FadeTransition(Duration.seconds(3), instructionLabel);
    fadeOut.setFromValue(1.0);
    fadeOut.setToValue(0.0);
    fadeOut.setDelay(Duration.seconds(2)); // Stay visible for 2 seconds before fading
    fadeOut.setOnFinished(e -> instructionLabel.setVisible(false));
    fadeOut.play();

    // Make only visible bubbles draggable
    for (ImageView bubble :
        new ImageView[] {
          speechBubble1,
          speechBubble2,
          speechBubble4,
          speechBubble5,
          speechBubble6,
          speechBubble7,
          speechBubble8,
          speechBubble9
        }) {
      if (bubble.getParent() instanceof StackPane && bubble.isVisible()) {
        makeDraggableWithBinDetection((StackPane) bubble.getParent(), bubble);
      }
    }

    clearNoiseBtn.setVisible(false);
  }
}
