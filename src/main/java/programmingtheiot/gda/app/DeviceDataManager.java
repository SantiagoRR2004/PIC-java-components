/**
 * This class is part of the Programming the Internet of Things project.
 * 
 * It is provided as a simple shell to guide the student and assist with
 * implementation for the Programming the Internet of Things exercises,
 * and designed to be modified by the student as needed.
 */

package programmingtheiot.gda.app;

import java.util.logging.Level;
import java.util.logging.Logger;

import programmingtheiot.common.ConfigConst;
import programmingtheiot.common.ConfigUtil;
import programmingtheiot.common.IActuatorDataListener;
import programmingtheiot.common.IDataMessageListener;
import programmingtheiot.common.ResourceNameEnum;

import programmingtheiot.data.ActuatorData;
import programmingtheiot.data.DataUtil;
import programmingtheiot.data.SensorData;
import programmingtheiot.data.SystemPerformanceData;
import programmingtheiot.data.SystemStateData;

import programmingtheiot.gda.connection.CloudClientConnector;
import programmingtheiot.gda.connection.CoapServerGateway;
import programmingtheiot.gda.connection.IPersistenceClient;
import programmingtheiot.gda.connection.IPubSubClient;
import programmingtheiot.gda.connection.IRequestResponseClient;
import programmingtheiot.gda.connection.MqttClientConnector;
import programmingtheiot.gda.connection.RedisPersistenceAdapter;
import programmingtheiot.gda.connection.SmtpClientConnector;
import programmingtheiot.gda.system.SystemPerformanceManager;
import redis.clients.jedis.JedisPubSub;

/**
 * Shell representation of class for student implementation.
 *
 */
public class DeviceDataManager extends JedisPubSub implements IDataMessageListener {
	// static

	private static final Logger _Logger = Logger.getLogger(DeviceDataManager.class.getName());

	// private var's

	private boolean enableMqttClient = true;
	private boolean enableCoapServer = false;
	private boolean enableCloudClient = false;
	private boolean enableSmtpClient = false;
	private boolean enablePersistenceClient = true;
	private boolean enableSystemPerf = false;

	private IPubSubClient mqttClient = null;
	private IPubSubClient cloudClient = null;
	private IPersistenceClient persistenceClient = null;
	private CoapServerGateway coapServer = null;
	private SystemPerformanceManager sysPerfMgr = null;

	// constructors

	public DeviceDataManager() {
		super();

		ConfigUtil configUtil = ConfigUtil.getInstance();

		this.enableMqttClient = configUtil.getBoolean(
				ConfigConst.GATEWAY_DEVICE, ConfigConst.ENABLE_MQTT_CLIENT_KEY);

		this.enableCoapServer = configUtil.getBoolean(
				ConfigConst.GATEWAY_DEVICE, ConfigConst.ENABLE_COAP_SERVER_KEY);

		this.enableCloudClient = configUtil.getBoolean(
				ConfigConst.GATEWAY_DEVICE, ConfigConst.ENABLE_CLOUD_CLIENT_KEY);

		this.enablePersistenceClient = configUtil.getBoolean(
				ConfigConst.GATEWAY_DEVICE, ConfigConst.ENABLE_PERSISTENCE_CLIENT_KEY);

		this.enablePersistenceClient = configUtil.getBoolean(ConfigConst.GATEWAY_DEVICE,
				ConfigConst.ENABLE_PERSISTENCE_CLIENT_KEY);

		initManager();

		initConnections();
	}

	public DeviceDataManager(
			boolean enableMqttClient,
			boolean enableCoapClient,
			boolean enableCloudClient,
			boolean enableSmtpClient,
			boolean enablePersistenceClient) {
		super();

		initConnections();
	}

	// public methods

	@Override
	public boolean handleActuatorCommandResponse(ResourceNameEnum resourceName, ActuatorData data) {
		if (data != null) {
			_Logger.info("Handling actuator response: " + data.getName());

			// this next call is optional for now
			// this.handleIncomingDataAnalysis(resourceName, data);

			if (data.hasError()) {
				_Logger.warning("Error flag set for ActuatorData instance.");
			}

			if (this.enablePersistenceClient) {
				this.persistenceClient.storeData(resourceName.getResourceName(), 0, data);
			}

			return true;
		} else {
			return false;
		}
	}

	@Override
	public boolean handleActuatorCommandRequest(ResourceNameEnum resourceName, ActuatorData data) {
		return false;
	}

	@Override
	public boolean handleIncomingMessage(ResourceNameEnum resourceName, String msg) {
		{
			if (msg != null) {
				_Logger.info("Handling incoming generic message: " + msg);

				return true;
			} else {
				return false;
			}
		}
	}

	@Override
	public boolean handleSensorMessage(ResourceNameEnum resourceName, SensorData data) {
		if (data != null) {
			_Logger.info("Handling sensor message: " + data.getName());

			if (data.hasError()) {
				_Logger.warning("Error flag set for SensorData instance.");
			}

			if (this.enablePersistenceClient) {
				this.persistenceClient.storeData(resourceName.getResourceName(), 0, data);
			}

			return true;
		} else {
			return false;
		}
	}

	@Override
	public boolean handleSystemPerformanceMessage(ResourceNameEnum resourceName, SystemPerformanceData data) {
		if (data != null) {
			_Logger.info("Handling system performance message: " + data.getName());

			if (data.hasError()) {
				_Logger.warning("Error flag set for SystemPerformanceData instance.");
			}

			if (this.enablePersistenceClient) {
				this.persistenceClient.storeData(resourceName.getResourceName(), 0, data);
			}

			return true;
		} else {
			return false;
		}
	}

	public void setActuatorDataListener(String name, IActuatorDataListener listener) {
	}

	private void initManager() {
		_Logger.info("DeviceDataManager has been initialized...");
		ConfigUtil configUtil = ConfigUtil.getInstance();

		this.enableSystemPerf = configUtil.getBoolean(ConfigConst.GATEWAY_DEVICE, ConfigConst.ENABLE_SYSTEM_PERF_KEY);

		if (this.enableSystemPerf) {
			this.sysPerfMgr = new SystemPerformanceManager();
			this.sysPerfMgr.setDataMessageListener(this);
		}

		if (this.enableMqttClient) {
			// TODO: implement this in Lab Module 7
		}

		if (this.enableCoapServer) {
			// TODO: implement this in Lab Module 8
		}

		if (this.enableCloudClient) {
			// TODO: implement this in Lab Module 10
		}

		if (this.enablePersistenceClient) {
			this.persistenceClient = new RedisPersistenceAdapter();
			_Logger.log(Level.INFO, "Redis persistence enabled");
		}
	}

	public void startManager() {
		_Logger.info("DeviceDataManager has been started...");
		if (this.sysPerfMgr != null) {
			this.sysPerfMgr.startManager();
		}
		if (this.persistenceClient != null) {
			this.persistenceClient.connectClient();

			// Check if persistenceClient is an instance of RedisPersistenceAdapter
			if (this.persistenceClient instanceof RedisPersistenceAdapter) {
				((RedisPersistenceAdapter) this.persistenceClient).subscribeToChannel(this,
						ResourceNameEnum.CDA_SENSOR_MSG_RESOURCE);
			}
		}
	}

	public void stopManager() {
		_Logger.info("DeviceDataManager has been stopped...");
		if (this.sysPerfMgr != null) {
			this.sysPerfMgr.stopManager();
		}
		if (this.persistenceClient != null) {
			this.persistenceClient.disconnectClient();
		}
	}

	// private methods

	/**
	 * Initializes the enabled connections. This will NOT start them, but only
	 * create the
	 * instances that will be used in the {@link #startManager() and #stopManager())
	 * methods.
	 * 
	 */
	private void initConnections() {
	}

	private void handleIncomingDataAnalysis(ResourceNameEnum resourceName, SystemStateData message) {
		_Logger.info("handleIncomingDataAnalysis has been initiated..");
	}

	private void handleIncomingDataAnalysis(ResourceNameEnum resourceName, ActuatorData message) {
		_Logger.info("handleIncomingDataAnalysis has been initiated..");
	}

	private boolean handleUpstreamTransmission(ResourceNameEnum resourceName, String jsonData, int qos) {
		_Logger.info("Persistence Client is active");
		return true;
	}

}
