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
public class SystemNetInUtilTask extends BaseSystemUtilTask
{
	private SystemInfo systemInfo;
	private long previousBytesReceived = 0;


	// constructors
	
	
	/**
	 * Default.
	 * 
	 */
	public SystemNetInUtilTask()
	{
		super(ConfigConst.NET_IN_UTIL_NAME, ConfigConst.NET_IN_UTIL_TYPE);
		this.systemInfo = new SystemInfo();
		this.previousBytesReceived = getTotalBytesReceived();
	}
	
	
	// public methods
	
	@Override
	public float getTelemetryValue()
	{
        long currentBytesReceived = getTotalBytesReceived();
        long bytesSinceLastCheck = currentBytesReceived - previousBytesReceived;
        
        // Update previousBytesReceived for next call
        previousBytesReceived = currentBytesReceived;

        return (float) bytesSinceLastCheck;
	}

	private long getTotalBytesReceived()
    {
        long totalBytesReceived = 0;
        
        for (NetworkIF net : systemInfo.getHardware().getNetworkIFs()) {
            totalBytesReceived += net.getBytesRecv();
        }

        return totalBytesReceived;
    }

	
}
