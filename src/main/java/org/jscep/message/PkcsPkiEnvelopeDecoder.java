package org.jscep.message;

import static org.slf4j.LoggerFactory.getLogger;

import java.security.PrivateKey;
import java.security.cert.X509Certificate;

import org.bouncycastle.asn1.cms.EnvelopedData;
import org.bouncycastle.cms.CMSEnvelopedData;
import org.bouncycastle.cms.CMSException;
import org.bouncycastle.cms.RecipientInformation;
import org.bouncycastle.cms.RecipientInformationStore;
import org.bouncycastle.cms.jcajce.JceKeyTransEnvelopedRecipient;
import org.bouncycastle.cms.jcajce.JceKeyTransRecipient;
import org.bouncycastle.cms.jcajce.JceKeyTransRecipientId;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.slf4j.Logger;

/**
 * This class is used to decrypt the <tt>pkcsPkiEnvelope</tt> of a SCEP secure
 * message object and extract the <tt>messageData</tt> from within.
 * 
 * @see PkcsPkiEnvelopeEncoder
 */
public final class PkcsPkiEnvelopeDecoder {
	private static final Logger LOGGER = getLogger(PkcsPkiEnvelopeDecoder.class);
	private final X509Certificate recipient;
	private final PrivateKey priKey;

	/**
	 * Creates a <tt>PkcsPkiEnveloperDecoder</tt> for the provided certificate
	 * and key.
	 * <p>
	 * The provided certificate is used to identify the envelope recipient info,
	 * which is then used with the key unwrapped using the provided key.
	 * 
	 * @param recipient
	 *            the entity for whom the message was enveloped.
	 * @param priKey
	 *            the key to unwrap the symmetric encrypting key.
	 */
	public PkcsPkiEnvelopeDecoder(X509Certificate recipient, PrivateKey priKey) {
		this.recipient = recipient;
		this.priKey = priKey;
	}

	/**
	 * Decrypts the provided <tt>pkcsPkiEnvelope</tt>, and extracts the content.
	 * 
	 * @param pkcsPkiEnvelope
	 *            the envelope to decrypt and open.
	 * @return the content of the <tt>pkcsPkiEnvelope</tt>, the SCEP
	 *         <tt>messageData</tt>.
	 * @throws MessageDecodingException
	 *             if the envelope cannot be decoded.
	 */
	public byte[] decode(CMSEnvelopedData pkcsPkiEnvelope)
			throws MessageDecodingException {
		LOGGER.debug("Decoding pkcsPkiEnvelope");
		validate(pkcsPkiEnvelope);

		LOGGER.debug(
				"Decrypting pkcsPkiEnvelope using key belonging to [issuer={}; serial={}]",
				recipient.getIssuerDN(), recipient.getSerialNumber());
		final RecipientInformationStore recipientInfos = pkcsPkiEnvelope
				.getRecipientInfos();
		RecipientInformation info = recipientInfos
				.get(new JceKeyTransRecipientId(recipient));

		if (info == null) {
			throw new MessageDecodingException(
					"Missing expected key transfer recipient");
		}

		LOGGER.debug("pkcsPkiEnvelope encryption algorithm: {}", info
				.getKeyEncryptionAlgorithm().getAlgorithm());

		try {
			JceKeyTransRecipient recipient = new JceKeyTransEnvelopedRecipient(priKey);
			recipient.setProvider(new BouncyCastleProvider());
			
			byte[] messageData = info.getContent(recipient);
			LOGGER.debug("Finished decoding pkcsPkiEnvelope");
			return messageData;
		} catch (CMSException e) {
			throw new MessageDecodingException(e);
		}
	}

	private void validate(CMSEnvelopedData pkcsPkiEnvelope) {
		EnvelopedData ed = EnvelopedData.getInstance(pkcsPkiEnvelope
				.toASN1Structure().getContent());
		LOGGER.debug("pkcsPkiEnvelope version: {}", ed.getVersion());
		LOGGER.debug("pkcsPkiEnvelope encryptedContentInfo contentType: {}", ed
				.getEncryptedContentInfo().getContentType());
	}
}
