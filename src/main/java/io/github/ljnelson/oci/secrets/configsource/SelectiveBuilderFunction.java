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

import com.oracle.bmc.secrets.requests.GetSecretBundleRequest;

public final class SelectiveBuilderFunction implements Function<String, GetSecretBundleRequest.Builder> {

    private final Predicate<? super String> propertyNameValidator;

    private final Function<? super String, ? extends GetSecretBundleRequest.Builder> builderFunction;

    public SelectiveBuilderFunction(Set<? extends String> validPropertyNames) {
        this(validPropertyNames, ignoredPropertyName -> GetSecretBundleRequest.builder());
    }

    public SelectiveBuilderFunction(Set<? extends String> validPropertyNames,
                                    Function<? super String, ? extends GetSecretBundleRequest.Builder> builderFunction) {
        super();
        final Set<String> s = Set.copyOf(validPropertyNames);
        this.propertyNameValidator = s::contains;
        this.builderFunction = Objects.requireNonNull(builderFunction, "builderFunction");
    }

    public SelectiveBuilderFunction(Predicate<? super String> propertyNameValidator) {
        this(propertyNameValidator, ignoredPropertyName -> GetSecretBundleRequest.builder());
    }

    public SelectiveBuilderFunction(Predicate<? super String> propertyNameValidator,
                                    Function<? super String, ? extends GetSecretBundleRequest.Builder> builderFunction) {
        super();
        this.propertyNameValidator = Objects.requireNonNull(propertyNameValidator, "propertyNameValidator");
        this.builderFunction = Objects.requireNonNull(builderFunction, "builderFunction");
    }

    @Override // Function
    public final GetSecretBundleRequest.Builder apply(String propertyName) {
        return this.propertyNameValidator.test(propertyName) ? this.builderFunction.apply(propertyName) : null;
    }

}
