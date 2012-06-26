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
import java.security.cert.CertificateEncodingException;
import java.security.cert.X509Certificate;

import org.bouncycastle.asn1.pkcs.PKCSObjectIdentifiers;
import org.bouncycastle.cert.crmf.CRMFException;
import org.bouncycastle.cert.crmf.jcajce.JceCRMFEncryptorBuilder;
import org.bouncycastle.cms.CMSEnvelopedData;
import org.bouncycastle.cms.CMSEnvelopedDataGenerator;
import org.bouncycastle.cms.CMSException;
import org.bouncycastle.cms.CMSProcessableByteArray;
import org.bouncycastle.cms.CMSTypedData;
import org.bouncycastle.cms.RecipientInfoGenerator;
import org.bouncycastle.cms.jcajce.JceKeyTransRecipientInfoGenerator;
import org.bouncycastle.operator.OutputEncryptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PkcsPkiEnvelopeEncoder {
    private static final Logger LOGGER = LoggerFactory
            .getLogger(PkcsPkiEnvelopeEncoder.class);
    private final X509Certificate recipient;

    public PkcsPkiEnvelopeEncoder(X509Certificate recipient) {
        this.recipient = recipient;
    }

    public byte[] encode(byte[] payload) throws IOException {
        LOGGER.debug("Encrypting message: {}", payload);

        CMSEnvelopedDataGenerator edGenerator = new CMSEnvelopedDataGenerator();
        CMSTypedData envelopable = new CMSProcessableByteArray(payload);
        RecipientInfoGenerator recipientGenerator;
        try {
            recipientGenerator = new JceKeyTransRecipientInfoGenerator(
                    recipient);
        } catch (CertificateEncodingException e) {
            IOException ioe = new IOException();
            ioe.initCause(e);

            throw ioe;
        }
        edGenerator.addRecipientInfoGenerator(recipientGenerator);
        LOGGER.debug("Encrypting session key using key belonging to '{}'",
                recipient.getSubjectDN());

        OutputEncryptor encryptor;
        try {
            encryptor = new JceCRMFEncryptorBuilder(
                    PKCSObjectIdentifiers.des_EDE3_CBC).build();
        } catch (CRMFException e) {
            IOException ioe = new IOException();
            ioe.initCause(e);

            throw ioe;
        }
        CMSEnvelopedData data;
        try {
            data = edGenerator.generate(envelopable, encryptor);
        } catch (CMSException e) {
            IOException ioe = new IOException();
            ioe.initCause(e);

            throw ioe;
        }
        LOGGER.debug("Encrypted to: {}", data.getEncoded());
        return data.getEncoded();
    }
}
