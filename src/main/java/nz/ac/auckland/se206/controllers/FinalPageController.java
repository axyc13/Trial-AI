package nz.ac.auckland.se206.controllers;

import java.io.File;
import java.io.IOException;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.VBox;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.util.Duration;
import nz.ac.auckland.apiproxy.chat.openai.ChatCompletionRequest;
import nz.ac.auckland.apiproxy.chat.openai.ChatCompletionRequest.Model;
import nz.ac.auckland.apiproxy.chat.openai.ChatCompletionResult;
import nz.ac.auckland.apiproxy.chat.openai.ChatMessage;
import nz.ac.auckland.apiproxy.chat.openai.Choice;
import nz.ac.auckland.apiproxy.config.ApiProxyConfig;
import nz.ac.auckland.apiproxy.exceptions.ApiProxyException;
import nz.ac.auckland.se206.AiWitnessStateManager;
import nz.ac.auckland.se206.App;
import nz.ac.auckland.se206.ChatStorage;
import nz.ac.auckland.se206.TimerManager;
import nz.ac.auckland.se206.prompts.PromptEngineering;

public class FinalPageController {

  @FXML private Label timer;
  @FXML private VBox overlay;
  @FXML private VBox overlaySuccess;
  @FXML private VBox overlayFailure;
  @FXML private Button yesButton;
  @FXML private Button noButton;
  @FXML private TextArea txtInput;
  @FXML private Button submitButton;
  @FXML private Label optionPickingMessage;
  @FXML private Label optionTextMessage;
  @FXML private Label questionLabel;
  @FXML private Button restartButton;
  @FXML private Label waitingLabel;
  @FXML private Label titleLabel1;
  @FXML private Label titleLabel2;

  private Timeline timeline;
  private final int totalSeconds = 60;
  private int remainingSeconds = totalSeconds;
  private boolean isYesClicked = false;
  private boolean isNoClicked = false;

  /**
   * Initializes the final page.
   *
   * @throws ApiProxyException if there is an error communicating with the API proxy
   */
  @FXML
  public void initialize() throws ApiProxyException {
    if (!App.getTalkedToAll()) {
      setLoseOverlay();
      txtInput.setText("You did not talk to all the characters!");
      questionLabel.setVisible(false);
      optionPickingMessage.setVisible(false);
      optionTextMessage.setVisible(false);
      timer.setVisible(false);
      yesButton.setDisable(true);
      noButton.setDisable(true);
      submitButton.setDisable(true);
      txtInput.setDisable(true);
      return;
    }
    // Stop 2:00 timer and play flashback tts
    TimerManager.getInstance().stop();

    // Erase all text
    optionPickingMessage.setText("");
    optionTextMessage.setText("");

    txtInput.clear();

    // Enable all buttons
    submitButton.setDisable(false);
    yesButton.setDisable(false);
    noButton.setDisable(false);
    txtInput.setDisable(false);
    restartButton.setVisible(false);
    restartButton.setDisable(true);
    titleLabel1.setDisable(false);
    titleLabel2.setDisable(false);
    timer.setDisable(false);

    questionLabel.setText("Briefly state why?");

    // Initialise yes and no buttons
    noButton.setScaleX(1);
    noButton.setScaleY(1);
    yesButton.setScaleX(1);
    yesButton.setScaleY(1);
    isYesClicked = false;
    isNoClicked = false;

    // Prevent line skipping with enter key
    txtInput.addEventFilter(
        KeyEvent.KEY_PRESSED,
        event -> {
          if (event.getCode() == KeyCode.ENTER) {
            event.consume();

            if (isNoClicked || isYesClicked) {
              onSendClick();
            } else {
              optionPickingMessage.setText("Please Choose Yes or No");
              yesButton.getStyleClass().add("button-error");
              noButton.getStyleClass().add("button-error");
            }
          }
        });

    String audioFile = "src/main/resources/sounds/oneMinuteLeft.mp3";

    Media sound = new Media(new File(audioFile).toURI().toString());
    MediaPlayer mediaPlayer = new MediaPlayer(sound);

    mediaPlayer.play();

    submitButton.setDisable(false);

    timer.setText(String.format("01:00"));

    // Start 60s timer
    timeline =
        new Timeline(
            new KeyFrame(
                Duration.seconds(1),
                e -> {
                  remainingSeconds--;
                  timer.setText(String.format("00:%02d", remainingSeconds % 60));

                  if (remainingSeconds <= 0) {
                    timeline.stop();

                    // auto submit everything
                    onSendClick();
                  }
                }));
    timeline.setCycleCount(totalSeconds);
    timeline.play();
  }

  @FXML
  private void onYesClick() {
    isYesClicked = true;
    isNoClicked = false;
    setYesOrNoClick();
  }

  @FXML
  private void onNoClick() {
    isNoClicked = true;
    isYesClicked = false;
    setYesOrNoClick();
  }

  private void setYesOrNoClick() {
    optionPickingMessage.setText("");

    // Reset both buttons to normal scale first
    yesButton.setScaleX(1);
    yesButton.setScaleY(1);
    noButton.setScaleX(1);
    noButton.setScaleY(1);

    // Remove selected and error styling from both buttons
    yesButton.getStyleClass().removeAll("selected-button", "button-error");
    noButton.getStyleClass().removeAll("selected-button", "button-error");

    // Scale up and add glow to the selected button
    if (isYesClicked) {
      yesButton.setScaleX(1.2);
      yesButton.setScaleY(1.2);
      yesButton.getStyleClass().add("selected-button");
    } else if (isNoClicked) {
      noButton.setScaleX(1.2);
      noButton.setScaleY(1.2);
      noButton.getStyleClass().add("selected-button");
    }
  }

  @FXML
  private void onSendClick() {
    String message = txtInput.getText().trim();
    boolean hasErrors = false;

    // Check for empty string
    if (message.isEmpty() && !(remainingSeconds <= 0)) {
      txtInput.getStyleClass().removeAll("text-area-normal", "text-area-error");
      txtInput.getStyleClass().add("text-area-error");
      optionTextMessage.setText("Please provide an answer");
      hasErrors = true;
    }

    // Check if at least yes or no is clicked
    if (isNoClicked == false && isYesClicked == false && !(remainingSeconds <= 0)) {
      optionPickingMessage.setText("Please Choose Yes or No");
      yesButton.getStyleClass().add("button-error");
      noButton.getStyleClass().add("button-error");
      hasErrors = true;
    }

    // Return early if there are any errors
    if (hasErrors) {
      return;
    }

    txtInput.clear();

    // No message and out of time
    if (message.isEmpty()) {
      txtInput.appendText("You Lose! Incorrect rationale was given.");
    } else if (isNoClicked == true && !(message.isEmpty())) {
      // The no button is clicked and timer is out
      txtInput.appendText("You Lose! Incorrect verdict was chosen.");
    } else if (isNoClicked == false && isYesClicked == false) {
      // None of the buttons are chosen and timer runs out
      txtInput.appendText("You Lose! No verdict was chosen.");
    }

    // Disable all buttons and stop timer
    submitButton.setDisable(true);
    timeline.stop();
    yesButton.setDisable(true);
    noButton.setDisable(true);
    txtInput.setDisable(true);
    titleLabel1.setDisable(true);
    titleLabel2.setDisable(true);
    timer.setDisable(true);
    onInputStartUp();

    questionLabel.setText("Feedback:");

    showOverlay();

    // yes button is chosen and message is not empty
    if (isYesClicked == true && !(message.isEmpty())) {
      Task<Void> task =
          new Task<>() {
            @Override
            protected Void call() {
              try {
                ChatMessage feedback = runGpt(message);
                boolean correctRationale = rationaleChecker(feedback.getContent());
                // Check rationale
                if (correctRationale) {
                  // Display win
                  txtInput.appendText("You Win! ");

                  setWinOverlay();
                } else {
                  // Display loss
                  txtInput.appendText("You Lose! ");
                  setLoseOverlay();
                }
                // Append feedback
                appendChatMessage(feedback);

              } catch (ApiProxyException e) {
                e.printStackTrace();
              }
              return null;
            }
          };

      new Thread(task).start();
    }

    // Enable the restart button
    restartButton.setVisible(true);
    restartButton.setDisable(false);
  }

  @FXML
  private void onInputStartUp() {
    txtInput.getStyleClass().removeAll("text-area-normal", "text-area-error");
    txtInput.getStyleClass().add("text-area-normal");
    optionTextMessage.setText("");
  }

  private void setWinOverlay() {
    // Display winning
    overlaySuccess.setVisible(true);
    Timeline winTime =
        new Timeline(new KeyFrame(Duration.seconds(1.5), e -> overlaySuccess.setVisible(false)));
    winTime.play();
  }

  private void setLoseOverlay() {
    // Display losing
    overlayFailure.setVisible(true);
    Timeline loseTime =
        new Timeline(new KeyFrame(Duration.seconds(1.5), e -> overlayFailure.setVisible(false)));
    loseTime.play();
  }

  private void showOverlay() {
    overlay.setVisible(true);

    // Updates loading label text
    Timeline loseTime =
        new Timeline(
            new KeyFrame(Duration.seconds(0.5), e -> waitingLabel.setText("Awaiting Results.")),
            new KeyFrame(Duration.seconds(1), e -> waitingLabel.setText("Awaiting Results..")),
            new KeyFrame(Duration.seconds(1.5), e -> waitingLabel.setText("Awaiting Results...")),
            new KeyFrame(Duration.seconds(2), e -> waitingLabel.setText("Awaiting Results....")),
            new KeyFrame(Duration.seconds(2.5), e -> waitingLabel.setText("Awaiting Results.....")),
            new KeyFrame(Duration.seconds(3), e -> waitingLabel.setText("Awaiting Results.")),
            new KeyFrame(Duration.seconds(3.5), e -> waitingLabel.setText("Awaiting Results.")),
            new KeyFrame(Duration.seconds(4), e -> waitingLabel.setText("Awaiting Results...")),
            new KeyFrame(Duration.seconds(4.5), e -> waitingLabel.setText("Awaiting Results....")),
            new KeyFrame(Duration.seconds(5), e -> overlay.setVisible(false)));
    loseTime.play();
  }

  @FXML
  private void onResetGame(ActionEvent event) throws ApiProxyException, IOException {
    ChatStorage.resetAllChats();
    RoomController.resetTimer();

    // Reset AI witness state so flashback and memory work properly on restart
    AiWitnessStateManager.getInstance().resetState();

    App.setRoot("room");
  }

  /**
   * Runs the GPT model with a given chat message.
   *
   * @param message the chat message to process
   * @return the response chat message
   * @throws ApiProxyException if there is an error communicating with the API proxy
   */
  private ChatMessage runGpt(String message) throws ApiProxyException {
    ApiProxyConfig config = ApiProxyConfig.readConfig();
    ChatCompletionRequest request =
        new ChatCompletionRequest(config)
            .setN(1)
            .setTemperature(0.2)
            .setTopP(0.5)
            .setModel(Model.GPT_4_1_MINI)
            .setMaxTokens(100);

    // Get prompt
    String systemPrompt = getSystemPrompt("feedbackResponse.txt");

    request.addMessage("system", systemPrompt);
    request.addMessage("user", "User's rationale: " + message);

    ChatCompletionResult result = request.execute();
    Choice choice = result.getChoices().iterator().next();
    ChatMessage assistantMsg = choice.getChatMessage();

    return assistantMsg;
  }

  public void appendChatMessage(ChatMessage msg) {
    if (msg.isSystemPrompt()) {
      return;
    }
    txtInput.appendText(msg.getContent());
  }

  private String getSystemPrompt(String profession) {
    return PromptEngineering.getPrompt(profession);
  }

  private boolean rationaleChecker(String message) throws ApiProxyException {
    ApiProxyConfig config = ApiProxyConfig.readConfig();
    ChatCompletionRequest request =
        new ChatCompletionRequest(config)
            .setN(1)
            .setTemperature(0.1)
            .setTopP(0.3)
            .setModel(Model.GPT_4o_MINI)
            .setMaxTokens(30);

    // Get prompt
    String systemPrompt = getSystemPrompt("rationaleChecker.txt");

    request.addMessage("system", systemPrompt);
    request.addMessage("user", message);

    ChatCompletionResult result = request.execute();
    Choice choice = result.getChoices().iterator().next();
    ChatMessage response = choice.getChatMessage();

    // Returns true for correct rationale
    return response.getContent().trim().equalsIgnoreCase("CORRECT");
  }
}
