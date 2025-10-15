package nz.ac.auckland.se206.controllers;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javafx.animation.PauseTransition;
import javafx.animation.TranslateTransition;
import javafx.application.Platform;
import javafx.concurrent.Task;
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
import javafx.scene.text.TextAlignment;
import javafx.scene.text.TextFlow;
import javafx.util.Duration;
import nz.ac.auckland.apiproxy.chat.openai.ChatCompletionRequest;
import nz.ac.auckland.apiproxy.chat.openai.ChatCompletionRequest.Model;
import nz.ac.auckland.apiproxy.chat.openai.ChatCompletionResult;
import nz.ac.auckland.apiproxy.chat.openai.ChatMessage;
import nz.ac.auckland.apiproxy.chat.openai.Choice;
import nz.ac.auckland.apiproxy.config.ApiProxyConfig;
import nz.ac.auckland.apiproxy.exceptions.ApiProxyException;
import nz.ac.auckland.se206.AiWitnessStateManager;
import nz.ac.auckland.se206.ChatStorage;

/**
 * Controller class for the AI Witness chat view. Handles user interactions and communication with
 * the GPT model via the API proxy.
 */
public class AiWitnessChatController extends ChatControllerCentre {

  // Tracks how many times the AI witness has been clicked
  @FXML private ImageView flashbackOne;
  @FXML private ImageView flashbackTwo;
  @FXML private ImageView flashbackThree;
  @FXML private Button continueButton;
  @FXML private TextArea txtaChat;
  @FXML private Slider slider;
  @FXML private VBox flashbackMessage;
  @FXML private ImageView speechBubble1;
  @FXML private ImageView speechBubble2;
  @FXML private ImageView speechBubble4;
  @FXML private ImageView speechBubble5;
  @FXML private ImageView speechBubble6;
  @FXML private ImageView speechBubble7;
  @FXML private ImageView speechBubble8;
  @FXML private ImageView speechBubble9;
  @FXML private Button clearNoiseBtn;
  @FXML private ImageView rumourBin;
  @FXML private TextArea chatTextArea;
  @FXML private TextFlow StartLabelText;
  @FXML private ImageView arrowHint;

  private int aiWitnessClickCount = 0;
  private int flashbackStep = 0;
  private final Map<ImageView, Label> speechBubbleLabels = new HashMap<>();
  private Label instructionLabel;
  private Label completionLabel;
  private Label sliderInstructionLabel;
  private List<String> playerActions = new ArrayList<>();

  /**
   * Handles clicks on the AI witness. Shows flashback scene on first click, memory on subsequent
   * clicks.
   */
  @FXML
  private void onAiWitnessClicked() {
    if (aiWitnessClickCount == 0) {
      showFlashbackScene();
    } else {
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

  /**
   * Logs a player action for tracking their progress through the memory/puzzle.
   *
   * @param action The description of the action performed
   */
  private void logAction(String action) {
    playerActions.add(action);

    if (action.startsWith("Disposed:")) {
      // Get the updated count after the bubble was just disposed
      AiWitnessStateManager stateManager = AiWitnessStateManager.getInstance();

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

              // Trigger automatic AI response about unreliable testimony
              triggerFinalAiStatement();
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
      case 1:
        // Show second flashback
        flashbackOne.setVisible(false);
        flashbackTwo.setVisible(true);
        flashbackThree.setVisible(false);
        continueButton.setText("Continue");
        break;
      case 2:
        // Show third flashback
        flashbackOne.setVisible(false);
        flashbackTwo.setVisible(false);
        flashbackThree.setVisible(true);
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

    // Reset click count so flashback shows properly on restart
    aiWitnessClickCount = 0;

    // Initialize flashback UI if we're in the flashback scene
    if (flashbackOne != null && continueButton != null) {
      // Show first flashback, hide others
      flashbackOne.setVisible(true);
      flashbackTwo.setVisible(false);
      flashbackThree.setVisible(false);
      continueButton.setText("Continue");
      flashbackStep = 0;
    }

    flashbackMessage.setVisible(true);
    setupSpeechBubbleTexts();

    arrowHint.setVisible(false);
    hoveringArrowAnimation(arrowHint);

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

      // Show slider instruction if user hasn't started sliding yet
      if (sliderValue == 0) {
        showSliderInstruction();
      }
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
    instructionLabel = new Label("Drag the speech bubbles into the bin");
    instructionLabel.setStyle(
        "-fx-font-size: 43px; -fx-text-fill: white; -fx-background-color: rgba(0, 0, 0, 0.7);"
            + " -fx-padding: 10px;");
    instructionLabel.setLayoutX(350); // Center horizontally
    instructionLabel.setLayoutY(250); // Position near top
    instructionLabel.setVisible(false);
    ((AnchorPane) slider.getParent()).getChildren().add(instructionLabel);

    // Create and style the slider instruction label
    sliderInstructionLabel = new Label("To start the interactable slide the slider");
    sliderInstructionLabel.setStyle(
        "-fx-font-size: 35px; -fx-text-fill: white; -fx-background-color: rgba(0, 0, 0, 0.7);"
            + " -fx-padding: 10px; -fx-background-radius: 5px;");
    sliderInstructionLabel.setWrapText(true);

    // Position the label to fit completely within the TextFlow area
    if (StartLabelText != null && StartLabelText.getParent() instanceof AnchorPane) {
      AnchorPane parent = (AnchorPane) StartLabelText.getParent();

      // Match the TextFlow's exact position and dimensions
      sliderInstructionLabel.setLayoutX(StartLabelText.getLayoutX());
      sliderInstructionLabel.setLayoutY(StartLabelText.getLayoutY());

      // Make the label fit completely within the TextFlow dimensions
      sliderInstructionLabel.setPrefWidth(StartLabelText.getPrefWidth());
      sliderInstructionLabel.setPrefHeight(StartLabelText.getPrefHeight());
      sliderInstructionLabel.setMaxWidth(StartLabelText.getPrefWidth());
      sliderInstructionLabel.setMaxHeight(StartLabelText.getPrefHeight());

      sliderInstructionLabel.setAlignment(Pos.CENTER);
      parent.getChildren().add(sliderInstructionLabel);
    } else {
      // Fallback: add to slider's parent if TextFlow positioning fails
      System.out.println("DEBUG: TextFlow not found or invalid parent, using fallback positioning");
      sliderInstructionLabel.setLayoutX(321.0); // Same as slider
      sliderInstructionLabel.setLayoutY(200.0); // Above slider
      sliderInstructionLabel.setAlignment(Pos.CENTER);
      if (slider != null && slider.getParent() instanceof AnchorPane) {
        ((AnchorPane) slider.getParent()).getChildren().add(sliderInstructionLabel);
      }
    }

    sliderInstructionLabel.setVisible(false);

    // Show slider instruction if user hasn't started sliding yet
    if (!state.hasShownAllBubbles() && state.getSliderValue() == 0) {
      showSliderInstruction();
      arrowHint.setVisible(true);
    }

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
    completionLabel.setLayoutX(400);
    completionLabel.setLayoutY(400);

    // Restore the visibility state of the completion label
    completionLabel.setVisible(state.isEndLabelVisible());

    ((AnchorPane) slider.getParent()).getChildren().add(completionLabel);

    slider
        .valueProperty()
        .addListener(
            (obs, oldVal, newVal) -> {
              // Hide slider instruction when user starts using slider
              if (newVal.intValue() > 0 && sliderInstructionLabel.isVisible()) {
                sliderInstructionLabel.setVisible(false);
              }
              // Hide arrow hint when user starts using slider
              if (arrowHint != null && newVal.intValue() > 0) {
                arrowHint.setVisible(false);
              }
              showSpeechBubble(newVal.intValue());
            });

    Platform.runLater(
        () -> {
          PauseTransition pause = new PauseTransition(Duration.seconds(1));
          pause.setOnFinished(e -> flashbackMessage.setVisible(false));
          pause.play();
        });
  }

  private void hideAllSpeechBubbles() {
    speechBubble1.setVisible(false);
    speechBubble2.setVisible(false);
    speechBubble4.setVisible(false);
    speechBubble5.setVisible(false);
    speechBubble6.setVisible(false);
    speechBubble7.setVisible(false);
    speechBubble8.setVisible(false);
    speechBubble9.setVisible(false);

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

    label.setVisible(false);
    speechBubbleLabels.put(bubble, label);
  }

  private void setupSpeechBubbleTexts() {
    // Add text to speech bubbles
    addTextToSpeechBubble(speechBubble2, "Did you hear about that new AI project?");
    addTextToSpeechBubble(speechBubble1, "Yeah, it was trained on a bunch of music.");
    addTextToSpeechBubble(speechBubble7, "Some people say the artists never agreed to it...");
    addTextToSpeechBubble(
        speechBubble4, "I thought I heard something about consent, but not sure.");
    addTextToSpeechBubble(speechBubble8, "Either way, musicians are upset.");
    addTextToSpeechBubble(speechBubble5, "They think their styles were copied.");
    addTextToSpeechBubble(speechBubble9, "I don’t know... inspiration isn’t the same as stealing.");
    addTextToSpeechBubble(speechBubble6, "Rumours spread so quickly about this stuff.");
  }

  private void showSpeechBubble(int value) {
    // Clear all sppech bubbles
    hideAllSpeechBubbles();
    // Show text corresponding to bar value
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
    if (value >= 9) {
      showBubbleWithText(speechBubble9);
    }
    if (value >= 10) {
      showBubbleWithText(speechBubble6);
    }
    if (value >= 11) {
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

  /** Shows the slider instruction label when user first enters memory scene. */
  private void showSliderInstruction() {
    // Check if label exists and was properly initialized
    if (sliderInstructionLabel == null) {
      return;
    }

    System.out.println("DEBUG: Showing slider instruction label");
    sliderInstructionLabel.setVisible(true);

    // Make sure it's on top
    if (sliderInstructionLabel.getParent() != null) {
      sliderInstructionLabel.toFront();
    }
  }

  private int getBubbleNumber(ImageView bubble) {
    // Mapping based on order of appearance in showSpeechBubble method
    if (bubble == speechBubble2) {
      return 1;
    }
    if (bubble == speechBubble1) {
      return 2;
    }
    if (bubble == speechBubble7) {
      return 3;
    }
    if (bubble == speechBubble4) {
      return 4;
    }
    if (bubble == speechBubble8) {
      return 5;
    }
    if (bubble == speechBubble5) {
      return 6;
    }
    if (bubble == speechBubble9) {
      return 7;
    }
    if (bubble == speechBubble6) {
      return 8;
    }
    return -1;
  }

  private ImageView getBubbleByNumber(int number) {
    // Map each number back to bubbles in order of appearance
    switch (number) {
      case 1:
        return speechBubble2;
      case 2:
        return speechBubble1;
      case 3:
        return speechBubble7;
      case 4:
        return speechBubble4;
      case 5:
        return speechBubble8;
      case 6:
        return speechBubble5;
      case 7:
        return speechBubble9;
      case 8:
        return speechBubble6;
      default:
        return null;
    }
  }

  private void makeDraggableWithBinDetection(StackPane stack, ImageView bubble) {
    // Make the speech bubbles draggable
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
          if (!isDragging[0]) {
            return;
          }

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

              // Send disposal context to GPT (not visible to player)
              sendDisposalContextToGpt(bubbleText);

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

  /**
   * Sends disposal context to GPT without showing it to player. This allows the GPT to be aware of
   * user disposal actions.
   */
  private void sendDisposalContextToGpt(String bubbleText) {
    // Create a system message with disposal context
    String contextMessage =
        "SYSTEM_CONTEXT: Player disposed of rumor: '"
            + bubbleText
            + "'. Current disposed count: "
            + AiWitnessStateManager.getInstance().getBubblesInBin()
            + "/8";

    // Add this as a system message to ChatStorage but don't display it
    ChatMessage contextMsg = new ChatMessage("system", contextMessage);
    ChatStorage.addMessage("aiwitness", contextMsg);
  }

  private void hoveringArrowAnimation(ImageView arrow) {
    // floating animation
    TranslateTransition floatTransition = new TranslateTransition(Duration.millis(1500), arrow);
    floatTransition.setFromY(0);
    floatTransition.setToY(-15);
    floatTransition.setAutoReverse(true);
    floatTransition.setCycleCount(TranslateTransition.INDEFINITE);

    floatTransition.play();
  }

  /**
   * Triggers an automatic AI statement when all rumors are disposed, acknowledging the unreliable
   * nature of rumor-based testimony.
   */
  public void triggerFinalAiStatement() {
    // Run GPT response in background task to generate automatic AI statement
    Task<Void> task =
        new Task<>() {
          @Override
          protected Void call() {
            try {
              // Create a system message that triggers the AI to make its own statement
              String systemPrompt =
                  "The user has now finished the interactable and has found out that the court has"
                      + " deemed your testimony unreliable due to it being based on rumours. Be a"
                      + " little bit defensive about this. (MAX 15 WORDS)";
              ChatMessage systemContext = new ChatMessage("system", systemPrompt);

              // Recreate the runGpt logic here since we can't access the private method
              ApiProxyConfig config = ApiProxyConfig.readConfig();
              ChatCompletionRequest request =
                  new ChatCompletionRequest(config)
                      .setN(1)
                      .setTemperature(0.2)
                      .setTopP(0.5)
                      .setModel(Model.GPT_4_1_MINI)
                      .setMaxTokens(150);

              // Get original system prompt
              ChatMessage originalSystemPrompt = ChatStorage.getSystemPrompt("aiwitness");
              if (originalSystemPrompt != null) {
                request.addMessage(originalSystemPrompt);
              }

              // Add the special context for this final statement
              request.addMessage(systemContext);

              // Add shared conversations between gpts (last 5 messages)
              List<ChatMessage> globalContext = ChatStorage.getContext();
              int start = Math.max(0, globalContext.size() - 5);
              for (ChatMessage contextMsg : globalContext.subList(start, globalContext.size())) {
                if (!contextMsg.isSystemPrompt()) {
                  request.addMessage(contextMsg);
                }
              }

              ChatCompletionResult result = request.execute();
              Choice choice = result.getChoices().iterator().next();
              ChatMessage assistantMsg = choice.getChatMessage();

              String content = assistantMsg.getContent().trim();
              String prefix = "Ai Witness:";

              // If GPT already added the same prefix, strip it
              if (content.startsWith(prefix)) {
                content = content.substring(prefix.length()).trim();
              }

              // This makes sure the GPT will know who said what
              assistantMsg.setContent(prefix + " " + content);
              ChatStorage.addMessage("aiwitness", assistantMsg);

              Platform.runLater(() -> appendChatMessage(assistantMsg));

            } catch (ApiProxyException e) {
              e.printStackTrace();
            }
            return null;
          }
        };

    new Thread(task).start();
  }
}
