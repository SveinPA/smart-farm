package edu.ntnu.bidata.smg.group8.sensor.entity.actuators;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Test class for {@link WindowActuator} to ensure correct functionality
 * of window actuator methods and behaviors.
 */
public class WindowActuatorTest {
  private WindowActuator window;

  /**
   * Set up a new WindowActuator instance before each test.
   */
  @BeforeEach
  public void setUp() {
    window = new WindowActuator();
  }

//________________POSITIVE TESTS____________________//

  /**
   * Test that the constructor initializes with correct values.
   *
   * <p>This should result in key "window", unit "%", current value 50.0,
   * min value 0.0, and max value 100.0.</p>
   */
  @Test
  public void testConstructorInitializesWithCorrectValues() {
    assertEquals("window", window.getKey());
    assertEquals("%", window.getUnit());
    assertEquals(50.0, window.getCurrentValue());
    assertEquals(0.0, window.getMinValue());
    assertEquals(100.0, window.getMaxValue());
  }

  /**
   * Test that the window is open when opening is set above 0.
   *
   * <p>This should result in isOpen() returning true
   * and current value being 1.0 after updates.</p>
   */
  @Test
  public void testIsOpenWhenOpeningAboveZero() {
    window.openPercentage(1.0);

    // Simulate updates to reach the target value
    for (int i = 0; i < 10; i++) {
      window.update();
    }

    assertTrue(window.isOpen());
    assertEquals(1.0, window.getCurrentValue());
  }

  /**
   * Test that the window is closed when opening is set to 0%.
   *
   * <p>This should result in isClosed() returning true
   * and current value being 0.0 after updates.</p>
   */
  @Test
  public void testIsClosedWhenWindowIsClosed() {
    window.close();

    // Simulate multiple updates to reach the target
    for (int i = 0; i < 20; i++) {
      window.update();
    }
    assertTrue(window.isClosed());
    assertEquals(0.0, window.getCurrentValue());
  }

  /**
   * Test that the window is fully open when by using
   * open() method.
   *
   * <p>This should result in isFullyOpen() returning true
   * and current value being 100.0 after updates.</p>
   */
  @Test
  public void testIsFullyOpenWhenWindowIsAtMax() {
    window.open();

    // Simulate multiple updates to reach the target
    for (int i = 0; i < 20; i++) {
      window.update();
    }
    assertTrue(window.isFullyOpen());
    assertEquals(100.0, window.getCurrentValue());
  }

  /**
   * Test setting the window opening ot a valid
   * percentage (e.g., 30%).
   *
   * <p>This should result in current value being 30.0 after updates.</p>
   */
  @Test
  public void testOpenPercentageWithValidValue() {
    window.openPercentage(30.0);

    // Simulate multiple updates to reach the target
    for (int i = 0; i < 20; i++) {
      window.update();
    }
    assertEquals(30.0, window.getCurrentValue());
  }

  /**
   * Test setting the window to zero percent opening
   * results in closed window.
   *
   * <p>This should result in current value being closed.</p>
   */
  @Test
  public void testOpenPercentageWithZeroValue() {
    window.openPercentage(0.0);

    // Simulate multiple updates to reach the target
    for (int i = 0; i < 20; i++) {
      window.update();
    }
    assertEquals(0.0, window.getCurrentValue());
    assertTrue(window.isClosed());
  }

  /**
   * Test opening the window slightly sets it to 25% open.
   *
   * <p>This should result in current value being 25.0 after updates.</p>
   */
  @Test
  public void testOpenSlightlySetsToTwentyFivePercent() {
    window.openSlightly();

    // Simulate multiple updates to reach the target
    for (int i = 0; i < 20; i++) {
      window.update();
    }
    assertEquals(25.0, window.getCurrentValue());
  }

  /**
   * Test opening the window halfway sets it to 50% open.
   *
   * <p>This should result in current value being 50.0 after updates.</p>
   */
  @Test
  public void testOpenHalfwaySetsToFiftyPercent() {
    window.openHalfway();

    // Simulate multiple updates to reach the target
     for (int i = 0; i < 20; i++) {
       window.update();
     }
     assertEquals(50.0, window.getCurrentValue());
  }

  /**
   * Test opening the window mostly sets it to 75% open.
   *
   * <p>This should result in current value being 75.0 after updates.</p>
   */
  @Test
  public void testOpenMostlySetsToSeventyFivePercent() {
    window.openMostly();

    // Simulate multiple updates to reach the target
    for (int i = 0; i < 20; i++) {
      window.update();
    }
    assertEquals(75.0, window.getCurrentValue());
  }

  /**
   * Test that update moves current opening towards target opening.
   *
   * <p>Each update should move by 5% of full range (5.0 units for 0-100% range).</p>
   */
  @Test
  public void testUpdateMovesCurrentOpeningTowardsTarget() {
    double initialValue = window.getCurrentValue(); // Should be 50.0
    window.openPercentage(80.0);

    window.update();
    double updatedValue = window.getCurrentValue();

    // The updated value should move closer to the target value
    assertTrue(updatedValue > initialValue, "Current value should increase towards the target.");
    assertTrue(updatedValue <= 80.0, "Current value should not exceed the target value.");
  }

  /**
   * Test that update can decrease opening towards target opening.
   *
   * <p>This should result in the current opening decreasing
   * to the target opening after sufficient updates.</p>
   */
  @Test
  public void testUpdateCanDecreaseOpening() {
    // Set window to high opening and reach it
    window.openPercentage(85.0);
    for (int i = 0; i < 20; i++) {
      window.update();
    }
    assertEquals(85.0, window.getCurrentValue(), 0.1);

    // Decrease to lower opening
    window.openPercentage(30.0);
    double valueBeforeDecrease = window.getCurrentValue(); // 85.0

    // Simulate multiple updates to ensure progress towards the target
    for (int i = 0; i < 10; i++) {
      window.update();
      double valueAfterUpdate = window.getCurrentValue();
      assertTrue(valueAfterUpdate <= valueBeforeDecrease, "Current value should decrease or remain the same.");
      valueBeforeDecrease = valueAfterUpdate;
    }

    // Ensure the current value does not drop below the target value
    assertTrue(window.getCurrentValue() >= 30.0, "Current value should not drop below the target value.");
  }

  /**
   * Test that the window does not overstep the target opening when updating.
   *
   * <p>This should result in the current opening being equal to the target
   * opening after multiple updates without exceeding it.</p>
   */
  @Test
  public void testUpdateStopsAtTargetWithoutOverstepping() {
    window.openPercentage(87.0);

    // Update multiple times (more than needed)
    for (int i = 0; i < 50; i++) {
      window.update();
    }

    // Should be at target, not beyond
    assertEquals(87.0, window.getCurrentValue(), 0.1);
  }

//________________NEGATIVE TESTS____________________//

  /**
   * Test setting the window opening above maximum value
   * throws IllegalArgumentException.
   *
   * <p>This should result in an exception being thrown
   * when trying to set opening above 100%.</p>
   */
  @Test
  public void testOpenPercentageAboveMaximumThrowsException() {
    double initialOpening = window.getCurrentValue(); // Store initial state

    IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> window.openPercentage(150.0)
    );

    // Assert exception message
    assertEquals("Percentage must be between 0 and 100.", exception.getMessage());

    // Ensure the state of the window remains unchanged
    assertEquals(initialOpening, window.getCurrentValue());
  }

  /**
   * Test setting the window opening below minimum value
   * throws IllegalArgumentException.
   *
   * <p>This should result in an exception being thrown
   * when trying to set opening below 0%.</p>
   */
  @Test
  public void testOpenPercentageBelowMinimumThrowsException() {
    double initialOpening = window.getCurrentValue(); // Store initial state

    IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> window.openPercentage(-10.0)
    );

    // Assert exception message
    assertEquals("Percentage must be between 0 and 100.", exception.getMessage());

    // Ensure the state of the window remains unchanged
    assertEquals(initialOpening, window.getCurrentValue());
  }
}
