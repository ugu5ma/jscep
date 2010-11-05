package org.jscep.util;

import java.util.logging.Logger;

import org.jscep.util.LoggingUtil;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class LoggingUtilTest {
	private Logger fixture;
	
	@Before
	public void setUp() {
		fixture = LoggingUtil.getLogger(LoggingUtil.class);
	}
	
	@Test
	public void testGetLoggerClass() {
		Assert.assertEquals(fixture, LoggingUtil.getLogger(LoggingUtil.class));
	}

	@Test
	public void testGetLoggerString() {
		Assert.assertEquals(fixture, LoggingUtil.getLogger("org.jscep.util"));
	}

}
