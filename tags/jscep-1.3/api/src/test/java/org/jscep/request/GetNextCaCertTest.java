package org.jscep.request;

import org.jscep.content.NextCaCertificateContentHandler;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.security.cert.X509Certificate;
import java.util.List;

public class GetNextCaCertTest {
	private Request<List<X509Certificate>> fixture;
	private String caIdentifier;
	
	@Before
	public void setUp() {
		caIdentifier = "id";
		fixture = new GetNextCaCert(caIdentifier, new NextCaCertificateContentHandler(null));
	}
	
	@Test
	public void testGetOperation() {
		Assert.assertSame(Operation.GetNextCACert, fixture.getOperation());
	}

	@Test
	public void testGetMessage() throws IOException {
		Assert.assertEquals(caIdentifier, fixture.getMessage());
	}

	@Test
	public void testContentHandler() {
		Assert.assertNotNull(fixture.getContentHandler());
	}
}
