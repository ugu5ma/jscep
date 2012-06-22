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
package org.jscep.asn1;

import java.io.IOException;

import org.bouncycastle.asn1.ASN1Encodable;
import org.bouncycastle.asn1.ASN1EncodableVector;
import org.bouncycastle.asn1.ASN1InputStream;
import org.bouncycastle.asn1.ASN1Object;
import org.bouncycastle.asn1.ASN1Primitive;
import org.bouncycastle.asn1.ASN1Sequence;
import org.bouncycastle.asn1.DERSequence;
import org.bouncycastle.asn1.x500.X500Name;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class represents the SCEP <code>IssuerAndSubject</code> ASN.1 object.
 * <p/>
 * This object is defined by the following ASN.1 notation:
 * 
 * <pre>
 * IssuerAndSubject ::= SEQUENCE {
 *     issuer Name,
 *     subject Name,
 * }
 * </pre>
 * 
 * @author David Grant
 */
public class IssuerAndSubject extends ASN1Object {
	private static final Logger LOGGER = LoggerFactory.getLogger(IssuerAndSubject.class);
	private final X500Name issuer;
	private final X500Name subject;

	public IssuerAndSubject(ASN1Sequence seq) {
		issuer = X500Name.getInstance(seq.getObjectAt(0));
		subject = X500Name.getInstance(seq.getObjectAt(1));
	}

	public IssuerAndSubject(X500Name issuer, X500Name subject) {
		this.issuer = issuer;
		this.subject = subject;
	}

	public IssuerAndSubject(byte[] bytes) {
		this(toDERSequence(bytes));
	}

	public X500Name getIssuer() {
		return issuer;
	}

	public X500Name getSubject() {
		return subject;
	}

	@Override
	public ASN1Primitive toASN1Primitive() {
		ASN1EncodableVector v = new ASN1EncodableVector();

		v.add(issuer);
		v.add(subject);

		return new DERSequence(v);
	}

	public static IssuerAndSubject getInstance(ASN1Encodable encodable) {
		final ASN1Sequence seq = DERSequence.getInstance(encodable);

		return new IssuerAndSubject(seq);
	}

	private static ASN1Sequence toDERSequence(byte[] bytes) {
		ASN1InputStream dIn = null;
		try {
			dIn = new ASN1InputStream(bytes);

			return (ASN1Sequence) dIn.readObject();
		} catch (Exception e) {
			throw new IllegalArgumentException("badly encoded request");
		} finally {
			if (dIn != null) {
				try {
					dIn.close();
				} catch (IOException e) {
					LOGGER.error("Failed to close ASN.1 stream", e);
				}
			}
		}
	}
}
