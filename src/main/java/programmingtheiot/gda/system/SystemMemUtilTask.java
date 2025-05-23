/**
 * This class is part of the Programming the Internet of Things project.
 * 
 * It is provided as a simple shell to guide the student and assist with
 * implementation for the Programming the Internet of Things exercises,
 * and designed to be modified by the student as needed.
 */

package programmingtheiot.gda.system;

import static programmingtheiot.gda.system.BaseSystemUtilTask._Logger;

import java.lang.management.ManagementFactory;
import java.lang.management.MemoryUsage;
import java.util.logging.Logger;

import programmingtheiot.common.ConfigConst;

/**
 * Shell representation of class for student implementation.
 * 
 */
public class SystemMemUtilTask extends BaseSystemUtilTask {
	// constructors

	/**
	 * Default.
	 * 
	 */
	public SystemMemUtilTask() {
		super(ConfigConst.MEM_UTIL_NAME, ConfigConst.MEM_UTIL_TYPE);
	}

	// public methods

	@Override
	public float getTelemetryValue() {
		MemoryUsage memUsage = ManagementFactory.getMemoryMXBean().getHeapMemoryUsage();
		double memUsed = (double) memUsage.getUsed();
		double memMax = (double) memUsage.getMax();

		_Logger.info("Mem used: " + memUsed + "; Mem Max: " + memMax);

		double memUtil = (memUsed / memMax) * 100.0d;

		return (float) memUtil;
	}

}
