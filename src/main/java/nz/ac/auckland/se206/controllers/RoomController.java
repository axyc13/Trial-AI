package nz.ac.auckland.se206.controllers;

import java.io.IOException;
import java.util.List;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.scene.shape.Rectangle;
import nz.ac.auckland.apiproxy.chat.openai.ChatCompletionRequest;
import nz.ac.auckland.apiproxy.chat.openai.ChatCompletionRequest.Model;
import nz.ac.auckland.apiproxy.chat.openai.ChatCompletionResult;
import nz.ac.auckland.apiproxy.chat.openai.ChatMessage;
import nz.ac.auckland.apiproxy.chat.openai.Choice;
import nz.ac.auckland.apiproxy.config.ApiProxyConfig;
import nz.ac.auckland.apiproxy.exceptions.ApiProxyException;
import nz.ac.auckland.se206.ChatStorage;
import nz.ac.auckland.se206.GameStateContext;
import nz.ac.auckland.se206.TimerManager;
import nz.ac.auckland.se206.prompts.PromptEngineering;

/**
 * Controller class for the room view. Handles user interactions within the room where the user can
 * chat with customers and guess their profession.
 */
public class RoomController {

  private static GameStateContext context = new GameStateContext();
  private static boolean isFirstTimeInit = true;
  private String profession;

  @FXML private Rectangle rectCashier;
  @FXML private Rectangle rectPerson1;
  @FXML private Rectangle rectPerson2;
  @FXML private Rectangle rectPerson3;
  @FXML private Rectangle rectWaitress;
  @FXML private Button btnGuess;
  @FXML private Label timer;
  @FXML private ProgressBar progressBar;
  @FXML private Pane overlay;
  @FXML private TextArea txtaChat;
  @FXML private TextField txtInput;

  /**
   * Initialises the room view. Start's the 2:00 timer and binds it's progress to the progress bar.
   */
  @FXML
  public void initialize() {
    TimerManager timer = TimerManager.getInstance();

    if (isFirstTimeInit) {
      timer.start();
      isFirstTimeInit = false;
    }

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
    String color = TimerManager.getAccentColor(progress);
    progressBar.setStyle("-fx-accent: " + color + ";");
  }

  /**
   * Handles mouse clicks on rectangles representing people in the room.
   *
   * @param event the mouse event triggered by clicking a rectangle
   * @throws IOException if there is an I/O error
   */
  @FXML
  private void handleRectangleClick(MouseEvent event) throws IOException {
    Rectangle clickedRectangle = (Rectangle) event.getSource();
    context.handleRectangleClick(event, clickedRectangle.getId());
  }

  /**
   * Handles the final decision button click event.
   *
   * @throws IOException if there is an I/O error
   */
  @FXML
  private void onDecisionClick() throws IOException {
    context.handleFinalDecisionClick();
  }

  public void showOverlay() {
    overlay.setVisible(true);
    overlay.toFront();
  }

  @FXML
  private void onGoBackClick() {
    overlay.setVisible(false);
    overlay.toBack();
  }

  /**
   * Handles the case when the user sends a message. The message is first stored in a chat history
   * database and then the message is fed to GPT
   */
  @FXML
  private void onSendClick() {
    String message = txtInput.getText().trim();
    if (message.isEmpty()) {
      return;
    }
    txtInput.clear();
    ChatMessage msg = new ChatMessage("user", message);
    ChatStorage.addMessage(this.profession, msg);
    appendChatMessage(msg);

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
   * Handles the key pressed event.
   *
   * @param event the key event
   */
  @FXML
  public void sendMessage(KeyEvent event) {
    System.out.println("Key " + event.getCode() + " pressed");
    if (event.getCode().equals(KeyCode.ENTER)) {
      // Send the message
      onSendClick();
    }
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
            .setMaxTokens(1);

    // Get prompt
    ChatMessage systemPrompt = ChatStorage.getSystemPrompt(profession);
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

    ChatStorage.addMessage(profession, assistantMsg);
    Platform.runLater(() -> appendChatMessage(assistantMsg));

    return assistantMsg;
  }

  /**
   * Initialises the ChatCompletionRequest.
   *
   * @param file the txt file used as a primpt
   * @param profession the profession to set
   */
  public void initialiseChatGpt(String file, String profession) {
    this.profession = profession;

    List<ChatMessage> history = ChatStorage.getHistory(profession);
    for (ChatMessage msg : history) {
      appendChatMessage(msg);
    }
    ChatMessage systemMsg = new ChatMessage("system", getSystemPrompt(file));
    systemMsg.setSystemPrompt(true);
    // Add prompt
    ChatStorage.setSystemPrompt(profession, systemMsg);

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
    String displayRole = msg.getRole().equals("assistant") ? this.profession : msg.getRole();
    txtaChat.appendText(displayRole + ": " + msg.getContent() + "\n\n");
  }

  /**
   * Generates the system prompt based on the profession.
   *
   * @return the system prompt string
   */
  private String getSystemPrompt(String file) {
    return PromptEngineering.getPrompt(file);
  }

  public static void resetTimer() {
    isFirstTimeInit = true;
  }
}
