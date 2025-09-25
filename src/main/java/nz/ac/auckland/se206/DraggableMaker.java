package nz.ac.auckland.se206;

import javafx.scene.Node;

public class DraggableMaker {

  private static double mouseX;
  private static double mouseY;

  public static void makeDraggable(Node node) {
    // Record current mouse position
    node.setOnMousePressed(
        mouseEvent -> {
          mouseX = mouseEvent.getSceneX();
          mouseY = mouseEvent.getSceneY();
        });

    node.setOnMouseDragged(
        mouseEvent -> {
          // Calculates new mouse position
          double deltaX = mouseEvent.getSceneX() - mouseX;
          double deltaY = mouseEvent.getSceneY() - mouseY;

          node.setLayoutX(node.getLayoutX() + deltaX);
          node.setLayoutY(node.getLayoutY() + deltaY);

          // Update new mouse position
          mouseX = mouseEvent.getSceneX();
          mouseY = mouseEvent.getSceneY();
        });
  }
}
