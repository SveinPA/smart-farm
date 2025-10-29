package edu.ntnu.bidata.smg.group8.sensor.entity.actuators;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Test class for {@link ValveActuator} to ensure correct functionality
 * of valve actuator methods and behaviors.
 *
 * <p>The following is tested:</p>
 * <h3>Positive Tests:</h3>
 * <ul>
 *     <li>Constructor initializes with correct values.</li>
 *     <li>Valve state methods (isOpen, isFullyOpen, isClosed).</li>
 *     <li>Setting valve opening within valid range.</li>
 *     <li>Opening and closing the valve.</li>
 *     <li>Update method moves current value towards target value.</li>
 * </ul>
 *
 * <h3>Negative Tests:</h3>
 * <ul>
 *     <li>Setting valve opening below minimum value throws exception.</li>
 *     <li>Setting valve opening above maximum value throws exception.</li>
 * </ul>
 */
public class ValveActuatorTest {
  private ValveActuator valve;

  /**
   * Set up a new ValveActuator instance before each test.
   */
  @BeforeEach
  public void setUp() {
    valve = new ValveActuator();
  }

//________________POSITIVE TESTS____________________//

  /**
   * Test that the constructor initializes with correct values.
   *
   * <p>This should result in key "valve", unit "%", current value 50.0,
   * min value 0.0, and max value 100.0.</p>
   */
  @Test
  public void testConstructorInitializesWithCorrectValues() {
    assertEquals ("valve", valve.getKey());
    assertEquals ("%", valve.getUnit());
    assertEquals (50.0, valve.getCurrentValue());
    assertEquals(0.0, valve.getMinValue());
    assertEquals(100.0, valve.getMaxValue());
  }

  /**
   * Test that the valve is open when opening is set above 0.
   *
   * <p>Test that the valve is considered "open" when it is set
   * at any value above 0%.</p>
   */
  @Test
  public void testValveIsOpenWhenOpeningAboveZero() {
    valve.setOpening(1.0);
    assertTrue(valve.isOpen());
  }

  /**
   * Test that the valve is closed when opening is set to 0%.
   *
   * <p>This should result in the valve being considered "closed".
   * By simulating multiple updates, we ensure the current opening
   * reaches the target opening of 0%.</p>
   */
  @Test
  public void testIsClosedWhenValveIsClosed() {
    valve.close();

    // Simulate multiple updates to reach the target
    for (int i = 0; i < 20; i++) {
      valve.update();
    }
    assertTrue(valve.isClosed());
    assertEquals(0.0, valve.getCurrentValue());
  }

  /**
   * Test that the valve is fully open when set to 100%.
   *
   * <p>This should result in the valve being considered
   * "fully open". By simulating multiple updates,
   * we ensure the current opening reaches the target opening of 100%.</p>
   */
  @Test
  public void testIsFullyOpenWhenValveIsAtMax() {
    valve.open();

    // Simulate multiple updates to reach the target
    for (int i = 0; i < 20; i++) {
      valve.update();
    }
    assertTrue(valve.isFullyOpen());
    assertEquals(100.0, valve.getCurrentValue());
  }

  /**
   * Test setting the valve opening to a valid value within range.
   *
   * <p>This should result in the target opening being set
   * to the specified percentage.</p>
   */
  @Test
  public void testSetOpeningWithinValidRange() {
    valve.setOpening(60.0);
    assertEquals(60.0, valve.getTargetValue());
  }

  /**
   * Test setting the valve opening to zero closes it.
   *
   * <p>This should result in the target opening being set to 0%.</p>
   */
  @Test
  public void testSetOpeningToZeroClosesValve() {
    valve.setOpening(0.0);
    assertEquals(0.0, valve.getTargetValue());
  }

  /**
   * Test opening the valve results in target opening of 100%.
   *
   * <p>This should result in the target opening being set to 100%.</p>
   */
  @Test
  public void testOpenSetsTargetToMax() {
    valve.open();
    assertEquals(100.0, valve.getTargetValue());
  }

  /**
   * Test closing the valve results in target opening of 0%.
   *
   * <p>This should result in the target opening being set to 0%.</p>
   */
  @Test
  public void testCloseSetsTargetToMin() {
    valve.close();
    assertEquals(0.0, valve.getTargetValue());
  }

  /**
   * Test that update moves current opening towards target opening.
   *
   * <p>Each update should move by 5% of full range (5.0 units for 0-100% range).</p>
   */
  @Test
  public void testUpdateMovesCurrentOpeningTowardsTarget() {
    double initialValue = valve.getCurrentValue(); // Should be 50.0
    valve.setOpening(85.0);

    valve.update();
    double updatedValue = valve.getCurrentValue();

    // The updated value should be greater than the initial value
    assertTrue(updatedValue > initialValue);
    assertTrue(updatedValue < 85.0);

    // The updated value should have increased by 5.0 (5% of range)
    assertEquals(initialValue + 5.0, updatedValue);
  }

  /**
   * Test that update can decrease opening towards target opening.
   *
   * <p>This should result in the current opening decreasing
   * to the target opening after sufficient updates.</p>
   */
  @Test
  public void testUpdateCanDecreaseOpening() {
    // First, set valve to high opening and reach it
    valve.setOpening(80.0);
    for (int i = 0; i < 20; i++) {
      valve.update();
    }
    assertEquals(80.0, valve.getCurrentValue());

    // Now decrease to lower opening
    valve.setOpening(35.0);
    double valueBeforeDecrease = valve.getCurrentValue(); // 80.0
    valve.update();
    double valueAfterOneUpdate = valve.getCurrentValue(); // Should be 75.0

    assertTrue(valueAfterOneUpdate < valueBeforeDecrease);
    assertTrue(valueAfterOneUpdate > 35.0);
    assertEquals(75.0, valueAfterOneUpdate);
  }

  /**
   * Test that the valve does not overstep the target opening when updating.
   *
   * <p>This should result in the current opening being equal to the target
   * opening after multiple updates without exceeding it.</p>
   */
  @Test
  public void testUpdateStopsAtTargetWithoutOverstepping() {
    valve.setOpening(88.0);

    // Update multiple times (more than needed)
    for (int i = 0; i < 50; i++) {
      valve.update();
    }

    // Should be at target, not beyond
    assertEquals(88.0, valve.getCurrentValue(), 0.1);
  }


//________________NEGATIVE TESTS____________________//

  /**
   * Test setting the valve opening below minimum value
   * throws IllegalArgumentException.
   *
   * <p>This should result in an exception being thrown
   * when trying to set opening below 0%.</p>
   */
  @Test
  public void testSetOpeningBelowMinimumThrowsException() {
    double initialOpening = valve.getCurrentValue(); // Store initial state

    IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> valve.setOpening(-10.0)
    );

    // Assert exception message
    assertEquals("Percentage must be between 0 and 100.", exception.getMessage());

    // Ensure the state of the valve remains unchanged
    assertEquals(initialOpening, valve.getCurrentValue());
  }

  /**
   * Test setting the valve opening above maximum value
   * throws IllegalArgumentException.
   *
   * <p>This should result in an exception being thrown
   * when trying to set opening above 100%.</p>
   */
  @Test
  public void testSetOpeningAboveMaximumThrowsException() {
    double initialOpening = valve.getCurrentValue(); // Store initial state

    IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> valve.setOpening(101.0)
    );

    // Assert exception message
    assertEquals("Percentage must be between 0 and 100.", exception.getMessage());

    // Ensure the state of the valve remains unchanged
    assertEquals(initialOpening, valve.getCurrentValue());
  }
}
