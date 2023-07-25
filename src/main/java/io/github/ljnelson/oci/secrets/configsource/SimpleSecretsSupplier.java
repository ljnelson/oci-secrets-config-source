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

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.Objects;
import java.util.function.Supplier;

import com.oracle.bmc.ConfigFileReader;
import com.oracle.bmc.auth.AbstractAuthenticationDetailsProvider;
import com.oracle.bmc.auth.ConfigFileAuthenticationDetailsProvider;
import com.oracle.bmc.secrets.Secrets;
import com.oracle.bmc.secrets.SecretsClient;

/**
 * A {@link Supplier} of {@link Secrets} instances.
 *
 * <a href="https://about.me/lairdnelson/" target="_top">Laird Nelson</a>
 */
public final class SimpleSecretsSupplier implements Supplier<Secrets> {


    /*
     * Instance fields.
     */


    private final Supplier<? extends SecretsClient.Builder> builderSupplier;

    private final Supplier<? extends AbstractAuthenticationDetailsProvider> adpSupplier;


    /*
     * Constructors.
     */


    /**
     * Creates a new {@link SimpleSecretsSupplier}.
     *
     * @see #SimpleSecretsSupplier(Supplier, Supplier)
     *
     * @see ConfigFileAuthenticationDetailsProvider
     *
     * @see ConfigFileReader#parseDefault()
     *
     * @see SecretsClient#builder()
     */
    public SimpleSecretsSupplier() {
        this(SecretsClient::builder,
             () -> {
                 try {
                     return new ConfigFileAuthenticationDetailsProvider(ConfigFileReader.parseDefault());
                 } catch (IOException ioException) {
                     throw new UncheckedIOException(ioException.getMessage(), ioException);
                 }
             });
    }

    /**
     * Creates a new {@link SimpleSecretsSupplier}.
     *
     * @param adpSupplier a {@link Supplier} of {@link AbstractAuthenticationDetailsProvider} instances; must not be
     * {@code null}
     *
     * @exception NullPointerException if {@code adpSupplier} is {@code null}
     *
     * @see #SimpleSecretsSupplier(Supplier, Supplier)
     *
     * @see SecretsClient#builder()
     */
    public SimpleSecretsSupplier(Supplier<? extends AbstractAuthenticationDetailsProvider> adpSupplier) {
        this(SecretsClient::builder, adpSupplier);
    }

    /**
     * Creates a new {@link SimpleSecretsSupplier}.
     *
     * @param builderSupplier a {@link Supplier} of non-{@code null} {@link SecretsClient.Builder} instances; must not
     * be {@code null}
     *
     * @param adpSupplier a {@link Supplier} of {@link AbstractAuthenticationDetailsProvider} instances; must not be
     * {@code null}
     *
     * @exception NullPointerException if either argument is {@code null}
     */
    public SimpleSecretsSupplier(Supplier<? extends SecretsClient.Builder> builderSupplier,
                                 Supplier<? extends AbstractAuthenticationDetailsProvider> adpSupplier) {
        super();
        this.builderSupplier = Objects.requireNonNull(builderSupplier, "builderSupplier");
        this.adpSupplier = Objects.requireNonNull(adpSupplier, "adpSupplier");
    }


    /*
     * Instance methods.
     */


    /**
     * Returns a {@link Secrets} instance.
     *
     * <p>This method never returns {@code null}.</p>
     *
     * @return a non-{@code null} {@link Secrets} instance
     */
    @Override // Supplier
    public final Secrets get() {
        return this.builderSupplier.get().build(this.adpSupplier.get());
    }

}
