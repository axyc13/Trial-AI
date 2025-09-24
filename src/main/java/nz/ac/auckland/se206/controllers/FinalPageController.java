package nz.ac.auckland.se206.controllers;

import java.io.File;
import java.io.IOException;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
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
                    String audioFile2 = "src/main/resources/sounds/gameOver.mp3";

                    Media sound2 = new Media(new File(audioFile2).toURI().toString());
                    MediaPlayer mediaPlayer2 = new MediaPlayer(sound2);

                    mediaPlayer2.play();
                  }
                }));
    timeline.setCycleCount(totalSeconds);
    timeline.play();
  }

  @FXML
  private void onYesClick() {
    yesButton.setScaleX(1.2);
    yesButton.setScaleY(1.2);

    isYesClicked = true;
    isNoClicked = false;
    setYesOrNoClick();
  }

  @FXML
  private void onNoClick() {
    noButton.setScaleX(1.2);
    noButton.setScaleY(1.2);

    isNoClicked = true;
    isYesClicked = false;
    setYesOrNoClick();
  }

  private void setYesOrNoClick() {
    optionPickingMessage.setText("");
    // If yes and no are not clicked yet
    if (isNoClicked == false && isYesClicked == false) {
      return;
      // If no is clicked after yes
    } else if (isNoClicked) {
      yesButton.setScaleX(1);
      yesButton.setScaleY(1);
      // If yes is clicked after no
    } else if (isYesClicked) {
      noButton.setScaleX(1);
      noButton.setScaleY(1);
    }
  }

  @FXML
  private void onSendClick() {
    String message = txtInput.getText().trim();

    // Checks for empty string
    if (message.isEmpty() && !(remainingSeconds <= 0)) {
      txtInput.getStyleClass().removeAll("text-area-normal", "text-area-error");
      txtInput.getStyleClass().add("text-area-error");
      optionTextMessage.setText("Please provide an answer");
      return;
    }

    // Check if at least yes or no is clicked
    if (isNoClicked == false && isYesClicked == false && !(remainingSeconds <= 0)) {
      optionPickingMessage.setText("Please Choose Yes or No");
      return;
    }

    txtInput.clear();

    // No message and out of time
    if (message.isEmpty()) {
      txtInput.appendText("You Lose! Incorrect rationale was given.");
    } // The no button is clicked and timer is out
    else if (isNoClicked == true && !(message.isEmpty())) {
      txtInput.appendText("You Lose! Incorrect verdict was chosen.");
    } // None of the buttons are chosen and timer runs out
    else if (isNoClicked == false && isYesClicked == false) {
      txtInput.appendText("You Lose! No verdict was chosen.");
    }

    // Disable all buttons and stop timer
    submitButton.setDisable(true);
    timeline.stop();
    yesButton.setDisable(true);
    noButton.setDisable(true);
    txtInput.setDisable(true);
    onInputStartUp();

    questionLabel.setText("Feedback:");

    // yes button is chosen and message is not empty
    if (isYesClicked == true && !(message.isEmpty())) {
      txtInput.appendText("You Win! ");

      // Display the winning vbox
      setWinOverlay();

      Task<Void> task =
          new Task<>() {
            @Override
            protected Void call() {
              try {
                runGpt(message);
              } catch (ApiProxyException e) {
                e.printStackTrace();
              }
              return null;
            }
          };

      new Thread(task).start();

      // Check rationale

    } else {
      // Display the losing vbox
      setLoseOverlay();
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
        new Timeline(new KeyFrame(Duration.seconds(2), e -> overlaySuccess.setVisible(false)));
    winTime.play();
  }

  private void setLoseOverlay() {
    // Display losing
    overlayFailure.setVisible(true);
    Timeline loseTime =
        new Timeline(new KeyFrame(Duration.seconds(2), e -> overlayFailure.setVisible(false)));
    loseTime.play();
  }

  @FXML
  private void onResetGame(ActionEvent event) throws ApiProxyException, IOException {
    ChatStorage.resetAllChats();
    RoomController.resetTimer();
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
            .setMaxTokens(1);

    // Get prompt
    String systemPrompt = getSystemPrompt("feedbackResponse.txt");

    request.addMessage("system", systemPrompt);
    request.addMessage("user", "User's rationale: " + message);

    ChatCompletionResult result = request.execute();
    Choice choice = result.getChoices().iterator().next();
    ChatMessage assistantMsg = choice.getChatMessage();

    Platform.runLater(() -> appendChatMessage(assistantMsg));

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
}
