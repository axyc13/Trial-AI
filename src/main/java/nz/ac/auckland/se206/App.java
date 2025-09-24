package nz.ac.auckland.se206;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Map;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.input.MouseEvent;
import javafx.stage.Stage;
import nz.ac.auckland.se206.controllers.ChatControllerCentre;

/**
 * This is the entry point of the JavaFX application. This class initializes and runs the JavaFX
 * application.
 */
public class App extends Application {
  private static Scene scene;
  private static final Map<String, String> flashbackFxmlMap =
      Map.of(
          "AI Defendant", "/fxml/defendantMemory.fxml",
          "AI Witness", "/fxml/AIWitnessFlashback.fxml",
          "Human Witness", "/fxml/humanWitness.fxml");
  private static final Map<String, String> chatFxmlMap =
      Map.of(
          "AI Defendant", "/fxml/defendant.fxml",
          "AI Witness", "/fxml/aiWitness.fxml",
          "Human Witness", "/fxml/humanWitnessMemory.fxml");
  private static final Map<String, String> trialTxtMap =
      Map.of(
          "AI Defendant", "defendant.txt",
          "AI Witness", "aiWitness.txt",
          "Human Witness", "humanWitness.txt");
  private static ArrayList<String> professionsOpened = new ArrayList<>();
  private static Stage primaryStage;

  /**
   * The main method that launches the JavaFX application.
   *
   * @param args the command line arguments
   */
  public static void main(final String[] args) {
    launch();
  }

  public static Scene getScene() {
    return scene;
  }

  /**
   * Sets the root of the scene to the specified FXML file.
   *
   * @param fxml the name of the FXML file (without extension)
   * @throws IOException if the FXML file is not found
   */
  public static void setRoot(String fxml) throws IOException {
    scene.setRoot(loadFxml(fxml));
  }

  /**
   * Loads the FXML file and returns the associated node. The method expects that the file is
   * located in "src/main/resources/fxml".
   *
   * @param fxml the name of the FXML file (without extension)
   * @return the root node of the FXML file
   * @throws IOException if the FXML file is not found
   */
  private static Parent loadFxml(final String fxml) throws IOException {
    return new FXMLLoader(App.class.getResource("/fxml/" + fxml + ".fxml")).load();
  }

  /**
   * Opens a flashback or chat view depending on now many times the profession has been opened.
   *
   * @param event the mouse event that triggered the method
   * @param profession the profession to set in the chat controller
   * @throws IOException if the FXML file is not found
   */
  public static void openChat(MouseEvent event, String profession) throws IOException {
    Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();

    // First time talking - load flashback
    if (!professionsOpened.contains(profession)) {
      professionsOpened.add(profession);
      FXMLLoader loader = new FXMLLoader(App.class.getResource(flashbackFxmlMap.get(profession)));
      Parent root = loader.load();
      scene = new Scene(root);
      stage.setScene(scene);
      stage.show();
    } else {
      // Second time talking - load chat rooms
      FXMLLoader loader;
      Parent root;
      loader = new FXMLLoader(App.class.getResource(chatFxmlMap.get(profession)));
      root = loader.load();
      ChatControllerCentre chatController = loader.getController();
      chatController.initialiseChatGpt(trialTxtMap.get(profession), profession);
      scene = new Scene(root);
      stage.setScene(scene);
      stage.show();
    }
  }

  public static ArrayList<String> getProfessionsOpened() {
    return professionsOpened;
  }

  public static void openFinalPage() throws IOException {
    Parent root = loadFxml("finalPage");
    scene = new Scene(root);
    primaryStage.setScene(scene);
    primaryStage.show();
  }

  /**
   * This method is invoked when the application starts. It loads and shows the "room" scene.
   *
   * @param stage the primary stage of the application
   * @throws IOException if the "src/main/resources/fxml/room.fxml" file is not found
   */
  @Override
  public void start(final Stage stage) throws IOException {
    primaryStage = stage;
    Parent root = loadFxml("room");
    scene = new Scene(root);
    stage.setScene(scene);
    stage.show();
    root.requestFocus();
  }
}
