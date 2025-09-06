package nz.ac.auckland.se206.controllers;

import java.io.IOException;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.Slider;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
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
  @FXML private Slider slidingBar;
  @FXML private ImageView image1;
  @FXML private ImageView image2;

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
  }

  private void showDialogue(int value) {
    if (value == 0) {
      scene1.setVisible(false);
      scene2.setVisible(false);
      scene3.setVisible(false);
    }
    if (value == 1) {
      image1.setVisible(true);
      scene1.setVisible(true);
    } else if (value == 2) {
      image1.setVisible(true);
      scene2.setVisible(true);
    } else if (value == 3) {
      image1.setVisible(true);
      scene3.setVisible(true);
    } else if (value == 4) {
      scene1.setVisible(false);
      scene2.setVisible(false);
      scene3.setVisible(false);
      image1.setVisible(false);
      scene4.setVisible(false);
    } else if (value == 5) {
      scene4.setVisible(true);
      scene5.setVisible(false);
    } else if (value == 6) {
      scene4.setVisible(false);
      scene5.setVisible(true);
      scene6.setVisible(false);
    } else if (value == 7) {
      scene5.setVisible(false);
      scene6.setVisible(true);
    } else if (value == 8) {
      scene6.setVisible(false);
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
  private void onGoBack(ActionEvent event) throws ApiProxyException, IOException {
    App.setRoot("room");
  }
}
