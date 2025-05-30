/**
 * This class is part of the Programming the Internet of Things project.
 * 
 * It is provided as a simple shell to guide the student and assist with
 * implementation for the Programming the Internet of Things exercises,
 * and designed to be modified by the student as needed.
 */

package programmingtheiot.gda.connection;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import programmingtheiot.common.ConfigConst;
import programmingtheiot.common.ConfigUtil;
import programmingtheiot.common.ResourceNameEnum;
import programmingtheiot.data.ActuatorData;
import programmingtheiot.data.DataUtil;
import programmingtheiot.data.SensorData;
import programmingtheiot.data.SystemPerformanceData;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPubSub;
import redis.clients.jedis.exceptions.JedisConnectionException;

/**
 * Shell representation of class for student implementation.
* 
*/
public class RedisPersistenceAdapter implements IPersistenceClient {
	// static

	private static final Logger _Logger = Logger.getLogger(RedisPersistenceAdapter.class.getName());

	// private var's

	// Redis client instance
	private JedisPool jedisPool;


	// Configuration properties
	private String host;
	private int port;
	private ConfigUtil configUtil;

	// constructors

	/**
	 * Default.
	* 
	*/
	public RedisPersistenceAdapter() {
		super();

		initConfig();
		this.jedisPool = null;
		_Logger.info("RedisPersistenceAdapter initialized with host: " + this.host + ", port: " + this.port);
	}

	// public methods

	// public methods

	/**
	 *
	*/
	@Override
	public boolean connectClient() {
		if (this.jedisPool != null) {
			_Logger.warning("JedisPool is already initialized.");
			return true;
		}
		try {
			this.jedisPool = new JedisPool(this.host, this.port);
			_Logger.info("Initialized JedisPool and connected to Redis.");
			return true;
		} catch (JedisConnectionException e) {
			_Logger.log(Level.SEVERE, "Failed to initialize JedisPool.", e);
			this.jedisPool = null;
			return false;
		}
	}

	/**
	 *
	*/
	@Override
	public boolean disconnectClient() {
		if (this.jedisPool == null) {
			_Logger.warning("JedisPool is already null.");
			return true;
		}
		try {
			this.jedisPool.close();
			this.jedisPool = null;
			_Logger.info("Closed JedisPool and disconnected from Redis.");
			return true;
		} catch (Exception e) {
			_Logger.log(Level.SEVERE, "Error closing JedisPool:", e);
			return false;
		}
	}

	/**
	 *
	*/
	@Override
	public ActuatorData[] getActuatorData(String topic, Date startDate, Date endDate) {
		try (Jedis jedis = jedisPool.getResource()) {
				_Logger.log(Level.INFO, "Getting actuator data from Redis under topic: {0}", topic);
			List<ActuatorData> actuatorDataList = new ArrayList<>();
			Map<String, String> actuatorDataMap = jedis.hgetAll(topic);

			for (Map.Entry<String, String> entry : actuatorDataMap.entrySet()) {
				ActuatorData actuatorData = DataUtil.getInstance().jsonToActuatorData(entry.getValue());
				if (actuatorData.getTimeStampMillis() >= startDate.getTime()
						&& actuatorData.getTimeStampMillis() <= endDate.getTime())
					actuatorDataList.add(actuatorData);
			}

			return actuatorDataList.toArray(new ActuatorData[0]);
		} catch (Exception e) {
			_Logger.log(Level.SEVERE, "Error getting actuator data from Redis:", e);
			return new ActuatorData[0];
		}
	}

	/**
	 *
	*/
	@Override
	public SensorData[] getSensorData(String topic, Date startDate, Date endDate) {
		try (Jedis jedis = jedisPool.getResource()) { 
			_Logger.log(Level.INFO, "Getting sensor data from Redis under topic: {0}", topic);
			List<SensorData> sensorDataList = new ArrayList<>();
			Map<String, String> sensorDataMap = jedis.hgetAll(topic);

			for (Map.Entry<String, String> entry : sensorDataMap.entrySet()) {
				SensorData sensorData = DataUtil.getInstance().jsonToSensorData(entry.getValue());
				if (sensorData.getTimeStampMillis() >= startDate.getTime()
						&& sensorData.getTimeStampMillis() <= endDate.getTime())
					sensorDataList.add(sensorData);
			}

			return sensorDataList.toArray(new SensorData[0]);
		} catch (Exception e) {
			_Logger.log(Level.SEVERE, "Error getting sensor data from Redis:", e);
			return new SensorData[0];
		}
	}

	/**
	 *
	*/
	@Override
	public void registerDataStorageListener(Class cType, IPersistenceListener listener, String... topics) {
	}

	/**
	 *
	*/
	@Override
	public boolean storeData(String topic, int qos, ActuatorData... data) {
		if (this.jedisPool == null) {
			_Logger.warning("JedisPool is not initialized.");
			return false;
		}

		try (Jedis jedis = jedisPool.getResource()) {
			for (ActuatorData actuatorData : data) {
				String actuatorDataJson = DataUtil.getInstance().actuatorDataToJson(actuatorData);
				jedis.hset(topic, actuatorData.getTimeStamp(), actuatorDataJson);
			}
			_Logger.log(Level.INFO, "Stored sensor data in Redis under topic: {0}", topic);
			return true;
		} catch (Exception e) {
			_Logger.log(Level.SEVERE, "Error storing actuator data in Redis:", e);
			return false;
		}
	}

	/**
	 *
	*/
	@Override
	public boolean storeData(String topic, int qos, SensorData... data) {
		if (this.jedisPool == null) {
			_Logger.warning("JedisPool is not initialized.");
			return false;
		}

		try (Jedis jedis = jedisPool.getResource()){
			for (SensorData sensorData : data) {
				String sensorDataJson = DataUtil.getInstance().sensorDataToJson(sensorData);
				jedis.hset(topic, sensorData.getTimeStamp(), sensorDataJson);
			}
			_Logger.log(Level.INFO, "Stored sensor data in Redis under topic: {0}", topic);
			return true;
		} catch (Exception e) {
			_Logger.log(Level.SEVERE, "Error storing sensor data in Redis:", e);
			return false;
		}
	}

	/**
	 *
	*/
	@Override
	public boolean storeData(String topic, int qos, SystemPerformanceData... data) {
		if (this.jedisPool == null) {
			_Logger.warning("JedisPool is not initialized.");
			return false;
		}

		try (Jedis jedis = jedisPool.getResource()) {
			for (SystemPerformanceData systemPerformanceData : data) {
				String systemPerformanceDataJson = DataUtil.getInstance()
						.systemPerformanceDataToJson(systemPerformanceData);
				jedis.hset(topic, systemPerformanceData.getTimeStamp(), systemPerformanceDataJson);
			}
			_Logger.log(Level.INFO, "Stored system performance data in Redis under topic: {0}", topic);
			return true;
		} catch (Exception e) {
			_Logger.log(Level.SEVERE, "Error storing system performance data in Redis:", e);
			return false;
		}
	}

	public void subscribeToChannel(JedisPubSub subscriber, ResourceNameEnum resource) {
		new Thread(() -> {
			try (Jedis jedis = jedisPool.getResource()) {
				jedis.subscribe(subscriber, resource.getResourceName());
			} catch (Exception e) {
				_Logger.log(Level.SEVERE, "Error in subscription thread", e);
			}
		}).start();
	}

	// private methods

	/**
	 * 
	*/
	private void initConfig() {
		this.configUtil = ConfigUtil.getInstance();
		this.host = this.configUtil.getProperty(ConfigConst.DATA_GATEWAY_SERVICE, ConfigConst.HOST_KEY);
		this.port = this.configUtil.getInteger(ConfigConst.DATA_GATEWAY_SERVICE, ConfigConst.PORT_KEY);
	}

}