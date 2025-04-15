package programmingtheiot.part02.integration.connection;

import static org.junit.Assert.*;

import java.util.logging.Logger;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.Date;

import programmingtheiot.gda.connection.RedisPersistenceAdapter;

import programmingtheiot.data.ActuatorData;
import programmingtheiot.data.SensorData;

import programmingtheiot.common.ConfigConst;

/**
 * This test case class contains very basic integration tests for
 * RedisPersistenceAdapter. It should not be considered complete,
 * but serve as a starting point for the student implementing
 * additional functionality within their Programming the IoT
 * environment.
 *
 */
public class RedisClientAdapterTest {
	// static

	private static final Logger _Logger = Logger.getLogger(PersistenceClientAdapterTest.class.getName());

	// member var's

	private RedisPersistenceAdapter rpa = null;

	// test setup methods

	/**
	 * @throws java.lang.Exception
	 */
	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
	}

	/**
	 * @throws java.lang.Exception
	 */
	@AfterClass
	public static void tearDownAfterClass() throws Exception {
	}

	/**
	 * @throws java.lang.Exception
	 */
	@Before
	public void setUp() throws Exception {
		this.rpa = new RedisPersistenceAdapter();
		_Logger.info("Running RedisPersistenceAdapter test cases...");
	}

	/**
	 * @throws java.lang.Exception
	 */
	@After
	public void tearDown() throws Exception {
	}

	// test methods

	/**
	 * Test method for
	 * {@link programmingtheiot.gda.connection.RedisPersistenceAdapter#connectClient()}.
	 */
	@Test
	public void testConnectClient() {
		_Logger.info("Checking RedisPersistenceAdapter is able to connect...");
		assertTrue(this.rpa.connectClient());
	}

	/**
	 * Test method for
	 * {@link programmingtheiot.gda.connection.RedisPersistenceAdapter#disconnectClient()}.
	 */
	@Test
	public void testDisconnectClient() {
		_Logger.info("Checking RedisPersistenceAdapter is able to disconnect...");
		this.rpa.connectClient(); // connect first
		assertTrue(this.rpa.disconnectClient());
	}

	/**
	 * Test method for
	 * {@link programmingtheiot.gda.connection.RedisPersistenceAdapter#getActuatorData(java.lang.String, java.util.Date, java.util.Date)}.
	 */
	@Test
	public void testGetActuatorData() {
		_Logger.info("Checking RedisPersistenceAdapter is able to get actuator data...");
		this.rpa.connectClient(); // connect first
		assertNotNull(this.rpa.getActuatorData("test", new Date(0), new Date()));
	}

	/**
	 * Test method for
	 * {@link programmingtheiot.gda.connection.RedisPersistenceAdapter#getSensorData(java.lang.String, java.util.Date, java.util.Date)}.
	 */
	@Test
	public void testGetSensorData() {
		_Logger.info("Checking RedisPersistenceAdapter is able to get sensor data...");
		this.rpa.connectClient(); // connect first
		assertNotNull(this.rpa.getSensorData("test", new Date(0), new Date()));
	}

	/**
	 * Test method for
	 * {@link programmingtheiot.gda.connection.RedisPersistenceAdapter#storeData(java.lang.String, int, programmingtheiot.data.ActuatorData[])}.
	 */
	@Test
	public void testStoreDataStringIntActuatorDataArray() {
		_Logger.info("Checking RedisPersistenceAdapter is able to store actuator data...");
		this.rpa.connectClient(); // connect first
		assertTrue(this.rpa.storeData("test", 1, new ActuatorData[] { new ActuatorData() }));
		// We also check that the default was stored
		ActuatorData[] data = this.rpa.getActuatorData("test", new Date(0), new Date());
		assertEquals(ConfigConst.DEFAULT_VAL, data[0].getValue(), 0.0001);
	}

	/**
	 * Test method for
	 * {@link programmingtheiot.gda.connection.RedisPersistenceAdapter#storeData(java.lang.String, int, programmingtheiot.data.SensorData[])}.
	 */
	@Test
	public void testStoreDataStringIntSensorDataArray() {
		_Logger.info("Checking RedisPersistenceAdapter is able to store sensor data...");
		this.rpa.connectClient(); // connect first
		assertTrue(this.rpa.storeData("test", 1, new SensorData[] { new SensorData() }));
		// We also check that the default was stored
		SensorData[] data = this.rpa.getSensorData("test", new Date(0), new Date());
		assertEquals(ConfigConst.DEFAULT_VAL, data[0].getValue(), 0.0001);
	}

	/**
	 * Test method for
	 * {@link programmingtheiot.gda.connection.RedisPersistenceAdapter#storeData(java.lang.String, int, programmingtheiot.data.SystemPerformanceData[])}.
	 */
	@Test
	public void testStoreDataStringIntSystemPerformanceDataArray() {
		_Logger.info("Checking RedisPersistenceAdapter is able to store system performance data...");
		this.rpa.connectClient(); // connect first
		assertTrue(this.rpa.storeData("test", 1, new SensorData[] { new SensorData() }));
		// We also check that the default was stored
		SensorData[] data = this.rpa.getSensorData("test", new Date(0), new Date());
		assertEquals(ConfigConst.DEFAULT_VAL, data[0].getValue(), 0.0001);
	}

}
