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
import java.util.HashMap;
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
	private Jedis client;

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
		this.client = null;
		_Logger.info("RedisPersistenceAdapter initialized with host: " + this.host + ", port: " + this.port);
	}

	// public methods

	// public methods

	/**
	 *
	 */
	@Override
	public boolean connectClient() {
		if (this.client != null && this.client.isConnected()) {
			_Logger.warning("Redis client is already connected.");
			return true;
		}
		try {
			this.client = new Jedis(this.host, this.port);
			_Logger.info("Connected to Redis successfully.");
			return true;
		} catch (JedisConnectionException e) {
			_Logger.log(Level.SEVERE, "Failed to connect to Redis.", e);
			this.client = null;
			return false;
		}
	}

	/**
	 *
	 */
	@Override
	public boolean disconnectClient() {
		if (this.client == null || !this.client.isConnected()) {
			_Logger.warning("Redis client is already disconnected.");
			return true;
		}
		try {
			this.client.close();
			this.client = null;
			_Logger.info("Disconnected from Redis successfully.");
			return true;
		} catch (JedisConnectionException e) {
			_Logger.log(Level.SEVERE, "Error disconnecting from Redis:", e);
			return false;
		}
	}

	/**
	 *
	 */
	@Override
	public ActuatorData[] getActuatorData(String topic, Date startDate, Date endDate) {
		_Logger.log(Level.INFO, "Getting actuator data from Redis under topic: {0}", topic);
		List<ActuatorData> actuatorDataList = new ArrayList<>();
		Map<String, String> actuatorDataMap = this.client.hgetAll(topic);
		
		for (Map.Entry<String, String> entry : actuatorDataMap.entrySet()) {
			ActuatorData actuatorData = DataUtil.getInstance().jsonToActuatorData(entry.getValue());
			if (actuatorData.getTimeStampMillis() >= startDate.getTime() && actuatorData.getTimeStampMillis() <= endDate.getTime())
				actuatorDataList.add(actuatorData);
		}
		
		return actuatorDataList.toArray(new ActuatorData[0]);
	}

	/**
	 *
	 */
	@Override
	public SensorData[] getSensorData(String topic, Date startDate, Date endDate) {
		_Logger.log(Level.INFO, "Getting sensor data from Redis under topic: {0}", topic);
		List<SensorData> sensorDataList = new ArrayList<>();
		Map<String, String> sensorDataMap = this.client.hgetAll(topic);
		
		for (Map.Entry<String, String> entry : sensorDataMap.entrySet()) {
			SensorData sensorData = DataUtil.getInstance().jsonToSensorData(entry.getValue());
			if (sensorData.getTimeStampMillis() >= startDate.getTime() && sensorData.getTimeStampMillis() <= endDate.getTime())
				sensorDataList.add(sensorData);
		}
		
		return sensorDataList.toArray(new SensorData[0]);
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
		if (this.client == null || this.client.ping() == null) {
			_Logger.warning("Failed to connect to Redis.");
			return false;
		}

		try {
			for (ActuatorData actuatorData : data) {
				String actuatorDataJson = DataUtil.getInstance().actuatorDataToJson(actuatorData);
				this.client.hset(topic, actuatorData.getTimeStamp(), actuatorDataJson);
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
		if (this.client == null || this.client.ping() == null) {
			_Logger.warning("Failed to connect to Redis.");
			return false;
		}

		try {
			for (SensorData sensorData : data) {
				String sensorDataJson = DataUtil.getInstance().sensorDataToJson(sensorData);
				this.client.hset(topic, sensorData.getTimeStamp(), sensorDataJson);
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
		if (this.client == null || this.client.ping() == null) {
			_Logger.warning("Failed to connect to Redis.");
			return false;
		}

		try {
			for (SystemPerformanceData systemPerformanceData : data) {
				String systemPerformanceDataJson = DataUtil.getInstance().systemPerformanceDataToJson(systemPerformanceData);
				this.client.hset(topic, systemPerformanceData.getTimeStamp(), systemPerformanceDataJson);
			}
			_Logger.log(Level.INFO, "Stored system performance data in Redis under topic: {0}", topic);
			return true;
		} catch (Exception e) {
			_Logger.log(Level.SEVERE, "Error storing system performance data in Redis:", e);
			return false;
		}
	}


	public void subscribeToChannel(JedisPubSub subscriber, ResourceNameEnum resource) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                client.subscribe(subscriber, resource.getResourceName());
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
