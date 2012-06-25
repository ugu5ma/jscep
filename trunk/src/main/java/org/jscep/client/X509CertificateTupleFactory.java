package org.jscep.client;

import java.security.cert.CertStore;
import java.security.cert.CertStoreException;
import java.security.cert.Certificate;
import java.security.cert.X509CertSelector;
import java.security.cert.X509Certificate;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Factory for creating X509CertificateTuple objects.
 */
public final class X509CertificateTupleFactory {
    private static final Logger LOGGER = LoggerFactory
	    .getLogger(X509CertificateTupleFactory.class);
    private static Map<CertStore, X509CertificateTuple> cache = new HashMap<CertStore, X509CertificateTuple>();
    private static final int DIGITAL_SIGNATURE = 0;
    private static final int KEY_ENCIPHERMENT = 2;
    private static final int DATA_ENCIPHERMENT = 3;

    private X509CertificateTupleFactory() {
    }

    /**
     * Creates a new tuple from the given store
     * 
     * @param store the store to examine
     * @return a tuple of certificates
     */
    public static X509CertificateTuple createTuple(CertStore store) {
	if (cache.containsKey(store)) {
	    LOGGER.debug(
		    "{} has already been inspected, retrieving result from cache.",
		    store);
	    return cache.get(store);
	} else if (!cache.isEmpty()) {
	    LOGGER.debug("Cache missed, so clearing");
	    cache.clear();
	}
	try {
	    Collection<? extends Certificate> certs = store
		    .getCertificates(null);
	    LOGGER.debug("CertStore contains {} certificate(s):", certs.size());
	    int i = 0;
	    for (Certificate cert : certs) {
		X509Certificate x509 = (X509Certificate) cert;
		LOGGER.debug("{}. '{}'", ++i, x509.getSubjectDN());
	    }
	} catch (CertStoreException e) {
	    throw new RuntimeException(e);
	}

	X509Certificate encryption = selectEncryptionCertificate(store);
	LOGGER.debug("Using {} for message encryption",
		encryption.getSubjectDN());

	X509Certificate signing = selectMessageVerifier(store);
	LOGGER.debug("Using {} for message verification",
		signing.getSubjectDN());

	X509Certificate issuer = selectIssuerCertificate(store);
	LOGGER.debug("Using {} for issuer", signing.getSubjectDN());

	X509CertificateTuple tuple = new X509CertificateTuple(signing,
		encryption, issuer);
	cache.put(store, tuple);

	return tuple;
    }

    private static X509Certificate selectIssuerCertificate(CertStore store) {
	X509CertSelector signingSelector = new X509CertSelector();
	boolean[] keyUsage = new boolean[9];
	signingSelector.setKeyUsage(keyUsage);
	signingSelector.setBasicConstraints(0);

	X509Certificate issuer;
	try {
	    LOGGER.debug("Selecting certificate with basicConstraints");
	    Collection<? extends Certificate> certs = store
		    .getCertificates(signingSelector);
	    if (certs.size() > 0) {
		issuer = (X509Certificate) certs.iterator().next();
	    } else {
		throw new RuntimeException(
			"No suitable certificate for verification");
	    }
	} catch (CertStoreException e) {
	    throw new RuntimeException(e);
	}
	return issuer;
    }

    private static X509Certificate selectMessageVerifier(CertStore store) {
	X509CertSelector signingSelector = new X509CertSelector();
	boolean[] keyUsage = new boolean[9];
	keyUsage[DIGITAL_SIGNATURE] = true;
	signingSelector.setKeyUsage(keyUsage);

	try {
	    LOGGER.debug("Selecting certificate with digitalSignature keyUsage");
	    Collection<? extends Certificate> certs = store
		    .getCertificates(signingSelector);
	    if (certs.size() > 0) {
		return (X509Certificate) certs.iterator().next();
	    } else {
		LOGGER.debug("No certificates found.  Falling back to CA certificate");
		keyUsage = new boolean[9];
		signingSelector.setKeyUsage(keyUsage);
		signingSelector.setBasicConstraints(0);

		certs = store.getCertificates(signingSelector);
		if (certs.size() > 0) {
		    return (X509Certificate) certs.iterator().next();
		} else {
		    throw new RuntimeException(
			    "No suitable certificate for verification");
		}
	    }
	} catch (CertStoreException e) {
	    throw new RuntimeException(e);
	}
    }

    private static X509Certificate selectEncryptionCertificate(CertStore store) {
	X509CertSelector signingSelector = new X509CertSelector();
	boolean[] keyUsage = new boolean[9];
	keyUsage[KEY_ENCIPHERMENT] = true;
	signingSelector.setKeyUsage(keyUsage);

	try {
	    LOGGER.debug("Selecting certificate with keyEncipherment keyUsage");
	    Collection<? extends Certificate> certs = store
		    .getCertificates(signingSelector);
	    if (certs.size() > 0) {
		return (X509Certificate) certs.iterator().next();
	    }

	    LOGGER.debug("No certificates found.  Selecting certificate with dataEncipherment keyUsage");
	    keyUsage = new boolean[9];
	    keyUsage[DATA_ENCIPHERMENT] = true;
	    signingSelector.setKeyUsage(keyUsage);

	    certs = store.getCertificates(signingSelector);
	    if (certs.size() > 0) {
		return (X509Certificate) certs.iterator().next();
	    }

	    LOGGER.debug("No certificates found.  Falling back to CA certificate");
	    keyUsage = new boolean[9];
	    signingSelector.setKeyUsage(keyUsage);
	    signingSelector.setBasicConstraints(0);

	    certs = store.getCertificates(signingSelector);
	    if (certs.size() > 0) {
		return (X509Certificate) certs.iterator().next();
	    } else {
		throw new RuntimeException(
			"No suitable certificate for encryption");
	    }
	} catch (CertStoreException e) {
	    throw new RuntimeException(e);
	}
    }
}