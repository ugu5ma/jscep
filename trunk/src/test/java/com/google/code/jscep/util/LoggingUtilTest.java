package com.google.code.jscep.util;

import java.util.logging.Logger;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.google.code.jscep.Client;

public class LoggingUtilTest {
	private Logger fixture;
	
	@Before
	public void setUp() {
		fixture = LoggingUtil.getLogger(Client.class);
	}
	
	@Test
	public void testGetLoggerClass() {
		Assert.assertEquals(fixture, LoggingUtil.getLogger(Client.class));
	}

	@Test
	public void testGetLoggerString() {
		Assert.assertEquals(fixture, LoggingUtil.getLogger("com.google.code.jscep"));
	}

}
