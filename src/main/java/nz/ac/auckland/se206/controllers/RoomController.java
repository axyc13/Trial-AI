package nz.ac.auckland.se206.controllers;

import java.io.IOException;
import javafx.animation.PauseTransition;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.shape.Rectangle;
import javafx.util.Duration;
import nz.ac.auckland.se206.App;
import nz.ac.auckland.se206.GameStateContext;
import nz.ac.auckland.se206.TimerManager;

/**
 * Controller class for the room view. Handles user interactions within the room where the user can
 * chat with customers and guess their profession.
 */
public class RoomController {

  private static GameStateContext context = new GameStateContext();
  private static boolean isFirstTimeInit = true;

  public static void resetTimer() {
    isFirstTimeInit = true;
  }

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
  @FXML private StackPane warningMessage;

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
    // Must talk to all three before being able to go to the final decision screen
    if (App.getProfessionsOpened().size() != 3) {
      warningMessage.setVisible(true);
      Platform.runLater(
          () -> {
            PauseTransition pause = new PauseTransition(Duration.seconds(2));
            pause.setOnFinished(e -> warningMessage.setVisible(false));
            pause.play();
          });
      return;
    }
    App.setTalkedToAll(true);
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
}
