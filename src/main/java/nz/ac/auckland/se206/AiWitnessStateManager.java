package nz.ac.auckland.se206;

import java.util.ArrayList;
import java.util.List;

/**
 * Manages the state of the AI Witness scene. This singleton class helps maintain the UI state when
 * navigating away and back to the AI Witness.
 */
public class AiWitnessStateManager {
  private static AiWitnessStateManager instance;

  // Scene state
  private double sliderValue = 0;
  private final List<Integer> disposedBubbles = new ArrayList<>();
  private boolean hasClickedClearNoise = false;
  private boolean hasShownAllBubbles = false;
  private int currentFlashbackState = 1;
  private boolean isEndLabelVisible = false;

  private AiWitnessStateManager() {
    // Private constructor for singleton
  }

  /**
   * Gets the singleton instance of the state manager.
   *
   * @return the singleton instance
   */
  public static AiWitnessStateManager getInstance() {
    if (instance == null) {
      instance = new AiWitnessStateManager();
    }
    return instance;
  }

  /**
   * Gets the current value of the slider.
   *
   * @return the slider value
   */
  public double getSliderValue() {
    return sliderValue;
  }

  /**
   * Sets the value of the slider.
   *
   * @param value the new slider value
   */
  public void setSliderValue(double value) {
    this.sliderValue = value;
  }

  /**
   * Gets the list of bubble numbers that have been disposed.
   *
   * @return list of disposed bubble numbers
   */
  public List<Integer> getDisposedBubbles() {
    return disposedBubbles;
  }

  /**
   * Adds a bubble to the list of disposed bubbles.
   *
   * @param bubbleNumber the number of the bubble that was disposed
   */
  public void addDisposedBubble(int bubbleNumber) {
    if (!disposedBubbles.contains(bubbleNumber)) {
      disposedBubbles.add(bubbleNumber);
    }
  }

  /**
   * Checks if the clear noise button has been clicked.
   *
   * @return true if the button has been clicked
   */
  public boolean hasClickedClearNoise() {
    return hasClickedClearNoise;
  }

  /**
   * Sets whether the clear noise button has been clicked.
   *
   * @param clicked whether the button has been clicked
   */
  public void setClearNoiseClicked(boolean clicked) {
    this.hasClickedClearNoise = clicked;
  }

  /**
   * Checks if all bubbles have been shown.
   *
   * @return true if all bubbles have been shown
   */
  public boolean hasShownAllBubbles() {
    return hasShownAllBubbles;
  }

  /**
   * Sets whether all bubbles have been shown.
   *
   * @param shown whether all bubbles have been shown
   */
  public void setShownAllBubbles(boolean shown) {
    this.hasShownAllBubbles = shown;
  }

  /**
   * Gets the number of bubbles in the bin.
   *
   * @return number of bubbles in the bin
   */
  public int getBubblesInBin() {
    return disposedBubbles.size();
  }

  /**
   * Gets the current flashback state.
   *
   * @return the current flashback state (1: first image, 2: second image, 3: third image)
   */
  public int getFlashbackState() {
    return currentFlashbackState;
  }

  /**
   * Sets the current flashback state.
   *
   * @param state the new flashback state
   */
  public void setFlashbackState(int state) {
    this.currentFlashbackState = state;
  }

  /**
   * Gets whether the end label is visible.
   *
   * @return true if the end label is visible
   */
  public boolean isEndLabelVisible() {
    return isEndLabelVisible;
  }

  /**
   * Sets whether the end label is visible.
   *
   * @param visible whether the end label should be visible
   */
  public void setEndLabelVisible(boolean visible) {
    this.isEndLabelVisible = visible;
  }

  /** Resets the state to initial values. Call this when starting a new game. */
  public void resetState() {
    sliderValue = 0;
    disposedBubbles.clear();
    hasClickedClearNoise = false;
    hasShownAllBubbles = false;
    currentFlashbackState = 1;
    isEndLabelVisible = false;
  }
}
