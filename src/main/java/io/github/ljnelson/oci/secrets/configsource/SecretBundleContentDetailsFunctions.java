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

import java.util.Objects;
import java.util.function.Function;
import java.util.function.Supplier;

import com.oracle.bmc.secrets.Secrets;
import com.oracle.bmc.secrets.model.SecretBundleContentDetails;
import com.oracle.bmc.secrets.requests.GetSecretBundleByNameRequest;

import static io.github.ljnelson.oci.secrets.configsource.SecretsSuppliers.secrets;
import static io.github.ljnelson.oci.secrets.configsource.Suppliers.memoizedSupplier;

public final class SecretBundleContentDetailsFunctions {

    private SecretBundleContentDetailsFunctions() {
        super();
    }

    @SuppressWarnings({"checkstyle:linelength"})
    public static Function<String, SecretBundleContentDetails> secretBundleContentDetailsByName(ConfigAccessor c,
                                                                                                Supplier<? extends GetSecretBundleByNameRequest.Builder> bs,
                                                                                                Supplier<? extends Secrets> ss) { // better be memoized
        return secretBundleContentDetailsByName(c, bs, r -> ss.get().getSecretBundleByName(r).getSecretBundle().getSecretBundleContent());
    }


    @SuppressWarnings({"checkstyle:linelength"})
    public static Function<String, SecretBundleContentDetails> secretBundleContentDetailsByName(ConfigAccessor c,
                                                                                                Supplier<? extends GetSecretBundleByNameRequest.Builder> bs,
                                                                                                Function<? super GetSecretBundleByNameRequest, ? extends SecretBundleContentDetails> f) {
        Objects.requireNonNull(c, "c");
        Objects.requireNonNull(bs, "bs");
        Objects.requireNonNull(f, "f");
        return propertyName -> {
            if (propertyName == null || propertyName.isBlank()) {
                return null;
            }
            final GetSecretBundleByNameRequest.Builder builder = bs.get(); // GetSecretBundleByNameRequest.builder();
            c.get(propertyName + ".opcRequestId", String.class).ifPresent(builder::opcRequestId);
            c.get(propertyName + ".secretName", String.class).ifPresent(builder::secretName);
            c.get(propertyName + ".secretVersionName", String.class).ifPresent(builder::secretVersionName);
            c.get(propertyName + ".stage", GetSecretBundleByNameRequest.Stage.class).ifPresent(builder::stage);
            c.get(propertyName + ".vaultId", String.class).ifPresent(builder::vaultId);
            c.get(propertyName + ".versionNumber", Long.class).ifPresent(builder::versionNumber);
            return f.apply(builder.build());
        };
    }

    private static <T> T returnNull() {
        return null;
    }

}
