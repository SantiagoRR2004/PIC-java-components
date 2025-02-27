/**
 * This class is part of the Programming the Internet of Things project.
 * 
 * It is provided as a simple shell to guide the student and assist with
 * implementation for the Programming the Internet of Things exercises,
 * and designed to be modified by the student as needed.
 */ 

package programmingtheiot.gda.system;

import static programmingtheiot.gda.system.BaseSystemUtilTask._Logger;

import java.nio.file.*;
import java.io.IOException;


import java.util.logging.Logger;

import programmingtheiot.common.ConfigConst;

/**
 * Shell representation of class for student implementation.
 * 
 */
public class SystemDiskUtilTask extends BaseSystemUtilTask
{
	// constructors
	
	/**
	 * Default.
	 * 
	 */
	public SystemDiskUtilTask()
	{
		super(ConfigConst.DISK_UTIL_NAME, ConfigConst.DISK_UTIL_TYPE);
	}
	
	
	// public methods
	
	@Override
	public float getTelemetryValue()
	{
		Path path = Paths.get("/").toAbsolutePath();
        try {
            FileStore fileStore = Files.getFileStore(path);

            long totalSpace = fileStore.getTotalSpace();
            long usableSpace = fileStore.getUsableSpace();
            long usedSpace = totalSpace - usableSpace;

            _Logger.fine("Disk used: " + usedSpace + "; Disk Total: " + totalSpace);

            double diskUtil = ((double) usedSpace / totalSpace) * 100.0d;

            return (float) diskUtil;
        } catch (IOException e) {
            _Logger.severe("Error retrieving disk usage: " + e.getMessage());
            return -1; // Return -1 to indicate an error
        }
	}
	
}
