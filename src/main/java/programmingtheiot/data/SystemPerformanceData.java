/**
 * This class is part of the Programming the Internet of Things project.
 * 
 * It is provided as a simple shell to guide the student and assist with
 * implementation for the Programming the Internet of Things exercises,
 * and designed to be modified by the student as needed.
 */

package programmingtheiot.data;

import java.io.Serializable;

import programmingtheiot.common.ConfigConst;

/**
 * Shell representation of class for student implementation.
 *
 */
public class SystemPerformanceData extends BaseIotData implements Serializable {
	// static

	// private var's
	private float cpuUtil = ConfigConst.DEFAULT_VAL;
	private float diskUtil = ConfigConst.DEFAULT_VAL;
	private float memUtil = ConfigConst.DEFAULT_VAL;
	private float netInUtil = ConfigConst.DEFAULT_VAL;
	private float netOutUtil = ConfigConst.DEFAULT_VAL;

	// constructors

	public SystemPerformanceData() {
		super();
		super.setName(ConfigConst.SYS_PERF_DATA);
	}

	// public methods

	public float getCpuUtilization() {
		return this.cpuUtil;
	}

	public float getDiskUtilization() {
		return this.diskUtil;
	}

	public float getMemoryUtilization() {
		return this.memUtil;
	}

	public float getNetInUtilization() {
		return this.netInUtil;
	}

	public float getNetOutUtilization() {
		return this.netOutUtil;
	}

	public void setCpuUtilization(float val) {
		super.updateTimeStamp();
		this.cpuUtil = val;
	}

	public void setDiskUtilization(float val) {
		super.updateTimeStamp();
		this.diskUtil = val;
	}

	public void setMemoryUtilization(float val) {
		super.updateTimeStamp();
		this.memUtil = val;
	}

	public void setNetInUtilization(float val) {
		super.updateTimeStamp();
		this.netInUtil = val;
	}

	public void setNetOutUtilization(float val) {
		super.updateTimeStamp();
		this.netOutUtil = val;
	}

	/**
	 * Returns a string representation of this instance. This will invoke the base
	 * class {@link #toString()} method, then append the output from this call.
	 * 
	 * @return String The string representing this instance, returned in CSV
	 *         'key=value' format.
	 */
	public String toString() {
		StringBuilder sb = new StringBuilder(super.toString());

		sb.append(',');
		sb.append(ConfigConst.CPU_UTIL_PROP).append('=').append(this.getCpuUtilization()).append(',');
		sb.append(ConfigConst.DISK_UTIL_PROP).append('=').append(this.getDiskUtilization()).append(',');
		sb.append(ConfigConst.MEM_UTIL_PROP).append('=').append(this.getMemoryUtilization()).append(',');
		sb.append(ConfigConst.NET_IN_UTIL_PROP).append('=').append(this.getNetInUtilization()).append(',');
		sb.append(ConfigConst.NET_OUT_UTIL_PROP).append('=').append(this.getNetOutUtilization());

		return sb.toString();
	}

	// protected methods

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * programmingtheiot.data.BaseIotData#handleUpdateData(programmingtheiot.data.
	 * BaseIotData)
	 */
	protected void handleUpdateData(BaseIotData data) {
		if (data instanceof SystemPerformanceData) {
			SystemPerformanceData spd = (SystemPerformanceData) data;

			this.setCpuUtilization(spd.getCpuUtilization());
			this.setDiskUtilization(spd.getDiskUtilization());
			this.setMemoryUtilization(spd.getMemoryUtilization());
			this.setNetInUtilization(spd.getNetInUtilization());
			this.setNetOutUtilization(spd.getNetOutUtilization());
		}
	}

}
