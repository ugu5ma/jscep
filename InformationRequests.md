The following operations can be performed **outside** of a transaction:

### Determining Server Capabilities ###

Calling [getCaCapabilities()](http://jscep.googlecode.com/svn/trunk/documentation/com/google/code/jscep/client/Client.html#getCaCapabilities()) on your client instance returns a new instance of  [Capabilities](http://jscep.googlecode.com/svn/trunk/documentation/com/google/code/jscep/response/Capabilities.html), which summarises the configuration of the SCEP server.  The following information is exposed:

  * if the server supports certificate rollover
  * if the server supports sending requests using HTTP POST
  * if the server supports renewal of certificates
  * the strongest hash algorithm supported by the server, out of:
    * MD5
    * SHA-1
    * SHA-256
    * SHA-512
  * the strongest cipher supported by the server, out of:
    * DES
    * Triple DES

Inspecting the capabilities of the SCEP server should be regarded as a prerequisite for both certificate enrollment (to check if renewal is supported), and attempted retrieval of a roll-over certificate (to check if such a certificate exists).

**Technical Note**

> jSCEP checks (and caches) the capabilities of the server before many internal operations.  Where possible, jSCEP will always send requests using HTTP POST, and will always use the strongest hash algorithms and ciphers supported by the server.

> Performing a manual request for capabilities will always refresh the cache.

### Retrieve CA Certificate ###

Calling [getCaCertificate()](http://jscep.googlecode.com/svn/trunk/documentation/com/google/code/jscep/client/Client.html#getCaCertificate()) will retrieve the current CA certificate from the SCEP server.  If the SCEP server is acting as an RA, both the CA and RA certificate will be retrieved.  The CA will **always** be the **first** certificate in the list:

```
final List<X509Certificate> certs = client.getCaCertificate();
final X509Certificate ca = certs.get(0);
final X509Certificate ra;

if (certs.size() == 2) {
    X509Certificate ra = certs.get(1);
} else {
    X509Certificate ra = null;
}
```

Each time the CA certificate is retrieved, a fingerprint is generated and verified, using either the pre-provisioned fingerprint details provided by the user, or by passing a callback to the callback handler provided by the user.

**Technical Note**

> jSCEP will only request verification for a particular certificate **once**, and thereafter will cache the verification outcome.

### Retrieve Rollover CA Certificate ###

When a CA rolls over to a new certificate, it may choose to indicate this state change through its SCEP capabilities.  In such a case, [Capabilities.isRolloverSupported()](http://jscep.googlecode.com/svn/trunk/documentation/com/google/code/jscep/response/Capabilities.html#isRolloverSupported()) will return `true`, and you are free to retrieve the certificate like so:

```
if (client.getCaCapabilities().isRolloverSupported()) {
    client.getRolloverCertificate();
}
```

When this method is invoked, jSCEP will check the server capabilities to ensure that roll-over is supported.  If roll-over is **not** supported, the method will throw an [UnsupportedOperationException](http://java.sun.com/javase/6/docs/api/java/lang/UnsupportedOperationException.html).