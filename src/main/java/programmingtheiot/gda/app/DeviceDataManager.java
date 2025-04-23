/**
 * This class is part of the Programming the Internet of Things project.
 * 
 * It is provided as a simple shell to guide the student and assist with
 * implementation for the Programming the Internet of Things exercises,
 * and designed to be modified by the student as needed.
 */

package programmingtheiot.gda.app;

import java.io.ObjectInputFilter.Config;
import java.util.Arrays;
import java.util.List;
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

	private static final List<ResourceNameEnum> topics = Arrays.asList(ResourceNameEnum.GDA_MGMT_STATUS_MSG_RESOURCE,
			ResourceNameEnum.CDA_ACTUATOR_RESPONSE_RESOURCE, ResourceNameEnum.CDA_SENSOR_MSG_RESOURCE,
			ResourceNameEnum.CDA_SYSTEM_PERF_MSG_RESOURCE);

	// private var's

	private boolean enableMqttClient = true;
	private boolean enableCoapServer = true;
	private boolean enableCloudClient = false;
	private boolean enableSmtpClient = false;
	private boolean enablePersistenceClient = true;
	private boolean enableSystemPerf = false;

	private MqttClientConnector mqttClient = null;
	private IPubSubClient cloudClient = null;
	private IPersistenceClient persistenceClient = null;
	private CoapServerGateway coapServer = null;
	private SystemPerformanceManager sysPerfMgr = null;
	private IActuatorDataListener actuatorDataListener = null;

	// constructors

	public DeviceDataManager() {
		super();

		ConfigUtil configUtil = ConfigUtil.getInstance();

		this.enableMqttClient = configUtil.getBoolean(ConfigConst.GATEWAY_DEVICE, ConfigConst.ENABLE_MQTT_CLIENT_KEY);

		this.enableCoapServer = configUtil.getBoolean(ConfigConst.GATEWAY_DEVICE, ConfigConst.ENABLE_COAP_SERVER_KEY);

		this.enableCloudClient = configUtil.getBoolean(ConfigConst.GATEWAY_DEVICE, ConfigConst.ENABLE_CLOUD_CLIENT_KEY);

		this.enablePersistenceClient = configUtil.getBoolean(ConfigConst.GATEWAY_DEVICE,
				ConfigConst.ENABLE_PERSISTENCE_CLIENT_KEY);

		this.enablePersistenceClient = configUtil.getBoolean(ConfigConst.GATEWAY_DEVICE,
				ConfigConst.ENABLE_PERSISTENCE_CLIENT_KEY);

		initManager();

		initConnections();
	}

	public DeviceDataManager(boolean enableMqttClient, boolean enableCoapClient, boolean enableCloudClient,
			boolean enableSmtpClient, boolean enablePersistenceClient) {
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
		if (listener != null) {
			// for now, just ignore 'name' - if you need more than one listener,
			// you can use 'name' to create a map of listener instances
			this.actuatorDataListener = listener;
		} else {
			_Logger.warning("ActuatorDataListener is null.");
		}
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
			this.mqttClient = new MqttClientConnector();
		}

		if (this.enableCoapServer) {
			this.coapServer = new CoapServerGateway(this);
			_Logger.info("CoAP server enabled");
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
		if (this.mqttClient != null) {
			if (this.mqttClient.connectClient()) {
				_Logger.info("Successfully connected MQTT client to broker.");

				int qos = ConfigUtil.getInstance().getInteger(ConfigConst.MQTT_GATEWAY_SERVICE,
						ConfigConst.DEFAULT_QOS_KEY, ConfigConst.DEFAULT_QOS);

				// IMPORTANT NOTE: The 'subscribeToTopic()' method calls shown
				// below will be moved to MqttClientConnector.connectComplete()
				// in Lab Module 10. For now, they can remain here.
				for (ResourceNameEnum topic : topics) {
					boolean toret = this.mqttClient.subscribeToTopic(topic, qos);
					if (!toret) {
						_Logger.warning("Failed to subscribe to topic: " + topic);
					}
				}

			} else {
				throw new RuntimeException("Failed to connect MQTT client to broker.");
			}
		}
		if (this.coapServer != null && this.enableCoapServer) {
			if (this.coapServer.startServer()) {
				_Logger.info("Successfully started CoAP server.");
			} else {
				throw new RuntimeException("Failed to start CoAP server.");
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
		if (this.mqttClient != null) {
			// NOTE: The unsubscribeFromTopic() method calls below should match with
			// the subscribeToTopic() method calls from startManager(). Also, the
			// unsubscribe logic below can be moved to MqttClientConnector's
			// disconnectClient() call PRIOR to actually disconnecting from
			// the MQTT broker.
			for (ResourceNameEnum topic : topics) {
				boolean toret = this.mqttClient.unsubscribeFromTopic(topic);
				if (!toret) {
					_Logger.warning("Failed to unsubscribe from topic: " + topic);
				}
			}
			if (this.mqttClient.disconnectClient()) {
				_Logger.info("Successfully disconnected MQTT client from broker.");
			} else {
				throw new RuntimeException("Failed to disconnect MQTT client from broker.");
			}
		}
		if (this.coapServer != null && this.enableCoapServer) {
			if (this.coapServer.stopServer()) {
				_Logger.info("Successfully stopped CoAP server.");
			} else {
				throw new RuntimeException("Failed to stop CoAP server.");
			}
		}
	}

	// private methods

	/**
	 * Initializes the enabled connections. This will NOT start them, but only
	 * create the instances that will be used in the {@link #startManager() and
	 * #stopManager()) methods.
	 * 
	 */
	private void initConnections() {
	}

	private void handleIncomingDataAnalysis(ResourceNameEnum resourceName, SystemStateData message) {
		_Logger.info("handleIncomingDataAnalysis has been initiated..");
	}

	private void handleIncomingDataAnalysis(ResourceNameEnum resourceName, ActuatorData message) {
		_Logger.info("Analyzing incoming actuator data: " + message.getName());

		if (message.isResponseFlagEnabled()) {
			// TODO: implement this
		} else {
			if (this.actuatorDataListener != null) {
				this.actuatorDataListener.onActuatorDataUpdate(message);
			}
		}
	}

	private boolean handleUpstreamTransmission(ResourceNameEnum resourceName, String jsonData, int qos) {
		_Logger.info("Persistence Client is active");
		return true;
	}

}
