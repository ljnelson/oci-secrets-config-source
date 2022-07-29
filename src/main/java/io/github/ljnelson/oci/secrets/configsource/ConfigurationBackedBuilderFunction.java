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
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;

import com.oracle.bmc.secrets.requests.GetSecretBundleRequest;
import com.oracle.bmc.secrets.requests.GetSecretBundleRequest.Stage;
import org.eclipse.microprofile.config.Config;
import org.eclipse.microprofile.config.ConfigProvider;

public final class ConfigurationBackedBuilderFunction implements Function<String, GetSecretBundleRequest.Builder> {

    private final Supplier<? extends BiFunction<? super String, ? super Class<?>, ? extends Optional<?>>> configSupplier;

    public ConfigurationBackedBuilderFunction() {
        this(() -> ConfigProvider.getConfig()::getOptionalValue);
    }

    public ConfigurationBackedBuilderFunction(ClassLoader cl) {
        this(() -> ConfigProvider.getConfig(cl)::getOptionalValue);
    }

    public ConfigurationBackedBuilderFunction(Config config) {
        this(() -> config::getOptionalValue);
    }

    public ConfigurationBackedBuilderFunction(BiFunction<? super String, ? super Class<?>, ? extends Optional<?>> configFunction) {
        this(() -> configFunction);
    }

    public ConfigurationBackedBuilderFunction(Supplier<? extends BiFunction<? super String, ? super Class<?>, ? extends Optional<?>>> configSupplier) {
        super();
        this.configSupplier = Objects.requireNonNull(configSupplier, "configSupplier");
    }

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
