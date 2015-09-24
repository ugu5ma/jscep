## About ##

The Simple Certificate Enrollment Protocol is a PKI communication protocol which leverages existing technology by using [PKCS#7](http://www.rsa.com/rsalabs/node.asp?id=2129) and [PKCS#10](http://www.rsa.com/rsalabs/node.asp?id=2132) over HTTP.  SCEP is the evolution of the enrollment protocol developed by VeriSign, Inc. for Cisco Systems, Inc.

## Latest Internet Draft ##

http://tools.ietf.org/html/draft-nourse-scep

## Overview ##

### Actors ###

  1. Requester or Client
  1. CA

### Steps ###

  1. Requester sends **GetCACert** message to SCEP URL
  1. Either CA or RA responds with single DER-encoded X.509 certificate DER-encoded 'degenerate' PKCS#7 SignedData message with an X.509 certificate chain (CA -> RA)
  1. Requester checks if CA certificate is trusted by prompting user with message digest of X.509 certificate
  1. Requester constructs a PKCS#10 certificate signing request (CSR)
  1. Requester constructs a PKCS#7 EnvelopedData object using the DER-encoded CSR and encrypts the envelope encryption key (DES or Triple-DES) using the message recipient's public key (either the CA, or an RA with a keyEncipherment KeyUsage extension)
  1. Requester constructs a PKCS#7 SignedData using the DER-encoded EnvelopedData and its certificate, and signs a (MD5, SHA-1, SHA-256 or SHA-512) digest of the data using its RSA private key
  1. Requester sends the DER-encoded SignedData object in a **PKCSReq** message to the RA (or CA)
  1. RA (or CA) opens the SignedData message and extracts the EnvelopedData.
  1. RA uses its private key to decrypt the EnvelopedData and extracts the PKCS#10 CSR
  1. RA examines the challengePassword in CSR to authenticate request
  1. RA sends CSR to CA
  1. CA generates X.509 certificate from CSR and signs it
  1. CA sends signed-certificate to RA
  1. RA creates a 'degenerate' SignedData and adds signed-certificate
  1. RA creates a EnvelopedData with the DER-encoded SignedData, encrypting using the Requesters public key (provided in the SignedData)
  1. RA creates a SignedData object and signs using the RA key with a digitalSignature KeyUsage extension
  1. RA sends the message to the Requester