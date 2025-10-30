package edu.ntnu.bidata.smg.group8.sensor.logic;

import edu.ntnu.bidata.smg.group8.common.sensor.Sensor;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * <h3>Unit Tests for NodeAgent</h3>
 *
 * <p>This class contains unit tests for the NodeAgent class, specifically
 * testing the behavior of network communication methods and connection handling.</p>
 *
 * <p>The tests cover both positive scenarios, ensuring correct initialization
 * and state management, as well as negative scenarios that validate graceful
 * handling of disconnected states and error conditions.</p>
 *
 * <p>We test the following cases:</p>
 * <ul>
 *  <li><b>Positive Tests:</b>
 *   <ul>
 *    <li>Successful NodeAgent creation</li>
 *    <li>Correct initial state (not connected)</li>
 *   </ul>
 *  </li>
 *
 *  <li><b>Negative Tests:</b>
 *   <ul>
 *    <li>Sending sensor data when not connected</li>
 *    <li>Sending heartbeat when not connected</li>
 *    <li>Disconnecting when not connected</li>
 *   </ul>
 *  </li>
 * </ul>
 *
 * <p><b>Note:</b> These are unit tests that verify NodeAgent's behavior in isolation.
 * Full integration testing with a real broker is done with {@link SensorNodeMain}.</p>
 *
 * @author Ida Soldal
 * @version 29.10.2025
 */
class NodeAgentTest {

    private NodeAgent agent; // Field for the NodeAgent instance used in tests

    /**
     * Sets up test fixture before each test.
     *
     * <p>Creates a NodeAgent instance configured to connect to localhost:8080.
     * This agent is not connected at this point, allowing tests to verify
     * behavior in disconnected state.</p>
     */
    @BeforeEach
    void setUp() {
        agent = new NodeAgent("test-node-1", "localhost", 8080);
    }

    /**
     * Cleans up after each test.
     *
     * <p>Ensures the agent is properly disconnected if a test somehow
     * established a connection. This prevents resource leaks between tests.</p>
     */
    @AfterEach
    void tearDown() {
        if (agent.isConnected()) {
            agent.disconnect();
        }
    }

    // -------------- POSITIVE TESTS --------------

    /**
     * Tests that NodeAgent can be created successfully.
     *
     * <p>Verifies that:</p>
     * <ul>
     *   <li>The NodeAgent object is not null after construction</li>
     *   <li>The agent is not connected initially</li>
     * </ul>
     *
     * <p>This ensures proper initialization without establishing a connection.</p>
     */
    @Test
    void testNodeAgentCreation() {
        assertNotNull(agent);
        assertFalse(agent.isConnected(), "Should not be connected initially");
    }

    /**
     * Tests that isConnected() returns false when not connected.
     *
     * <p>Verifies that the connection state is correctly reported as false
     * when no connection has been established to the broker.</p>
     */
    @Test
    void testIsConnectedWhenNotConnected() {
        assertFalse(agent.isConnected());
    }

    // -------------- NEGATIVE TESTS (Disconnected State) --------------

    /**
     * Tests that sendSensorData() handles disconnected state gracefully.
     *
     * <p>Verifies that calling sendSensorData() when not connected to the broker
     * does not throw an exception. The method should log an error internally
     * but handle the situation gracefully without crashing.</p>
     *
     * <p>This test uses a simple anonymous Sensor implementation to provide
     * test data without requiring a full sensor object.</p>
     */
    @Test
    void testSendSensorDataWhenNotConnected() {
        // Create a simple test sensor
        Sensor testSensor = new Sensor() {
            @Override
            public String getKey() { return "temp"; }
            @Override
            public String getUnit() { return "°C"; }
            @Override
            public double getReading() { return 22.5; }
        };

        // Should not throw exception, just log error
        assertDoesNotThrow(() -> agent.sendSensorData(testSensor));
    }

    /**
     * Tests that sendHeartbeat() handles disconnected state gracefully.
     *
     * <p>Verifies that calling sendHeartbeat() when not connected to the broker
     * does not throw an exception. The method should log an error internally
     * but handle the situation gracefully without crashing.</p>
     *
     * <p>This is important because heartbeats may be attempted automatically
     * by timers even if the connection is temporarily lost.</p>
     */
    @Test
    void testSendHeartbeatWhenNotConnected() {
        // Should not throw exception, just log error
        assertDoesNotThrow(() -> agent.sendHeartbeat());
    }

    /**
     * Tests that disconnect() can be called safely when not connected.
     *
     * <p>Verifies that calling disconnect() when no connection exists
     * handles the situation gracefully without throwing exceptions.</p>
     *
     * <p>This ensures robust cleanup behavior even if disconnect() is called
     * multiple times or when no connection was ever established.</p>
     */
    @Test
    void testDisconnectWhenNotConnected() {
        // Should handle gracefully
        assertDoesNotThrow(() -> agent.disconnect());
    }
}