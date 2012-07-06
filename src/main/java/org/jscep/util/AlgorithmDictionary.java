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
package org.jscep.util;

import java.util.HashMap;
import java.util.Map;

import org.bouncycastle.asn1.DERObjectIdentifier;
import org.bouncycastle.asn1.nist.NISTObjectIdentifiers;
import org.bouncycastle.asn1.oiw.OIWObjectIdentifiers;
import org.bouncycastle.asn1.pkcs.PKCSObjectIdentifiers;
import org.bouncycastle.asn1.smime.SMIMECapabilities;
import org.bouncycastle.asn1.x509.AlgorithmIdentifier;
import org.bouncycastle.asn1.x509.X509ObjectIdentifiers;

/**
 * This class provides a utility to lookup a friendly name for an algorithm
 * given a particular OID or AlgorithmIdentifier.
 * <p/>
 * The internal dictionary is by no means comprehensive, and new algorithms are
 * generally as and when they are required by changes to the SCEP specification.
 * 
 * @author David Grant
 * @link 
 *       http://java.sun.com/javase/6/docs/technotes/guides/security/StandardNames
 *       .html
 */
public final class AlgorithmDictionary {
    private static final Map<DERObjectIdentifier, String> CONTENTS = new HashMap<DERObjectIdentifier, String>();

    static {
        // Asymmetric Ciphers
        CONTENTS.put(PKCSObjectIdentifiers.rsaEncryption, "RSA");
        // Digital Signatures
        CONTENTS.put(PKCSObjectIdentifiers.sha1WithRSAEncryption, "SHA1withRSA");
        CONTENTS.put(new DERObjectIdentifier("1.2.840.113549.1.1.4"),
                "md5withRSA");
        CONTENTS.put(new DERObjectIdentifier("1.2.840.113549.1.1.11"),
                "sha256withRSA");
        CONTENTS.put(new DERObjectIdentifier("1.2.840.113549.1.1.13"),
                "sha512withRSA");
        // Symmetric Ciphers
        CONTENTS.put(SMIMECapabilities.dES_CBC, "DES/CBC/PKCS5Padding"); // DES
        CONTENTS.put(SMIMECapabilities.dES_EDE3_CBC, "DESede/CBC/PKCS5Padding"); // DESEDE
        // Message Digests
        CONTENTS.put(X509ObjectIdentifiers.id_SHA1, "SHA");
        CONTENTS.put(new DERObjectIdentifier("1.2.840.113549.2.5"), "MD5");
        CONTENTS.put(new DERObjectIdentifier("2.16.840.1.101.3.4.2.1"),
                "SHA-256");
        CONTENTS.put(new DERObjectIdentifier("2.16.840.1.101.3.4.2.3"),
                "SHA-512");
    }

    private static final Map<String, DERObjectIdentifier> OIDS = new HashMap<String, DERObjectIdentifier>();

    static {
        // Cipher
        OIDS.put("DES/CBC/PKCS5Padding", OIWObjectIdentifiers.desCBC);
        OIDS.put("DESede/CBC/PKCS5Padding", PKCSObjectIdentifiers.des_EDE3_CBC);
        // KeyFactory or KeyPairGenerator
        OIDS.put("RSA", PKCSObjectIdentifiers.rsaEncryption);
        // KeyGenerator, AlgorithmParameters or SecretKeyFactory
        OIDS.put("DES", null);
        OIDS.put("DESede", null);
        // MessageDigest
        OIDS.put("MD5", PKCSObjectIdentifiers.md5);
        OIDS.put("SHA-1", X509ObjectIdentifiers.id_SHA1);
        OIDS.put("SHA-256", NISTObjectIdentifiers.id_sha256);
        OIDS.put("SHA-512", NISTObjectIdentifiers.id_sha512);
        // Signature
        OIDS.put("MD5withRSA", PKCSObjectIdentifiers.md5WithRSAEncryption);
        OIDS.put("SHA1withRSA", PKCSObjectIdentifiers.sha1WithRSAEncryption);
        OIDS.put("SHA256withRSA", PKCSObjectIdentifiers.sha256WithRSAEncryption);
        OIDS.put("SHA512withRSA", PKCSObjectIdentifiers.sha512WithRSAEncryption);
    }

    private AlgorithmDictionary() {
        // This constructor will never be invoked.
    }

    /**
     * Returns the cipher part of the provided transformation.
     * 
     * @param transformation
     *            the transformation, e.g. "DES/CBC/PKCS5Padding"
     * @return the cipher, e.g. "DES"
     */
    public static String fromTransformation(String transformation) {
        return transformation.split("/")[0];
    }

    /**
     * Returns the name of the given algorithm.
     * 
     * @param alg
     *            the algorithm to look up.
     * @return the algorithm name.
     */
    public static String lookup(AlgorithmIdentifier alg) {
        return CONTENTS.get(alg.getAlgorithm());
    }
}
