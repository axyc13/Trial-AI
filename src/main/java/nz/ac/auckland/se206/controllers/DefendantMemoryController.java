package nz.ac.auckland.se206.controllers;

import java.io.IOException;
import javafx.animation.PauseTransition;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.Slider;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.util.Duration;
import nz.ac.auckland.apiproxy.exceptions.ApiProxyException;
import nz.ac.auckland.se206.App;
import nz.ac.auckland.se206.TimerManager;

public class DefendantMemoryController {

  @FXML private Label timer;
  @FXML private ProgressBar progressBar;
  @FXML private StackPane scene1;
  @FXML private StackPane scene2;
  @FXML private StackPane scene3;
  @FXML private StackPane scene4;
  @FXML private StackPane scene5;
  @FXML private StackPane scene6;
  @FXML private StackPane scene7;
  @FXML private StackPane scene8;
  @FXML private StackPane scene9;
  @FXML private StackPane scene11;
  @FXML private Slider slidingBar;
  @FXML private ImageView image1;
  @FXML private ImageView image2;
  @FXML private Button proceedButton;
  @FXML private VBox memoryMessage;

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

    slidingBar
        .valueProperty()
        .addListener(
            (obs, oldVal, newVal) -> {
              showDialogue(newVal.intValue());
            });
    memoryMessage.setVisible(true);
    Platform.runLater(
        () -> {
          PauseTransition pause = new PauseTransition(Duration.seconds(1));
          pause.setOnFinished(e -> memoryMessage.setVisible(false));
          pause.play();
        });
  }

  private void showDialogue(int value) {
    scene1.setVisible(false);
    scene2.setVisible(false);
    scene3.setVisible(false);
    scene4.setVisible(false);
    scene5.setVisible(false);
    scene6.setVisible(false);
    scene7.setVisible(false);
    scene8.setVisible(false);
    scene9.setVisible(false);
    scene11.setVisible(false);
    image1.setVisible(false);
    proceedButton.setVisible(false);

    switch (value) {
      case 0:
        image1.setVisible(true);
        break;
      case 1:
        image1.setVisible(true);
        scene1.setVisible(true);
        break;
      case 2:
        image1.setVisible(true);
        scene1.setVisible(true);
        scene2.setVisible(true);
        break;
      case 3:
        image1.setVisible(true);
        scene1.setVisible(true);
        scene2.setVisible(true);
        scene3.setVisible(true);
        break;
      case 5:
        scene4.setVisible(true);
        break;
      case 6:
        scene5.setVisible(true);
        break;
      case 7:
        scene6.setVisible(true);
        break;
      case 8:
        image1.setVisible(true);
        break;
      case 9:
        image1.setVisible(true);
        scene7.setVisible(true);
        break;
      case 10:
        image1.setVisible(true);
        scene7.setVisible(true);
        scene8.setVisible(true);
        break;
      case 12:
        scene4.setVisible(true);
        break;
      case 13:
        scene9.setVisible(true);
        break;
      case 14:
        scene11.setVisible(true);
        break;
      case 15:
        proceedButton.setVisible(true);
        break;
      default:
        break;
    }
  }

  private void applyColor(double progress) {
    // Progress bar changes colour depending on the time
    String color = TimerManager.getAccentColor(progress);
    progressBar.setStyle("-fx-accent: " + color + ";");
  }

  /**
   * Navigates back to the previous view.
   *
   * @param event the action event triggered by the go back button
   * @throws ApiProxyException if there is an error communicating with the API proxy
   * @throws IOException if there is an I/O error
   */
  @FXML
  private void onProceed(ActionEvent event) throws ApiProxyException, IOException {
    FXMLLoader loader = new FXMLLoader(App.class.getResource("/fxml/defendant.fxml"));
    Parent root = loader.load();
    ChatControllerCentre chatController = loader.getController();
    chatController.initialiseChatGpt("defendant.txt", "AI Defendant");
    App.getScene().setRoot(root);
  }
}
