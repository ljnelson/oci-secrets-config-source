# OCI Secrets ConfigSource

## Status

Experimental and unsupported and not Oracle software.

## Gist

This project is a sort of compositional toolkit for assembling a `org.eclipse.microprofile.config.spi.ConfigSource` that
knows how to [get a property value from an OCI
secret](https://docs.oracle.com/en-us/iaas/tools/java/latest/com/oracle/bmc/secrets/Secrets.html#getSecretBundle-com.oracle.bmc.secrets.requests.GetSecretBundleRequest-).

In your project:

1. Subclass `io.github.ljnelson.oci.secrets.configsource.AbstractSecretBundleConfigSource`.
2. Give your subclass a `public`, zero-argument constructor.
3. In that constructor, call `super()` with appropriate arguments.
4. Edit `src/main/resources/META-INF/services/org.eclipse.microprofile.config.spi.ConfigSource` to contain a line with
   the fully qualified name of your subclass.
5. Put your project's jar file on the runtime classpath.  It will now be used.

Because this toolkit is compositional, it's virtually impossible to list all the possible ways you can use it.

## Structure

The actual `ConfigSource` implementation is `AbstractSecretBundleConfigSource`.  It is not abstract, but it requires a
couple of constructor arguments, so (by design) cannot be used itself as a `java.util.ServiceLoader`-compatible service
provider.

This class takes a `Function` that knows how to return `com.oracle.bmc.secrets.model.SecretBundleContentDetails`
instances, when supplied with a property name whose value is stored in an OCI Vault.  Normally, of course, the innards
of this `Function` will use a `com.oracle.bmc.secrets.Secrets` to communicate with OCI, but that detail is deliberately
a property of the function and not exposed.

The class also takes a `Supplier` of `Secrets` which should expose any `Secrets` used by the supplied `Function` so that
the `Secrets` in question may be closed when the `AbstractSecretBundleConfigSource` is closed.  This `Supplier` can
return `null` in which case no closing will happen (not recommended).

Normally you put these two things together so that the `Secrets` used internally by the `Function` is the same one that
the supplied `Supplier` returns.  The `io.github.ljnelson.oci.secrets.configsource.Suppliers` class can help with
memoization.

Each of these two parameters may be implemented however you like.  The rest of the toolkit exists to make supplying
these two arguments relatively simple.

The toolkit comes with the `io.github.ljnelson.oci.secrets.configsource.Guards` utility class can be used to help an
`AbstractSecretBundleConfigSource` subclass figure out which configuration properties it actually wants to handle.

The toolkit comes with the `io.github.ljnelson.oci.secrets.configsource.ConfigAccessor` interface that can be used to
abstract over configuration systems. This is helpful primarily with testing so you don't have to set up a full
MicroProfile Config implementation or anything like that.  (It also has a static utility method that uses MicroProfile
Config of course.)

## Example

Here is a bare-bones example of a `ConfigSource` implementation built from parts of this toolkit that will handle any
MicroProfile Config request for the `someSensitivePropertyName` property, provided that there is an `~/.oci/config` file
and a value for `someSensitivePropertyName.secretId` set as well in the MicroProfile Config universe:

```java
package example;

import java.util.function.Supplier;

import com.oracle.bmc.secrets.Secrets;
import com.oracle.bmc.secrets.requests.GetSecretBundleByNameRequest;

import static io.github.ljnelson.oci.secrets.configsource.Guards.guardWithAcceptPattern;
import static io.github.ljnelson.oci.secrets.configsource.SecretBundleContentDetailsFunctions.secretBundleContentDetailsByName;
import static io.github.ljnelson.oci.secrets.configsource.SecretsSuppliers.secrets;

public class ExampleSecretBundleConfigSource extends AbstractSecretBundleConfigSource {

  public ExampleSecretBundleConfigSource() {
      this(ConfigAccessor.ofMicroProfileConfig(), secrets()); // memoized
  }

  private ExampleSecretBundleConfigSource(ConfigAccessor c, Supplier<? extends Secrets> ss) {
      super(guardWithAcceptPattern(secretBundleContentDetailsByName(c,
                                                                    GetSecretBundleByNameRequest::builder,
                                                                    ss),
                                   c),
            ss);
  }

}
```

If you edit `src/main/resources/META-INF/services/org.eclipse.microprofile.config.spi.ConfigSource` so that it contains a line like this:
```
example.ExampleSecretBundleConfigSource
```
…then the example above, when present in a jar file on the runtime
classpath, will be used by MicroProfile Config as just another config
source.

If you then set, for example, some system properties like this:
```shell
-DsomeSensitivePropertyName.vauleId=ocid1.vault.oc1.iad... # i.e. a valid OCID to a vault
-DsomeSensitivePropertyName.secretName=SOME_SENSITIVE_PROPERTY_NAME # i.e. a valid secret name in that vault
```
…then any time a developer or a library or any code anywhere does
this:
```java
String s = ConfigProvider.getConfig().getPropertyValue("someSensitivePropertyName", String.class);
```
…or this:
```java
@Inject
@ConfigProperty("someSensitivePropertyName")
private String s;
```
…she will get the value of the secret in your vault named
"`SOME_SENSITIVE_PROPERTY_NAME`".

The `.secretName` suffix (and other ones) is not magic; it is just a thing that a pre-canned `Function` supplied by
`io.github.ljnelson.oci.secrets.configsource.SecretBundleContentDetailsFunctions` happens to look for. You can write
your own function to do something different.
