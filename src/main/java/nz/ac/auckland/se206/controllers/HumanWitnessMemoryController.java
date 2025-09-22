package nz.ac.auckland.se206.controllers;

import java.io.IOException;
import javafx.animation.FadeTransition;
import javafx.animation.PauseTransition;
import javafx.animation.RotateTransition;
import javafx.animation.TranslateTransition;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.image.Image;
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

  private int currentSongIndex = 0;
  private String[] songs = {
    "HUMBLE - Kendrick Llama",
    "Blinding Lights - The Weekday",
    "Yellow - Hotplay",
    "Rap god - Skittles",
    "Believer - Imagine Slightly Larger Lizards",
    "Old Town Road - Lil Nas Y",
    "Do I Wanna Know? - Arctic Gorillas",
    "Smoke Weed Everyday - Snoop Catt"
  };
  private Image image1 = new Image(getClass().getResourceAsStream("/images/carRadio.jpg"));
  private Image image2 = new Image(getClass().getResourceAsStream("/images/cityLights.jpg"));
  private Image image3 = new Image(getClass().getResourceAsStream("/images/catSleeping.jpg"));
  private Image image4 = new Image(getClass().getResourceAsStream("/images/coffeeMug.jpg"));
  private Image image5 = new Image(getClass().getResourceAsStream("/images/concert.jpg"));
  private Image image6 = new Image(getClass().getResourceAsStream("/images/deers.jpg"));
  private Image image7 = new Image(getClass().getResourceAsStream("/images/ocean.jpg"));
  private Image image8 = new Image(getClass().getResourceAsStream("/images/eggs.jpg"));

  private boolean isLocked = false;
  private double initialAreaX = 589;
  private double initialAreaY = 286;
  private double targetAreaX = 374;
  private double targetAreaY = 454;
  private double targetAreaSize = 200;

  @FXML private TextArea txtaChat;
  @FXML private Label timer;
  @FXML private ImageView cassetteTape;
  @FXML private VBox flashbackMessage;
  @FXML private Label songLabel;
  @FXML private ImageView musicCover;
  @FXML private Button playPreviousSongButton;
  @FXML private Button playNextSongButton;
  @FXML private Label robotTextDisplay;
  @FXML private ImageView rotatingCassetteTape;
  @FXML private ImageView rotatingCassetteTape1;

  @Override
  @FXML
  public void initialize() {
    try {
      super.initialize();
    } catch (ApiProxyException e) {
      e.printStackTrace();
    }

    robotTextDisplay.setText("     Choose Song to\r\n" + "⬇Insert beats below⬇");

    // Disable and turn visibility off the cassette tape
    cassetteTape.setVisible(false);
    cassetteTape.setDisable(true);
    rotatingCassetteTape.setVisible(false);
    rotatingCassetteTape1.setVisible(false);

    // Set initial song
    songLabel.setText(songs[currentSongIndex]);

    // Set initial cover image
    musicCover.setImage(image1);

    // Display flashback message at the beginning
    flashbackMessage.setVisible(true);
    Platform.runLater(
        () -> {
          PauseTransition pause = new PauseTransition(Duration.seconds(1));
          pause.setOnFinished(e -> flashbackMessage.setVisible(false));
          pause.play();
        });

    DraggableMaker.makeDraggable(cassetteTape);

    // Set up cassette tape settings
    setUpCassetteTape();

    cassetteTape.setOnMouseReleased(
        event -> {
          if (!isLocked) {
            checkIfCassetteInTargetArea();
          }
        });
  }

  private void setUpCassetteTape() {
    // Enable dragging
    cassetteTape.setDisable(false);

    // Resets cassetteTape positions
    cassetteTape.setLayoutX(initialAreaX);
    cassetteTape.setLayoutY(initialAreaY);

    cassetteTape.setOpacity(1);
  }

  private void checkIfCassetteInTargetArea() {
    // Get cassette tape position
    double cassetteCenterX =
        cassetteTape.getLayoutX() + cassetteTape.getBoundsInLocal().getWidth() / 2;
    double cassetteCenterY =
        cassetteTape.getLayoutY() + cassetteTape.getBoundsInLocal().getHeight() / 2;

    // Check if cassette is within target area
    if (cassetteCenterX >= targetAreaX
        && cassetteCenterX <= targetAreaX + targetAreaSize
        && cassetteCenterY >= targetAreaY
        && cassetteCenterY <= targetAreaY + targetAreaSize) {

      lockCassetteInTarget();
    }
  }

  private void lockCassetteInTarget() {
    isLocked = true;

    // Disable dragging
    cassetteTape.setDisable(true);

    // Snap to exact target position
    cassetteTape.setLayoutX(targetAreaX);
    cassetteTape.setLayoutY(targetAreaY);

    cassetteTape.setOpacity(0.8);

    // Set timer for each text
    robotTextDisplay.setText("Generating beats...");
    PauseTransition pauseText = new PauseTransition(Duration.seconds(3));
    pauseText.setOnFinished(e -> robotTextDisplay.setText("Scanning for \r\n" + "copyright....."));
    pauseText.play();
    PauseTransition pauseText2 = new PauseTransition(Duration.seconds(6));
    pauseText2.setOnFinished(
        e -> {
          // Displays last text and begins rotating casette tape
          robotTextDisplay.setText("Playing new song");
          rotateCasetteTape();
        });
    pauseText2.play();
  }

  private void rotateCasetteTape() {
    // Enable visibility of casette tapes
    rotatingCassetteTape.setVisible(true);
    rotatingCassetteTape1.setVisible(true);

    // Rotate both of the casette tapes
    RotateTransition rotationTransform = new RotateTransition();
    rotationTransform.setNode(rotatingCassetteTape);

    rotationTransform.setDuration(Duration.millis(5000));
    rotationTransform.setCycleCount(TranslateTransition.INDEFINITE);
    rotationTransform.setByAngle(360);
    rotationTransform.play();

    RotateTransition rotationTransform1 = new RotateTransition();
    rotationTransform1.setNode(rotatingCassetteTape1);

    rotationTransform1.setDuration(Duration.millis(5000));
    rotationTransform1.setCycleCount(TranslateTransition.INDEFINITE);
    rotationTransform1.setByAngle(360);
    rotationTransform1.play();
  }

  @FXML
  private void onTurnOnCassetteTape() {
    // Enable and turn visibility on the cassette tape
    cassetteTape.setVisible(true);
    cassetteTape.setDisable(false);
  }

  @FXML
  private void onGoToNextSong() {
    // Fade transitions
    FadeTransition fadeOut = new FadeTransition(Duration.millis(300), songLabel);
    fadeOut.setFromValue(1.0);
    fadeOut.setToValue(0.0);

    FadeTransition fadeIn = new FadeTransition(Duration.millis(300), songLabel);
    fadeIn.setFromValue(0.0);
    fadeIn.setToValue(1.0);

    // Play animation
    fadeOut.setOnFinished(
        e -> {
          // Update label
          currentSongIndex = (currentSongIndex + 1) % songs.length;
          setImageCover(currentSongIndex);
          songLabel.setText(songs[currentSongIndex]);
          fadeIn.play();
        });

    fadeOut.play();
  }

  @FXML
  private void onGoToPreviousSong() {
    // Fade transitions
    FadeTransition fadeOut = new FadeTransition(Duration.millis(300), songLabel);
    fadeOut.setFromValue(1.0);
    fadeOut.setToValue(0.0);

    FadeTransition fadeIn = new FadeTransition(Duration.millis(300), songLabel);
    fadeIn.setFromValue(0.0);
    fadeIn.setToValue(1.0);

    // Play animation
    fadeOut.setOnFinished(
        e -> {
          // Update label
          currentSongIndex = (currentSongIndex - 1 + songs.length) % songs.length;
          setImageCover(currentSongIndex);
          songLabel.setText(songs[currentSongIndex]);
          fadeIn.play();
        });

    fadeOut.play();
  }

  @FXML
  private void onGoBack(ActionEvent event) throws ApiProxyException, IOException {
    App.setRoot("room");
  }

  @FXML
  private void onlightUpPreviousButton() {
    playPreviousSongButton.setOpacity(1);
  }

  @FXML
  private void onlightDownPreviousButton() {
    playPreviousSongButton.setOpacity(0.7);
  }

  @FXML
  private void onlightUpNextButton() {
    playNextSongButton.setOpacity(1);
  }

  @FXML
  private void onlightDownNextButton() {
    playNextSongButton.setOpacity(0.7);
  }

  private void setImageCover(int index) {
    // Switches image cover to match song name
    if (index == 0) {
      musicCover.setImage(image1);
    } else if (index == 1) {
      musicCover.setImage(image2);
    } else if (index == 2) {
      musicCover.setImage(image3);
    } else if (index == 3) {
      musicCover.setImage(image4);
    } else if (index == 4) {
      musicCover.setImage(image5);
    } else if (index == 5) {
      musicCover.setImage(image6);
    } else if (index == 6) {
      musicCover.setImage(image7);
    } else if (index == 7) {
      musicCover.setImage(image8);
    }
  }
}
