package edu.ntnu.bidata.smg.group8.sensor.entity.actuators;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Test class for {@link FanActuator} to ensure correct functionality
 * of fan actuator methods and behaviors.
 *
 * <p>The following is tested:</p>
 * <h3>Positive Tests:</h3>
 * <ul>
 *     <li>Constructor initializes with correct values.</li>
 *     <li>Fan state methods (isOn, isOff, isAtFullSpeed).</li>
 *     <li>Setting fan speed within valid range.</li>
 *     <li>Turning fan on to full, low, medium, and high speeds.</li>
 *     <li>Update method moves current value towards target value.</li>
 * </ul>
 * <h3>Negative Tests:</h3>
 * <ul>
 *     <li>Setting fan speed below minimum value throws exception.</li>
 *     <li>Setting fan speed above maximum value throws exception.</li>
 * </ul>
 *
 * @author Mona Amundsen
 * @version 26.10.25
 */
public class FanActuatorTest {
  private FanActuator fan;

  /**
   * Set up a new FanActuator instance before each test.
   */
  @BeforeEach
  public void setUp() {
    fan = new FanActuator();
  }

//________________POSITIVE TESTS____________________//
  /**
   * Test that the constructor initializes with correct values.
   *
   * <p>This should result in key "fan", unit "%", current value 50.0,
   * min value 0.0, and max value 100.0.</p>
   */
  @Test
  public void testConstructorInitializesWithCorrectValues() {
    assertEquals ("fan", fan.getKey());
    assertEquals ("%", fan.getUnit());
    assertEquals (50.0, fan.getCurrentValue());
    assertEquals(0.0, fan.getMinValue());
    assertEquals(100.0, fan.getMaxValue());
  }

  /**
   * Test that the fan is on when speed is set above 0.
   *
   * <p>Test that the fan is considered "on" when speed is set to 1.0
   * (any setSpeed value above 0 should turn the fan on).</p>
   */
  @Test
  public void testIsOnWhenFanIsRunning() {
    fan.setSpeed(1.0);
    assertTrue(fan.isOn());
  }

  /**
   * Test that the fan is off when speed is set to 0.
   *
   * <p>This should result in the fan is considered "off".
   * By simulating multiple updates, we ensure the current value
   * reaches the target value of 0.</p>
   */
  @Test
  public void testIsOffWhenFanIsStopped() {
    fan.turnOff();

    // Simulate multiple updates to reach the target
    for (int i = 0; i < 10; i++) {
      fan.update();
    }
    assertTrue(fan.isOff());
  }

  /**
   * Test that the fan is at full speed when set to 100%.
   *
   * <p>This should result in the fan being considered
   * "at full speed". By simulating multiple updates,
   * we ensure the current value reaches the target value of 100.</p>
   */
  @Test
  public void testIsAtFullSpeedWhenFanIsAtMax() {
    fan.turnOnFull();
    // Simulate multiple updates to reach the target
    for (int i = 0; i < 20; i++) {
      fan.update();
    }
    assertTrue(fan.isAtFullSpeed());
  }

  /**
   * Test setting the fan speed to a valid value within range.
   *
   * <p>This should result in the target value being set
   * to the specified speed.</p>
   */
  @Test
  public void testSetSpeedWithinValidSpeed() {
    fan.setSpeed(70.0);
    assertEquals(70.0, fan.getTargetValue());
  }

  /**
   * Test setting the fan speed to zero turns it off.
   *
   * <p>This should result in the target value being set to 0%.</p>
   */
  @Test
  public void testSetSpeedToZeroEqualsTunedOff() {
    fan.setSpeed(0.0);
    assertEquals(0.0, fan.getTargetValue());
  }

  /**
   * Test turning the fan on to full speed results
   * in target value of 100%.
   *
   * <p>This should result in the target value being set to 100%.</p>
   */
  @Test
  public void testTurnOnFullSetsTargetToMax() {
    fan.turnOnFull();
    assertEquals(100.0, fan.getTargetValue());
  }

  /**
   *Test that tuning the fan to low speed sets target to 25%.
   *
   * <p>This should result in the target value being set to 25%.</p>
   */
  @Test
  public void testTurnOnLowSetsTargetTo25() {
    fan.turnOnLow();
    assertEquals(25.0, fan.getTargetValue());
  }

  /**
   * Test that tuning the fan to medium speed sets target to 50%.
   *
   * <p>This should result in the target value being set to 50%.</p>
   */
  @Test
  public void testTurnOnMediumSetsTargetTo50() {
    fan.turnOnMedium();
    assertEquals(50.0, fan.getTargetValue());
  }

  /**
   * Test that tuning the fan to high speed sets target to 75%.
   *
   * <p>This should result in the target value being set to 75%.</p>
   */
  @Test
  public void testTurnOnHighSetsTargetTo75() {
    fan.turnOnHigh();
    assertEquals(75.0, fan.getTargetValue());
  }


  /**
   * Test that update moves current value towards target value.
   */
  @Test
  public void testUpdateMovesCurrentValueTowardsTarget() {
    double initialValue = fan.getCurrentValue();
    fan.setSpeed(80.0);

    fan.update();
    double updatedValue = fan.getCurrentValue();

    // The updated value should move closer to the target value
    assertTrue(updatedValue > initialValue, "Current value should increase towards the target.");
    assertTrue(updatedValue <= 80.0, "Current value should not exceed the target value.");
  }

  /**
   * Test that update eventually reaches the target value after multiple calls.
   *
   * <p>This should result in the current value being equal to the target value
   * after sufficient updates.</p>
   */
  @Test
  public void testUpdateEventuallyReachesTargetValue() {
    fan.setSpeed(95.0);

    // Simulate multiple updates to reach the target
    for (int i = 0; i < 20; i++) {
      fan.update();
    }
    assertEquals(95.0, fan.getCurrentValue(), 0.1);
  }

  /**
   * Test that update can decrease speed towards target value.
   *
   * <p>This should result in the current value decreasing towards
   * the target value when the target is lower than
   * the current value.</p>
   */
  @Test
  public void testUpdateCanDecreaseSpeed() {
    // Set fan to high speed and reach it
    fan.setSpeed(80.0);
    for (int i = 0; i < 20; i++) {
      fan.update();
    }
    assertEquals(80.0, fan.getCurrentValue(), 0.1);

    // Decrease to lower speed
    fan.setSpeed(30.0);
    double valueBeforeDecrease = fan.getCurrentValue(); // 80.0

    // Simulate multiple updates to ensure progress towards the target
    for (int i = 0; i < 10; i++) {
      fan.update();
      double valueAfterUpdate = fan.getCurrentValue();
      assertTrue(valueAfterUpdate <= valueBeforeDecrease, "Current value should decrease or remain the same.");
      valueBeforeDecrease = valueAfterUpdate;
    }

    // Ensure the current value does not drop below the target value
    assertTrue(fan.getCurrentValue() >= 30.0, "Current value should not drop below the target value.");
  }

  /**
   * Test that the fan does not overstep the target value when updating.
   *
   * <p>This should result in the current value being equal to the target value
   * after multiple updates without exceeding it.</p>
   */
  @Test
  public void testUpdateStopsAtTargetWithoutOverstepping() {
    fan.setSpeed(90.0);

    // Update multiple times
    for (int i = 0; i < 50; i++) {
      fan.update();
    }

    //Should be at max now, set to lower speed
    assertEquals(90.0, fan.getCurrentValue(), 0.1);
  }

//________________NEGATIVE TESTS____________________//
  /**
   * Test setting the fan speed below minimum value
   * throws IllegalArgumentException.
   *
   * <p>This should result in an exception being thrown
   * when trying to set speed below 0%.</p>
   */
  @Test
  public void testSetSpeedBelowMinimumThrowsException() {
    assertThrows(IllegalArgumentException.class, () -> {
        fan.setSpeed(-10.0);
    });
  }

  /**
   * Test setting the fan speed above maximum value
   * throws IllegalArgumentException.
   *
   * <p>This should result in an exception being thrown
   * when trying to set speed above 100%.</p>
   */
  @Test
  public void testSetSpeedAboveMaximumThrowsException() {
    assertThrows(IllegalArgumentException.class, () -> {
        fan.setSpeed(150.0);
    });
  }
}
