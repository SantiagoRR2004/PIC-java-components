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

import programmingtheiot.gda.system.SystemNetOutUtilTask;

/**
 * This test case class contains very basic unit tests for
 * SystemNetOutUtilTaskTest. It should not be considered complete,
 * but serve as a starting point for the student implementing
 * additional functionality within their Programming the IoT
 * environment.
 *
 */
public class SystemNetOutUtilTaskTest {
	// static

	private static final Logger _Logger = Logger.getLogger(SystemNetOutUtilTaskTest.class.getName());

	// member var's

	private SystemNetOutUtilTask netOutUtilTask = null;

	// test setup methods

	/**
	 * @throws java.lang.Exception
	 */
	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
	}

	/**
	 * @throws java.lang.Exception
	 */
	@AfterClass
	public static void tearDownAfterClass() throws Exception {
	}

	/**
	 * @throws java.lang.Exception
	 */
	@Before
	public void setUp() throws Exception {
		this.netOutUtilTask = new SystemNetOutUtilTask();
	}

	/**
	 * @throws java.lang.Exception
	 */
	@After
	public void tearDown() throws Exception {
	}

	// test methods

	/**
	 * Test method for
	 * {@link programmingtheiot.gda.system.SystemNetOutUtilTask#getTelemetryValue()}.
	 */
	@Test
	public void testGetTelemetryValue() {
		float netOutUtil = 0.0f;
		int totTests = 5;

		netOutUtil = this.netOutUtilTask.getTelemetryValue();

		for (int i = 1; i <= totTests; i++) {
			if (netOutUtil >= 0.0f) {
				_Logger.info("Test " + i + ": NetOut Util: " + netOutUtil);
				assertTrue(netOutUtil >= 0.0f);
			} else if (netOutUtil < 0.0f) {
				_Logger.info("Test " + i + ": NetOut Util not supported on this OS: " + netOutUtil);
			} else {
				fail("Failed to retrieve number of bytes sent.");
			}
		}
	}

}
