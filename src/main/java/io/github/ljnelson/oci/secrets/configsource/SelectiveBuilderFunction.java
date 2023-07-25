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
import java.util.Set;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.oracle.bmc.secrets.requests.GetSecretBundleRequest;

/**
 * A decorator {@link Function} that guards another similar {@link Function} with a {@link Predicate} controlling when
 * it will be executed.
 *
 * @author <a href="https://about.me/lairdnelson/" target="_top">Laird Nelson</a>
 *
 * @see #apply(String)
 */
public final class SelectiveBuilderFunction implements Function<String, GetSecretBundleRequest.Builder> {


    /*
     * Instance fields.
     */


    private final Function<? super String, ? extends GetSecretBundleRequest.Builder> builderFunction;

    private final Predicate<? super String> propertyNameValidator;


    /*
     * Constructors.
     */


    /**
     * Creates a new {@link SelectiveBuilderFunction}.
     *
     * @param builderFunction a {@link Function} that, {@linkplain Function#apply(Object) when supplied with a
     * MicroProfile Config property name}, returns a fully-configured {@link GetSecretBundleRequest.Builder} suitable
     * for the property name, or {@code null} if the property name is unsuitable; will only be executed if the property
     * name is non-{@code null} and {@linkplain Matcher#matches() matched} by the supplied {@link Pattern}; must not be
     * {@code null}
     *
     * @param validPropertyNamesPattern a {@link Pattern} used to {@linkplain Matcher#matches() match} property names
     * for further processing by the supplied {@code builderFunction}; must not be {@code null}
     *
     * @exception NullPointerException if either argument is {@code null}
     *
     * @see #SelectiveBuilderFunction(Function, Predicate)
     *
     * @see #apply(String)
     */
    public SelectiveBuilderFunction(Function<? super String, ? extends GetSecretBundleRequest.Builder> builderFunction,
                                    final Pattern validPropertyNamesPattern) {
        super();
        this.builderFunction = Objects.requireNonNull(builderFunction, "builderFunction");
        Objects.requireNonNull(validPropertyNamesPattern, "validPropertyNamesPattern");
        this.propertyNameValidator = pn -> pn != null && validPropertyNamesPattern.matcher(pn).matches();
    }

    /**
     * Creates a new {@link SelectiveBuilderFunction}.
     *
     * @param builderFunction a {@link Function} that, {@linkplain Function#apply(Object) when supplied with a
     * MicroProfile Config property name}, returns a fully-configured {@link GetSecretBundleRequest.Builder} suitable
     * for the property name, or {@code null} if the property name is unsuitable; will only be executed if the property
     * name is {@linkplain String#equals(Object) equal to} the supplied {@code validPropertyName0} argument; must not be
     * {@code null}
     *
     * @param validPropertyName0 a MicroProfile Config property name to which any property name supplied to the {@link
     * #apply(String)} method must be equal in order for a non-{@code null} {@link GetSecretBundleRequest.Builder} to be
     * returned by it; must not be {@code null}
     *
     * @exception NullPointerException if either argument is {@code null}
     *
     * @see #SelectiveBuilderFunction(Function, Set)
     *
     * @see #apply(String)
     */
    public SelectiveBuilderFunction(Function<? super String, ? extends GetSecretBundleRequest.Builder> builderFunction,
                                    String validPropertyName0) {
        this(builderFunction,
             Set.of(validPropertyName0));
    }

    /**
     * Creates a new {@link SelectiveBuilderFunction}.
     *
     * @param builderFunction a {@link Function} that, {@linkplain Function#apply(Object) when supplied with a
     * MicroProfile Config property name}, returns a fully-configured {@link GetSecretBundleRequest.Builder} suitable
     * for the property name, or {@code null} if the property name is unsuitable; will only be executed if the property
     * name is {@linkplain String#equals(Object) equal to} one of the supplied valid property names; must not be {@code
     * null}
     *
     * @param validPropertyName0 a MicroProfile Config property name; must not be {@code null}
     *
     * @param validPropertyName1 a MicroProfile Config property name; must not be {@code null}
     *
     * @exception NullPointerException if any argument is {@code null}
     *
     * @see #SelectiveBuilderFunction(Function, Set)
     *
     * @see #apply(String)
     */
    public SelectiveBuilderFunction(Function<? super String, ? extends GetSecretBundleRequest.Builder> builderFunction,
                                    String validPropertyName0,
                                    String validPropertyName1) {
        this(builderFunction,
             Set.of(validPropertyName0,
                    validPropertyName1));
    }

    /**
     * Creates a new {@link SelectiveBuilderFunction}.
     *
     * @param builderFunction a {@link Function} that, {@linkplain Function#apply(Object) when supplied with a
     * MicroProfile Config property name}, returns a fully-configured {@link GetSecretBundleRequest.Builder} suitable
     * for the property name, or {@code null} if the property name is unsuitable; will only be executed if the property
     * name is {@linkplain String#equals(Object) equal to} one of the supplied valid property names; must not be {@code
     * null}
     *
     * @param validPropertyName0 a MicroProfile Config property name; must not be {@code null}
     *
     * @param validPropertyName1 a MicroProfile Config property name; must not be {@code null}
     *
     * @param validPropertyName2 a MicroProfile Config property name; must not be {@code null}
     *
     * @exception NullPointerException if any argument is {@code null}
     *
     * @see #SelectiveBuilderFunction(Function, Set)
     *
     * @see #apply(String)
     */
    public SelectiveBuilderFunction(Function<? super String, ? extends GetSecretBundleRequest.Builder> builderFunction,
                                    String validPropertyName0,
                                    String validPropertyName1,
                                    String validPropertyName2) {
        this(builderFunction,
             Set.of(validPropertyName0,
                    validPropertyName1,
                    validPropertyName2));
    }

    /**
     * Creates a new {@link SelectiveBuilderFunction}.
     *
     * @param builderFunction a {@link Function} that, {@linkplain Function#apply(Object) when supplied with a
     * MicroProfile Config property name}, returns a fully-configured {@link GetSecretBundleRequest.Builder} suitable
     * for the property name, or {@code null} if the property name is unsuitable; will only be executed if the property
     * name is {@linkplain String#equals(Object) equal to} one of the elements in the supplied {@code
     * validPropertyNames} {@link Set}; must not be {@code null}
     *
     * @param validPropertyNames a {@link Set} of MicroProfile Config property names for which this {@link
     * SelectiveBuilderFunction} will potentially {@linkplain #apply(String) respond} with a non-{@code null} {@link
     * GetSecretBundleRequest.Builder}; must not be {@code null}
     *
     * @exception NullPointerException if either argument is {@code null}
     *
     * @see #SelectiveBuilderFunction(Function, Predicate)
     *
     * @see #apply(String)
     */
    public SelectiveBuilderFunction(Function<? super String, ? extends GetSecretBundleRequest.Builder> builderFunction,
                                    Set<? extends String> validPropertyNames) {
        super();
        this.builderFunction = Objects.requireNonNull(builderFunction, "builderFunction");
        this.propertyNameValidator = Set.copyOf(validPropertyNames)::contains;
    }

    /**
     * Creates a new {@link SelectiveBuilderFunction}.
     *
     * @param builderFunction a {@link Function} that, {@linkplain Function#apply(Object) when supplied with a
     * MicroProfile Config property name}, returns a fully-configured {@link GetSecretBundleRequest.Builder} suitable
     * for the property name, or {@code null} if the property name is unsuitable; will only be executed if the supplied
     * {@code propertyNameValidator} returns {@code true} {@linkplain Predicate#test(Object) when supplied with the same
     * property name}; must not be {@code null}
     *
     * @param propertyNameValidator a {@link Predicate} that, when supplied with a MicroProfile Config property name,
     * returns {@code true} if the supplied {@code builderFunction} should be executed with the same property name; must
     * not be {@code null}
     *
     * @exception NullPointerException if either argument is {@code null}
     *
     * @see #apply(String)
     */
    public SelectiveBuilderFunction(Function<? super String, ? extends GetSecretBundleRequest.Builder> builderFunction,
                                    Predicate<? super String> propertyNameValidator) {
        super();
        this.builderFunction = Objects.requireNonNull(builderFunction, "builderFunction");
        this.propertyNameValidator = Objects.requireNonNull(propertyNameValidator, "propertyNameValidator");
    }


    /*
     * Instance methods.
     */


    /**
     * If the {@code propertyNameValidator} {@linkplain #SelectiveBuilderFunction(Function, Predicate) supplied at
     * construction time} returns {@code true} when supplied with the supplied {@code propertyName}, then the {@code
     * builderFunction} {@linkplain #SelectiveBuilderFunction(Function, Predicate) supplied at construction time} is
     * executed with the same {@code propertyName} and its return value is returned.
     *
     * <p>In all other cases this method returns {@code null}.</p>
     *
     * @param propertyName a MicroProfile Config property name; may be {@code null}
     *
     * @return a fully-configured {@link GetSecretBundleRequest.Builder} suitable for the supplied {@code propertyName},
     * or {@code null}
     *
     * @see #SelectiveBuilderFunction(Function, Predicate)
     */
    @Override // Function
    public final GetSecretBundleRequest.Builder apply(String propertyName) {
        return this.propertyNameValidator.test(propertyName) ? this.builderFunction.apply(propertyName) : null;
    }

}
