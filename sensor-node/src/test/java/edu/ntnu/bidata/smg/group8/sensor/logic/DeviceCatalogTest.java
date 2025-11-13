package edu.ntnu.bidata.smg.group8.sensor.logic;

import edu.ntnu.bidata.smg.group8.common.actuator.Actuator;
import edu.ntnu.bidata.smg.group8.common.sensor.Sensor;
import edu.ntnu.bidata.smg.group8.sensor.entity.actuators.FanActuator;
import edu.ntnu.bidata.smg.group8.sensor.entity.actuators.HeaterActuator;
import edu.ntnu.bidata.smg.group8.sensor.entity.sensors.HumiditySensor;
import edu.ntnu.bidata.smg.group8.sensor.entity.sensors.TemperatureSensor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Collection;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * <h3>Unit Tests for DeviceCatalog</h3>
 *
 * <p>This class contains unit tests for the DeviceCatalog class, specifically
 * testing the management of sensors and actuators within a sensor node.</p>
 *
 * <p>The tests cover both positive scenarios, ensuring correct functionality
 * for adding, retrieving, and managing devices, as well as negative scenarios
 * that validate error handling and defensive programming practices.</p>
 *
 * <p>We test the following cases:</p>
 * <ul>
 *  <li><b>Positive Tests:</b>
 *   <ul>
 *    <li>Catalog initialization and empty state</li>
 *    <li>Adding and retrieving single and multiple sensors</li>
 *    <li>Adding and retrieving single and multiple actuators</li>
 *    <li>Bulk operations (getting all sensor readings)</li>
 *    <li>Mixed device management (sensors and actuators together)</li>
 *    <li>Utility methods (summary, isEmpty)</li>
 *   </ul>
 *  </li>
 *
 *  <li><b>Negative Tests:</b>
 *   <ul>
 *    <li>Adding null sensors and actuators</li>
 *    <li>Retrieving non-existent devices</li>
 *    <li>Verifying defensive copies (unmodifiable collections)</li>
 *   </ul>
 *  </li>
 * </ul>
 *
 * @author Ida Soldal
 * @version 29.10.2025
 */
class DeviceCatalogTest {

    private DeviceCatalog catalog; // Field to hold the DeviceCatalog instance for testing

    /**
     * Sets up test fixture before each test.
     *
     * <p>Creates a fresh, empty DeviceCatalog instance to ensure test isolation.</p>
     */
    @BeforeEach
    void setUp() {
        catalog = new DeviceCatalog();
    }

    // -------------- POSITIVE TESTS --------------

    /**
     * Tests that a newly created catalog is empty.
     *
     * <p>Verifies that:</p>
     * <ul>
     *   <li>The catalog object is created successfully</li>
     *   <li>No sensors are present initially</li>
     *   <li>No actuators are present initially</li>
     *   <li>isEmpty() returns true</li>
     * </ul>
     */
    @Test
    void testCatalogInitiallyEmpty() {
        assertNotNull(catalog);
        assertEquals(0, catalog.getSensorCount());
        assertEquals(0, catalog.getActuatorCount());
        assertTrue(catalog.isEmpty());
    }

    /**
     * Tests adding a single sensor to the catalog.
     *
     * <p>Verifies that after adding a sensor:</p>
     * <ul>
     *   <li>Sensor count increases to 1</li>
     *   <li>The sensor can be retrieved by its key</li>
     *   <li>The catalog is no longer empty</li>
     * </ul>
     */
    @Test
    void testAddSingleSensor() {
        Sensor tempSensor = new TemperatureSensor();
        catalog.addSensor(tempSensor);

        assertEquals(1, catalog.getSensorCount());
        assertNotNull(catalog.getSensor("temp"));
        assertFalse(catalog.isEmpty());
    }

    /**
     * Tests adding multiple sensors to the catalog.
     *
     * <p>Verifies that:</p>
     * <ul>
     *   <li>Multiple sensors can be added</li>
     *   <li>Each sensor can be retrieved individually by key</li>
     *   <li>Sensor count reflects the correct number</li>
     * </ul>
     */
    @Test
    void testAddMultipleSensors() {
        catalog.addSensor(new TemperatureSensor());
        catalog.addSensor(new HumiditySensor());

        assertEquals(2, catalog.getSensorCount());
        assertNotNull(catalog.getSensor("temp"));
        assertNotNull(catalog.getSensor("hum"));
    }

    /**
     * Tests getting all sensor readings at once.
     *
     * <p>Verifies that:</p>
     * <ul>
     *   <li>getAllSensorReadings() returns a map with all sensors</li>
     *   <li>Each sensor's key is present in the map</li>
     *   <li>Each reading is a valid double value</li>
     * </ul>
     */
    @Test
    void testGetAllSensorReadings() {
        catalog.addSensor(new TemperatureSensor());
        catalog.addSensor(new HumiditySensor());

        Map<String, Double> readings = catalog.getAllSensorReadings();

        assertEquals(2, readings.size());
        assertTrue(readings.containsKey("temp"));
        assertTrue(readings.containsKey("hum"));
        assertNotNull(readings.get("temp"));
        assertNotNull(readings.get("hum"));
    }

    /**
     * Tests getting all sensor readings when catalog is empty.
     *
     * <p>Verifies that the method returns an empty map rather than null
     * or throwing an exception, ensuring robust error handling.</p>
     */
    @Test
    void testGetAllSensorReadingsWhenEmpty() {
        Map<String, Double> readings = catalog.getAllSensorReadings();

        assertNotNull(readings);
        assertTrue(readings.isEmpty());
    }

    /**
     * Tests getting all sensors as a collection.
     *
     * <p>Verifies that getAllSensors() returns all added sensors and that
     * the returned collection size matches the sensor count.</p>
     */
    @Test
    void testGetAllSensors() {
        catalog.addSensor(new TemperatureSensor());
        catalog.addSensor(new HumiditySensor());

        Collection<Sensor> sensors = catalog.getAllSensors();

        assertEquals(2, sensors.size());
    }

    /**
     * Tests adding a single actuator to the catalog.
     *
     * <p>Verifies that after adding an actuator:</p>
     * <ul>
     *   <li>Actuator count increases to 1</li>
     *   <li>The actuator can be retrieved by its key</li>
     *   <li>The catalog is no longer empty</li>
     * </ul>
     */
    @Test
    void testAddSingleActuator() {
        Actuator heater = new HeaterActuator();
        catalog.addActuator(heater);

        assertEquals(1, catalog.getActuatorCount());
        assertNotNull(catalog.getActuator("heater"));
        assertFalse(catalog.isEmpty());
    }

    /**
     * Tests adding multiple actuators to the catalog.
     *
     * <p>Verifies that:</p>
     * <ul>
     *   <li>Multiple actuators can be added</li>
     *   <li>Each actuator can be retrieved individually by key</li>
     *   <li>Actuator count reflects the correct number</li>
     * </ul>
     */
    @Test
    void testAddMultipleActuators() {
        catalog.addActuator(new HeaterActuator());
        catalog.addActuator(new FanActuator());

        assertEquals(2, catalog.getActuatorCount());
        assertNotNull(catalog.getActuator("heater"));
        assertNotNull(catalog.getActuator("fan"));
    }

    /**
     * Tests getting all actuators as a collection.
     *
     * <p>Verifies that getAllActuators() returns all added actuators and that
     * the returned collection size matches the actuator count.</p>
     */
    @Test
    void testGetAllActuators() {
        catalog.addActuator(new HeaterActuator());
        catalog.addActuator(new FanActuator());

        Collection<Actuator> actuators = catalog.getAllActuators();

        assertEquals(2, actuators.size());
    }

    /**
     * Tests adding both sensors and actuators to the catalog.
     *
     * <p>Verifies that:</p>
     * <ul>
     *   <li>Sensors and actuators can coexist in the same catalog</li>
     *   <li>Each type maintains its own count</li>
     *   <li>Devices don't interfere with each other</li>
     *   <li>The catalog correctly reports not empty</li>
     * </ul>
     */
    @Test
    void testMixedDevices() {
        catalog.addSensor(new TemperatureSensor());
        catalog.addSensor(new HumiditySensor());
        catalog.addActuator(new HeaterActuator());
        catalog.addActuator(new FanActuator());

        assertEquals(2, catalog.getSensorCount());
        assertEquals(2, catalog.getActuatorCount());
        assertFalse(catalog.isEmpty());
    }

    /**
     * Tests the summary() method output.
     *
     * <p>Verifies that the summary contains information about both
     * sensors and actuators in a readable format, including counts
     * and device keys.</p>
     */
    @Test
    void testSummary() {
        catalog.addSensor(new TemperatureSensor());
        catalog.addActuator(new HeaterActuator());

        String summary = catalog.summary();

        assertNotNull(summary);
        assertTrue(summary.contains("sensors=1"));
        assertTrue(summary.contains("actuators=1"));
        assertTrue(summary.contains("temp"));
        assertTrue(summary.contains("heater"));
    }

    /**
     * Tests the summary() method when catalog is empty.
     *
     * <p>Verifies that summary works correctly even with no devices,
     * showing zero counts.</p>
     */
    @Test
    void testSummaryWhenEmpty() {
        String summary = catalog.summary();

        assertNotNull(summary);
        assertTrue(summary.contains("sensors=0"));
        assertTrue(summary.contains("actuators=0"));
    }

    /**
     * Tests the isEmpty() method in various states.
     *
     * <p>Verifies that isEmpty() correctly reports the catalog state:</p>
     * <ul>
     *   <li>True when empty</li>
     *   <li>False when sensors are present</li>
     *   <li>False when actuators are present</li>
     * </ul>
     */
    @Test
    void testCatalogIsEmpty() {
        // Initially empty
        assertTrue(catalog.isEmpty());

        // Add sensor - no longer empty
        catalog.addSensor(new TemperatureSensor());
        assertFalse(catalog.isEmpty());

        // Create new catalog and add only actuator
        DeviceCatalog catalog2 = new DeviceCatalog();
        catalog2.addActuator(new HeaterActuator());
        assertFalse(catalog2.isEmpty());
    }

    // -------------- NEGATIVE TESTS --------------

    /**
     * Tests that adding a null sensor throws IllegalArgumentException.
     *
     * <p>This ensures defensive programming - the catalog should reject
     * invalid inputs rather than storing null values that could cause
     * NullPointerExceptions later.</p>
     */
    @Test
    void testAddNullSensorThrowsException() {
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> catalog.addSensor(null)
        );
        
        assertEquals("Sensor cannot be null", exception.getMessage());
    }

    /**
     * Tests retrieving a sensor that doesn't exist.
     *
     * <p>Verifies that getSensor() returns null for non-existent sensors
     * rather than throwing an exception, allowing for graceful handling
     * of missing devices.</p>
     */
    @Test
    void testGetNonExistentSensor() {
        assertNull(catalog.getSensor("nonexistent"));
    }

    /**
     * Tests that adding a null actuator throws IllegalArgumentException.
     *
     * <p>This ensures defensive programming - the catalog should reject
     * invalid inputs rather than storing null values that could cause
     * NullPointerExceptions later.</p>
     */
    @Test
    void testAddNullActuatorThrowsException() {
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> catalog.addActuator(null)
        );
        
        assertEquals("Actuator cannot be null", exception.getMessage());
    }

    /**
     * Tests retrieving an actuator that doesn't exist.
     *
     * <p>Verifies that getActuator() returns null for non-existent actuators
     * rather than throwing an exception, allowing for graceful handling
     * of missing devices.</p>
     */
    @Test
    void testGetNonExistentActuator() {
        assertNull(catalog.getActuator("nonexistent"));
    }

    /**
     * Tests that getAllSensors() returns an unmodifiable collection.
     *
     * <p>Verifies defensive copying: the returned collection should not
     * allow modifications that would affect the internal catalog state.
     * This prevents external code from breaking catalog invariants.</p>
     */
    @Test
    void testGetAllSensorsReturnsUnmodifiableCollection() {
        catalog.addSensor(new TemperatureSensor());
        catalog.addSensor(new HumiditySensor());

        Collection<Sensor> sensors = catalog.getAllSensors();

        // Attempting to modify should throw UnsupportedOperationException
        assertThrows(UnsupportedOperationException.class, () -> {
            sensors.clear();
        });
    }

    /**
     * Tests that getAllActuators() returns an unmodifiable collection.
     *
     * <p>Verifies defensive copying: the returned collection should not
     * allow modifications that would affect the internal catalog state.
     * This prevents external code from breaking catalog invariants.</p>
     */
    @Test
    void testGetAllActuatorsReturnsUnmodifiableCollection() {
        catalog.addActuator(new HeaterActuator());
        catalog.addActuator(new FanActuator());

        Collection<Actuator> actuators = catalog.getAllActuators();

        // Attempting to modify should throw UnsupportedOperationException
        assertThrows(UnsupportedOperationException.class, () -> {
            actuators.clear();
        });
    }
}