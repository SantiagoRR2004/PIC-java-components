/**
 * This class is part of the Programming the Internet of Things
 * project, and is available via the MIT License, which can be
 * found in the LICENSE file at the top level of this repository.
 * 
 * Copyright (c) 2020 by Andrew D. King
 */ 

package programmingtheiot.part01.unit.system;

import static org.junit.Assert.*;

import java.util.logging.Logger;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import programmingtheiot.gda.system.SystemNetInUtilTask;

/**
 * This test case class contains very basic unit tests for
 * SystemNetInUtilTaskTest. It should not be considered complete,
 * but serve as a starting point for the student implementing
 * additional functionality within their Programming the IoT
 * environment.
 *
 */
public class SystemNetInUtilTaskTest
{
	// static
	
	private static final Logger _Logger =
		Logger.getLogger(SystemNetInUtilTaskTest.class.getName());
	
	// member var's
	
	private SystemNetInUtilTask netInUtilTask = null;
	
	// test setup methods
	
	/**
	 * @throws java.lang.Exception
	 */
	@BeforeClass
	public static void setUpBeforeClass() throws Exception
	{
	}
	
	/**
	 * @throws java.lang.Exception
	 */
	@AfterClass
	public static void tearDownAfterClass() throws Exception
	{
	}
	
	/**
	 * @throws java.lang.Exception
	 */
	@Before
	public void setUp() throws Exception
	{
		this.netInUtilTask = new SystemNetInUtilTask();
	}
	
	/**
	 * @throws java.lang.Exception
	 */
	@After
	public void tearDown() throws Exception
	{
	}
	
	// test methods
	
	/**
	 * Test method for {@link programmingtheiot.gda.system.SystemNetInUtilTask#getTelemetryValue()}.
	 */
	@Test
	public void testGetTelemetryValue()
	{
		float netInUtil  = 0.0f;
		int   totTests = 5;
		
		netInUtil = this.netInUtilTask.getTelemetryValue();
		
		for (int i = 1; i <= totTests; i++) {
			if (netInUtil >= 0.0f) {
				_Logger.info("Test " + i + ": NetIn Util: " + netInUtil);
				assertTrue(netInUtil >= 0.0f);
			} else if (netInUtil < 0.0f) {
				_Logger.info("Test " + i + ": NetIn Util not supported on this OS: " + netInUtil);
			} else {
				fail("Failed to retrieve bytes.");
			}
		}
	}
	
}
