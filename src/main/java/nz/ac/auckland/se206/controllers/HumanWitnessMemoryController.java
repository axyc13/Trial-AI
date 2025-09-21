package nz.ac.auckland.se206.controllers;

import java.io.IOException;
import javafx.animation.PauseTransition;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import javafx.util.Duration;
import nz.ac.auckland.apiproxy.exceptions.ApiProxyException;
import nz.ac.auckland.se206.App;
import nz.ac.auckland.se206.DraggableMaker;

/**
 * Controller class for the chat view. Handles user interactions and communication with the GPT
 * model via the API proxy.
 */
public class HumanWitnessMemoryController extends ChatControllerCentre {

  @FXML private TextArea txtaChat;
  @FXML private Label timer;
  @FXML private ImageView cassetteTape;
  @FXML private VBox flashbackMessage;

  @Override
  @FXML
  public void initialize() {
    try {
      super.initialize();
    } catch (ApiProxyException e) {
      e.printStackTrace();
    }

    // Disable and turn visibility off the cassette tape
    cassetteTape.setVisible(false);
    cassetteTape.setDisable(true);

    // Display flashback message at the beginning
    flashbackMessage.setVisible(true);
    Platform.runLater(
        () -> {
          PauseTransition pause = new PauseTransition(Duration.seconds(1));
          pause.setOnFinished(e -> flashbackMessage.setVisible(false));
          pause.play();
        });

    DraggableMaker.makeDraggable(cassetteTape);
  }

  @FXML
  private void turnOnCassetteTape() {
    // Enable and turn visibility on the cassette tape
    cassetteTape.setVisible(true);
    cassetteTape.setDisable(false);
  }

  @FXML
  private void onGoBack(ActionEvent event) throws ApiProxyException, IOException {
    App.setRoot("room");
  }
}
