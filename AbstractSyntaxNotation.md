| **Message** | **Message Data** |
|:------------|:-----------------|
| PKCSReq     | CertificationRequest |
| CertRep     | SignedData       |
| GetCertInitial | IssuerAndSubject |
| GetCert     | IssuerAndSerial  |
| GetCRL      | IssuerAndSerial  |

```
SEQUENCE [ContentInfo]
  // contentType
  OBJECT IDENTIFIER pkcs-7 signedData
  // content
  [0] 
    // pkiMessage
    SEQUENCE [SignedData]
      // version
      INTEGER 1
      // digestAlgorithms
      SET
        // algorithmIdentifier
        SEQUENCE
          // algorithm
          OBJECT IDENTIFIER 1.3.14.3.2.26 (sha1)
          // parameters
          NULL
      // contentInfo
      SEQUENCE
        // contentType
        OBJECT IDENTIFIER pkcs-7 data
        // content
        SEQUENCE
          // contentType
          OBJECT IDENTIFIER pkcs-7 envelopedData
          // content
          [0]
            // pkcsPkiEnvelope
            SEQUENCE [EnvelopedData]
              // version
              INTEGER 0
              // recipientInfos [RecipientInfos]
              SET
                // RecipientInfo
                SEQUENCE
                  // version
                  INTEGER 0
                  // issuerAndSerialNumber
                  SEQUENCE
                    // issuer [Name]
                    SEQUENCE
                      // RelativeDistinguishedName
                      SET
                        // AttributeTypeAndDistinguishedValue
                        SEQUENCE
                        // type
                        OBJECT IDENTIFIER id-at-commonName
                        // value
                        PrintableString ?
                    // serialNumber
                    INTEGER ?
                  // keyEncryptionAlgorithm
                  OBJECT IDENTIFIER 1.2.840.113549.1.1.1 (rsa)
                  // encryptedKey
                  OCTET STRING
              // encryptedContentInfo [EncryptedContentInfo]
              SEQUENCE
                // contentType
                OBJECT IDENTIFIER pkcs-7 data
                // contentEncryptionAlgorithm
                SEQUENCE
                  // algorithm
                  OBJECT IDENTIFIER 1.2.840.113549.3.7
                  // parameters (IV)
                  OCTET STRING
                // encryptedContent
                OCTET STRING
      // certificates
      // crls
      // signerInfos (SignerInfos)
      SET
        // signerInfo (SignerInfo)
        SEQUENCE
          // version
          INTEGER 1
          // issuerAndSerialNumber
          SEQUENCE
            // issuer (Name)
            SEQUENCE
              // RelativeDistinguishedName
              SET
                // AttributeTypeAndDistinguishedValue
                SEQUENCE
                  // type
                  OBJECT IDENTIFIER id-at-commonName
                  // value
                  PrintableString ?
            // serialNumber (CertificateSerialNumber)
            INTEGER ?
          // digestAlgorithm (DigestAlgorithmIdentifier)
          SEQUENCE
            // algorithm
            OBJECT IDENTIFIER 1.3.14.3.2.26 (sha1)
            // parameters
            NULL
          // authenticatedAttributes
          [0]
            // Attribute
            SEQUENCE
              // type
              OBJECT IDENTIFIER pkcs-9 contentType
              // values
              SET
                // ContentType
                OBJECT IDENTIFIER pkcs-7 data
            // Attribute
            SEQUENCE
              // type
              OBJECT IDENTIFIER pkcs-9 signingTime
              // values
              SET
                // SigningTime
                UTCTime ?
            // Attribute
            SEQUENCE
              // type
              OBJECT IDENTIFIER pkcs-9 messageDigest
              // values
              SET
                // MessageDigest
                OCTET STRING
          // digestEncryptionAlgorithm (DigestEncryptionAlgorithmIdentifier)
          SEQUENCE
            // algorithm
            OBJECT IDENTIFIER 1.2.840.113549.1.1.1 (RSA)
            // parameters
            NULL
          // unauthenticatedAttributes
          // encryptedDigest (EncryptedDigest)
          OCTET STRING
```