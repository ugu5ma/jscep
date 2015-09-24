
```
SecureMessageObject = ContentInfo {
   contentType = signedData
   content = PkiMessage
}

PkiMessage = SignedData {
   version = 1
   digestAlgorithms = ...
   contentInfo = ContentInfo {
     contentType = data
     content = PkcsPkiEnvelope as OctetString
   } 
   certificates = ...
   crls = ...
   signerInfos = ...
}

PkcsPkiEnvelope = ContentInfo {
  contentType = envelopedData
  content = EnvelopedData {
  }
}
```