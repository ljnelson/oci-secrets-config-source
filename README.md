# OCI Secrets Config Source

## Status

Experimental and unsupported and not Oracle software.

## Gist

This project is a sort of compositional toolkit for assembling a
`org.eclipse.microprofile.config.spi.ConfigSource` that knows how to
[get a property value from an OCI
secret](https://docs.oracle.com/en-us/iaas/tools/java/latest/com/oracle/bmc/secrets/Secrets.html#getSecretBundle-com.oracle.bmc.secrets.requests.GetSecretBundleRequest-).

In your project:

1. Subclass `io.github.ljnelson.oci.secrets.configsource.SecretBundleConfigSource`.
2. Give it a `public` zero-argument constructor.
3. In that constructor, call `super()` with appropriate arguments.
   See
   `io.github.ljnelson.oci.secrets.configsource.SimpleSecretsSupplier`
   and
   `io.github.ljnelson.oci.secrets.configsource.SelectiveBuilderFunction`
   as examples.
4. Edit
   `src/main/resources/META-INF/services/org.eclipse.microprofile.config.spi.ConfigSource`
   to contain a line with the fully qualified name of your subclass.
5. Put your project's jar file on the runtime classpath.  It will now be used.

Because this toolkit is compositional, it's virtually impossible to
list all the possible ways you can use it.

## Structure

The actual `ConfigSource` implementation is
`SecretBundleConfigSource`.  It is not abstract, but it requires a
couple of constructor arguments, so (by design) cannot be used itself
as a `java.util.ServiceLoader`-compatible service provider.

This class takes a `Supplier` that knows how to return
`com.oracle.bmc.secrets.Secrets` instances.  It uses
`SimpleSecretsSupplier` if none is supplied.

It also takes a `Function` that, when supplied with a MicroProfile
Config property name, returns a configured
`GetSecretBundleRequest.Builder`, or `null` if the property name is
not handled.

Each of these two parameters may be implemented however you like.

The toolkit comes with the `SelectiveBuilderFunction` class, which
wraps a delegate builder function with a `Predicate`, so that the
delegate builder function is invoked only for valid property names
that are intended to be handled.

The toolkit also comes with a `ConfigurationBackedBuilderFunction`.
This uses MicroProfile Config, or any `BiFunction` capable of
returning an optional value when given a string and a type, to make a
`GetSecretBundleRequest.Builder` out of configuration values.  It may
be used, or it may not, depending on your use cases.

Here is a bare-bones example of a `ConfigSource` implementation built
from parts of this toolkit that will handle any MicroProfile Config
request for the `someSensitivePropertyName` property, provided that
there is an `~/.oci/config` file and a value for
`someSensitivePropertyName.secretId` set as well in the MicroProfile
Config universe:

```java
package example;

import io.github.ljnelson.oci.secrets.configsource.ConfigurationBackedBuilderFunction;
import io.github.ljnelson.oci.secrets.configsource.SecretBundleConfigSource;
import io.github.ljnelson.oci.secrets.configsource.SelectiveBuilderFunction;

public class ExampleSecretBundleConfigSource extends SecretBundleConfigSource {

  public ExampleSecretBundleConfigSource() {
    super(new SelectiveBuilderFunction("someSensitivePropertyName",
                                       new ConfigurationBackedBuilderFunction()));
  }

}
```
