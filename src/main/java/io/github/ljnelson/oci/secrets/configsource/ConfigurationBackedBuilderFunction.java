/*
 * Copyright © 2022–2023 Laird Nelson.
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
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;

import com.oracle.bmc.secrets.requests.GetSecretBundleRequest;
import com.oracle.bmc.secrets.requests.GetSecretBundleRequest.Stage;
import org.eclipse.microprofile.config.Config;
import org.eclipse.microprofile.config.ConfigProvider;

/**
 * A {@link Function} that, when supplied with a <a href="https://github.com/eclipse/microprofile-config"
 * target="_top">MicroProfile Config</a> property name, returns a fully-configured {@link
 * GetSecretBundleRequest.Builder}, or {@code null} if the property name is unsupported.
 *
 * <p>This class uses a user-supplied configuration facility to look up certain configuration values to supply to any
 * {@link GetSecretBundleRequest.Builder} instances it may return from its {@link #apply(String)} method.</p>
 *
 * @author <a href="https://about.me/lairdnelson/" target="_top">Laird Nelson</a>
 *
 * @see #ConfigurationBackedBuilderFunction(Supplier)
 *
 * @see #apply(String)
 */
@Deprecated
public final class ConfigurationBackedBuilderFunction implements Function<String, GetSecretBundleRequest.Builder> {


    /*
     * Instance fields.
     */


    private final Supplier<? extends BiFunction<? super String, ? super Class<?>, ? extends Optional<?>>> configSupplier;


    /*
     * Constructors.
     */


    /**
     * Creates a new {@link ConfigurationBackedBuilderFunction} that will use an invocation of the {@link
     * Config#getOptionalValue(String, Class)} method on the return value of an invocation of {@link
     * ConfigProvider#getConfig()} for its configuration facility.
     *
     * @see #ConfigurationBackedBuilderFunction(Supplier)
     */
    public ConfigurationBackedBuilderFunction() {
        this(() -> ConfigProvider.getConfig()::getOptionalValue);
    }

    /**
     * Creates a new {@link ConfigurationBackedBuilderFunction}.
     *
     * @param cl a {@link ClassLoader} that will be supplied to invocations of the {@link
     * ConfigProvider#getConfig(ClassLoader)} method; may be {@code null}
     *
     * @see #ConfigurationBackedBuilderFunction(Supplier)
     */
    public ConfigurationBackedBuilderFunction(ClassLoader cl) {
        this(() -> ConfigProvider.getConfig(cl)::getOptionalValue);
    }

    /**
     * Creates a new {@link ConfigurationBackedBuilderFunction}.
     *
     * @param config a {@link Config} whose {@link Config#getOptionalValue(String, Class)} method will be used to
     * acquire property name values; must not be {@code null}
     *
     * @exception NullPointerException if {@code config} is {@code null}
     *
     * @see #ConfigurationBackedBuilderFunction(Supplier)
     */
    public ConfigurationBackedBuilderFunction(Config config) {
        this(() -> config::getOptionalValue);
    }

    /**
     * Creates a new {@link ConfigurationBackedBuilderFunction}.
     *
     * @param configFunction a {@link BiFunction} that, when supplied with a property name and a {@link Class}, returns
     * a non-{@code null} {@link Optional} value for that property name whose value is castable to the {@link Class} in
     * question; must not be {@code null}
     *
     * @exception NullPointerException if {@code configFunction} is {@code null}
     *
     * @see #ConfigurationBackedBuilderFunction(Supplier)
     */
    public ConfigurationBackedBuilderFunction(BiFunction<? super String, ? super Class<?>, ? extends Optional<?>> configFunction) {
        this(() -> configFunction);
    }

    /**
     * Creates a new {@link ConfigurationBackedBuilderFunction}.
     *
     * @param configSupplier a {@link Supplier} of a {@link BiFunction} that, when supplied with a property name and a
     * {@link Class}, returns a non-{@code null} {@link Optional} value for that property name whose value is castable
     * to the {@link Class} in question; must not be {@code null}
     *
     * @exception NullPointerException if {@code configSupplier} is {@code null}
     */
    public ConfigurationBackedBuilderFunction(Supplier<? extends BiFunction<? super String, ? super Class<?>, ? extends Optional<?>>> configSupplier) {
        super();
        this.configSupplier = Objects.requireNonNull(configSupplier, "configSupplier");
    }


    /*
     * Instance methods.
     */


    /**
     * Returns a {@link GetSecretBundleRequest.Builder} suitable for the supplied {@code propertyName}, or {@code null}
     * if the property name is unsuitable.
     *
     * <p>If the supplied {@code propertyName} is {@code null} or {@linkplain String#isBlank() blank}, invocations of
     * this method will return {@code null}.</p>
     *
     * <p>Otherwise, values for the following property names will be sought using the {@link BiFunction} {@linkplain
     * #ConfigurationBackedBuilderFunction(Supplier) supplied at construction time}:</p>
     *
     * <ul>
     *
     * <li>{@linkplain GetSecretBundleRequest.Builder#opcRequestId(String)
     * <em><code>propertyName</code></em><code>.opcRequestId</code>}</li>
     *
     * <li>{@linkplain GetSecretBundleRequest.Builder#secretId(String)
     * <em><code>propertyName</code></em><code>.secretId</code>} (required)</li>
     *
     * <li>{@linkplain GetSecretBundleRequest.Builder#secretVersionName(String)
     * <em><code>propertyName</code></em><code>.secretVersionName</code>}</li>
     *
     * <li>{@linkplain GetSecretBundleRequest.Builder#stage(Stage)
     * <em><code>propertyName</code></em><code>.stage</code>}</li>
     *
     * <li>{@linkplain GetSecretBundleRequest.Builder#versionNumber(Long)
     * <em><code>propertyName</code></em><code>.versionNumber</code>}</li>
     *
     * </ul>
     *
     * <p>These values, if present, will be used to configure a {@link GetSecretBundleRequest.Builder} that will be
     * returned.</p>
     *
     * @param propertyName the name of a property for which a {@link GetSecretBundleRequest.Builder} is to be returned;
     * may be {@code null} in which case {@code null} will be returned
     *
     * @return a fully-configured {@link GetSecretBundleRequest.Builder} suitable for the supplied {@code propertyName},
     * or {@code null}
     *
     * @exception NullPointerException if the supplied {@code propertyName} is non-{@code null} and not {@linkplain
     * String#isBlank() blank} and a value for the <em><code>propertyName</code></em><code>.secretId</code> property
     * name is {@code null} or {@linkplain String#isBlank() blank}
     *
     * @see GetSecretBundleRequest.Builder
     */
    @Override // Function
    @SuppressWarnings("unchecked")
    public final GetSecretBundleRequest.Builder apply(String propertyName) {
        if (propertyName == null || propertyName.isBlank()) {
            return null;
        }
        GetSecretBundleRequest.Builder builder = GetSecretBundleRequest.builder();
        BiFunction<? super String, ? super Class<?>, ? extends Optional<?>> config = this.configSupplier.get();
        Optional<String> secretId = (Optional<String>) config.apply(propertyName + ".secretId", String.class);
        if (secretId.isPresent()) {
            builder = builder.secretId(secretId.orElseThrow());
        }
        Optional<String> opcRequestId = (Optional<String>) config.apply(propertyName + ".opcRequestId", String.class);
        if (opcRequestId.isPresent()) {
            builder = builder.opcRequestId(opcRequestId.orElseThrow());
        }
        Optional<Long> versionNumber = (Optional<Long>) config.apply(propertyName + ".versionNumber", Long.class);
        if (versionNumber.isPresent()) {
            builder = builder.versionNumber(versionNumber.orElseThrow());
        }
        Optional<String> secretVersionName = (Optional<String>) config.apply(propertyName + ".secretVersionName", String.class);
        if (secretVersionName.isPresent()) {
            builder = builder.secretVersionName(secretVersionName.orElseThrow());
        }
        Optional<Stage> stage = (Optional<Stage>) config.apply(propertyName + ".stage", Stage.class);
        return builder.stage(stage.orElse(Stage.Latest));
    }

}
