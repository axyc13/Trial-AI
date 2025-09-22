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
import nz.ac.auckland.se206.controllers.RoomController;

/**
 * This is the entry point of the JavaFX application. This class initializes and runs the JavaFX
 * application.
 */
public class App extends Application {
  private static Scene scene;
  private static final Map<String, String> fxmlMap =
      Map.of(
          "AI Defendant", "/fxml/defendantMemory.fxml",
          "AI Witness", "/fxml/aiWitness.fxml",
          "Human Witness", "/fxml/humanWitness.fxml");
  private static final Map<String, String> trialTxtMap =
      Map.of(
          "AI Defendant", "defendant.txt",
          "AI Witness", "aiWitness.txt",
          "Human Witness", "humanWitness.txt");
  private static final Map<String, String> flashBackTxtMap =
      Map.of(
          "AI Defendant", "defendantFlashback.txt",
          "AI Witness", "aiWitnessFlashback.txt",
          "Human Witness", "humanWitnessFlashback.txt");
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

    // First time talking
    if (!professionsOpened.contains(profession)) {
      professionsOpened.add(
          profession); // Add the profession immediately to track that it's been clicked
      if (profession.equals("AI Witness")) {
        // First click - show flashback
        FXMLLoader loader = new FXMLLoader(App.class.getResource("/fxml/AIWitnessFlashback.fxml"));
        Parent root = loader.load();
        scene = new Scene(root);
        stage.setScene(scene);
        stage.show();
      } else if (profession.equals("AI Defendant")) {
        FXMLLoader loader = new FXMLLoader(App.class.getResource(fxmlMap.get(profession)));
        Parent root = loader.load();
        scene = new Scene(root);
        stage.setScene(scene);
        stage.show();
      } else if (profession.equals("Human Witness")) {
        FXMLLoader loader = new FXMLLoader(App.class.getResource("/fxml/humanWitness.fxml"));
        Parent root = loader.load();
        scene = new Scene(root);
        stage.setScene(scene);
        stage.show();
      } else {
        FXMLLoader loader = new FXMLLoader(App.class.getResource(fxmlMap.get(profession)));
        Parent root = loader.load();
        ChatControllerCentre chatController = loader.getController();
        chatController.initialiseChatGpt(flashBackTxtMap.get(profession), profession);
        scene = new Scene(root);
        stage.setScene(scene);
        stage.show();
      }
    } else {
      // Second time talking - always load main scene
      FXMLLoader loader;
      Parent root;
      if (profession.equals("AI Witness")) {
        loader = new FXMLLoader(App.class.getResource(fxmlMap.get(profession)));
        root = loader.load();
        ChatControllerCentre chatController = loader.getController();
        chatController.initialiseChatGpt(trialTxtMap.get(profession), profession);
      } else if (profession.equals("AI Defendant")) {
        loader = new FXMLLoader(App.class.getResource("/fxml/defendant.fxml"));
        root = loader.load();
      } else if (profession.equals("Human Witness")) {
        // Return to room with human witness
        loader = new FXMLLoader(App.class.getResource("/fxml/humanWitnessMemory.fxml"));
        root = loader.load();
        ChatControllerCentre chatController = loader.getController();
        chatController.initialiseChatGpt(trialTxtMap.get(profession), profession);
      } else {
        loader = new FXMLLoader(App.class.getResource("/fxml/room.fxml"));
        root = loader.load();
        RoomController controller = loader.getController();
        controller.initialiseChatGpt(trialTxtMap.get(profession), profession);
        controller.showOverlay();
      }
      scene = new Scene(root);
      stage.setScene(scene);
      stage.show();
    }
  }

  public static void openFinalPage() throws IOException {
    // Uncomment this to force all professions to be opened before accessing final page
    // if (professionsOpened.size() != 3) {
    //   System.out.println("Not all professions have been opened yet.");
    //   return;
    // }
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
