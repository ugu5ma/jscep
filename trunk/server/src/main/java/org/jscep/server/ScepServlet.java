/*
 * Copyright (c) 2009-2010 David Grant
 * Copyright (c) 2010 ThruPoint Ltd
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

package org.jscep.server;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.Writer;
import java.math.BigInteger;
import java.security.GeneralSecurityException;
import java.security.PrivateKey;
import java.security.cert.CertStore;
import java.security.cert.CertStoreException;
import java.security.cert.CertStoreParameters;
import java.security.cert.Certificate;
import java.security.cert.CollectionCertStoreParameters;
import java.security.cert.X509CRL;
import java.security.cert.X509Certificate;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;

import javax.security.auth.x500.X500Principal;
import javax.servlet.ServletException;
import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.bouncycastle.asn1.ASN1Encodable;
import org.bouncycastle.asn1.ASN1Object;
import org.bouncycastle.asn1.cms.ContentInfo;
import org.bouncycastle.asn1.cms.IssuerAndSerialNumber;
import org.bouncycastle.asn1.cms.SignedData;
import org.bouncycastle.asn1.pkcs.CertificationRequest;
import org.bouncycastle.asn1.pkcs.PKCSObjectIdentifiers;
import org.bouncycastle.asn1.x509.X509Name;
import org.bouncycastle.cms.CMSException;
import org.bouncycastle.cms.CMSSignedData;
import org.bouncycastle.cms.CMSSignedDataGenerator;
import org.bouncycastle.util.encoders.Base64;
import org.jscep.asn1.IssuerAndSubject;
import org.jscep.message.CertRep;
import org.jscep.message.PkcsPkiEnvelopeDecoder;
import org.jscep.message.PkcsPkiEnvelopeEncoder;
import org.jscep.message.PkiMessage;
import org.jscep.message.PkiMessageDecoder;
import org.jscep.message.PkiMessageEncoder;
import org.jscep.request.Operation;
import org.jscep.response.Capability;
import org.jscep.transaction.FailInfo;
import org.jscep.transaction.MessageType;
import org.jscep.transaction.Nonce;
import org.jscep.transaction.OperationFailureException;
import org.jscep.transaction.TransactionId;
import org.jscep.util.LoggingUtil;

public abstract class ScepServlet extends HttpServlet {
	private final static String GET = "GET";
	private final static String POST = "POST";
	private final static String MSG_PARAM = "message";
	private final static String OP_PARAM = "operation";
	private static Logger LOGGER = LoggingUtil.getLogger(ScepServlet.class);
	/**
	 * Serialization ID
	 */
	private static final long serialVersionUID = 1L;
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public void service(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
		LOGGER.entering(getClass().getName(), "service");
		
		byte[] body = getMessageBytes(req);
		
		final Operation op;
		try {
			op = getOperation(req);
			if (op == null) {
				// The operation parameter must be set.
				
				res.setStatus(HttpServletResponse.SC_BAD_REQUEST);
				Writer writer = res.getWriter();
				writer.write("Missing \"operation\" parameter.");
				writer.flush();
			
				return;
			}
		} catch (IllegalArgumentException e) {
			// The operation was not recognised.
			
			res.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			Writer writer = res.getWriter();
			writer.write("Invalid \"operation\" parameter.");
			writer.flush();
		
			return;
		}
		
		LOGGER.fine("Incoming Operation: " + op);
		
		final String reqMethod = req.getMethod();
			
		if (op == Operation.PKIOperation) {
			if (reqMethod.equals(POST) == false && reqMethod.equals(GET) == false) {
				// PKIOperation must be sent using GET or POST
			
				res.setStatus(HttpServletResponse.SC_METHOD_NOT_ALLOWED);
				res.addHeader("Allow", GET + ", " + POST);
				
				return;
			}
		} else {
			if (reqMethod.equals(GET) == false) {
				// Operations other than PKIOperation must be sent using GET
				
				res.setStatus(HttpServletResponse.SC_METHOD_NOT_ALLOWED);
				res.addHeader("Allow", GET);
				
				return;
			}
		}
		
		LOGGER.fine("Method " + reqMethod + " Allowed for Operation: " + op);
		
		if (op == Operation.GetCACaps) {
			doGetCaCaps(req, res);
		} else if (op == Operation.GetCACert) {
			try {
				doGetCaCert(req, res);
			} catch (GeneralSecurityException e) {
				throw new ServletException(e);
			} catch (CMSException e) {
				throw new ServletException(e);
			}
		} else if (op == Operation.GetNextCACert) {
			try {
				doGetNextCaCert(req, res);
			} catch (GeneralSecurityException e) {
				throw new ServletException(e);
			} catch (CMSException e) {
				throw new ServletException(e);
			}
		} else if (op == Operation.PKIOperation) {
			// PKIOperation
			
			res.setHeader("Content-Type", "application/x-pki-message");

			CMSSignedData sd;
			try {
				sd = new CMSSignedData(body);
			} catch (CMSException e) {
				throw new ServletException(e);
			}
			
			CertStore reqStore;
			try {
				reqStore = sd.getCertificatesAndCRLs("Collection", (String) null);
			} catch (GeneralSecurityException e) {
				throw new ServletException(e);
			} catch (CMSException e) {
				throw new ServletException(e);
			}
			Collection<? extends Certificate> reqCerts;
			try {
				reqCerts = reqStore.getCertificates(null);
			} catch (CertStoreException e) {
				throw new ServletException(e);
			}
			X509Certificate reqCert = (X509Certificate) reqCerts.iterator().next();
			
			PkcsPkiEnvelopeDecoder envDecoder = new PkcsPkiEnvelopeDecoder(getPrivate());
			PkiMessageDecoder decoder = new PkiMessageDecoder(envDecoder);
			PkiMessage<? extends ASN1Encodable> msg = decoder.decode(sd);
			
			MessageType msgType = msg.getMessageType();
			ASN1Encodable msgData = msg.getMessageData();
			
			Nonce senderNonce = Nonce.nextNonce();
			TransactionId transId = msg.getTransactionId();
			Nonce recipientNonce = msg.getSenderNonce();
			CertRep certRep;
			
			if (msgType == MessageType.GetCert) {
				final IssuerAndSerialNumber iasn = (IssuerAndSerialNumber) msgData;
				final X509Name principal = iasn.getName();
				final BigInteger serial = iasn.getSerialNumber().getValue();

				try {
					List<X509Certificate> issued = doGetCert(principal, serial);
					certRep = new CertRep(transId, senderNonce, recipientNonce, FailInfo.badCertId);
					CertStoreParameters params = new CollectionCertStoreParameters(issued);
					CertStore store = CertStore.getInstance("Collection", params);
					SignedData messageData = getMessageData(store);
					
					certRep = new CertRep(transId, senderNonce, recipientNonce, messageData);
				} catch (OperationFailureException e) {
					certRep = new CertRep(transId, senderNonce, recipientNonce, e.getFailInfo());
				} catch (GeneralSecurityException e) {
					throw new ServletException(e);
				} catch (CMSException e) {
					throw new ServletException(e);
				}
			} else if (msgType == MessageType.GetCertInitial) {
				final IssuerAndSubject ias = (IssuerAndSubject) msgData;
				final X509Name issuer = ias.getIssuer();
				final X509Name subject = ias.getSubject();

				try {
					List<X509Certificate> issued = doGetCertInitial(issuer, subject);
					
					if (issued.size() == 0) {
						certRep = new CertRep(transId, senderNonce, recipientNonce);
					} else {
						CertStoreParameters params = new CollectionCertStoreParameters(issued);
						CertStore store = CertStore.getInstance("Collection", params);
						SignedData messageData = getMessageData(store);
						
						certRep = new CertRep(transId, senderNonce, recipientNonce, messageData);
					}
				}  catch (OperationFailureException e) {
					certRep = new CertRep(transId, senderNonce, recipientNonce, e.getFailInfo());
				}catch (GeneralSecurityException e) {
					throw new ServletException(e);
				} catch (CMSException e) {
					throw new ServletException(e);
				}
			} else if (msgType == MessageType.GetCRL) {
				final IssuerAndSerialNumber iasn = (IssuerAndSerialNumber) msgData;
				final X500Principal issuer = new X500Principal(iasn.getName().getDEREncoded());
				final BigInteger serialNumber = iasn.getSerialNumber().getValue();

				try {
					X509CRL crl = doGetCrl(issuer, serialNumber);
					CertStoreParameters params = new CollectionCertStoreParameters(Collections.singleton(crl));
					CertStore store = CertStore.getInstance("Collection", params);
					SignedData messageData = getMessageData(store);
					
					certRep = new CertRep(transId, senderNonce, recipientNonce, messageData);
				} catch (OperationFailureException e) {
					certRep = new CertRep(transId, senderNonce, recipientNonce, e.getFailInfo());
				} catch (GeneralSecurityException e) {
					throw new ServletException(e);
				} catch (CMSException e) {
					throw new ServletException(e);
				}
			} else if (msgType == MessageType.PKCSReq) {
				final CertificationRequest certReq = (CertificationRequest) msgData;
				
				try {
					List<X509Certificate> issued = doEnroll(certReq);
					
					if (issued.size() == 0) {
						certRep = new CertRep(transId, senderNonce, recipientNonce);
					} else {
						CertStoreParameters params = new CollectionCertStoreParameters(issued);
						CertStore store = CertStore.getInstance("Collection", params);
						SignedData messageData = getMessageData(store);
						
						certRep = new CertRep(transId, senderNonce, recipientNonce, messageData);
					}
				} catch (OperationFailureException e) {
					 certRep = new CertRep(transId, senderNonce, recipientNonce, e.getFailInfo());
				} catch (GeneralSecurityException e) {
					throw new ServletException(e);
				} catch (CMSException e) {
					throw new ServletException(e);
				}
			} else {
				throw new ServletException("Unknown Message for Operation");
			}
			
			PkcsPkiEnvelopeEncoder envEncoder = new PkcsPkiEnvelopeEncoder(reqCert);
			PkiMessageEncoder encoder = new PkiMessageEncoder(getPrivate(), getSender(), envEncoder);
			CMSSignedData signedData = encoder.encode(certRep);
			byte[] resBytes = signedData.getEncoded();
			
			res.getOutputStream().write(resBytes);
			res.getOutputStream().close();
		} else {
			res.sendError(HttpServletResponse.SC_BAD_REQUEST, "Unknown Operation");
		}
		LOGGER.exiting(getClass().getName(), "service");
	}
	
	private SignedData getMessageData(CertStore store) throws GeneralSecurityException, CMSException, IOException {
		CMSSignedDataGenerator generator = new CMSSignedDataGenerator();
		generator.addCertificatesAndCRLs(store);
		
		CMSSignedData cmsMessageData = generator.generate(null, (String) null);
		ContentInfo cmsContentInfo = ContentInfo.getInstance(ASN1Object.fromByteArray(cmsMessageData.getEncoded()));
		
		return SignedData.getInstance(cmsContentInfo.getContent());
	}

	private void doGetNextCaCert(HttpServletRequest req, HttpServletResponse res) throws GeneralSecurityException, CMSException, IOException {
		res.setHeader("Content-Type", "application/x-x509-next-ca-cert");
		
		final List<X509Certificate> certs = getNextCaCertificate(req.getParameter(MSG_PARAM));
		
		if (certs.size() == 0) {
			res.sendError(HttpServletResponse.SC_NOT_IMPLEMENTED, "GetNextCACert Not Supported");
		} else {
			CertStore store = CertStore.getInstance("Collection", new CollectionCertStoreParameters(certs));
			CMSSignedDataGenerator generator = new CMSSignedDataGenerator();
			generator.addCertificatesAndCRLs(store);
			generator.addSigner(getPrivate(), getSender(), PKCSObjectIdentifiers.sha1WithRSAEncryption.getId());
			CMSSignedData degenerateSd = generator.generate(null, (String) null);
			byte[] bytes = degenerateSd.getEncoded();
			
			res.getOutputStream().write(bytes);
			res.getOutputStream().close();
		}
	}

	private void doGetCaCert(HttpServletRequest req, HttpServletResponse res) throws GeneralSecurityException, CMSException, IOException {
		final List<X509Certificate> certs = doGetCaCertificate(req.getParameter(MSG_PARAM));
		final byte[] bytes;
		if (certs.size() == 1) {
			res.setHeader("Content-Type", "application/x-x509-ca-cert");
			bytes = certs.get(0).getEncoded();
		} else {
			res.setHeader("Content-Type", "application/x-x509-ca-ra-cert");
			CertStore store = CertStore.getInstance("Collection", new CollectionCertStoreParameters(certs));
			CMSSignedDataGenerator generator = new CMSSignedDataGenerator();
			generator.addCertificatesAndCRLs(store);
			CMSSignedData degenerateSd = generator.generate(null, (String) null);
			bytes = degenerateSd.getEncoded();
		}
		
		res.getOutputStream().write(bytes);
		res.getOutputStream().close();
	}
	
	private Operation getOperation(HttpServletRequest req) {
		String op = req.getParameter(OP_PARAM);
		if (op == null)
		{
			return null;
		}
		return Operation.valueOf(req.getParameter(OP_PARAM));
	}
	
	private void doGetCaCaps(HttpServletRequest req, HttpServletResponse res) throws IOException {
		res.setHeader("Content-Type", "text/plain");
		final Set<Capability> caps = doCapabilities(req.getParameter("message"));
		for (Capability cap : caps) {
			res.getWriter().write(cap.toString());
			res.getWriter().write('\n');
		}
		res.getWriter().close();
	}
	
	/**
	 * Returns the capabilities of the specified CA.
	 * 
	 * @param identifier the CA.
	 * @return the capabilities.
	 */
	abstract protected Set<Capability> doCapabilities(String identifier);
	/**
	 * Returns the certificate of the specified CA.
	 * 
	 * @param identifier the CA.
	 * @return the CA's certificate.
	 */
	abstract protected List<X509Certificate> doGetCaCertificate(String identifier);
	/**
	 * Return the next X.509 certificate which will be used by
	 * the specified CA.
	 * 
	 * @param identifier the CA. 
	 * @return the list of certificates.
	 */
	abstract protected List<X509Certificate> getNextCaCertificate(String identifier);
	/**
	 * Retrieve the certificate identified by the given parameters.
	 * 
	 * @param issuer the issuer name.
	 * @param serial the serial number.
	 * @return the identified certificate, if any.
	 * @throws OperationFailureException if the operation cannot be completed
	 */
	abstract protected List<X509Certificate> doGetCert(X509Name issuer, BigInteger serial) throws OperationFailureException;
	/**
	 * Get Cert Initial
	 * <p>
	 * This method should return an empty list to represent a pending request.
	 * 
	 * @param issuer the issuer name.
	 * @param subject the subject name.
	 * @return the identified certificate, if any.
	 * @throws OperationFailureException if the operation cannot be completed
	 */
	abstract protected List<X509Certificate> doGetCertInitial(X509Name issuer, X509Name subject) throws OperationFailureException;
	/**
	 * Retrieve the CRL covering the given certificate identifiers.
	 * 
	 * @param issuer the certificate issuer.
	 * @param serial the certificate serial number.
	 * @return the CRL.
	 * @throws OperationFailureException if the operation cannot be completed
	 */
	abstract protected X509CRL doGetCrl(X500Principal issuer, BigInteger serial) throws OperationFailureException;
	/**
	 * Enroll a certificate into the PKI
	 * <p>
	 * This method should return an empty list to represent a pending request.
	 * 
	 * @param certificationRequest the PKCS #10 CertificationRequest
	 * @return the certificate chain.
	 * @throws OperationFailureException if the operation cannot be completed
	 */
	abstract protected List<X509Certificate> doEnroll(CertificationRequest certificationRequest) throws OperationFailureException;
	abstract protected PrivateKey getPrivate();
	abstract protected X509Certificate getSender();
	
	private byte[] getBody(ServletInputStream servletIn) throws IOException {
		final ByteArrayOutputStream baos = new ByteArrayOutputStream();
		
		int b;
		while ((b = servletIn.read()) != -1) {
			baos.write(b);
		
		}
		baos.close();
		
		return baos.toByteArray();
	}
	
	private byte[] getMessageBytes(HttpServletRequest req) throws IOException {
		if (req.getMethod().equals(POST)) {
			return getBody(req.getInputStream());
		} else {
			Operation op = getOperation(req);
			
			if (op == Operation.PKIOperation) {
				String msg = req.getParameter(MSG_PARAM);
				if (msg.isEmpty()) {
					return new byte[0];
				}
				return Base64.decode(msg);
			} else {
				return new byte[0];
			}
		}
	}
}
