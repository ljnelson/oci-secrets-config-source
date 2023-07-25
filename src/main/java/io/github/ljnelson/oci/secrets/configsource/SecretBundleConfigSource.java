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
package io.github.ljnelson.oci.secrets.configsource;

import java.lang.invoke.MethodHandles;
import java.lang.invoke.VarHandle;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Supplier;

import com.oracle.bmc.secrets.Secrets;
import com.oracle.bmc.secrets.model.Base64SecretBundleContentDetails;
import com.oracle.bmc.secrets.requests.GetSecretBundleRequest;
import org.eclipse.microprofile.config.spi.ConfigSource;

/**
 * A {@link ConfigSource} implementation that returns certain <a href="https://github.com/eclipse/microprofile-config"
 * target="_top">MicroProfile Config</a> property values from an <a
 * href="https://docs.oracle.com/en-us/iaas/tools/java/latest/com/oracle/bmc/secrets/Secrets.html#getSecretBundle-com.oracle.bmc.secrets.requests.GetSecretBundleRequest-">OCI
 * vault secret</a>.
 *
 * @author <a href="https://about.me/lairdnelson/" target="_top">Laird Nelson</a>
 *
 * @see #getValue(String)
 */
public class SecretBundleConfigSource implements AutoCloseable, ConfigSource {


    /*
     * Static fields.
     */


    private static final VarHandle SECRETS;

    static {
        try {
            SECRETS = MethodHandles.lookup().findVarHandle(SecretBundleConfigSource.class, "secrets", Secrets.class);
        } catch (final NoSuchFieldException | IllegalAccessException reflectiveOperationException) {
            throw new ExceptionInInitializerError(reflectiveOperationException);
        }
    }


    /*
     * Instance fields.
     */


    private volatile Secrets secrets;

    private final Supplier<? extends Secrets> secretsSupplier;

    private final Function<? super String, ? extends GetSecretBundleRequest.Builder> builderFunction;


    /*
     * Constructors.
     */


    /**
     * Creates a new {@link SecretBundleConfigSource}.
     *
     * @param builderFunction a {@link Function} that, when given a property name, returns either a fully configured
     * {@link GetSecretBundleRequest.Builder} or {@code null} if the property is not handled
     *
     * @exception NullPointerException if {@code builderFunction} is {@code null}
     *
     * @see #SecretBundleConfigSource(Supplier, Function)
     */
    public SecretBundleConfigSource(Function<? super String, ? extends GetSecretBundleRequest.Builder> builderFunction) {
        this(new SimpleSecretsSupplier(), builderFunction);
    }

    /**
     * Creates a new {@link SecretBundleConfigSource}.
     *
     * @param secretsSupplier a {@link Supplier} that returns {@link Secrets} instances; must not be {@code null}
     *
     * @param builderFunction a {@link Function} that, when given a property name, returns either a fully configured
     * {@link GetSecretBundleRequest.Builder} or {@code null} if the property is not handled
     *
     * @exception NullPointerException if either argument is {@code null}
     *
     * @see SimpleSecretsSupplier
     *
     * @see SelectiveBuilderFunction
     */
    public SecretBundleConfigSource(Supplier<? extends Secrets> secretsSupplier,
                                    Function<? super String, ? extends GetSecretBundleRequest.Builder> builderFunction) {
        super();
        this.secretsSupplier = Objects.requireNonNull(secretsSupplier, "secretsSupplier");
        this.builderFunction = Objects.requireNonNull(builderFunction, "builderFunction");
    }


    /*
     * Instance methods.
     */


    /**
     * Closes this {@link SecretBundleConfigSource}.
     */
    @Override // AutoCloseable
    public final void close() {
        Secrets secrets = this.secrets; // volatile read
        if (secrets != null) {
            try {
                secrets.close();
            } catch (RuntimeException runtimeException) {
                throw runtimeException;
            } catch (Exception exception) {
                if (exception instanceof InterruptedException) {
                    Thread.currentThread().interrupt();
                }
                throw new IllegalStateException(exception.getMessage(), exception);
            }
        }
    }

    /**
     * Returns a name for this {@link SecretBundleConfigSource}.
     *
     * <p>The default implementation of this method returns a valud as if computed by {@link Class#getName()
     * this.getClass().getName()}.</p>
     *
     * <p>This method does not, and overrides of this method must not, return {@code null}.</p>
     *
     * <p>This method is, and overrides of this method must be, safe for concurrent use by multiple threads.</p>
     *
     * @return a non-{@code null} name
     *
     * @see ConfigSource#getName()
     */
    @Override // ConfigSource
    public String getName() {
        return this.getClass().getName();
    }

    /**
     * Returns a {@link Map} representing properties which this {@link SecretBundleConfigSource} may be capable of
     * reproducing, or not.
     *
     * <p>This area of the specification is so underspecified as to be useless.  Consequently the default implementation
     * of this method returns a value equal to that computed by an invocation of {@link Map#of()}.</p>
     *
     * <p>Subclasses may feel free to override this method to do almost anything since the specification permits all
     * possible behaviors.</p>
     *
     * <p>This method does not, and overrides of this method must not, return {@code null}.</p>
     *
     * <p>This method is, and overrides of this method must be, safe for concurrent use by multiple threads.</p>
     *
     * @return a non-{@code null}, immutable {@link Map}
     */
    @Override // ConfigSource
    public Map<String, String> getProperties() {
        return Map.of();
    }

    /**
     * Returns a {@link Set} representing property names for which this {@link SecretBundleConfigSource} may or may not
     * be capable of locating values.
     *
     * <p>This area of the specification is so underspecified as to be useless.  Consequently the default implementation
     * of this method returns a value equal to that computed by an invocation of {@link Set#of()}.</p>
     *
     * <p>Subclasses may feel free to override this method to do almost anything since the specification permits all
     * possible behaviors.</p>
     *
     * <p>This method does not, and overrides of this method must not, return {@code null}.</p>
     *
     * <p>This method is, and overrides of this method must be, safe for concurrent use by multiple threads.</p>
     *
     * @return a non-{@code null}, immutable {@link Set}
     */
    @Override // ConfigSource
    public Set<String> getPropertyNames() {
        return Set.of();
    }

    /**
     * Returns a value for the supplied {@code propertyName}, or {@code null} if there is no such value at the moment of
     * invocation.
     *
     * <p>This method is safe for concurrent use by multiple threads.</p>
     *
     * @param propertyName the name of the property; may be {@code null}
     *
     * @return a suitable value, or {@code null} if there is no value suitable for the supplied {@code propertyName};
     * this implies that no property name may have a suitable value of {@code null} which is but one of several
     * deficiencies of the MicroProfile Config specification
     *
     * @see #SecretBundleConfigSource(Supplier. Function)
     */
    @Override // ConfigSource
    public final String getValue(String propertyName) {
        GetSecretBundleRequest.Builder builder = this.builderFunction.apply(propertyName);
        if (builder != null
            && this.secrets().getSecretBundle(builder.build())
                   .getSecretBundle()
                   .getSecretBundleContent() instanceof Base64SecretBundleContentDetails b64) {
            return new String(Base64.getDecoder().decode(b64.getContent()), StandardCharsets.UTF_8);
        }
        return null;
    }

    private Secrets secrets() {
        Secrets secrets = this.secrets; // volatile read
        if (secrets == null) {
            secrets = this.secretsSupplier.get();
            if (!SECRETS.compareAndSet(this, null, Objects.requireNonNull(secrets, "secretsSupplier.get() == null"))) { // volatile write
                return this.secrets; // volatile read
            }
        }
        return secrets;
    }

}
