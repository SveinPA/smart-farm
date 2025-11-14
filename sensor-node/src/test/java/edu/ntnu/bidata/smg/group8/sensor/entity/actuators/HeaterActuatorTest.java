package edu.ntnu.bidata.smg.group8.sensor.entity.actuators;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Test class for {@link HeaterActuator} to verify its functionality
 * of heater actuator methods and behaviors.
 *
 * <p>The following is tested:</p>
 * <h3>Positive Tests:</h3>
 * <ul>
 *     <li>Constructor initializes with correct values.</li>
 *     <li>Heater state methods (isOn, isOff, isAtMaxTemperature).</li>
 *     <li>Setting target temperature within valid range.</li>
 *     <li>Turning off the heater sets target to minimum.</li>
 *     <li>Update method moves current value towards target value.</li>
 * </ul>
 * <h3>Negative Tests:</h3>
 * <ul>
 *     <li>Setting target temperature below minimum value throws exception.</li>
 *     <li>Setting target temperature above maximum value throws exception.</li>
 * </ul>
 *
 * @author Mona Amundsen
 * @version 26.10.25
 */
public class HeaterActuatorTest {
  private HeaterActuator heater;

  /**
   * Set up a new HeaterActuator instance before each test.
   */
  @BeforeEach
  public void setUp() {
    heater = new HeaterActuator();
  }

//________________POSITIVE TESTS____________________//

  /**
   * Test that the constructor initializes with correct values.
   *
   * <p>This should result in key "heater", unit "°C", current value 25.0,
   * * min value 0.0, and max value 40.0.</p>
   */
  @Test
  public void testConstructorInitializesWithCorrectValues() {
    assertEquals ("heater", heater.getKey());
    assertEquals ("°C", heater.getUnit());
    assertEquals(25.0, heater.getTargetValue());
    assertEquals(0.0, heater.getMinValue());
    assertEquals(40.0, heater.getMaxValue());
  }

  /**
   * Test that the heater is on when temperatures is set above minimum.
   *
   * <p>Test that heater is considered "on" when current temperature is above 0°C.
   * (any temperature value above 0 should turn the heater on).</p>
   */
  @Test
  public void testHeaterIsOnWhenAboveZeroDegrees() {
    heater.setTargetTemperature(1.0);
    assertTrue(heater.isOn());
  }

  /**
   * Test that the heater is off when temperature is at minimum.
   *
   * <p>Test that heater is
   * considered "off" when current temperature is at 0°C.</p>
   */
  @Test
  public void testHeaterIsOffAtZeroDegrees() {
    heater.setTargetTemperature(0.0);

    // Simulate multiple updates to reach the target
    for (int i = 0; i < 10; i++) {
      heater.update();
    }
    assertTrue(heater.isOff());
  }

  /**
   * Test that turning off the heater sets target temperature to minimum.
   *
   * <p>This should result in the target temperature being set to 0°C
   * when the heater is turned off.</p>
   */
  @Test
  public void testTurnOffSetsTargetToMinimum() {
    heater.turnOff();

    // Simulate multiple updates to reach the target
    for (int i = 0; i < 10; i++) {
      heater.update();
    }

    assertTrue(heater.isOff());
    assertEquals(0.0, heater.getTargetValue());
  }

/**
 * Test that the heater is at maximum temperature when set to 40°C.
 *
 * <p>This should result in the heater being considered
 * "at maximum temperature". By simulating multiple updates,
 * we ensure the current value reaches the target value of 40°C.</p>
 */
  @Test
  public void testHeaterIsAtMaxTemperatureAtFortyDegrees() {
    heater.setTargetTemperature(40.0);

    // Simulate multiple updates to reach the target
    for (int i = 0; i < 20; i++) {
      heater.update();
    }

    assertTrue(heater.isAtMaxTemperature());
    assertEquals(40.0, heater.getTargetValue());
  }

  /**
   * Test setting the heater temperature to a valid value within range.
   *
   * <p>This should result in the target temperature being set
   * to the specified temperature.</p>
   */
  @Test
  public void testSetTargetTemperatureWithinValidRange() {
    heater.setTargetTemperature(30.5);
    assertEquals(30.5, heater.getTargetValue());
    assertEquals(30.5, heater.getTargetTemperature());
  }

  /**
   * Test setting the heater temperature to minimum value
   * (0°C) turns it off.
   *
   * <p>When the heater is set to 0°C, it should be considered "off".</p>
   */
  @Test
  public void testSetTargetTemperatureToMinimumTurnsHeaterOff() {
    heater.setTargetTemperature(0.0);

    // Simulate multiple updates to reach the target
    for (int i = 0; i < 10; i++) {
      heater.update();
    }
    assertEquals(0.0, heater.getTargetValue());
    assertTrue(heater.isOff());
  }

  /**
   * Test that update gradually changes current value towards target value.
   *
   * <p>This should result in the current value moving closer to the target
   * value with each update call.</p>
   */
  @Test
  public void testUpdateMovesCurrentTemperatureTowardsTarget() {
    double initialValue = heater.getCurrentValue(); // Should be 25.0
    heater.setTargetTemperature(35.0);

    heater.update();
    double updatedValue = heater.getCurrentValue();

    // The updated value should move closer to the target value
    assertTrue(updatedValue > initialValue, "Current value should increase towards the target.");
    assertTrue(updatedValue <= 35.0, "Current value should not exceed the target value.");
  }

  /**
   * Test that update eventually reaches the target temperature after multiple calls.
   *
   * <p>This should result in the current temperature being equal to the target
   * temperature after sufficient updates.</p>
   */
  @Test
  public void testUpdateEventuallyReachesTargetTemperature() {
    heater.setTargetTemperature(38.0);

    // Simulate multiple updates to reach the target
    for (int i = 0; i < 20; i++) {
      heater.update();
    }
    assertEquals(38.0, heater.getCurrentValue(), 0.1);
  }

  /**
   * Test that update can decrease temperature towards target temperature.
   *
   * <p>This should result in the current temperature decreasing to the target
   * temperature after sufficient updates.</p>
   */
  @Test
  public void testUpdateCanDecreaseTemperature() {
    // Set heater to high temperature and reach it
    heater.setTargetTemperature(35.0);
    for (int i = 0; i < 20; i++) {
      heater.update();
    }
    assertEquals(35.0, heater.getCurrentValue(), 0.1);

    // Decrease to lower temperature
    heater.setTargetTemperature(15.0);
    double valueBeforeDecrease = heater.getCurrentValue(); // 35.0

    // Simulate multiple updates to ensure progress towards the target
    for (int i = 0; i < 10; i++) {
      heater.update();
      double valueAfterUpdate = heater.getCurrentValue();
      assertTrue(valueAfterUpdate <= valueBeforeDecrease, "Current value should decrease or remain the same.");
      valueBeforeDecrease = valueAfterUpdate;
    }

    // Ensure the current value does not drop below the target value
    assertTrue(heater.getCurrentValue() >= 15.0, "Current value should not drop below the target value.");
  }

  /**
   * Test that the heater does not overstep the target temperature when updating.
   *
   * <p>This should result in the current temperature being equal to the target
   * temperature after multiple updates without exceeding it.</p>
   */
  @Test
  public void testUpdateStopsAtTargetWithoutOverstepping() {
    heater.setTargetTemperature(28.0);

    // Update multiple times (more than needed)
    for (int i = 0; i < 50; i++) {
      heater.update();
    }

    // Should be at target, not beyond
    assertEquals(28.0, heater.getCurrentValue(), 0.1);
  }

//________________NEGATIVE TESTS____________________//

  /**
   * Test setting the heater temperature below minimum value
   * (0°C) throws IllegalArgumentException.
   *
   * <p>An IllegalArgumentException should be thrown
   * when attempting to set the target temperature below 0°C.</p>
   */
  @Test
  public void testSetTemperatureBelowMinimumThrowsException() {
    double initialTargetTemperature = heater.getTargetTemperature(); // Store initial state

    IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> heater.setTargetTemperature(-5.0)
    );

    // Assert exception message
    assertEquals("Temperature out of range", exception.getMessage());

    // Ensure the state of the heater remains unchanged
    assertEquals(initialTargetTemperature, heater.getTargetTemperature());
  }

  /**
   * Test setting the heater temperature above maximum value
   * (40°C) throws IllegalArgumentException.
   *
   * <p> An IllegalArgumentException should be thrown
   * when attempting to set the target temperature above 40°C.</p>
   */
  @Test
  public void testSetTemperatureAboveMaximumThrowsException() {
    double initialTargetTemperature = heater.getTargetTemperature(); // Store initial state

    IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> heater.setTargetTemperature(45.0)
    );

    // Assert exception message
    assertEquals("Temperature out of range", exception.getMessage());

    // Ensure the state of the heater remains unchanged
    assertEquals(initialTargetTemperature, heater.getTargetTemperature());
  }
}
