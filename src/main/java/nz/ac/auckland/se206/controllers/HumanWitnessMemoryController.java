package nz.ac.auckland.se206.controllers;

import java.io.IOException;
import javafx.animation.KeyFrame;
import javafx.animation.PauseTransition;
import javafx.animation.RotateTransition;
import javafx.animation.Timeline;
import javafx.animation.TranslateTransition;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.ToggleButton;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.util.Duration;
import nz.ac.auckland.apiproxy.exceptions.ApiProxyException;
import nz.ac.auckland.se206.App;
import nz.ac.auckland.se206.DraggableMaker;

/**
 * Controller class for the chat view. Handles user interactions and communication with the GPT
 * model via the API proxy.
 */
public class HumanWitnessMemoryController extends ChatControllerCentre {

  private final int rows = 3;
  private final int cols = 5;
  private final double BEAT_DURATION = 500;
  private final Rectangle[][] cells = new Rectangle[rows][cols];
  private final boolean[][] pattern = new boolean[rows][cols];

  private final boolean[][] correctPattern = {
    {true, false, false, true, true},
    {false, false, false, false, true},
    {true, true, true, true, false}
  };

  private boolean isPatternCorrect = false;
  private boolean isLocked = false;
  private double initialAreaX = 415;
  private double initialAreaY = 614;
  private double targetAreaX = 864;
  private double targetAreaY = 643;
  private double targetAreaSize = 200;
  private Timeline movingBarTimeline;
  private MediaPlayer[] soundPlayers = new MediaPlayer[rows];
  private Image mute = new Image(getClass().getResourceAsStream("/images/mute.jpg"));
  private Image unmute = new Image(getClass().getResourceAsStream("/images/unmute.jpg"));

  @FXML private Rectangle movingBar;
  @FXML private GridPane beatGrid;
  @FXML private TextArea txtaChat;
  @FXML private Label timer;
  @FXML private ImageView cassetteTape;
  @FXML private VBox flashbackMessage;
  @FXML private Label robotTextDisplay;
  @FXML private ImageView rotatingCassetteTape;
  @FXML private ImageView rotatingCassetteTape1;
  @FXML private Label instructionLabel;
  @FXML private ToggleButton soundButton;
  @FXML private ImageView muteCover;
  @FXML private ImageView arrowHint1;
  @FXML private ImageView arrowHint2;
  @FXML private ImageView arrowHint3;
  @FXML private Label gameInstruction;

  @Override
  @FXML
  public void initialize() {
    try {
      super.initialize();
    } catch (ApiProxyException e) {
      e.printStackTrace();
    }

    createBeatGrid();
    setupMovingBar();
    setupSounds();

    // Enable arrow hints
    setUpHints();

    robotTextDisplay.setText("Complete the \r\n" + "pattern first!");

    // Disable and turn visibility off the cassette tape
    cassetteTape.setVisible(false);
    cassetteTape.setDisable(true);
    rotatingCassetteTape.setVisible(false);
    rotatingCassetteTape1.setVisible(false);

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

  private void setUpHints() {
    // Show the hints
    arrowHint1.setVisible(true);
    gameInstruction.setVisible(true);
    arrowHint2.setVisible(false);
    arrowHint3.setVisible(false);

    // Set up the hovering animation
    hoveringArrowAnimation(arrowHint1);
    hoveringArrowAnimation(arrowHint2);
    hoveringArrowAnimation(arrowHint3);
  }

  private void setupSounds() {

    Media sound1 = new Media(getClass().getResource("/sounds/sound1.mp3").toString());
    Media sound2 = new Media(getClass().getResource("/sounds/sound2.mp3").toString());
    Media sound3 = new Media(getClass().getResource("/sounds/sound3.mp3").toString());

    soundPlayers[0] = new MediaPlayer(sound1);
    soundPlayers[1] = new MediaPlayer(sound2);
    soundPlayers[2] = new MediaPlayer(sound3);

    // Setting mute cover and volume
    muteCover.setImage(unmute);
    soundPlayers[0].setVolume(0.1);
    soundPlayers[1].setVolume(0.1);
    soundPlayers[2].setVolume(0.1);
  }

  private void setupMovingBar() {
    movingBar.setVisible(true);

    // Shifting the moving bar
    movingBarTimeline =
        new Timeline(
            new KeyFrame(Duration.ZERO, e -> moveBar(0)),
            new KeyFrame(Duration.millis(BEAT_DURATION * 0.5), e -> moveBar(0.5)),
            new KeyFrame(Duration.millis(BEAT_DURATION), e -> moveBar(1)),
            new KeyFrame(Duration.millis(BEAT_DURATION * 1.5), e -> moveBar(1.5)),
            new KeyFrame(Duration.millis(BEAT_DURATION * 2), e -> moveBar(2)),
            new KeyFrame(Duration.millis(BEAT_DURATION * 2.5), e -> moveBar(2.5)),
            new KeyFrame(Duration.millis(BEAT_DURATION * 3), e -> moveBar(3)),
            new KeyFrame(Duration.millis(BEAT_DURATION * 3.5), e -> moveBar(3.5)),
            new KeyFrame(Duration.millis(BEAT_DURATION * 4), e -> moveBar(4)),
            new KeyFrame(Duration.millis(BEAT_DURATION * 4.5), e -> moveBar(4.5)),
            new KeyFrame(Duration.millis(BEAT_DURATION * 5), e -> moveBar(0)));

    movingBarTimeline.setCycleCount(Timeline.INDEFINITE);
    movingBarTimeline.play();
  }

  private void moveBar(double beat) {
    double cellWidth = beatGrid.getPrefWidth() / cols;
    movingBar.setTranslateX(beat * cellWidth);

    int currentColumn = (int) beat;
    if (beat % 1 == 0) {
      playSoundsForColumn(currentColumn);
    }
  }

  private void playSoundsForColumn(int column) {
    // Sound will play for clicked rectangle in column
    for (int row = 0; row < rows; row++) {
      if (pattern[row][column]) {
        playSound(row);

        // pulses rectangle
        Rectangle rectangle = cells[row][column];
        if (rectangle != null) {
          pulsingRectangle(rectangle);
        }
      }
    }
  }

  private void pulsingRectangle(Rectangle rectangle) {

    // Plays a pulsing rectangle
    javafx.animation.ScaleTransition rectanglePulse =
        new javafx.animation.ScaleTransition(Duration.millis(50), rectangle);

    rectanglePulse.setToX(1.1);
    rectanglePulse.setToY(1.1);
    rectanglePulse.setAutoReverse(true);
    rectanglePulse.setCycleCount(2);
    rectanglePulse.play();
  }

  private void playSound(int row) {
    soundPlayers[row].stop();
    soundPlayers[row].seek(Duration.ZERO);
    soundPlayers[row].play();
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
    arrowHint2.setVisible(false);

    txtaChat.appendText(
        "[Human Witness]: Don't worry about copyright the defendant always checks"
            + " before playing, even when it comes to creating music. \n\n");
    isLocked = true;

    // Disable dragging
    cassetteTape.setDisable(true);

    // Snap to exact target position
    cassetteTape.setLayoutX(targetAreaX);
    cassetteTape.setLayoutY(targetAreaY);

    cassetteTape.setOpacity(0.8);

    rotateCasetteTape();
    robotTextDisplay.setText("SONG CREATED!\r\n" + "YOU WIN!");

    instructionLabel.setText("SONG CREATED! YOU WIN!");
  }

  private void createBeatGrid() {
    // Get size of cell
    double cellWidth = beatGrid.getPrefWidth() / cols;
    double cellHeight = beatGrid.getPrefHeight() / rows;

    // Set up rectangles
    for (int currentRow = 0; currentRow < rows; currentRow++) {
      for (int currentColumn = 0; currentColumn < cols; currentColumn++) {
        // Create new rectangle
        Rectangle rectangle = new Rectangle(cellWidth - 2, cellHeight - 2, Color.WHITE);
        rectangle.setArcWidth(1);
        rectangle.setArcHeight(1);
        rectangle.setStroke(Color.BLACK);
        rectangle.setStrokeWidth(2);

        final int row = currentRow;
        final int col = currentColumn;

        // Check if rectangle is active
        rectangle.setOnMouseClicked(
            e -> {
              if (!isPatternCorrect) {
                boolean isActiveCell = pattern[row][col];
                pattern[row][col] = !isActiveCell;
                if (!isActiveCell) {
                  rectangle.setFill(getRowColor(row));
                } else {
                  rectangle.setFill(Color.WHITE);
                }
                checkPattern();
              }
            });

        cells[currentRow][currentColumn] = rectangle;
        beatGrid.add(rectangle, currentColumn, currentRow);
      }
    }
  }

  private Color getRowColor(int row) {
    // Sets colour of rectangle based off row
    switch (row) {
      case 0:
        return Color.web("#fd0000ff");
      case 1:
        return Color.web("#001affff");
      case 2:
        return Color.web("#04ff00ff");
      default:
        return Color.LIMEGREEN;
    }
  }

  private void checkPattern() {
    // Checks for the winning pattern
    for (int row = 0; row < rows; row++) {
      for (int col = 0; col < cols; col++) {
        if (pattern[row][col] != correctPattern[row][col]) {
          isPatternCorrect = false;
          return;
        }
      }
    }
    isPatternCorrect = true;
    onPatternCorrect();
  }

  private void onPatternCorrect() {
    movingBarTimeline.stop();
    instructionLabel.setText("Drag cassette tape onto the Defendant");
    robotTextDisplay.setText("Drag cassette \r\n" + "tape HERE");
    txtaChat.appendText("[Human Witness]: Great choice of beats! \n\n");
    arrowHint3.setVisible(true);
    onTurnOnCassetteTape();
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
  private void onGoBack(ActionEvent event) throws ApiProxyException, IOException {
    movingBarTimeline.stop();
    App.setRoot("room");
  }

  @FXML
  private void onMuteOrUnmute(ActionEvent event) {
    // Toggles cover and volume
    if (soundButton.isSelected()) {
      soundPlayers[0].setVolume(0);
      soundPlayers[1].setVolume(0);
      soundPlayers[2].setVolume(0);
      muteCover.setImage(mute);
    } else {
      soundPlayers[0].setVolume(0.1);
      soundPlayers[1].setVolume(0.1);
      soundPlayers[2].setVolume(0.1);
      muteCover.setImage(unmute);
    }
  }

  @FXML
  private void onFirstMouseClick() {
    arrowHint1.setVisible(false);
    gameInstruction.setVisible(false);
  }

  @FXML
  private void onCassetteClick() {
    arrowHint2.setVisible(true);
    arrowHint3.setVisible(false);
  }

  private void hoveringArrowAnimation(ImageView arrow) {
    // floating animation
    TranslateTransition floatTransition = new TranslateTransition(Duration.millis(1500), arrow);
    floatTransition.setFromY(0);
    floatTransition.setToY(-15);
    floatTransition.setAutoReverse(true);
    floatTransition.setCycleCount(TranslateTransition.INDEFINITE);

    floatTransition.play();
  }
}
