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
import java.util.function.Function;
import java.util.function.Supplier;

import com.oracle.bmc.ConfigFileReader;
import com.oracle.bmc.auth.AbstractAuthenticationDetailsProvider;
import com.oracle.bmc.auth.ConfigFileAuthenticationDetailsProvider;
import com.oracle.bmc.secrets.Secrets;
import com.oracle.bmc.secrets.SecretsClient;

public final class SecretsSuppliers {

    private SecretsSuppliers() {
        super();
    }

    public static Supplier<? extends Secrets> secrets() {
        return secrets(() -> {
                try {
                    return new ConfigFileAuthenticationDetailsProvider(ConfigFileReader.parseDefault());
                } catch (IOException ioException) {
                    throw new UncheckedIOException(ioException.getMessage(), ioException);
                }
            });
    }

    public static Supplier<? extends Secrets> secrets(AbstractAuthenticationDetailsProvider adp) {
        return secrets(() -> adp);
    }
    
    public static Supplier<? extends Secrets> secrets(Supplier<? extends AbstractAuthenticationDetailsProvider> adpSupplier) {
        return secrets(SecretsClient.builder()::build, adpSupplier);
    }
    
    public static Supplier<? extends Secrets> secrets(Function<? super AbstractAuthenticationDetailsProvider, ? extends Secrets> f,
                                                      Supplier<? extends AbstractAuthenticationDetailsProvider> adpSupplier) {
        Objects.requireNonNull(f, "f");
        Objects.requireNonNull(adpSupplier, "adpSupplier");
        return () -> f.apply(adpSupplier.get());
    }
  
}
