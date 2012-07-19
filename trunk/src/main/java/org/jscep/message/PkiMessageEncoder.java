/*
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
package org.jscep.message;

import java.io.IOException;
import java.security.PrivateKey;
import java.security.Provider;
import java.security.cert.CertificateEncodingException;
import java.security.cert.X509Certificate;
import java.util.Collection;
import java.util.Collections;

import org.bouncycastle.asn1.ASN1Object;
import org.bouncycastle.asn1.cms.AttributeTable;
import org.bouncycastle.cert.jcajce.JcaCertStore;
import org.bouncycastle.cms.CMSAttributeTableGenerator;
import org.bouncycastle.cms.CMSProcessableByteArray;
import org.bouncycastle.cms.CMSSignedData;
import org.bouncycastle.cms.CMSSignedDataGenerator;
import org.bouncycastle.cms.DefaultSignedAttributeTableGenerator;
import org.bouncycastle.cms.SignerInfoGenerator;
import org.bouncycastle.cms.jcajce.JcaSignerInfoGeneratorBuilder;
import org.bouncycastle.operator.jcajce.JcaContentSignerBuilder;
import org.bouncycastle.operator.jcajce.JcaDigestCalculatorProviderBuilder;
import org.bouncycastle.pkcs.PKCS10CertificationRequest;
import org.jscep.transaction.PkiStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PkiMessageEncoder {
    private static final Logger LOGGER = LoggerFactory
            .getLogger(PkiMessageEncoder.class);
    private final PrivateKey senderKey;
    private final X509Certificate senderCert;
    private final PkcsPkiEnvelopeEncoder encoder;

    public PkiMessageEncoder(PrivateKey priKey, X509Certificate sender,
            PkcsPkiEnvelopeEncoder enveloper) {
        this.senderKey = priKey;
        this.senderCert = sender;
        this.encoder = enveloper;
    }

    public byte[] encode(PkiMessage<?> message) throws MessageEncodingException {
        LOGGER.debug("Encoding message: {}", message);
        CMSProcessableByteArray signable;

        boolean hasMessageData = true;
        if (message instanceof PkiResponse<?>) {
            PkiResponse<?> response = (PkiResponse<?>) message;
            if (response.getPkiStatus() != PkiStatus.SUCCESS) {
                hasMessageData = false;
            }
        }
        if (hasMessageData) {
            byte[] ed;
            if (message.getMessageData() instanceof byte[]) {
                ed = encoder.encode((byte[]) message.getMessageData());
            } else if (message.getMessageData() instanceof PKCS10CertificationRequest) {
                try {
                    ed = encoder.encode(((PKCS10CertificationRequest) message
                            .getMessageData()).getEncoded());
                } catch (IOException e) {
                    throw new MessageEncodingException(e);
                }
            } else {
                try {
                    ed = encoder.encode(((ASN1Object) message.getMessageData())
                            .getEncoded());
                } catch (IOException e) {
                    throw new MessageEncodingException(e);
                }
            }
            signable = new CMSProcessableByteArray(ed);
        } else {
            signable = null;
        }

        AttributeTableFactory attrFactory = new AttributeTableFactory();
        AttributeTable signedAttrs = attrFactory.fromPkiMessage(message);
        Collection<X509Certificate> certColl = Collections
                .singleton(senderCert);
        JcaCertStore store;
        try {
            store = new JcaCertStore(certColl);
        } catch (CertificateEncodingException e) {
            throw new MessageEncodingException(e);
        }

        CMSSignedDataGenerator sdGenerator = new CMSSignedDataGenerator();
        LOGGER.debug("Signing message using key belonging to '{}'",
                senderCert.getSubjectDN());
        try {
            JcaSignerInfoGeneratorBuilder builder = new JcaSignerInfoGeneratorBuilder(
                    new JcaDigestCalculatorProviderBuilder().build());
            CMSAttributeTableGenerator signedGen = new DefaultSignedAttributeTableGenerator(
                    signedAttrs);
            builder.setSignedAttributeGenerator(signedGen);
            JcaContentSignerBuilder contentSignerBuilder = new JcaContentSignerBuilder(
                    "SHA1withRSA");
            SignerInfoGenerator infoGen = builder.build(
                    contentSignerBuilder.build(senderKey), senderCert);
            sdGenerator.addSignerInfoGenerator(infoGen);
            sdGenerator.addCertificates(store);
        } catch (Exception e) {
            throw new MessageEncodingException(e);
        }
        LOGGER.debug("Signing {} content", signable);
        CMSSignedData sd;
        try {
            sd = sdGenerator.generate("1.2.840.113549.1.7.1", signable, true,
                    (Provider) null, true);
            return sd.getEncoded();
        } catch (Exception e) {
            throw new MessageEncodingException(e);
        }
    }
}
