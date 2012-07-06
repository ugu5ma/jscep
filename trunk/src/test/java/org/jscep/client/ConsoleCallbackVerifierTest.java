package org.jscep.client;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayInputStream;
import java.security.GeneralSecurityException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.cert.X509Certificate;

import javax.security.auth.callback.Callback;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.x500.X500Principal;

import org.jscep.CertificateVerificationCallback;
import org.jscep.client.verification.ConsoleCertificateVerifier;
import org.jscep.x509.X509Util;
import org.junit.Before;
import org.junit.Test;

import com.google.common.base.Charsets;

public class ConsoleCallbackVerifierTest {
    private X509Certificate cert;
    private CallbackHandler handler;

    @Before
    public void setUp() throws GeneralSecurityException {
        X500Principal subject = new X500Principal("CN=example");
        KeyPair keyPair = KeyPairGenerator.getInstance("RSA").genKeyPair();
        cert = X509Util.createEphemeralCertificate(subject, keyPair);
        handler = new DefaultCallbackHandler(new ConsoleCertificateVerifier());
    }

    @Test
    public void testYesResponse() throws Exception {
        CertificateVerificationCallback callback = getCallback();

        byte[] bytes = String.format("Y%n").getBytes(Charsets.US_ASCII.name());
        System.setIn(new ByteArrayInputStream(bytes));
        handler.handle(new Callback[] {callback});

        assertTrue(callback.isVerified());
    }

    private CertificateVerificationCallback getCallback() {
        return new CertificateVerificationCallback(cert);
    }

    @Test
    public void testEmptyResponse() throws Exception {
        CertificateVerificationCallback callback = getCallback();

        byte[] bytes = String.format("%n").getBytes(Charsets.US_ASCII.name());
        System.setIn(new ByteArrayInputStream(bytes));
        handler.handle(new Callback[] {callback});

        assertFalse(callback.isVerified());
    }

    @Test
    public void testNoResponse() throws Exception {
        CertificateVerificationCallback callback = getCallback();

        byte[] bytes = String.format("N%n").getBytes(Charsets.US_ASCII.name());
        System.setIn(new ByteArrayInputStream(bytes));
        handler.handle(new Callback[] {callback});

        assertFalse(callback.isVerified());
    }

    @Test
    public void testInvalidResponse() throws Exception {
        CertificateVerificationCallback callback = getCallback();

        byte[] bytes = String.format("X%nY%n").getBytes(
                Charsets.US_ASCII.name());
        System.setIn(new ByteArrayInputStream(bytes));
        handler.handle(new Callback[] {callback});

        assertTrue(callback.isVerified());
    }
}
