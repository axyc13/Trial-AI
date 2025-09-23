package nz.ac.auckland.se206.controllers;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import javafx.animation.AnimationTimer;
import javafx.animation.PauseTransition;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.VBox;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.text.Text;
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
  @FXML private AnchorPane disc1;
  @FXML private AnchorPane disc2;
  @FXML private AnchorPane disc3;
  @FXML private AnchorPane disc4;
  @FXML private AnchorPane disc5;
  @FXML private ImageView basket;
  @FXML private Button gameButton;
  @FXML private Button replayButton;
  @FXML private Text message;
  @FXML private AnchorPane messageBox;
  @FXML private AnchorPane instructions;

  @FXML private VBox flashbackMessage;
  private MediaPlayer mediaPlayer;
  private List<AnchorPane> discs;
  private AnimationTimer gameLoop;
  private int discIndex = 0;
  private int score = 0;

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
    DraggableMaker.makeDraggable(basket);
    basket
        .layoutXProperty()
        .addListener(
            (obs, oldX, newX) -> {
              double minX = 300;
              double maxX = 680;
              if (newX.doubleValue() < minX) {
                basket.setLayoutX(minX);
              } else if (newX.doubleValue() > maxX) {
                basket.setLayoutX(maxX);
              }
            });
    double basketY = basket.getLayoutY();

    basket
        .layoutYProperty()
        .addListener(
            (obs, oldY, newY) -> {
              if (!newY.equals(basketY)) {
                basket.setLayoutY(basketY);
              }
            });
    discs = Arrays.asList(disc1, disc4, disc3, disc2, disc5);
  }

  private void dropDisc(AnchorPane disc) {
    double minX = 310;
    double maxX = 650;
    double x = minX + Math.random() * (maxX - minX);

    disc.setLayoutY(70);
    disc.setLayoutX(x);
    disc.setVisible(true);
  }

  private void sendNextDisc() {
    discIndex++;
    if (discIndex < discs.size()) {
      dropDisc(discs.get(discIndex));
    } else {
      basket.setVisible(false);
      message.setText("Game Over! You made the correct judgement on " + score + "/5 songs");
      messageBox.setVisible(true);
      Platform.runLater(
          () -> {
            PauseTransition pause = new PauseTransition(Duration.seconds(3));
            pause.setOnFinished(e -> messageBox.setVisible(false));
            pause.play();
          });
      gameLoop.stop();
      replayButton.setVisible(true);
      return;
    }
  }

  private void startGame() {
    gameLoop =
        new AnimationTimer() {
          @Override
          public void handle(long now) {
            AnchorPane disc = discs.get(discIndex);

            if (disc.isVisible()) {
              disc.setLayoutY(disc.getLayoutY() + 2);

              if (disc.getBoundsInParent().intersects(basket.getBoundsInParent())) {
                // Disc caught
                if (discIndex == 0 || discIndex == 3) {
                  score += 1;
                }
                disc.setVisible(false);
                sendNextDisc();
              }

              if (disc.getLayoutY() > 530) {
                // Disc missed
                if (discIndex != 0 || discIndex != 3) {
                  score += 1;
                }
                disc.setVisible(false);
                sendNextDisc();
              }
            }
          }
        };
    gameLoop.start();
  }

  @FXML
  private void onGameStart(ActionEvent event) {

    gameButton.setVisible(false);
    replayButton.setVisible(false);
    instructions.setVisible(true);

    PauseTransition pause = new PauseTransition(Duration.seconds(2));
    pause.setOnFinished(
        e -> {
          instructions.setVisible(false);
          basket.setVisible(true);
          // Start the game
          startGame();
          discIndex = 0;
          score = 0;
          dropDisc(discs.get(discIndex));
        });
    pause.play();
  }

  @FXML
  private void onGoBack(ActionEvent event) throws ApiProxyException, IOException {
    App.setRoot("room");
  }
}
