/**
 * This class is part of the Programming the Internet of Things project.
 * 
 * It is provided as a simple shell to guide the student and assist with
 * implementation for the Programming the Internet of Things exercises,
 * and designed to be modified by the student as needed.
 */

package programmingtheiot.gda.app;

import java.io.ObjectInputFilter.Config;
import java.time.OffsetDateTime;
import java.time.temporal.ChronoUnit;
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
import programmingtheiot.data.BaseIotData;
import programmingtheiot.data.DataUtil;
import programmingtheiot.data.SensorData;
import programmingtheiot.data.SystemPerformanceData;
import programmingtheiot.data.SystemStateData;
import programmingtheiot.gda.connection.CloudClientConnector;
import programmingtheiot.gda.connection.CoapServerGateway;
import programmingtheiot.gda.connection.ICloudClient;
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
	private ICloudClient cloudClient = null;
	private IPersistenceClient persistenceClient = null;
	private CoapServerGateway coapServer = null;
	private SystemPerformanceManager sysPerfMgr = null;
	private IActuatorDataListener actuatorDataListener = null;

	private ActuatorData latestHumidifierActuatorData = null;
	private ActuatorData latestHumidifierActuatorResponse = null;
	private SensorData latestHumiditySensorData = null;
	private OffsetDateTime latestHumiditySensorTimeStamp = null;

	private boolean handleHumidityChangeOnDevice = false;// optional
	private int lastKnownHumidifierCommand = ConfigConst.OFF_COMMAND;

	private long humidityMaxTimePastThreshold = 300;// seconds
	private float nominalHumiditySetting = 40.0f;
	private float triggerHumidifierFloor = 30.0f;
	private float triggerHumidifierCeiling = 50.0f;

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

		this.handleHumidityChangeOnDevice = configUtil.getBoolean(ConfigConst.GATEWAY_DEVICE,
				ConfigConst.HANDLE_HUMIDITY);

		this.humidityMaxTimePastThreshold = configUtil.getInteger(ConfigConst.GATEWAY_DEVICE,
				ConfigConst.HUMIDIFIER_MAX_TIME_KEY);

		this.nominalHumiditySetting = configUtil.getFloat(ConfigConst.GATEWAY_DEVICE, ConfigConst.IDEAL_HUMIDITY_KEY);

		this.triggerHumidifierFloor = configUtil.getFloat(ConfigConst.GATEWAY_DEVICE, ConfigConst.HUMIDIFIER_FLOOR_KEY);

		this.triggerHumidifierCeiling = configUtil.getFloat(ConfigConst.GATEWAY_DEVICE,
				ConfigConst.HUMIDIFIER_CEILING_KEY);

		if (this.humidityMaxTimePastThreshold < 10 || this.humidityMaxTimePastThreshold > 7200) {
			this.humidityMaxTimePastThreshold = 300;
		}
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
		if (data != null) {
			// NOTE: Feel free to update this log message for debugging and monitoring
			_Logger.log(Level.FINE, "Actuator request received: {0}. Message: {1}",
					new Object[]{resourceName.getResourceName(), Integer.valueOf((data.getCommand()))});

			if (data.hasError()) {
				_Logger.warning("Error flag set for ActuatorData instance.");
			}

			int qos = ConfigUtil.getInstance().getInteger(ConfigConst.MQTT_GATEWAY_SERVICE, ConfigConst.DEFAULT_QOS_KEY,
					ConfigConst.DEFAULT_QOS);

			_Logger.info("Processing custom actuator command: " + data.getCommand());

			// Recall that this private method was implement in Lab Module 10
			// See PIOT-GDA-10-003 for details
			this.sendActuatorCommandtoCda(resourceName, data);

			return true;
		} else {
			return false;
		}
	}

	@Override
	public boolean handleIncomingMessage(ResourceNameEnum resourceName, String msg) {
		if (resourceName != null && msg != null) {
			try {
				if (resourceName == ResourceNameEnum.CDA_ACTUATOR_CMD_RESOURCE) {
					_Logger.info("Handling incoming ActuatorData message: " + msg);

					// NOTE: it may seem wasteful to convert to ActuatorData and back while
					// the JSON data is already available; however, this provides a validation
					// scheme to ensure the data is actually an 'ActuatorData' instance
					// prior to sending off to the CDA
					ActuatorData ad = DataUtil.getInstance().jsonToActuatorData(msg);
					String jsonData = DataUtil.getInstance().actuatorDataToJson(ad);

					if (this.mqttClient != null) {
						int qos = ConfigUtil.getInstance().getInteger(ConfigConst.MQTT_GATEWAY_SERVICE,
								ConfigConst.DEFAULT_QOS_KEY, ConfigConst.DEFAULT_QOS);
						_Logger.fine("Publishing data to MQTT broker: " + jsonData);
						return this.mqttClient.publishMessage(resourceName, jsonData, qos);
					}


				} else {
					_Logger.warning("Failed to parse incoming message. Unknown type: " + msg);

					return false;
				}
			} catch (Exception e) {
				_Logger.log(Level.WARNING, "Failed to process incoming message for resource: " + resourceName, e);
			}
		} else {
			_Logger.warning("Incoming message has no data. Ignoring for resource: " + resourceName);
		}

		return false;
	}

	@Override
	public boolean handleSensorMessage(ResourceNameEnum resourceName, SensorData data) {
		if (data != null) {
			_Logger.info("Handling sensor message: " + data.getName());

			if (data.hasError()) {
				_Logger.warning("Error flag set for SensorData instance.");
			}

			String jsonData = DataUtil.getInstance().sensorDataToJson(data);

			_Logger.info("JSON [SensorData] -> " + jsonData);

			int qos = ConfigUtil.getInstance().getInteger(ConfigConst.MQTT_GATEWAY_SERVICE, ConfigConst.DEFAULT_QOS_KEY,
					ConfigConst.DEFAULT_QOS);

			if (this.enablePersistenceClient && this.persistenceClient != null) {
				this.persistenceClient.storeData(resourceName.getResourceName(), qos, data);
			}

			this.handleIncomingDataAnalysis(resourceName, data);

			this.handleUpstreamTransmission(resourceName, data, qos);

			return true;
		} else {
			return false;
		}
	}

	private void handleIncomingDataAnalysis(ResourceNameEnum resource, SensorData data) {
		// check either resource or SensorData for type
		if (data.getTypeID() == ConfigConst.HUMIDITY_SENSOR_TYPE) {
			handleHumiditySensorAnalysis(resource, data);
		}
	}

	private void handleHumiditySensorAnalysis(ResourceNameEnum resource, SensorData data) {
		_Logger.info("Analyzing humidity data from CDA: " + data.getLocationID() + ". Value: " + data.getValue());

		boolean isLow = data.getValue() < this.triggerHumidifierFloor;
		boolean isHigh = data.getValue() > this.triggerHumidifierCeiling;

		if (isLow || isHigh) {
			_Logger.info("Humidity data from CDA exceeds nominal range.");

			if (this.latestHumiditySensorData == null) {
				// set properties then exit - nothing more to do until the next sample
				this.latestHumiditySensorData = data;
				this.latestHumiditySensorTimeStamp = getDateTimeFromData(data);

				_Logger.info("Starting humidity nominal exception timer. Waiting for seconds: "
						+ this.humidityMaxTimePastThreshold);

				return;
			} else {
				OffsetDateTime curHumiditySensorTimeStamp = getDateTimeFromData(data);

				long diffSeconds = ChronoUnit.SECONDS.between(this.latestHumiditySensorTimeStamp,
						curHumiditySensorTimeStamp);

				_Logger.info("Checking Humidity value exception time delta: " + diffSeconds);

				if (diffSeconds >= this.humidityMaxTimePastThreshold) {
					ActuatorData ad = new ActuatorData();
					ad.setName(ConfigConst.HUMIDIFIER_ACTUATOR_NAME);
					ad.setLocationID(data.getLocationID());
					ad.setTypeID(ConfigConst.HUMIDIFIER_ACTUATOR_TYPE);
					ad.setValue(this.nominalHumiditySetting);

					if (isLow) {
						ad.setCommand(ConfigConst.ON_COMMAND);
					} else if (isHigh) {
						ad.setCommand(ConfigConst.OFF_COMMAND);
					}

					_Logger.info("Humidity exceptional value reached. Sending actuation event to CDA: " + ad);

					this.lastKnownHumidifierCommand = ad.getCommand();
					sendActuatorCommandtoCda(ResourceNameEnum.CDA_ACTUATOR_CMD_RESOURCE, ad);

					// set ActuatorData and reset SensorData (and timestamp)
					this.latestHumidifierActuatorData = ad;
					this.latestHumiditySensorData = null;
					this.latestHumiditySensorTimeStamp = null;
				}
			}
		} else if (this.lastKnownHumidifierCommand == ConfigConst.ON_COMMAND) {
			// check if we need to turn off the humidifier
			if (this.latestHumidifierActuatorData != null) {
				// check the value - if the humidifier is on, but not yet at nominal, keep it on
				if (this.latestHumidifierActuatorData.getValue() >= this.nominalHumiditySetting) {
					this.latestHumidifierActuatorData.setCommand(ConfigConst.OFF_COMMAND);

					_Logger.info("Humidity nominal value reached. Sending OFF actuation event to CDA: "
							+ this.latestHumidifierActuatorData);

					sendActuatorCommandtoCda(ResourceNameEnum.CDA_ACTUATOR_CMD_RESOURCE,
							this.latestHumidifierActuatorData);

					// reset ActuatorData and SensorData (and timestamp)
					this.lastKnownHumidifierCommand = this.latestHumidifierActuatorData.getCommand();
					this.latestHumidifierActuatorData = null;
					this.latestHumiditySensorData = null;
					this.latestHumiditySensorTimeStamp = null;
				} else {
					_Logger.info("Humidifier is still on. Not yet at nominal levels (OK).");
				}
			} else {
				// shouldn't happen, unless some other logic
				// nullifies the class-scoped ActuatorData instance
				_Logger.warning("ERROR: ActuatorData for humidifier is null (shouldn't be). Can't send command.");
			}
		}
	}

	private void sendActuatorCommandtoCda(ResourceNameEnum resource, ActuatorData data) {
		// NOTE: This is how an ActuatorData command will get passed to the CDA
		// when the GDA is providing the CoAP server and hosting the appropriate
		// ActuatorData resource. It will typically be used when the OBSERVE
		// client (the CDA, assuming the GDA is the server and CDA is the client)
		// has sent an OBSERVE GET request to the ActuatorData resource.
		if (this.actuatorDataListener != null) {
			this.actuatorDataListener.onActuatorDataUpdate(data);
		}

		// NOTE: This is how an ActuatorData command will get passed to the CDA
		// when using MQTT to communicate between the GDA and CDA
		if (this.enableMqttClient && this.mqttClient != null) {
			String jsonData = DataUtil.getInstance().actuatorDataToJson(data);

			if (this.mqttClient.publishMessage(resource, jsonData, ConfigConst.DEFAULT_QOS)) {
				_Logger.info("Published ActuatorData command from GDA to CDA: " + data.getCommand());
			} else {
				_Logger.warning("Failed to publish ActuatorData command from GDA to CDA: " + data.getCommand());
			}
		}
	}

	private OffsetDateTime getDateTimeFromData(BaseIotData data) {
		OffsetDateTime odt = null;

		try {
			odt = OffsetDateTime.parse(data.getTimeStamp());
		} catch (Exception e) {
			_Logger.warning("Failed to extract ISO 8601 timestamp from IoT data. Using local current time.");

			// TODO: this won't be accurate, but should be reasonably close, as the CDA will
			// most likely have recently sent the data to the GDA
			odt = OffsetDateTime.now();
		}

		return odt;
	}

	@Override
	public boolean handleSystemPerformanceMessage(ResourceNameEnum resourceName, SystemPerformanceData data) {
		if (data != null) {
			_Logger.info("Handling system performance message: " + data.getName());

			if (data.hasError()) {
				_Logger.warning("Error flag set for SystemPerformanceData instance.");
			}

			int qos = ConfigUtil.getInstance().getInteger(ConfigConst.MQTT_GATEWAY_SERVICE, ConfigConst.DEFAULT_QOS_KEY,
					ConfigConst.DEFAULT_QOS);

			if (this.enablePersistenceClient) {
				this.persistenceClient.storeData(resourceName.getResourceName(), qos, data);
			}

			this.handleUpstreamTransmission(resourceName, data, qos);

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
			this.mqttClient.setDataMessageListener(this);
		}

		if (this.enableCoapServer) {
			this.coapServer = new CoapServerGateway(this);
			_Logger.info("CoAP server enabled");
		}

		if (this.enableCloudClient) {
			this.cloudClient = new CloudClientConnector();
			_Logger.info("Cloud client enabled");
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
		if (this.cloudClient != null && this.enableCloudClient) {
			if (this.cloudClient.connectClient()) {
				_Logger.info("Successfully connected cloud client.");
			} else {
				throw new RuntimeException("Failed to connect cloud client.");
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
		if (this.cloudClient != null && this.enableCloudClient) {
			this.cloudClient.disconnectClient();
			_Logger.info("Cloud client disconnected.");
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

	private boolean handleUpstreamTransmission(ResourceNameEnum resourceName, BaseIotData data, int qos) {
		_Logger.fine("Sending JSON data to cloud service: " + resourceName);

		if (this.cloudClient != null) {

			try {

				if (data instanceof SensorData) {
					this.cloudClient.sendEdgeDataToCloud(resourceName, (SensorData) data);
				} else if (data instanceof SystemPerformanceData) {
					this.cloudClient.sendEdgeDataToCloud(resourceName, (SystemPerformanceData) data);
				} else {
					_Logger.warning("Unsupported data type for cloud transmission: " + data.getClass().getName());
					return false;
				}

			} catch (Exception e) {
				_Logger.log(Level.WARNING, "Failed to send data to cloud service: " + resourceName, e);
				return false;
			}

			_Logger.info("Sent JSON data upstream to CSP: " + resourceName);
			return true;

		} else {
			_Logger.warning("Cloud client is not enabled or initialized.");
			return false;
		}
	}

}
