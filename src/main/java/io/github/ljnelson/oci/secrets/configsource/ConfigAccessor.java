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

import java.util.Optional;

import org.eclipse.microprofile.config.ConfigProvider;

/**
 * A {@linkplain FunctionalInterface functional interface} providing access to a configuration system, such as
 * {@linkplain org.eclipse.microprofile.config.Config MicroProfile Config}.
 *
 * @author <a href="https://about.me/lairdnelson/" target="_top">Laird Nelson</a>
 */
@FunctionalInterface
public interface ConfigAccessor {

    /**
     * Returns a non-{@code null} but possibly {@linkplain Optional#isEmpty() empty} {@link Optional} representing a
     * configuration property value corresponding to the supplied name.
     *
     * <p>Implementations of this method must be safe for concurrent use by multiple threads.</p>
     *
     * @param <T> the type of the anticipated configuration property value
     *
     * @param name the configuration property name; must not be {@code null}
     *
     * @param c the anticipated type of the configuration property value; must not be {@code null}
     *
     * @return a non-{@code null} {@link Optional}
     *
     * @see org.eclipse.microprofile.config.Config#getOptionalValue(String, Class)
     */
    public <T> Optional<T> get(String name, Class<T> c);

    /**
     * Returns a non-{@code null} {@link ConfigAccessor} implemented by the {@link
     * org.eclipse.microprofile.config.Config#getOptionalValue(String, Class)} method.
     *
     * <p>This method is safe for concurrent use by multiple threads.</p>
     *
     * @return a non-{@code null} {@link ConfigAccessor}
     */
    public static ConfigAccessor ofMicroProfileConfig() {
        return ConfigProvider.getConfig()::getOptionalValue;
    }

}
