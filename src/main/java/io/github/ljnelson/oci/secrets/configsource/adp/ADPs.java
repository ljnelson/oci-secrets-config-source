/*
 * Copyright © 2022–2023 Laird Nelson.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations under the License.
 */
package io.github.ljnelson.oci.secrets.configsource.adp;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.ConnectException;
import java.net.InetAddress;
import java.net.URI;
import java.net.UnknownHostException;
import java.nio.file.NoSuchFileException;
import java.nio.file.Paths;
import java.util.Optional;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;
import java.util.stream.Stream;

import com.oracle.bmc.Region;
import com.oracle.bmc.auth.BasicAuthenticationDetailsProvider;
import com.oracle.bmc.auth.ConfigFileAuthenticationDetailsProvider;
import com.oracle.bmc.auth.InstancePrincipalsAuthenticationDetailsProvider.InstancePrincipalsAuthenticationDetailsProviderBuilder;
import com.oracle.bmc.auth.InstancePrincipalsAuthenticationDetailsProvider;
import com.oracle.bmc.auth.ResourcePrincipalAuthenticationDetailsProvider.ResourcePrincipalAuthenticationDetailsProviderBuilder;
import com.oracle.bmc.auth.ResourcePrincipalAuthenticationDetailsProvider;
import com.oracle.bmc.auth.SimpleAuthenticationDetailsProvider.SimpleAuthenticationDetailsProviderBuilder;
import com.oracle.bmc.auth.SimpleAuthenticationDetailsProvider;
import com.oracle.bmc.auth.SimplePrivateKeySupplier;
import com.oracle.bmc.auth.StringPrivateKeySupplier;
import io.github.ljnelson.oci.secrets.configsource.ConfigAccessor;

import static com.oracle.bmc.auth.AbstractFederationClientAuthenticationDetailsProviderBuilder.METADATA_SERVICE_BASE_URL;

public final class ADPs {

    private static final class IMDS {
        private static final String DEFAULT_HOSTNAME = URI.create(METADATA_SERVICE_BASE_URL).getHost();
        private IMDS() {}
    }

    private static final String OCI_AUTH_FINGERPRINT = "oci.auth.fingerprint";

    private static final String OCI_AUTH_PASSPHRASE = "oci.auth.passphrase"; // optional for simple

    private static final String OCI_AUTH_PRIVATE_KEY = "oci.auth.private-key"; // optional for simple

    private static final String OCI_AUTH_REGION = "oci.auth.region";

    private static final String OCI_AUTH_TENANT_ID = "oci.auth.tenant-id";

    private static final String OCI_AUTH_USER_ID = "oci.auth.user-id";

    private ADPs() {
        super();
    }

    public static final Optional<Supplier<SimpleAuthenticationDetailsProvider>> simple() {
        return simple(ConfigAccessor.ofMicroProfileConfig());
    }

    public static final Optional<Supplier<SimpleAuthenticationDetailsProvider>> simple(ConfigAccessor c) {
        return simple(c, SimpleAuthenticationDetailsProvider::builder, UnaryOperator.identity());
    }

    @SuppressWarnings("checkstyle:linelength")
    public static final Optional<Supplier<SimpleAuthenticationDetailsProvider>>
        simple(ConfigAccessor c,
               Supplier<? extends SimpleAuthenticationDetailsProviderBuilder> bs,
               UnaryOperator<SimpleAuthenticationDetailsProviderBuilder> op) {
        return
            c.get(OCI_AUTH_FINGERPRINT, String.class)
            .flatMap(fingerprint -> c.get(OCI_AUTH_REGION, Region.class)
                     .flatMap(region -> c.get(OCI_AUTH_TENANT_ID, String.class)
                              .flatMap(tenantId -> c.get(OCI_AUTH_USER_ID, String.class)
                                       .map(userId -> {
                                               SimpleAuthenticationDetailsProviderBuilder b = bs.get();
                                               b.fingerprint(fingerprint);
                                               b.region(region);
                                               b.tenantId(tenantId);
                                               b.userId(userId);
                                               c.get(OCI_AUTH_PASSPHRASE, String.class).ifPresent(b::passPhrase);
                                               c.get(OCI_AUTH_PRIVATE_KEY, String.class)
                                                   .ifPresentOrElse(pk -> b.privateKeySupplier(new StringPrivateKeySupplier(pk)),
                                                                    () -> b.privateKeySupplier(new SimplePrivateKeySupplier(c.get(OCI_AUTH_PRIVATE_KEY + "-path",
                                                                                                                                  String.class)
                                                                                                                            .orElse(c.get("oci.auth.keyFile",
                                                                                                                                          String.class)
                                                                                                                                    .orElse(Paths.get(System.getProperty("user.home"),
                                                                                                                                                      ".oci",
                                                                                                                                                      "oci_api_key.pem")
                                                                                                                                            .toString())))));
                                               return op.apply(b)::build;
                                           }))));
    }

    public static final Optional<Supplier<ConfigFileAuthenticationDetailsProvider>> configFile() {
        return configFile(ConfigAccessor.ofMicroProfileConfig());
    }

    public static final Optional<Supplier<ConfigFileAuthenticationDetailsProvider>> configFile(ConfigAccessor c) {
        String ociConfigProfile = c.get("oci.config.profile", String.class).orElse("DEFAULT");
        String ociConfigPath = c.get("oci.config.path", String.class).orElse(null);
        return configFile(ociConfigPath, ociConfigProfile);
    }

    public static final Optional<Supplier<ConfigFileAuthenticationDetailsProvider>> configFile(String ociConfigPath,
                                                                                               String ociConfigProfile) {
        if (ociConfigProfile == null) {
            ociConfigProfile = "DEFAULT";
        }
        ConfigFileAuthenticationDetailsProvider adp;
        try {
            if (ociConfigPath == null) {
                adp = new ConfigFileAuthenticationDetailsProvider(ociConfigProfile);
            } else {
                adp = new ConfigFileAuthenticationDetailsProvider(ociConfigPath, ociConfigProfile);
            }
        } catch (FileNotFoundException | NoSuchFileException e) {
            return Optional.empty();
        } catch (IOException e) {
            // The underlying ConfigFileReader that does the real work does not throw a FileNotFoundException in this
            // case (as it probably should). We have no choice but to parse the error message. See
            // https://github.com/oracle/oci-java-sdk/blob/v3.21.0/bmc-common/src/main/java/com/oracle/bmc/ConfigFileReader.java#L91-L95.
            String message = e.getMessage();
            if (message != null
                && message.startsWith("Can't load the default config from ")
                && message.endsWith(" because it does not exist or it is not a file.")) {
                return Optional.empty();
            }
            throw new UncheckedIOException(message, e);
        }
        return Optional.of(() -> adp);
    }

    public static final Optional<Supplier<InstancePrincipalsAuthenticationDetailsProvider>> instancePrincipals() {
        return instancePrincipals(ConfigAccessor.ofMicroProfileConfig());
    }

    public static final Optional<Supplier<InstancePrincipalsAuthenticationDetailsProvider>> instancePrincipals(ConfigAccessor c) {
        return instancePrincipals(c, InstancePrincipalsAuthenticationDetailsProvider::builder, UnaryOperator.identity());
    }

    public static final Optional<Supplier<InstancePrincipalsAuthenticationDetailsProvider>>
        instancePrincipals(ConfigAccessor c,
                           Supplier<? extends InstancePrincipalsAuthenticationDetailsProviderBuilder> bs,
                           UnaryOperator<InstancePrincipalsAuthenticationDetailsProviderBuilder> op) {
        InetAddress imds = null;
        try {
            imds = InetAddress.getByName(c.get("oci.imds.hostname", String.class).orElse(IMDS.DEFAULT_HOSTNAME));
        } catch (UnknownHostException unknownHostException) {
            throw new UncheckedIOException(unknownHostException.getMessage(), unknownHostException);
        }
        int ociImdsTimeoutMillis = 100;
        try {
            ociImdsTimeoutMillis =
                Math.max(0, c.get("oci.imds.timeout.milliseconds", Integer.class).orElse(Integer.valueOf(100)));
        } catch (IllegalArgumentException conversionException) {
        }
        return instancePrincipals(imds, ociImdsTimeoutMillis, bs, op);
    }

    public static final Optional<Supplier<InstancePrincipalsAuthenticationDetailsProvider>>
        instancePrincipals(InetAddress imds,
                           int timeoutMillis,
                           Supplier<? extends InstancePrincipalsAuthenticationDetailsProviderBuilder> bs,
                           UnaryOperator<InstancePrincipalsAuthenticationDetailsProviderBuilder> op) {
        try {
            if (!imds.isReachable(timeoutMillis)) {
                return Optional.empty();
            }
        } catch (ConnectException e) {
            return Optional.empty();
        } catch (IOException ioException) {
            throw new UncheckedIOException(ioException.getMessage(), ioException);
        }
        return Optional.of(op.apply(bs.get())::build);
    }

    public static final Optional<Supplier<ResourcePrincipalAuthenticationDetailsProvider>> resourcePrincipal() {
        return resourcePrincipal(ResourcePrincipalAuthenticationDetailsProvider::builder, UnaryOperator.identity());
    }

    public static final Optional<Supplier<ResourcePrincipalAuthenticationDetailsProvider>>
        resourcePrincipal(Supplier<? extends ResourcePrincipalAuthenticationDetailsProviderBuilder> bs,
                          UnaryOperator<ResourcePrincipalAuthenticationDetailsProviderBuilder> op) {
        return
            System.getenv("OCI_RESOURCE_PRINCIPAL_VERSION") == null ? Optional.empty() : Optional.of(op.apply(bs.get())::build);
    }

    public static final Supplier<? extends BasicAuthenticationDetailsProvider> adp() {
        return adp(ConfigAccessor.ofMicroProfileConfig());
    }

    public static final Supplier<? extends BasicAuthenticationDetailsProvider> adp(ConfigAccessor c) {
        return adp(c,
                   SimpleAuthenticationDetailsProvider::builder,
                   UnaryOperator.identity(),
                   InstancePrincipalsAuthenticationDetailsProvider::builder,
                   UnaryOperator.identity(),
                   ResourcePrincipalAuthenticationDetailsProvider::builder,
                   UnaryOperator.identity());
    }

    public static final Supplier<? extends BasicAuthenticationDetailsProvider>
        adp(ConfigAccessor c,
            Supplier<? extends SimpleAuthenticationDetailsProviderBuilder> simpleBs,
            UnaryOperator<SimpleAuthenticationDetailsProviderBuilder> simpleOp,
            Supplier<? extends InstancePrincipalsAuthenticationDetailsProviderBuilder> instanceBs,
            UnaryOperator<InstancePrincipalsAuthenticationDetailsProviderBuilder> instanceOp,
            Supplier<? extends ResourcePrincipalAuthenticationDetailsProviderBuilder> resourceBs,
            UnaryOperator<ResourcePrincipalAuthenticationDetailsProviderBuilder> resourceOp) {
        return
            Stream.of(simple(c, simpleBs, simpleOp),
                      configFile(c),
                      instancePrincipals(c, instanceBs, instanceOp),
                      resourcePrincipal(resourceBs, resourceOp))
            .flatMap(Optional::stream)
            .findFirst()
            .get();
    }

}