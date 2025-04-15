/**
 * This class is part of the Programming the Internet of Things project.
 * 
 * It is provided as a simple shell to guide the student and assist with
 * implementation for the Programming the Internet of Things exercises,
 * and designed to be modified by the student as needed.
 */

package programmingtheiot.gda.connection.handlers;

import org.eclipse.californium.core.coap.CoAP.ResponseCode;
import org.eclipse.californium.core.coap.MediaTypeRegistry;
import org.eclipse.californium.core.server.resources.CoapExchange;

import programmingtheiot.common.IActuatorDataListener;
import programmingtheiot.data.ActuatorData;
import programmingtheiot.data.DataUtil;

/**
 * Shell representation of class for student implementation.
 *
 */
public class GetActuatorCommandResourceHandler extends GenericCoapResourceHandler implements IActuatorDataListener {
	// static

	// params

	private ActuatorData actuatorData = null;

	// constructors

	public GetActuatorCommandResourceHandler(String resourceName) {
		super(resourceName);

		// set the resource to be observable
		super.setObservable(true);
	}

	public boolean onActuatorDataUpdate(ActuatorData data) {
		if (data != null && this.actuatorData != null) {
			this.actuatorData.updateData(data);

			// notify all connected clients
			super.changed();

			_Logger.fine("Actuator data updated for URI: " + super.getURI() + ": Data value = "
					+ this.actuatorData.getValue());

			return true;
		}

		return false;
	}

	@Override
	public void handleGET(CoapExchange context) {

		// validate 'context'
		if (context == null) {
			_Logger.warning("CoapExchange context is null. Cannot process request.");
			return;
		}

		_Logger.info(
				"GET request received for resource: " + super.getURI() + " with query: " + context.getRequestText());

		// accept the request
		context.accept();

		// Convert the locally stored ActuatorData to JSON using DataUtil
		String jsonData = DataUtil.getInstance().actuatorDataToJson(this.actuatorData);

		// send an appropriate response
		context.respond(ResponseCode.CONTENT, jsonData, MediaTypeRegistry.APPLICATION_JSON);

	}

}
