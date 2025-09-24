package nz.ac.auckland.se206.controllers;

import java.io.IOException;
import java.util.List;
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

    // Prints previous conversations in chat box
    List<ChatMessage> history = ChatStorage.getHistory(this.profession);
    for (ChatMessage msg : history) {
      appendChatMessage(msg);
    }

    // Add prompt
    ChatMessage systemMsg = new ChatMessage("system", getSystemPrompt(file));
    systemMsg.setSystemPrompt(true);
    ChatStorage.setSystemPrompt(this.profession, systemMsg);

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
  }

  /**
   * Appends a chat message to the chat text area.
   *
   * @param msg the chat message to append
   */
  public void appendChatMessage(ChatMessage msg) {
    if (msg.isSystemPrompt()) {
      return;
    }
    if ("user".equals(msg.getRole())) {
      txtaChat.appendText("user: " + msg.getContent() + "\n\n");
      return;
    }
    txtaChat.appendText(msg.getContent() + "\n\n");
  }

  /**
   * Runs the GPT model with a given chat message.
   *
   * @param msg the chat message to process
   * @return the response chat message
   * @throws ApiProxyException if there is an error communicating with the API proxy
   */
  private ChatMessage runGpt(ChatMessage msg) throws ApiProxyException {
    ApiProxyConfig config = ApiProxyConfig.readConfig();
    ChatCompletionRequest request =
        new ChatCompletionRequest(config)
            .setN(1)
            .setTemperature(0.2)
            .setTopP(0.5)
            .setModel(Model.GPT_4_1_MINI)
            .setMaxTokens(50);

    // Get prompt
    ChatMessage systemPrompt = ChatStorage.getSystemPrompt(this.profession);
    if (systemPrompt != null) {
      request.addMessage(systemPrompt);
    }

    // Add shared conversations between gpts (last 5 messages)
    List<ChatMessage> globalContext = ChatStorage.getContext();
    int start = Math.max(0, globalContext.size() - 5);
    for (ChatMessage contextMsg : globalContext.subList(start, globalContext.size())) {
      if (!contextMsg.isSystemPrompt()) {
        request.addMessage(contextMsg);
      }
    }

    // Add current message
    request.addMessage(msg);

    ChatCompletionResult result = request.execute();
    Choice choice = result.getChoices().iterator().next();
    ChatMessage assistantMsg = choice.getChatMessage();

    String content = assistantMsg.getContent().trim();
    String prefix = "[" + this.profession + "]:";
    // If GPT already added the same prefix, strip it
    if (content.startsWith(prefix) && this.profession != "user") {
      content = content.substring(prefix.length()).trim();
    }

    // This makes sure, the gpt will know who said what
    assistantMsg.setContent(prefix + " " + content);
    ChatStorage.addMessage(this.profession, assistantMsg);

    Platform.runLater(() -> appendChatMessage(assistantMsg));

    return assistantMsg;
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
