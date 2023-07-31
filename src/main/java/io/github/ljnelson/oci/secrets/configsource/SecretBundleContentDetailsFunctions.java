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
            var b = bs.get(); // e.g. GetSecretBundleByNameRequest.b();
            c.get(propertyName + ".opcRequestId", String.class).ifPresent(b::opcRequestId);
            c.get(propertyName + ".secretName", String.class).ifPresentOrElse(b::secretName, () -> b.secretName(propertyName));
            c.get(propertyName + ".secretVersionName", String.class).ifPresent(b::secretVersionName);
            c.get(propertyName + ".stage", GetSecretBundleByNameRequest.Stage.class).ifPresent(b::stage);
            c.get(propertyName + ".vaultId", String.class).ifPresent(b::vaultId);
            c.get(propertyName + ".versionNumber", Long.class).ifPresent(b::versionNumber);
            return f.apply(op.apply(b).build());
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

    /**
     * Returns a {@link Function} that, when supplied with a configuration property name, will return a {@link
     * SecretBundleContentDetails} for it, inferring connectivity and production information from the supplied {@link
     * ConfigAccessor}.
     *
     * <p>When the {@link Function} that is returned is invoked with a configuration property name of, for example,
     * {@code foo.bar}, then, in the course of producing a {@link SecretBundleContentDetails}, the supplied {@link
     * ConfigAccessor} will be asked to {@linkplain ConfigAccessor#get(String, Class) supply values} for the following
     * configuration property name constructions:</p>
     *
     * <ul>
     *
     * <li>{@code foo.bar.opcRequestId}</li>
     *
     * <li>{@code foo.bar.secretId}</li>
     *
     * <li>{@code foo.bar.secretVersionName}</li>
     *
     * <li>{@code foo.bar.stage}</li>
     *
     * <li>{@code foo.bar.versionNumber}</li>
     *
     * </ul>
     *
     * @param c the {@link ConfigAccessor}; must not be {@code null}
     *
     * @param bs a {@link Supplier} of {@link GetSecretBundleRequest.Builder} instances; must not be {@code null}; must
     * not return {@code null} {@link GetSecretBundleRequest.Builder} instances
     *
     * @param op an operation that customizes and returns a {@link GetSecretBundleRequest.Builder} after connectivity
     * and production information has been applied; must not be {@code null} but may be (and commonly is) {@link
     * UnaryOperator#identity}
     *
     * @param f a {@link Function} that, when supplied with a {@link GetSecretBundleRequest}, returns a {@link
     * SecretBundleContentDetails}; must not be {@code null}; must not return {@code null} {@link
     * SecretBundleContentDetails} instances
     *
     * @exception NullPointerException if any argument is {@code null}
     *
     * @exception com.oracle.bmc.model.BmcException if there was an Oracle Cloud Infrastructure-related error, typically
     * encountered by the supplied {@code op} {@link Function}
     */
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
            var b = bs.get(); // e.g. GetSecretBundleRequest.b();
            c.get(propertyName + ".opcRequestId", String.class).ifPresent(b::opcRequestId);
            c.get(propertyName + ".secretId", String.class).ifPresent(b::secretId);
            c.get(propertyName + ".secretVersionName", String.class).ifPresent(b::secretVersionName);
            c.get(propertyName + ".stage", GetSecretBundleRequest.Stage.class).ifPresent(b::stage);
            c.get(propertyName + ".versionNumber", Long.class).ifPresent(b::versionNumber);
            return f.apply(op.apply(b).build());
        };
    }

}
