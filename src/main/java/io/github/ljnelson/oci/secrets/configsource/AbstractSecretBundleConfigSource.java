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

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;

import com.oracle.bmc.secrets.Secrets;
import com.oracle.bmc.secrets.model.Base64SecretBundleContentDetails;
import com.oracle.bmc.secrets.model.SecretBundleContentDetails;
import org.eclipse.microprofile.config.spi.ConfigSource;

@SuppressWarnings("try")
public class AbstractSecretBundleConfigSource implements AutoCloseable, ConfigSource {


    /*
     * Instance fields.
     */


    private final Function<? super String, ? extends String> base64Decoder;

    private final Function<? super String, ? extends SecretBundleContentDetails> f;

    private final Supplier<? extends Secrets> secretsSupplier; // memoized


    /*
     * Constructors.
     */


    public AbstractSecretBundleConfigSource(final Function<? super String, ? extends SecretBundleContentDetails> f,
                                            final Supplier<? extends Secrets> secretsSupplier) {
        this(f, secretsSupplier, null);
    }

    public AbstractSecretBundleConfigSource(final Function<? super String, ? extends SecretBundleContentDetails> f,
                                            final Supplier<? extends Secrets> secretsSupplier,
                                            final Function<? super String, ? extends String> base64Decoder) {
        super();
        this.f = Objects.requireNonNull(f, "f");
        this.secretsSupplier = Suppliers.memoizedSupplier(Objects.requireNonNull(secretsSupplier, "secretsSupplier"));
        this.base64Decoder =
            base64Decoder == null ? s -> s == null ? null : new String(Base64.getDecoder().decode(s), StandardCharsets.UTF_8) : base64Decoder;
    }


    /*
     * Instance methods.
     */


    /**
     * Closes this {@link AbstractSecretBundleConfigSource}.
     */
    @Override // AutoCloseable
    public void close() {
        AutoCloseable ac = this.secretsSupplier.get();
        if (ac != null) {
            try {
                ac.close();
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
     * Returns a name for this {@link AbstractSecretBundleConfigSource}.
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
     * Returns a {@link Map} representing a subset of properties which this {@link AbstractSecretBundleConfigSource} may
     * be capable of reproducing, or not.
     *
     * <p>This area of the specification is so underspecified as to be useless. Consequently the default implementation
     * of this method returns a value equal to that returned by an invocation of {@link Map#of()}.</p>
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
     * Returns a {@link Set} representing a subset of the property names for which this {@link
     * AbstractSecretBundleConfigSource} may or may not be capable of locating values.
     *
     * <p>This area of the specification is so underspecified as to be useless. Consequently the default implementation
     * of this method returns a value equal to that returned by an invocation of {@link Set#of()}.</p>
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
     * this implies that no property name may have a suitable value of {@code null} (which is but one of several
     * deficiencies of the MicroProfile Config specification)
     */
    @Override // ConfigSource
    public final String getValue(String propertyName) {
        return
            this.f.apply(propertyName) instanceof Base64SecretBundleContentDetails b64
            ? this.base64Decoder.apply(b64.getContent())
            : null;
    }

}
