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
import java.util.function.UnaryOperator;

import com.oracle.bmc.secrets.Secrets;
import com.oracle.bmc.secrets.model.SecretBundleContentDetails;
import com.oracle.bmc.secrets.requests.GetSecretBundleByNameRequest;
import com.oracle.bmc.secrets.requests.GetSecretBundleRequest;

public final class SecretBundleContentDetailsFunctions {

    private SecretBundleContentDetailsFunctions() {
        super();
    }

    @SuppressWarnings({"checkstyle:linelength"})
    public static Function<String, SecretBundleContentDetails> secretBundleContentDetailsByName(ConfigAccessor c,
                                                                                                Supplier<? extends GetSecretBundleByNameRequest.Builder> bs,
                                                                                                Supplier<? extends Secrets> ss) { // better be memoized
        return
            secretBundleContentDetailsByName(c,
                                             bs,
                                             UnaryOperator.identity(),
                                             r -> ss.get().getSecretBundleByName(r).getSecretBundle().getSecretBundleContent());
    }


    @SuppressWarnings({"checkstyle:linelength"})
    public static Function<String, SecretBundleContentDetails> secretBundleContentDetailsByName(ConfigAccessor c,
                                                                                                Supplier<? extends GetSecretBundleByNameRequest.Builder> bs,
                                                                                                UnaryOperator<GetSecretBundleByNameRequest.Builder> op,
                                                                                                Function<? super GetSecretBundleByNameRequest, ? extends SecretBundleContentDetails> f) {
        Objects.requireNonNull(c, "c");
        Objects.requireNonNull(bs, "bs");
        Objects.requireNonNull(op, "op");
        Objects.requireNonNull(f, "f");
        return propertyName -> {
            if (propertyName == null || propertyName.isBlank()) {
                return null;
            }
            var builder = bs.get(); // e.g. GetSecretBundleByNameRequest.builder();
            c.get(propertyName + ".opcRequestId", String.class).ifPresent(builder::opcRequestId);
            c.get(propertyName + ".secretName", String.class).ifPresentOrElse(builder::secretName, () -> builder.secretName(propertyName));
            c.get(propertyName + ".secretVersionName", String.class).ifPresent(builder::secretVersionName);
            c.get(propertyName + ".stage", GetSecretBundleByNameRequest.Stage.class).ifPresent(builder::stage);
            c.get(propertyName + ".vaultId", String.class).ifPresent(builder::vaultId);
            c.get(propertyName + ".versionNumber", Long.class).ifPresent(builder::versionNumber);
            return f.apply(op.apply(builder).build());
        };
    }

    @SuppressWarnings({"checkstyle:linelength"})
    public static Function<String, SecretBundleContentDetails> secretBundleContentDetailsById(ConfigAccessor c,
                                                                                              Supplier<? extends GetSecretBundleRequest.Builder> bs,
                                                                                              Supplier<? extends Secrets> ss) { // better be memoized
        return
            secretBundleContentDetailsById(c,
                                           bs,
                                           UnaryOperator.identity(),
                                           r -> ss.get().getSecretBundle(r).getSecretBundle().getSecretBundleContent());
    }

    @SuppressWarnings({"checkstyle:linelength"})
    public static Function<String, SecretBundleContentDetails> secretBundleContentDetailsById(ConfigAccessor c,
                                                                                              Supplier<? extends GetSecretBundleRequest.Builder> bs,
                                                                                              UnaryOperator<GetSecretBundleRequest.Builder> op,
                                                                                              Function<? super GetSecretBundleRequest, ? extends SecretBundleContentDetails> f) {
        Objects.requireNonNull(c, "c");
        Objects.requireNonNull(bs, "bs");
        Objects.requireNonNull(op, "op");
        Objects.requireNonNull(f, "f");
        return propertyName -> {
            if (propertyName == null || propertyName.isBlank()) {
                return null;
            }
            var builder = bs.get(); // e.g. GetSecretBundleRequest.builder();
            c.get(propertyName + ".opcRequestId", String.class).ifPresent(builder::opcRequestId);
            c.get(propertyName + ".secretId", String.class).ifPresent(builder::secretId);
            c.get(propertyName + ".secretVersionName", String.class).ifPresent(builder::secretVersionName);
            c.get(propertyName + ".stage", GetSecretBundleRequest.Stage.class).ifPresent(builder::stage);
            c.get(propertyName + ".versionNumber", Long.class).ifPresent(builder::versionNumber);
            return f.apply(op.apply(builder).build());
        };
    }

}
