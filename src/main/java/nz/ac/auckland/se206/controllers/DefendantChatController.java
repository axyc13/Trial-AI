package nz.ac.auckland.se206.controllers;

import java.io.File;
import java.io.IOException;
import javafx.animation.PauseTransition;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.VBox;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.util.Duration;
import nz.ac.auckland.apiproxy.exceptions.ApiProxyException;
import nz.ac.auckland.se206.App;
import nz.ac.auckland.se206.DraggableMaker;

/**
 * Controller class for the chat view. Handles user interactions and communication with the GPT
 * model via the API proxy.
 */
public class DefendantChatController extends ChatControllerCentre {

  @FXML private TextArea txtaChat;
  @FXML private Label timer;
  @FXML private ImageView headphones;
  @FXML private VBox flashbackMessage;
  private MediaPlayer mediaPlayer;
  private int count = 0;

  @Override
  @FXML
  public void initialize() {
    try {
      super.initialize();
    } catch (ApiProxyException e) {
      e.printStackTrace();
    }
    flashbackMessage.setVisible(true);
    Platform.runLater(
        () -> {
          PauseTransition pause = new PauseTransition(Duration.seconds(1));
          pause.setOnFinished(e -> flashbackMessage.setVisible(false));
          String audioFile = "src/main/resources/sounds/flashback.mp3";

          Media sound = new Media(new File(audioFile).toURI().toString());
          MediaPlayer mediaPlayer = new MediaPlayer(sound);

          mediaPlayer.play();
          pause.play();
        });
    DraggableMaker.makeDraggable(headphones);
  }

  @FXML
  public void onFlashbackButtonPress(MouseEvent event) {
    if (count % 2 == 0) {
      String audioFile = "src/main/resources/sounds/defendantSfx.mp3";

      Media sound = new Media(new File(audioFile).toURI().toString());
      this.mediaPlayer = new MediaPlayer(sound);

      this.mediaPlayer.play();
    } else {
      this.mediaPlayer.stop();
    }
    count++;
  }

  @FXML
  private void onGoBack(ActionEvent event) throws ApiProxyException, IOException {
    if (this.mediaPlayer != null) {
      this.mediaPlayer.stop();
    }
    App.setRoot("room");
  }
}
