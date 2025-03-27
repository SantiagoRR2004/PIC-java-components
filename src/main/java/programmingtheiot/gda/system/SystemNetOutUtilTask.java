/**
 * This class is part of the Programming the Internet of Things project.
 * 
 * It is provided as a simple shell to guide the student and assist with
 * implementation for the Programming the Internet of Things exercises,
 * and designed to be modified by the student as needed.
 */

package programmingtheiot.gda.system;

import oshi.SystemInfo;
import oshi.hardware.NetworkIF;

import java.util.logging.Logger;

import programmingtheiot.common.ConfigConst;

/**
 * Shell representation of class for student implementation.
 * 
 */
public class SystemNetOutUtilTask extends BaseSystemUtilTask {
	private SystemInfo systemInfo;
	private long previousBytesSent = 0;

	// constructors

	/**
	 * Default.
	 * 
	 */
	public SystemNetOutUtilTask() {
		super(ConfigConst.NET_OUT_UTIL_NAME, ConfigConst.NET_OUT_UTIL_TYPE);
		this.systemInfo = new SystemInfo();
		this.previousBytesSent = getTotalBytesSent();
	}

	// public methods

	@Override
	public float getTelemetryValue() {
		long currentBytesSent = getTotalBytesSent();
		long bytesSinceLastCheck = currentBytesSent - previousBytesSent;

		// Update previousBytesSent for next call
		previousBytesSent = currentBytesSent;

		return (float) bytesSinceLastCheck;
	}

	private long getTotalBytesSent() {
		long totalBytesSent = 0;

		for (NetworkIF net : systemInfo.getHardware().getNetworkIFs()) {
			totalBytesSent += net.getBytesSent();
		}

		return totalBytesSent;
	}

}
