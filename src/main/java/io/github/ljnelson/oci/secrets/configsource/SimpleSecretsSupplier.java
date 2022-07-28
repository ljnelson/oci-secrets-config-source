/*
 * Copyright © 2022 Laird Nelson.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
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

public final class SimpleSecretsSupplier implements Supplier<Secrets> {

    private final Supplier<? extends SecretsClient.Builder> builderSupplier;

    private final Supplier<? extends AbstractAuthenticationDetailsProvider> adpSupplier;

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

    public SimpleSecretsSupplier(Supplier<? extends AbstractAuthenticationDetailsProvider> adpSupplier) {
        this(SecretsClient::builder, adpSupplier);
    }

    public SimpleSecretsSupplier(Supplier<? extends SecretsClient.Builder> builderSupplier,
                                 Supplier<? extends AbstractAuthenticationDetailsProvider> adpSupplier) {
        super();
        this.builderSupplier = Objects.requireNonNull(builderSupplier, "builderSupplier");
        this.adpSupplier = Objects.requireNonNull(adpSupplier, "adpSupplier");
    }

    @Override // Supplier
    public final Secrets get() {
        return this.builderSupplier.get().build(this.adpSupplier.get());
    }

}
