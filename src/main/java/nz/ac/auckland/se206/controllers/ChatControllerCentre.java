package nz.ac.auckland.se206.controllers;

import java.io.IOException;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
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

/**
 * Abstract class that controls all chat views. Handles user interactions and communication with the
 * GPT model via the API proxy.
 */
public abstract class ChatControllerCentre {

  @FXML private TextArea txtaChat;
  @FXML private TextField txtInput;
  @FXML private Button btnSend;
  @FXML private Label timer;
  @FXML private ProgressBar progressBar;

  private ChatCompletionRequest chatCompletionRequest;
  private String profession;

  /**
   * Initializes the chat view. Starts the 2:00 timer and binds it's progress to the progress bar.
   *
   * @throws ApiProxyException if there is an error communicating with the API proxy
   */
  @FXML
  public void initialize() throws ApiProxyException {
    TimerManager timer = TimerManager.getInstance();

    // Set initial state immediately
    this.timer.setText(TimerManager.formatTime(timer.getSecondsRemainingProperty().get()));
    progressBar.progressProperty().bind(timer.getProgressProperty());
    applyColor(timer.getProgressProperty().get());

    // Bind updates
    timer
        .getSecondsRemainingProperty()
        .addListener(
            (obs, oldVal, newVal) -> {
              this.timer.setText(TimerManager.formatTime(newVal.intValue()));
            });

    timer
        .getProgressProperty()
        .addListener(
            (obs, oldVal, newVal) -> {
              applyColor(newVal.doubleValue());
            });
  }

  private void applyColor(double progress) {
    // Progress bar changes colour depending on the time
    String color = TimerManager.getAccentColor(progress);
    progressBar.setStyle("-fx-accent: " + color + ";");
  }

  /**
   * Generates the system prompt based on the profession.
   *
   * @return the system prompt string
   */
  private String getSystemPrompt(String file) {
    return PromptEngineering.getPrompt(file);
  }

  /**
   * Initialises the ChatCompletionRequest.
   *
   * @param profession the profession to set
   */
  public void initialiseChatGpt(String file, String profession) {
    this.profession = profession;

    // Get prompt and save it
    ChatMessage systemMsg = new ChatMessage("system", getSystemPrompt(file));
    ChatStorage.addMessage("system", systemMsg);
    systemMsg.setSystemPrompt(true);

    // Initialise gpt and run it
    try {
      ApiProxyConfig config = ApiProxyConfig.readConfig();

      chatCompletionRequest =
          new ChatCompletionRequest(config)
              .setN(1)
              .setTemperature(0.1) // Lower temperature for more focused responses
              .setTopP(0.2) // More restrictive sampling
              .setModel(Model.GPT_4_1_MINI)
              .setMaxTokens(1);

      Task<Void> task =
          new Task<>() {
            @Override
            protected Void call() {
              try {
                runGpt(systemMsg);
              } catch (ApiProxyException e) {
                e.printStackTrace();
              }
              return null;
            }
          };
      new Thread(task).start();
    } catch (ApiProxyException e) {
      e.printStackTrace();
    }
  }

  /**
   * Appends a chat message to the chat text area.
   *
   * @param msg the chat message to append
   */
  private void appendChatMessage(ChatMessage msg) {
    if (msg.isSystemPrompt()) {
      return;
    }
    String displayRole = msg.getRole().equals("assistant") ? this.profession : msg.getRole();
    txtaChat.appendText(displayRole + ": " + msg.getContent() + "\n\n");
  }

  /**
   * Runs the GPT model with a given chat message.
   *
   * @param msg the chat message to process
   * @return the response chat message
   * @throws ApiProxyException if there is an error communicating with the API proxy
   */
  private ChatMessage runGpt(ChatMessage msg) throws ApiProxyException {
    // Feed message to gpt
    chatCompletionRequest.addMessage(msg);

    // Execute gpt
    try {
      ChatCompletionResult chatCompletionResult = chatCompletionRequest.execute();
      Choice result = chatCompletionResult.getChoices().iterator().next();
      chatCompletionRequest.addMessage(result.getChatMessage());

      // Save the gpt response for later
      ChatStorage.addMessage(this.profession, result.getChatMessage());
      Platform.runLater(() -> appendChatMessage(result.getChatMessage()));

      return result.getChatMessage();
    } catch (ApiProxyException e) {
      e.printStackTrace();
      return null;
    }
  }

  /**
   * Handles the key pressed event.
   *
   * @param event the key event
   */
  @FXML
  public void sendMessage(KeyEvent event) {
    if (event.getCode().equals(KeyCode.ENTER)) {
      onSendMessage();
    }
  }

  /**
   * Sends a message to the GPT model.
   *
   * @param event the action event triggered by the send button
   * @throws ApiProxyException if there is an error communicating with the API proxy
   * @throws IOException if there is an I/O error
   */
  @FXML
  private void onSendMessage() {
    String message = txtInput.getText().trim();
    // Don't do anything if no text sent
    if (message.isEmpty()) {
      return;
    }
    txtInput.clear();

    // Store user message (to be printed later on)
    ChatMessage msg = new ChatMessage("user", message);
    ChatStorage.addMessage(this.profession, msg);
    appendChatMessage(msg);

    // Run chatgpt
    Task<Void> task =
        new Task<>() {
          @Override
          protected Void call() {
            try {
              runGpt(msg);
            } catch (ApiProxyException e) {
              e.printStackTrace();
            }
            return null;
          }
        };

    new Thread(task).start();
  }

  /**
   * Navigates back to the previous view.
   *
   * @param event the action event triggered by the go back button
   * @throws ApiProxyException if there is an error communicating with the API proxy
   * @throws IOException if there is an I/O error
   */
  @FXML
  private void onGoBack(ActionEvent event) throws ApiProxyException, IOException {
    App.setRoot("room");
  }
}
