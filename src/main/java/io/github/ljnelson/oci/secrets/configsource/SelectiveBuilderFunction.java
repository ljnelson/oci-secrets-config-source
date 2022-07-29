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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.oracle.bmc.secrets.requests.GetSecretBundleRequest;

public final class SelectiveBuilderFunction implements Function<String, GetSecretBundleRequest.Builder> {

    private final Predicate<? super String> propertyNameValidator;

    private final Function<? super String, ? extends GetSecretBundleRequest.Builder> builderFunction;

    public SelectiveBuilderFunction(final Pattern validPropertyNamesPattern) {
        this(ignoredPropertyName -> GetSecretBundleRequest.builder(),
             validPropertyNamesPattern);
    }

    public SelectiveBuilderFunction(Function<? super String, ? extends GetSecretBundleRequest.Builder> builderFunction,
                                    final Pattern validPropertyNamesPattern) {
        super();
        this.builderFunction = Objects.requireNonNull(builderFunction, "builderFunction");
        Objects.requireNonNull(validPropertyNamesPattern, "validPropertyNamesPattern");
        this.propertyNameValidator = pn -> pn == null ? false : validPropertyNamesPattern.matcher(pn).matches();
    }

    public SelectiveBuilderFunction(String validPropertyName0) {
        this(Set.of(validPropertyName0));
    }

    public SelectiveBuilderFunction(Function<? super String, ? extends GetSecretBundleRequest.Builder> builderFunction,
                                    String validPropertyName0) {
        this(builderFunction,
             Set.of(validPropertyName0));
    }

    public SelectiveBuilderFunction(Function<? super String, ? extends GetSecretBundleRequest.Builder> builderFunction,
                                    String validPropertyName0,
                                    String validPropertyName1) {
        this(builderFunction,
             Set.of(validPropertyName0,
                    validPropertyName1));
    }

    public SelectiveBuilderFunction(String validPropertyName0,
                                    String validPropertyName1) {
        this(ignoredPropertyName -> GetSecretBundleRequest.builder(),
             Set.of(validPropertyName0,
                    validPropertyName1));
    }

    public SelectiveBuilderFunction(Function<? super String, ? extends GetSecretBundleRequest.Builder> builderFunction,
                                    String validPropertyName0,
                                    String validPropertyName1,
                                    String validPropertyName2) {
        this(builderFunction,
             Set.of(validPropertyName0,
                    validPropertyName1,
                    validPropertyName2));
    }

    public SelectiveBuilderFunction(String validPropertyName0,
                                    String validPropertyName1,
                                    String validPropertyName2) {
        this(ignoredPropertyName -> GetSecretBundleRequest.builder(),
             Set.of(validPropertyName0,
                    validPropertyName1,
                    validPropertyName2));
    }

    public SelectiveBuilderFunction(Set<? extends String> validPropertyNames) {
        this(ignoredPropertyName -> GetSecretBundleRequest.builder(), validPropertyNames);
    }

    public SelectiveBuilderFunction(Function<? super String, ? extends GetSecretBundleRequest.Builder> builderFunction,
                                    Set<? extends String> validPropertyNames) {
        super();
        this.builderFunction = Objects.requireNonNull(builderFunction, "builderFunction");
        final Set<String> s = Set.copyOf(validPropertyNames);
        this.propertyNameValidator = s::contains;
    }

    public SelectiveBuilderFunction(Predicate<? super String> propertyNameValidator) {
        this(ignoredPropertyName -> GetSecretBundleRequest.builder(), propertyNameValidator);
    }

    public SelectiveBuilderFunction(Function<? super String, ? extends GetSecretBundleRequest.Builder> builderFunction,
                                    Predicate<? super String> propertyNameValidator) {
        super();
        this.builderFunction = Objects.requireNonNull(builderFunction, "builderFunction");
        this.propertyNameValidator = Objects.requireNonNull(propertyNameValidator, "propertyNameValidator");
    }

    @Override // Function
    public final GetSecretBundleRequest.Builder apply(String propertyName) {
        return this.propertyNameValidator.test(propertyName) ? this.builderFunction.apply(propertyName) : null;
    }

}
