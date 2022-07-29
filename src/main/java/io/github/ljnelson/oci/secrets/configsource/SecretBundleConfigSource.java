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

import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodHandles.Lookup;
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

public class SecretBundleConfigSource implements ConfigSource {

    private static final VarHandle SECRETS;

    static {
        Lookup lookup = MethodHandles.lookup();
        try {
            SECRETS = lookup.findVarHandle(SecretBundleConfigSource.class, "secrets", Secrets.class);
        } catch (final NoSuchFieldException | IllegalAccessException reflectiveOperationException) {
            throw new ExceptionInInitializerError(reflectiveOperationException);
        }
    }

    private volatile Secrets secrets;

    private final Supplier<? extends Secrets> secretsSupplier;

    private final Function<? super String, ? extends GetSecretBundleRequest.Builder> builderFunction;

    public SecretBundleConfigSource(Function<? super String, ? extends GetSecretBundleRequest.Builder> builderFunction) {
        this(new SimpleSecretsSupplier(), builderFunction);
    }
    
    public SecretBundleConfigSource(Supplier<? extends Secrets> secretsSupplier,
                                    Function<? super String, ? extends GetSecretBundleRequest.Builder> builderFunction) {
        super();
        this.secretsSupplier = Objects.requireNonNull(secretsSupplier, "secretsSupplier");
        this.builderFunction = Objects.requireNonNull(builderFunction, "builderFunction");
    }

    @Override // ConfigSource
    public String getName() {
        return this.getClass().getName();
    }

    @Override // ConfigSource
    public Map<String, String> getProperties() {
        return Map.of();
    }

    @Override // ConfigSource
    public Set<String> getPropertyNames() {
        return Set.of();
    }

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
