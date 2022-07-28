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

import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

import com.oracle.bmc.secrets.requests.GetSecretBundleRequest;

public class DefaultBuilderFunction implements Function<String, GetSecretBundleRequest.Builder> {

    private final Predicate<? super String> propertyNameValidator;

    private final Supplier<? extends GetSecretBundleRequest.Builder> builderSupplier;

    public DefaultBuilderFunction(Set<? extends String> validPropertyNames) {
        this(validPropertyNames, GetSecretBundleRequest::builder);
    }

    public DefaultBuilderFunction(Set<? extends String> validPropertyNames,
                                  Supplier<? extends GetSecretBundleRequest.Builder> builderSupplier) {
        super();
        final Set<String> s = Set.copyOf(validPropertyNames);
        this.propertyNameValidator = s.isEmpty() ? pn -> false : s::contains;
        this.builderSupplier = Objects.requireNonNull(builderSupplier, "builderSupplier");
    }

    public DefaultBuilderFunction(Predicate<? super String> propertyNameValidator) {
        this(propertyNameValidator, GetSecretBundleRequest::builder);
    }

    public DefaultBuilderFunction(Predicate<? super String> propertyNameValidator,
                                  Supplier<? extends GetSecretBundleRequest.Builder> builderSupplier) {
        super();
        this.propertyNameValidator = Objects.requireNonNull(propertyNameValidator, "propertyNameValidator");
        this.builderSupplier = Objects.requireNonNull(builderSupplier, "builderSupplier");
    }

    @Override // Function
    public final GetSecretBundleRequest.Builder apply(String propertyName) {
        return this.propertyNameValidator.test(propertyName) ? this.builderSupplier.get() : null;
    }

}
