

# Introduction #

**jscep** is an open-source Java implementation of the [Simple Certificate Enrollment Protocol](http://tools.ietf.org/html/draft-nourse-scep) (SCEP).

Find out more **[about jscep](Features.md)**, how to **[use it](CompiledPackages.md)**, read the **[documentation](ClientUsage.md)**, **[get support](Support.md)** or find out how you can **[get involved](GettingInvolved.md)**.

# Usage Instructions #

## Client ##

To construct a client, you'll need the following items:

  * the URL of your SCEP server
  * an RSA key pair
  * a certificate
  * a callback handler to verify the identity of the CA
  * a profile identifier (optional)

The URL is simple enough to build, and should be provided by you network administrator, or PKI administrator:

```
URL url = new URL("http://example.org/scep/pkiclient.exe");
```

Note: it's completely acceptable to use an IP address in your URL.  In fact, any HTTP or HTTPS URL will be accepted.

### Constructing an RSA Key Pair ###

If you're _renewing_ a certificate rather than enrolling a new entity, you'll need to use the key pair associated with that certificate.  In the following example, we're enrolling a new entity, so we create a _new_ key pair.

You'll need to keep a reference to the key pair for storing it alongside your enrolled certificate in a [KeyStore](http://docs.oracle.com/javase/7/docs/api/java/security/KeyStore.html).

```
KeyPair keyPair = KeyPairGenerator.getInstance("RSA").generateKeyPair();
```

**Note:** You are permitted to reuse an existing key pair for enrolling a new entity.

### Constructing a Certificate ###

If you're _renewing_, you must use your existing certificate.  Otherwise, you can use an ephemeral certificate, as demonstrated below:

```
X500Principal entityName = new X500Principal("CN=localhost.localdomain");
X509Certificate entity = X509Util.createEphemeralCertificate(entityName, keyPair);
```

The SCEP server will use the certificate to verify the signature on SCEP secure message objects, so it is **essential** that the certificate public key is paired with the private key you provide to the client.

### Constructing a Callback Handler ###

The `CallbackHandler` parameter requires a [CallbackHandler](http://docs.oracle.com/javase/7/docs/api/javax/security/auth/callback/CallbackHandler.html) that handles a [CertificateVerificationCallback](https://jscep.ci.cloudbees.com/job/jscep/site/apidocs/org/jscep/CertificateVerificationCallback.html).

If your application does not require additional callbacks to be handled, you can simply use the [DefaultCallbackHandler](https://jscep.ci.cloudbees.com/job/jscep/site/apidocs/org/jscep/client/DefaultCallbackHandler.html), which delegates verification to an instance of [CertificateVerifier](https://jscep.ci.cloudbees.com/job/jscep/site/apidocs/org/jscep/client/CertificateVerifier.html).

Supplied implementations include verifiers which:

  * verify through your system [console](https://jscep.ci.cloudbees.com/job/jscep/site/apidocs/org/jscep/client/ConsoleCertificateVerifier.html)
  * use a known [message digest](https://jscep.ci.cloudbees.com/job/jscep/site/apidocs/org/jscep/client/MessageDigestCertificateVerifier.html)
  * use a [pre-provisioned certificate](https://jscep.ci.cloudbees.com/job/jscep/site/apidocs/org/jscep/client/PreProvisionedCertificateVerifier.html)

There are a couple of test verifiers too:

  * [verify always](https://jscep.ci.cloudbees.com/job/jscep/site/apidocs/org/jscep/client/OptimisticCertificateVerifier.html)
  * [verify never](https://jscep.ci.cloudbees.com/job/jscep/site/apidocs/org/jscep/client/PessimisticCertificateVerifier.html)

```
CallbackHandler handler = new DefaultCallbackHandler(new ConsoleCertificateVerifier());
```

See the [org.jscep.client](https://jscep.ci.cloudbees.com/job/jscep/site/apidocs/org/jscep/client/package-summary.html) package for more details.

### Profile ###

### Creating a Certificate Signing Request with Bouncy Castle ###

```
JcaContentSignerBuilder csb = new JcaContentSignerBuilder("SHA1withRSA");
ContentSigner cs = csb.build(keyPair.getPrivate());

PKCS10CertificationRequestBuilder crb = new JcaPKCS10CertificationRequestBuilder(entity, keyPair.getPublic());

DERPrintableString password = new DERPrintableString("secret");
crb.addAttribute(PKCSObjectIdentifiers.pkcs_9_at_challengePassword, password);
PKCS10CertificationRequest csr = crb.build(cs);
```

## Server ##

# Quick Start #

The following example shows how to use **jscep** to create a SCEP client to automatically enrol with the SCEP server at  `example.org`.

```
// The identity of the SCEP client
X509Certificate client = ...;
// The RSA private key of the SCEP client
PrivateKey priKey = ...;
// URL used by the SCEP server at example.org
URL url = new URL("http://example.org/scep/pkiclient.exe");
// Callback handler to check the CA certificate
CallbackHandler handler = ...;

// Construct the client
Client client = new Client(url, client, priKey, handler);

// The certification request to send to the SCEP server
CertificationRequest csr = ...;

// Send the enrolment request
EnrolmentTransaction txn = client.enrol(csr);

// Automatic enrolment, so this should be issued
if (State.CERT_ISSUED == txn.send()) {
    // Retrieve the certificate from the store
    CertStore store = txn.getCertStore();
    Certificate cert = store.getCertficates(null).iterator().next();
}
```

# Thanks #

Thanks to all those [people who have contributed](Contributors.md) to **jscep** in some form, plus the following companies:

[![](http://code.google.com/images/code_logo.gif)](http://code.google.com/hosting/)

[![](http://web-static-cloudfront.s3.amazonaws.com/images/badges/BuiltOnDEV.png)](http://www.cloudbees.com/foss/index.cb)

[![](http://www.sonarsource.org/wp-content/themes/sonarsource.org/images/sonar.png)](http://www.sonarsource.org/)

[![](http://www.jetbrains.com/img/logos/logo_jetbrains_small.gif)](http://www.jetbrains.com/idea/)

[![](http://nexus.sonatype.org/images/JIRAsonatype.jpg)](http://www.sonatype.com/)