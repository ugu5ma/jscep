/*
 * Copyright (c) 2009-2010 David Grant
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

package com.google.code.jscep.operations;

import java.io.IOException;
import java.security.GeneralSecurityException;

import org.bouncycastle.asn1.ASN1Encodable;

import com.google.code.jscep.transaction.MessageType;

/**
 * This interface defines the common structure of a <tt>SCEP</tt> <tt>pkiMessage</tt>
 * 
 * @see <a href="http://tools.ietf.org/html/draft-nourse-scep-20#section-3.1">SCEP Internet-Draft Reference</a>
 */
public interface PkiOperation<T extends ASN1Encodable> {
	/**
	 * Returns the message type for this operation.
	 * 
	 * @return the message type.
	 */
	MessageType getMessageType();
	/**
	 * Returns the message data for this operation.
	 * 
	 * @return the message data.
	 * @throws IOException if any I/O error occurs.
	 * @throws GeneralSecurityException if any security error occurs.
	 */
    T getMessageData() throws IOException;
}
